/*
 * QEMU posix-aio emulation
 *
 * Copyright IBM, Corp. 2008
 *
 * Authors:
 *  Anthony Liguori   <aliguori@us.ibm.com>
 *
 * This work is licensed under the terms of the GNU GPL, version 2.  See
 * the COPYING file in the top-level directory.
 *
 * Contributions after 2012-01-13 are licensed under the terms of the
 * GNU GPL, version 2 or (at your option) any later version.
 */

#include <sys/ioctl.h>
#include <sys/types.h>
#include <pthread.h>
#include <unistd.h>
#include <errno.h>
#include <time.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

#include "qemu-queue.h"
#include "osdep.h"
#include "sysemu.h"
#include "qemu-common.h"
#include "trace.h"
#include "block_int.h"

#include "block/raw-posix-aio.h"

static void do_spawn_thread(void);

struct qemu_paiocb {
    BlockDriverAIOCB common;
    int aio_fildes;

    union {
        struct iovec *aio_iov;
        void *aio_ioctl_buf;
    };
    int aio_niov;
    size_t aio_nbytes;
#define aio_ioctl_cmd   aio_nbytes /* for QEMU_AIO_IOCTL */
    off_t aio_offset;

    QTAILQ_ENTRY(qemu_paiocb) node;
    int aio_type;
    ssize_t ret;
    int active;
    struct qemu_paiocb *next;
};

typedef struct PosixAioState {
    int rfd, wfd;
    struct qemu_paiocb *first_aio;
} PosixAioState;


static pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t cond = PTHREAD_COND_INITIALIZER;
static pthread_t thread_id;
static pthread_attr_t attr;
static int max_threads = 64; //TK Async IO creates problems so this is overriden to 1 thread for pre-ICS devices
static int cur_threads = 0;
static int idle_threads = 0;
static int new_threads = 0; /* backlog of threads we need to create */
static int pending_threads = 0; /* threads created but not running yet */
static QEMUBH *new_thread_bh;
static QTAILQ_HEAD(, qemu_paiocb) request_list;

#ifdef CONFIG_PREADV
static int preadv_present = 1;
#else
static int preadv_present = 0;
#endif

extern void setAIOMaxThreads(int threads){
	max_threads = threads;
	LOGV("Changing MAX AIO Threads to: %d", max_threads);
}
static void die2(int err, const char *what) {
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    fprintf(stderr, "%s failed: %s\n", what, strerror(err));
    abort();
}

static void die(const char *what) {
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    die2(errno, what);
}

static void mutex_lock(pthread_mutex_t *mutex) {
    int ret = pthread_mutex_lock(mutex);
    if (ret) die2(ret, "pthread_mutex_lock");
}

static void mutex_unlock(pthread_mutex_t *mutex) {
    int ret = pthread_mutex_unlock(mutex);
    if (ret) die2(ret, "pthread_mutex_unlock");
}

static int cond_timedwait(pthread_cond_t *cond, pthread_mutex_t *mutex,
        struct timespec *ts) {
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    int ret = pthread_cond_timedwait(cond, mutex, ts);
    if (ret && ret != ETIMEDOUT) die2(ret, "pthread_cond_timedwait");
    return ret;
}

static void cond_signal(pthread_cond_t *cond) {
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    int ret = pthread_cond_signal(cond);
    if (ret) die2(ret, "pthread_cond_signal");
}

static void thread_create(pthread_t *thread, pthread_attr_t *attr,
        void *(*start_routine)(void*), void *arg) {
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    int ret = pthread_create(thread, attr, start_routine, arg);
    if (ret) die2(ret, "pthread_create");
}

static ssize_t handle_aiocb_ioctl(struct qemu_paiocb *aiocb) {
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    int ret;

    ret = ioctl(aiocb->aio_fildes, aiocb->aio_ioctl_cmd, aiocb->aio_ioctl_buf);
    if (ret == -1)
        return -errno;

    /*
     * This looks weird, but the aio code only considers a request
     * successful if it has written the full number of bytes.
     *
     * Now we overload aio_nbytes as aio_ioctl_cmd for the ioctl command,
     * so in fact we return the ioctl command here to make posix_aio_read()
     * happy..
     */
    return aiocb->aio_nbytes;
}

static ssize_t handle_aiocb_flush(struct qemu_paiocb *aiocb) {
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    int ret;

    ret = qemu_fdatasync(aiocb->aio_fildes);
    if (ret == -1)
        return -errno;
    return 0;
}

#ifdef CONFIG_PREADV

static ssize_t
qemu_preadv(int fd, const struct iovec *iov, int nr_iov, off_t offset) {
    return preadv(fd, iov, nr_iov, offset);
}

static ssize_t
qemu_pwritev(int fd, const struct iovec *iov, int nr_iov, off_t offset) {
    return pwritev(fd, iov, nr_iov, offset);
}

#else

static ssize_t
qemu_preadv(int fd, const struct iovec *iov, int nr_iov, off_t offset) {
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    return -ENOSYS;
}

static ssize_t
qemu_pwritev(int fd, const struct iovec *iov, int nr_iov, off_t offset) {
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    return -ENOSYS;
}

#endif

static ssize_t handle_aiocb_rw_vector(struct qemu_paiocb *aiocb) {
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    ssize_t len;

    do {
        if (aiocb->aio_type & QEMU_AIO_WRITE)
            len = qemu_pwritev(aiocb->aio_fildes,
                aiocb->aio_iov,
                aiocb->aio_niov,
                aiocb->aio_offset);
        else
            len = qemu_preadv(aiocb->aio_fildes,
                aiocb->aio_iov,
                aiocb->aio_niov,
                aiocb->aio_offset);
    } while (len == -1 && errno == EINTR);

    if (len == -1)
        return -errno;
    return len;
}

/*
 * Read/writes the data to/from a given linear buffer.
 *
 * Returns the number of bytes handles or -errno in case of an error. Short
 * reads are only returned if the end of the file is reached.
 */

int preadcb(int fd, char *buf, ssize_t count, ssize_t offset) {
    //1st Seek
    int r = lseek(fd, offset, SEEK_SET); //offset at 2
    //    LOGD_AIO("%s:%s, lseek() = %d", __FILE__, __func__, r);


    //1st Read
    int len = read(fd, buf, count); //offset is already at 2 reading 5 bytes

#ifdef DEBUG_ANDROID_AIO
    int length = 1000;
    if (len < 1000)
        length = len;

    char ** hex_dump = (char **) calloc(length, sizeof (buf));
    char * phex_dump = (char *) calloc(length, 16 * sizeof (char));
    for (int i = 0; i < length; i++) {
        //    	LOGD_AIO("Buff[%d]=%x", i,buf[i]);
        hex_dump[i] = (char *) calloc(16, sizeof (char));
        memset(hex_dump[i], '\0', 1);
        sprintf(hex_dump[i], "%08x|", (unsigned int) buf[i]);
        strcat(phex_dump, hex_dump[i]);
    }
    strcat(phex_dump, "...");
    //    LOGD_AIO("Buff(hex) = %s",phex_dump);

    LOGD_AIO("%s:%s, read(%d,%d<=%d=>%d) len = %d, hex = %s",
            __FILE__, __func__, fd, offset, count, offset + count, len, phex_dump);

    free(hex_dump);
    free(phex_dump);
#endif

    //Seek Rest
    r = lseek(fd, 0, SEEK_SET);
    //    LOGD_AIO("%s:%s, reset lseek() = %d", __FILE__, __func__, r);
    return len;

}

int pwritecb(int fd, char *buf, ssize_t count, ssize_t offset) {
    //1st Seek
    int r = lseek(fd, offset, SEEK_SET); //offset at 2
    //    LOGD_AIO("%s:%s, lseek() ret = %d", __FILE__, __func__, r);



    int len = write(fd, buf, count);

#ifdef DEBUG_ANDROID_AIO
    int length = 1000;
    if (len < 1000)
        length = len;

    char ** hex_dump = (char **) calloc(length, sizeof (buf));
    char * phex_dump = (char *) calloc(length, 16 * sizeof (char));
    for (int i = 0; i < length; i++) {
        //    	LOGD_AIO("Buff[%d]=%x", i,buf[i]);
        hex_dump[i] = (char *) calloc(16, sizeof (char));
        memset(hex_dump[i], '\0', 1);
        sprintf(hex_dump[i], "%08x|", (unsigned int) buf[i]);
        strcat(phex_dump, hex_dump[i]);
    }
    strcat(phex_dump, "...");
    //    LOGD_AIO("Buff(hex) = %s",phex_dump);
    LOGD_AIO("%s:%s, write(%d, %d<=%d=>%d) ret = %d, hex = %s",
            __FILE__, __func__, fd, offset, count, offset + count, len, phex_dump);

    free(hex_dump);
    free(phex_dump);
#endif

    //Seek Rest
    r = lseek(fd, 0, SEEK_SET);
    //    LOGD_AIO("%s:%s, reset lseek() ret = %d", __FILE__, __func__, r);

    return len;

}

char * buf2Hex(char *buf, int len) {
    int length = 5;
    if (len < 5)
        length = len;


    char * phex_dump = (char *) calloc(length + 2, 8 * sizeof (char));
    for (int i = 0; i < length; i++) {
        char * tmpdump = (char *) calloc(8, sizeof (char));
        sprintf(tmpdump, "[%03d]%02x|", i, (unsigned int) buf[i]);
        strcat(phex_dump, tmpdump);
        free(tmpdump);
    }
    strcat(phex_dump, "...");
    char * tmpdump = (char *) calloc(8, sizeof (char));
    sprintf(tmpdump, "[%03d]%02x|", len - 1, (unsigned int) buf[len - 1]);
    strcat(phex_dump, tmpdump);
    free(tmpdump);

    //    LOGD_AIO("Buff(hex) = %s",phex_dump);

    return phex_dump;

}

static int aiocb_counter = 0;

static ssize_t handle_aiocb_rw_linear(struct qemu_paiocb *aiocb, unsigned char *buf) {
    ssize_t offset = 0;
    ssize_t len;
    aiocb_counter++;
    LOGD_AIO("%s:%s, file = %s, aiocb_counter = %d, offset = %d, aio_nbytes=%d\n",
            __FILE__, __func__, aiocb->common.bs->filename,
            aiocb_counter, offset, aiocb->aio_nbytes);
    int counter = 0;
    while (offset < aiocb->aio_nbytes) {

        counter++;
        if (aiocb->aio_type & QEMU_AIO_WRITE) {
            //len = pwrite(aiocb->aio_fildes,//TK

            len = pwrite(aiocb->aio_fildes,
                    (const char *) buf + offset,
                    aiocb->aio_nbytes - offset,
                    aiocb->aio_offset + offset);

            LOGD_AIO("%s:%s, pwrite(%d:%s, %d<=%d=>%d), counter = %d, len = %d, buffer = %02x|%02x|%02x|...%02x|\n",
                    __FILE__, __func__, aiocb_counter, aiocb->common.bs->filename,
                    aiocb->aio_offset + offset,
                    aiocb->aio_nbytes - offset,
                    aiocb->aio_offset + aiocb->aio_nbytes,
                    counter, len
                    , buf[offset],
                    buf[offset + 1],
                    buf[offset + 2],
                    buf[offset + len-1]
                    //,buf2Hex((const char *)buf + offset, len)
                    );

        } else {
            //len = pread(aiocb->aio_fildes, //TK
            len = pread(aiocb->aio_fildes,
                    (const char *) buf + offset,
                    aiocb->aio_nbytes - offset,
                    aiocb->aio_offset + offset);
            LOGD_AIO("%s:%s, pread(%d:%s, %d<=%d=>%d), counter = %d, len = %d, buffer = %02x|%02x|%02x|...%02x|\n",
                    __FILE__, __func__, aiocb_counter,
                    aiocb->common.bs->filename,
                    aiocb->aio_offset + offset,
                    aiocb->aio_nbytes - offset,
                    aiocb->aio_offset + aiocb->aio_nbytes,
                    counter, len
                    , buf[offset],
                    buf[offset + 1],
                    buf[offset + 2],
                    buf[offset + len-1]
                    //                         		 ,buf2Hex((const char *)buf + offset, len)
                    );
        }
        if (len == -1 && errno == EINTR) {
            LOGD_AIO("%s:%s, file = %s, EINTR :::: aiocb_counter = %d, offset = %d, aio_nbytes =%d\n",
                    __FILE__, __func__, aiocb->common.bs->filename,
                    aiocb_counter, offset, aiocb->aio_nbytes);
            continue;
        } else if (len == -1) {
            LOGD_AIO("%s:%s, file = %s, LEN=-1 :::: aiocb_counter = %d, offset = %d, aio_nbytes=%d\n",
                    __FILE__, __func__, aiocb->common.bs->filename,
                    aiocb_counter, offset, aiocb->aio_nbytes);
            offset = -errno;
            break;
        } else if (len == 0) {
            LOGD_AIO("%s:%s, file = %s, LEN=0 :::: aiocb_counter = %d, offset = %d, aio_nbytes=%d\n",
                    __FILE__, __func__, aiocb->common.bs->filename,
                    aiocb_counter, offset, aiocb->aio_nbytes);
            break;
        }

        offset += len;
    }

    return offset;
}

static ssize_t handle_aiocb_rw(struct qemu_paiocb *aiocb) {

    ssize_t nbytes;
    unsigned char *buf;

    if (!(aiocb->aio_type & QEMU_AIO_MISALIGNED)) {
        /*
         * If there is just a single buffer, and it is properly aligned
         * we can just use plain pread/pwrite without any problems.
         */
        if (aiocb->aio_niov == 1) {
            LOGD_AIO("%s:%s, ALIGNED AND ONLY ONE IOV: %s: (%d,%d,%d)\n",
                    __FILE__, __func__, aiocb->common.bs->filename,
                    (int) (aiocb->aio_nbytes), (int) (aiocb->aio_offset), sizeof (aiocb->aio_iov->iov_base)
                    );
            return handle_aiocb_rw_linear(aiocb, aiocb->aio_iov->iov_base);
        }

        /*
         * We have more than one iovec, and all are properly aligned.
         *
         * Try preadv/pwritev first and fall back to linearizing the
         * buffer if it's not supported.
         */
        if (preadv_present) {
            LOGD_AIO("%s:%s, ALIGNED AND MANY IOVS for %s", __FILE__, __func__, aiocb->common.bs->filename);
            nbytes = handle_aiocb_rw_vector(aiocb);
            if (nbytes == aiocb->aio_nbytes)
                return nbytes;
            if (nbytes < 0 && nbytes != -ENOSYS)
                return nbytes;
            preadv_present = 0;
        }

        /*
         * XXX(hch): short read/write.  no easy way to handle the reminder
         * using these interfaces.  For now retry using plain
         * pread/pwrite?
         */
    }

    LOGD_AIO("%s:%s, MISALIGNED AND MANY SEGMENTS for %s", __FILE__, __func__, aiocb->common.bs->filename);
    /*
     * Ok, we have to do it the hard way, copy all segments into
     * a single aligned buffer.
     */
    buf = qemu_blockalign(aiocb->common.bs, aiocb->aio_nbytes);
    if (aiocb->aio_type & QEMU_AIO_WRITE) {
        unsigned char *p = buf;
        int i;

        for (i = 0; i < aiocb->aio_niov; ++i) {
            memcpy(p, aiocb->aio_iov[i].iov_base, aiocb->aio_iov[i].iov_len);
            p += aiocb->aio_iov[i].iov_len;
        }
    }

    nbytes = handle_aiocb_rw_linear(aiocb, buf);
    if (!(aiocb->aio_type & QEMU_AIO_WRITE)) {
        unsigned char *p = buf;
        size_t count = aiocb->aio_nbytes, copy;
        int i;

        for (i = 0; i < aiocb->aio_niov && count; ++i) {
            copy = count;
            if (copy > aiocb->aio_iov[i].iov_len)
                copy = aiocb->aio_iov[i].iov_len;
            memcpy(aiocb->aio_iov[i].iov_base, p, copy);
            p += copy;
            count -= copy;
        }
    }
    qemu_vfree(buf);

    return nbytes;
}

static void posix_aio_notify_event(void);

int loglevel = 0;

static void *aio_thread(void *unused) {
    mutex_lock(&lock);
    pending_threads--;
    mutex_unlock(&lock);
    do_spawn_thread();

    LOGD_AIO("%s:%s before main loop\n", __FILE__, __func__);
    while (1) {
        struct qemu_paiocb *aiocb;
        ssize_t ret = 0;
        qemu_timeval tv;
        struct timespec ts;

        qemu_gettimeofday(&tv);
        ts.tv_sec = tv.tv_sec + 10;
        ts.tv_nsec = 0;

        mutex_lock(&lock);

        while (QTAILQ_EMPTY(&request_list) &&
                !(ret == ETIMEDOUT)) {
            idle_threads++;
            ret = cond_timedwait(&cond, &lock, &ts);
            idle_threads--;
        }

        if (QTAILQ_EMPTY(&request_list))
            break;

        aiocb = QTAILQ_FIRST(&request_list);

        //        if (loglevel)


        QTAILQ_REMOVE(&request_list, aiocb, node);
        aiocb->active = 1;
        mutex_unlock(&lock);

        switch (aiocb->aio_type & QEMU_AIO_TYPE_MASK) {
            case QEMU_AIO_READ:
                if (aiocb == NULL)
                    LOGD_AIO("%s:%s => NULL, %s aiocb = NULL\n", __FILE__,
                        __func__,
                        ((aiocb->common).bs)->filename);
                else
                    LOGD_AIO("%s:%s =>handle_aiocb_rw, %s,%d,%d,%d,%d\n",
                        __FILE__,
                        __func__,
                        ((aiocb->common).bs)->filename,
                        aiocb->active,
                        aiocb->aio_type,
                        (int) aiocb->aio_offset,
                        (int) aiocb->aio_nbytes
                        );
                ret = handle_aiocb_rw(aiocb);
                if (ret >= 0 && ret < aiocb->aio_nbytes && aiocb->common.bs->growable) {
                    /* A short read means that we have reached EOF. Pad the buffer
                     * with zeros for bytes after EOF. */
                    QEMUIOVector qiov;

                    qemu_iovec_init_external(&qiov, aiocb->aio_iov,
                            aiocb->aio_niov);
                    qemu_iovec_memset_skip(&qiov, 0, aiocb->aio_nbytes - ret, ret);

                    ret = aiocb->aio_nbytes;
                }
                break;
            case QEMU_AIO_WRITE:
                if (aiocb != NULL)
//                    LOGD_AIO("Write request\n");
                ret = handle_aiocb_rw(aiocb);
                break;
            case QEMU_AIO_FLUSH:
                ret = handle_aiocb_flush(aiocb);
                break;
            case QEMU_AIO_IOCTL:
                ret = handle_aiocb_ioctl(aiocb);
                break;
            default:
                fprintf(stderr, "invalid aio request (0x%x)\n", aiocb->aio_type);
                ret = -EINVAL;
                break;
        }

        mutex_lock(&lock);
        aiocb->ret = ret;
        mutex_unlock(&lock);

        posix_aio_notify_event();
    }

    cur_threads--;
    mutex_unlock(&lock);

    return NULL;
}

static void do_spawn_thread(void) {

    sigset_t set, oldset;

    mutex_lock(&lock);
    if (!new_threads) {
        mutex_unlock(&lock);
        return;
    }

    new_threads--;
    pending_threads++;

    mutex_unlock(&lock);

    /* block all signals */
    if (sigfillset(&set)) die("sigfillset");
    if (sigprocmask(SIG_SETMASK, &set, &oldset)) die("sigprocmask");

    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    thread_create(&thread_id, &attr, aio_thread, NULL);

    if (sigprocmask(SIG_SETMASK, &oldset, NULL)) die("sigprocmask restore");
}

static void spawn_thread_bh_fn(void *opaque) {
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    do_spawn_thread();
}

static void spawn_thread(void) {
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    cur_threads++;
    new_threads++;
    /* If there are threads being created, they will spawn new workers, so
     * we don't spend time creating many threads in a loop holding a mutex or
     * starving the current vcpu.
     *
     * If there are no idle threads, ask the main thread to create one, so we
     * inherit the correct affinity instead of the vcpu affinity.
     */
    if (!pending_threads) {
        qemu_bh_schedule(new_thread_bh);
    }
}

static void qemu_paio_submit(struct qemu_paiocb *aiocb) {

    if (aiocb == NULL)
        LOGD_AIO("%s:%s => NULL, %s aiocb = NULL\n", __FILE__,
            __func__,
            ((aiocb->common).bs)->filename);
    else
        LOGD_AIO("%s:%s => QTAILQ_INSERT_TAIL, %s,%d,%d,%d,%d\n",
            __FILE__,
            __func__,
            ((aiocb->common).bs)->filename,
            aiocb->active,
            aiocb->aio_type,
            (int) aiocb->aio_offset,
            (int) aiocb->aio_nbytes
            );

    aiocb->ret = -EINPROGRESS;
    aiocb->active = 0;
    mutex_lock(&lock);
    if (idle_threads == 0 && cur_threads < max_threads)
        spawn_thread();
    QTAILQ_INSERT_TAIL(&request_list, aiocb, node);
    mutex_unlock(&lock);
    cond_signal(&cond);
}

static ssize_t qemu_paio_return(struct qemu_paiocb *aiocb) {
    ssize_t ret;
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    mutex_lock(&lock);
    ret = aiocb->ret;
    mutex_unlock(&lock);

    return ret;
}

static int qemu_paio_error(struct qemu_paiocb *aiocb) {
    
    ssize_t ret = qemu_paio_return(aiocb);
    LOGD_AIO("%s:%s, Checking for Errors:%d\n", __FILE__, __func__, ret);
        
    if (ret < 0)
        ret = -ret;
    else
        ret = 0;

    return ret;
}

static int aio_read_counter = 0;

static void posix_aio_read(void *opaque) {
    PosixAioState *s = opaque;
    struct qemu_paiocb *acb, **pacb;
    int ret;
    ssize_t len;

    aio_read_counter++;
    int counter = 0;
    /* read all bytes from signal pipe */
    for (;;) {
        char bytes[16];
        counter++;

        len = read(s->rfd, bytes, sizeof (bytes));
        LOGD_AIO("%s:%s, read(%d), counter = %d, len = %d, bytes = %s",
                __FILE__, __func__, aio_read_counter, counter, len, bytes);
        if (len == -1 && errno == EINTR) {
            LOGD_AIO("%s:%s, error EINTR read(%d), counter = %d, len = %d, buffer = %s",
                    __FILE__, __func__, aio_read_counter, counter, len, bytes);
            continue; /* try again */
        }
        if (len == sizeof (bytes)) {
            LOGD_AIO("%s:%s, more pread(%d), counter = %d, len = %d, buffer = %s",
                    __FILE__, __func__, aio_read_counter, counter, len, bytes);
            continue; /* more to read */
        }
        break;
    }

    for (;;) {
        pacb = &s->first_aio;
        for (;;) {
            acb = *pacb;
            if (!acb)
                return;

            ret = qemu_paio_error(acb);
            if (ret == ECANCELED) {
                LOGD_AIO("%s:%s ECANCELED\n", __FILE__, __func__);
                /* remove the request */
                *pacb = acb->next;
                qemu_aio_release(acb);
            } else if (ret != EINPROGRESS) {
                /* end of aio */
                if (ret == 0) {
                    ret = qemu_paio_return(acb);
                    if (ret == acb->aio_nbytes)
                        ret = 0;
                    else
                        ret = -EINVAL;
                } else {
                    ret = -ret;
                }

                trace_paio_complete(acb, acb->common.opaque, ret);
                LOGD_AIO("%s:%s Complete\n", __FILE__, __func__);
                
                /* remove the request */
                *pacb = acb->next;
                /* call the callback */
                acb->common.cb(acb->common.opaque, ret);
                qemu_aio_release(acb);
                
                break;
            } else {
                LOGD_AIO("%s:%s NEXT\n", __FILE__, __func__);
                pacb = &acb->next;
            }
        }
    }
}

static int posix_aio_flush(void *opaque) {
    
    PosixAioState *s = opaque;
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    return !!s->first_aio;
}

static PosixAioState *posix_aio_state;

static void posix_aio_notify_event(void) {
    char byte = 0;
    ssize_t ret;
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    ret = write(posix_aio_state->wfd, &byte, sizeof (byte));
    LOGD_AIO("%s:%s=%x, write(), len = %d\n", __FILE__, __func__, byte, ret);
    if (ret < 0 && errno != EAGAIN)
        die("write()");
}

static void paio_remove(struct qemu_paiocb *acb) {
    struct qemu_paiocb **pacb;
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    /* remove the callback from the queue */
    pacb = &posix_aio_state->first_aio;
    for (;;) {
        if (*pacb == NULL) {
            fprintf(stderr, "paio_remove: aio request not found!\n");
            break;
        } else if (*pacb == acb) {
            *pacb = acb->next;
            qemu_aio_release(acb);
            break;
        }
        pacb = &(*pacb)->next;
    }
}

static void paio_cancel(BlockDriverAIOCB *blockacb) {
    struct qemu_paiocb *acb = (struct qemu_paiocb *) blockacb;
    int active = 0;

    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    trace_paio_cancel(acb, acb->common.opaque);

    mutex_lock(&lock);
    if (!acb->active) {
        QTAILQ_REMOVE(&request_list, acb, node);
        acb->ret = -ECANCELED;
    } else if (acb->ret == -EINPROGRESS) {
        active = 1;
    }
    mutex_unlock(&lock);

    if (active) {
        /* fail safe: if the aio could not be canceled, we wait for
           it */
        while (qemu_paio_error(acb) == EINPROGRESS)
            ;
    }

    paio_remove(acb);
}

static AIOPool raw_aio_pool = {
    .aiocb_size = sizeof (struct qemu_paiocb),
    .cancel = paio_cancel,
};

BlockDriverAIOCB *paio_submit(BlockDriverState *bs, int fd,
        int64_t sector_num, QEMUIOVector *qiov, int nb_sectors,
        BlockDriverCompletionFunc *cb, void *opaque, int type) {
    struct qemu_paiocb *acb;
    LOGD_AIO("%s:%s => qemu_aio_get, %s\n", __FILE__, __func__, bs->filename);
    acb = qemu_aio_get(&raw_aio_pool, bs, cb, opaque);
    acb->aio_type = type;
    acb->aio_fildes = fd;

    if (qiov) {
        acb->aio_iov = qiov->iov;
        acb->aio_niov = qiov->niov;
    }
    acb->aio_nbytes = nb_sectors * 512;
    acb->aio_offset = sector_num * 512;

    acb->next = posix_aio_state->first_aio;
    posix_aio_state->first_aio = acb;

    LOGD_AIO("%s:%s => qemu_paio_submit, file=%s (nb_sectors=%d, sector_num=%d\n",
            __FILE__, __func__, bs->filename, nb_sectors, sector_num);

    trace_paio_submit(acb, opaque, sector_num, nb_sectors, type);
    qemu_paio_submit(acb);
    return &acb->common;
}

BlockDriverAIOCB *paio_ioctl(BlockDriverState *bs, int fd,
        unsigned long int req, void *buf,
        BlockDriverCompletionFunc *cb, void *opaque) {
    struct qemu_paiocb *acb;
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    acb = qemu_aio_get(&raw_aio_pool, bs, cb, opaque);
    acb->aio_type = QEMU_AIO_IOCTL;
    acb->aio_fildes = fd;
    acb->aio_offset = 0;
    acb->aio_ioctl_buf = buf;
    acb->aio_ioctl_cmd = req;

    acb->next = posix_aio_state->first_aio;
    posix_aio_state->first_aio = acb;

    qemu_paio_submit(acb);
    return &acb->common;
}

int paio_init(void) {
    PosixAioState *s;
    int fds[2];
    int ret;
    LOGD_AIO("%s:%s\n", __FILE__, __func__);
    if (posix_aio_state)
        return 0;

    s = g_malloc(sizeof (PosixAioState));

    s->first_aio = NULL;
    if (qemu_pipe(fds) == -1) {
        fprintf(stderr, "failed to create pipe\n");
        g_free(s);
        return -1;
    }

    s->rfd = fds[0];
    s->wfd = fds[1];

    //    fcntl(s->rfd, F_SETFL, O_NDELAY);
    //    fcntl(s->wfd, F_SETFL, O_NDELAY);

    fcntl(s->rfd, F_SETFL, O_NONBLOCK);
    fcntl(s->wfd, F_SETFL, O_NONBLOCK);
    LOGD_AIO("%s:%s, fcntl(F_SETFL, O_NONBLOCK) = %d", __FILE__, __func__, s->rfd);


    qemu_aio_set_fd_handler(s->rfd, posix_aio_read, NULL, posix_aio_flush, s);

    ret = pthread_attr_init(&attr);
    if (ret)
        die2(ret, "pthread_attr_init");

    // MK - Setting high priority based on FIFO

    /*
#ifdef __ANDROID__
	int rt_max_prio, rt_min_prio;
	struct sched_param rt_param;
	pthread_attr_setschedpolicy(&attr, SCHED_FIFO);
	rt_max_prio = sched_get_priority_max(SCHED_FIFO);
	rt_min_prio = sched_get_priority_min(SCHED_FIFO);
	printf("Thread Priority Range: %d ... %d\n", rt_min_prio, rt_max_prio);
	rt_param.sched_priority = rt_max_prio;
	printf("Setting Priority for paio_init(): %d\n",rt_param.sched_priority);
	pthread_attr_setschedparam(&attr, &rt_param);
#endif // MK
*/


    ret = pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
    if (ret)
        die2(ret, "pthread_attr_setdetachstate");

    QTAILQ_INIT(&request_list);
    new_thread_bh = qemu_bh_new(spawn_thread_bh_fn, NULL);

    posix_aio_state = s;
    return 0;
}
