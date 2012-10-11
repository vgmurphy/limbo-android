/*
 Copyright (C) Max Kastanas 2012

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include "logutils.h"
#include "image-creator-jni.h"
#include <dlfcn.h>
#include <fcntl.h>
#include <errno.h>
#include <malloc.h>
#include "./../limbo-host-config.h"

#define MSG_BUFSIZE 1024
#define MAX_STRING_LEN 1024
#define PAGE_SIZE (sysconf(_SC_PAGESIZE))

extern int qemu_img_start(int argc, char **argv, char **envp);

/* JNI interface: constructs arguments and calls main function
 */

static int started = 0;
void * handle;

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_ImageCreator_start(
        JNIEnv* env, jobject thiz) {
    int res;
    char res_msg[MSG_BUFSIZE + 1] = {0};

    LOGV("***************** Create Image ************************");

    /* Read the member values from the Java Object
     */
    jclass c = (*env)->GetObjectClass(env, thiz);
    jfieldID fid = (*env)->GetFieldID(env, c, "filename", "Ljava/lang/String;");
    jstring jcpu = (*env)->GetObjectField(env, thiz, fid);
    const char *filename_str = (*env)->GetStringUTFChars(env, jcpu, 0);

    fid = (*env)->GetFieldID(env, c, "lib_path", "Ljava/lang/String;");
    jstring lib_path = (*env)->GetObjectField(env, thiz, fid);
    const char *lib_path_str = (*env)->GetStringUTFChars(env, lib_path, 0);

    fid = (*env)->GetFieldID(env, c, "memory", "I");
    int size = (*env)->GetIntField(env, thiz, fid);

    fid = (*env)->GetFieldID(env, c, "prealloc", "I");
    int prealloc = (*env)->GetIntField(env, thiz, fid);

    int params = 7;

    if (prealloc) {
        params += 2;
    }


    char size_str[MAX_STRING_LEN] = "128";
    sprintf(size_str, "%d", size);

    LOGV("Filename: %s ", filename_str);

    int i = 0;
    LOGV("Params = %d", params);
    char ** argv = (char **) malloc(params * sizeof (*argv));
    for (i = 0; i < params; i++) {
        argv[i] = (char *) malloc(MAX_STRING_LEN * sizeof (char));
    }

    int param = 0;

    strcpy(argv[param++], "libqemu-img");
    strcpy(argv[param++], "create");
    strcpy(argv[param++], "-f");
    strcpy(argv[param++], "qcow2");
    strcpy(argv[param++], filename_str);
    strcpy(argv[param], size_str);
    strcat(argv[param++], "M");
    if (prealloc) {
        strcpy(argv[param++], "-o");
        strcpy(argv[param++], "preallocation=metadata");
    }

    argv[param] = NULL;
    int argc = params - 1;
    for (i = 0; i < argc; i++) {
        LOGV("Arg(%d): %s", i, argv[i]);
    }

    LOGV("***************** INIT QEMU-IMG ************************");
    started = 1;

    //LOAD LIB
    sprintf(res_msg, "Loading lib: %s", lib_path_str);
    LOGV(res_msg);
    handle = dlopen(lib_path_str, RTLD_LAZY);

    if (!handle) {
        sprintf(res_msg, "Error opening lib: %s :%s", lib_path_str, dlerror());
        LOGV(res_msg);
        return (*env)->NewStringUTF(env, res_msg);
    }
    typedef void (*qemu_img_start_t)();

    // reset errors
    dlerror();
    qemu_img_start_t qemu_img_start = (qemu_img_start_t) dlsym(handle, "qemu_img_start");
    const char *dlsym_error = dlerror();
    if (dlsym_error) {
        LOGV("Cannot load symbol 'qemu_img_start': %s\n", dlsym_error);
        dlclose(handle);
        handle = NULL;
        return (*env)->NewStringUTF(env, res_msg);
    }

    qemu_img_start(argc, argv, NULL);
    //UNLOAD LIB // CRASHES DUNNO WHY
//    sprintf(res_msg, "Closing lib: %s", lib_path_str);
//    LOGV(res_msg);
//    dlclose(handle);
//    handle = NULL;

    /* Log and return a string of success or error message.
     * This can be enhanced semantically with codes.
     */
    sprintf(res_msg, "VM IMG has been created");
    LOGV(res_msg);

    return NULL;
}




