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
#include "vm-executor-jni.h"
#include <dlfcn.h>
#include <fcntl.h>
#include <errno.h>
#include <malloc.h>
#include "./../limbo-host-config.h"

#define MSG_BUFSIZE 1024
#define MAX_STRING_LEN 1024
#define PAGE_SIZE (sysconf(_SC_PAGESIZE))

extern int qemu_start(int argc, char **argv, char **envp);

extern void vnc_change_pwd(char * passwd);

extern int get_state(int *state);

extern int get_save_state(int *state);

extern void change_dev(const char * device, const char *target);

extern int set_dns_addr_str(const char *dns_addr_str1);

extern void setAIOMaxThreads(int threads);

extern void sendMonitorCommand(const char *cmdline);

/* JNI interface: constructs arguments and calls main function
 */

static int started = 0;
void * handle;

void loadLib(const char * lib_path_str) {

	char res_msg[MAX_STRING_LEN];
	sprintf(res_msg, "Loading lib: %s", lib_path_str);
	LOGV(res_msg);
	handle = dlopen(lib_path_str, RTLD_LAZY);
}

//MK removing, not needed for any specific purpose
//#include <machine/cpu-features.h>

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *pvt) {
	printf("* JNI_OnLoad called\n");
//    int cpufamily = android_getCpuFamily();
//    int features = android_getCpuFeatures();
//    printf("cpufamily = %ld", cpufamily);
//    printf("cpufeatures = %ld", features);
	/*
	 if ((features & ANDROID_CPU_ARM_FEATURE_ARMv7) != 0) {
	 printf("Detected ARMv7 CPU !\n");
	 }

	 if ((features & ANDROID_CPU_ARM_FEATURE_NEON) != 0) {
	 printf("Detected NEON support !\n");
	 }
	 */
	return JNI_VERSION_1_2;
}

extern void do_sdl_resize(int width, int height, int bpp);

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_VMExecutor_resize(
		JNIEnv* env, jobject thiz) {
	char res_msg[MSG_BUFSIZE + 1] = { 0 };

	if (handle == NULL)
		return (*env)->NewStringUTF(env, "VM not running");

	LOGV("Resize: %s\n");

	typedef void (*do_sdl_resize_t)();
	dlerror();
	do_sdl_resize_t do_sdl_resize = (do_sdl_resize_t) dlsym(handle,
			"do_sdl_resize");
	const char *dlsym_error = dlerror();
	if (dlsym_error) {
		LOGV("Cannot load symbol 'do_sdl_resize': %s\n", dlsym_error);
//        dlclose(handle);
//        handle = NULL;
		return (*env)->NewStringUTF(env, res_msg);
	}

	do_sdl_resize(720, 400, 16);
	sprintf(res_msg, "Resized");
	LOGV(res_msg);

	return (*env)->NewStringUTF(env, res_msg);
}

extern void scale(int width, int height);

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_VMExecutor_scale(
		JNIEnv* env, jobject thiz) {
	char res_msg[MSG_BUFSIZE + 1] = { 0 };

	if (handle == NULL)
		return (*env)->NewStringUTF(env, "VM not running");

	LOGV("Scaling\n");
	jclass c = (*env)->GetObjectClass(env, thiz);
	jfieldID fid = (*env)->GetFieldID(env, c, "width", "I");
	int width = (*env)->GetIntField(env, thiz, fid);

	fid = (*env)->GetFieldID(env, c, "height", "I");
	int height = (*env)->GetIntField(env, thiz, fid);

	typedef void (*scale_t)();
	dlerror();
	scale_t scale = (scale_t) dlsym(handle, "scale");
	const char *dlsym_error = dlerror();
	if (dlsym_error) {
		LOGV("Cannot load symbol 'scale': %s\n", dlsym_error);
//        dlclose(handle);
//        handle = NULL;
		return (*env)->NewStringUTF(env, res_msg);
	}

	scale(width, height);
	sprintf(res_msg, "Scaled");
	LOGV(res_msg);

	return (*env)->NewStringUTF(env, res_msg);
}

extern void toggleFullScreen();

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_VMExecutor_togglefullscreen(
		JNIEnv* env, jobject thiz) {
	char res_msg[MSG_BUFSIZE + 1] = { 0 };

	if (handle == NULL)
		return (*env)->NewStringUTF(env, "VM not running");

	LOGV("Toggle Fullscreen\n");

	typedef void (*toggleFullScreen_t)();
	dlerror();
	toggleFullScreen_t toggleFullScreen = (toggleFullScreen_t) dlsym(handle,
			"toggleFullScreen");
	const char *dlsym_error = dlerror();
	if (dlsym_error) {
		LOGV("Cannot load symbol 'toggleFullScreen': %s\n", dlsym_error);
		return (*env)->NewStringUTF(env, res_msg);
	}

	toggleFullScreen();
	sprintf(res_msg, "Full Screen");
	LOGV(res_msg);

	return (*env)->NewStringUTF(env, res_msg);
}

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_VMExecutor_savevm1(
		JNIEnv* env, jobject thiz) {
	char res_msg[MSG_BUFSIZE + 1] = { 0 };

	if (handle == NULL)
		return (*env)->NewStringUTF(env, "VM not running");

	jclass c = (*env)->GetObjectClass(env, thiz);
	jfieldID fid = (*env)->GetFieldID(env, c, "snapshot_name",
			"Ljava/lang/String;");
	jstring snapshot_name = (*env)->GetObjectField(env, thiz, fid);
	const char *snapshot_name_str = (*env)->GetStringUTFChars(env,
			snapshot_name, 0);

	char cmd[MSG_BUFSIZE + 1];

	LOGV("Save VM TEST\n");

	typedef void (*sendMonitorCommand_t)();
	dlerror();
	sendMonitorCommand_t sendMonitorCommand = (sendMonitorCommand_t) dlsym(
			handle, "sendMonitorCommand");
	const char *dlsym_error = dlerror();
	if (dlsym_error) {
		LOGV("Cannot load symbol 'sendMonitorCommand': %s\n", dlsym_error);
		return (*env)->NewStringUTF(env, res_msg);
	}

	strcpy(cmd, "savevm ");
	strcat(cmd, snapshot_name_str);
	LOGV("Sending Monitor command: %s\n", cmd);
	sendMonitorCommand(cmd);
	sprintf(res_msg, "Complete saving VM");
	LOGV(res_msg);

	return (*env)->NewStringUTF(env, res_msg);
}

extern void saveSnapshot(const char *snapshot);

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_VMExecutor_savevm2(
		JNIEnv* env, jobject thiz) {
	char res_msg[MSG_BUFSIZE + 1] = { 0 };

	if (handle == NULL)
		return (*env)->NewStringUTF(env, "VM not running");

	jclass c = (*env)->GetObjectClass(env, thiz);
	jfieldID fid = (*env)->GetFieldID(env, c, "snapshot_name",
			"Ljava/lang/String;");
	jstring snapshot_name = (*env)->GetObjectField(env, thiz, fid);
	const char *snapshot_name_str = (*env)->GetStringUTFChars(env,
			snapshot_name, 0);

	char cmd[MSG_BUFSIZE + 1];

	LOGV("Save VM TEST2\n");

	typedef void (*saveSnapshot_t)();
	dlerror();
	saveSnapshot_t saveSnapshot = (saveSnapshot_t) dlsym(handle,
			"saveSnapshot");
	const char *dlsym_error = dlerror();
	if (dlsym_error) {
		LOGV("Cannot load symbol 'saveSnapshot': %s\n", dlsym_error);
		return (*env)->NewStringUTF(env, res_msg);
	}

	LOGV("Saving Snapshot: %s\n", snapshot_name_str);
	saveSnapshot(snapshot_name_str);
	sprintf(res_msg, "Complete saving VM");
	LOGV(res_msg);

	return (*env)->NewStringUTF(env, res_msg);
}

extern void sendHumanMonitorCommand(const char *command_line);

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_VMExecutor_savevm3(
		JNIEnv* env, jobject thiz) {
	char res_msg[MSG_BUFSIZE + 1] = { 0 };

	if (handle == NULL)
		return (*env)->NewStringUTF(env, "VM not running");

	jclass c = (*env)->GetObjectClass(env, thiz);
	jfieldID fid = (*env)->GetFieldID(env, c, "snapshot_name",
			"Ljava/lang/String;");
	jstring snapshot_name = (*env)->GetObjectField(env, thiz, fid);
	const char *snapshot_name_str = (*env)->GetStringUTFChars(env,
			snapshot_name, 0);

	char cmd[MSG_BUFSIZE + 1];

	LOGV("Save VM TEST\n");

	typedef void (*sendHumanMonitorCommand_t)();
	dlerror();
	sendHumanMonitorCommand_t sendHumanMonitorCommand =
			(sendHumanMonitorCommand_t) dlsym(handle,
					"sendHumanMonitorCommand");
	const char *dlsym_error = dlerror();
	if (dlsym_error) {
		LOGV("Cannot load symbol 'sendHumanMonitorCommand': %s\n", dlsym_error);
		return (*env)->NewStringUTF(env, res_msg);
	}

	strcpy(cmd, "savevm ");
	strcat(cmd, snapshot_name_str);
	LOGV("Sending Human Monitor command: %s\n", cmd);
	sendHumanMonitorCommand(cmd);
	sprintf(res_msg, "Complete saving VM");
	LOGV(res_msg);

	return (*env)->NewStringUTF(env, res_msg);
}

//Not working machine needs to be suspended for now using QMP monitor workaround

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_VMExecutor_save(
		JNIEnv* env, jobject thiz) {
	char res_msg[MSG_BUFSIZE + 1] = { 0 };

	if (handle == NULL)
		return (*env)->NewStringUTF(env, "VM not running");

	jclass c = (*env)->GetObjectClass(env, thiz);
	jfieldID fid = (*env)->GetFieldID(env, c, "snapshot_name",
			"Ljava/lang/String;");
	jstring snapshot_name = (*env)->GetObjectField(env, thiz, fid);
	const char *snapshot_name_str = (*env)->GetStringUTFChars(env,
			snapshot_name, 0);

	LOGV("Saving VM State: %s\n", snapshot_name_str);

	typedef void (*save_vm_t)();
	dlerror();
	save_vm_t save_vm = (save_vm_t) dlsym(handle, "and_do_savevm");
	const char *dlsym_error = dlerror();
	if (dlsym_error) {
		LOGV("Cannot load symbol 'save_vm': %s\n", dlsym_error);
//        dlclose(handle);
//        handle = NULL;
		return (*env)->NewStringUTF(env, res_msg);
	}

	save_vm(snapshot_name_str);
	sprintf(res_msg, "VM State Saved");
	LOGV(res_msg);

	return (*env)->NewStringUTF(env, res_msg);
}

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_VMExecutor_vncchangepassword(
		JNIEnv* env, jobject thiz) {
	char res_msg[MSG_BUFSIZE + 1] = { 0 };
	if (handle == NULL)
		return (*env)->NewStringUTF(env, "VM not running");

	jclass c = (*env)->GetObjectClass(env, thiz);
	jfieldID fid = (*env)->GetFieldID(env, c, "vnc_passwd",
			"Ljava/lang/String;");
	jstring jvnc_passwd = (*env)->GetObjectField(env, thiz, fid);
	const char *vnc_passwd_str = (*env)->GetStringUTFChars(env, jvnc_passwd, 0);

	typedef void (*vnc_change_pwd_t)();
	dlerror();
	vnc_change_pwd_t vnc_change_pwd = (vnc_change_pwd_t) dlsym(handle,
			"vnc_change_pwd");
	const char *dlsym_error = dlerror();
	if (dlsym_error) {
		LOGV("Cannot load symbol 'vnc_change_pwd': %s\n", dlsym_error);
//        dlclose(handle);
//        handle = NULL;
		return (*env)->NewStringUTF(env, res_msg);
	}

	vnc_change_pwd(vnc_passwd_str);

	sprintf(res_msg, "VNC Password Changed");
	LOGV(res_msg);

	return (*env)->NewStringUTF(env, res_msg);
}

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_VMExecutor_dnschangeaddr(
		JNIEnv* env, jobject thiz) {
	char res_msg[MSG_BUFSIZE + 1] = { 0 };

	jclass c = (*env)->GetObjectClass(env, thiz);
	jfieldID fid = (*env)->GetFieldID(env, c, "dns_addr", "Ljava/lang/String;");
	jstring jdns_addr = (*env)->GetObjectField(env, thiz, fid);
	const char * dns_addr_str = NULL;
	if (jdns_addr != NULL)
		dns_addr_str = (*env)->GetStringUTFChars(env, jdns_addr, 0);

	fid = (*env)->GetFieldID(env, c, "libqemu", "Ljava/lang/String;");
	jstring jlib_path = (*env)->GetObjectField(env, thiz, fid);
	const char * lib_path_str = NULL;
	if (jlib_path != NULL)
		lib_path_str = (*env)->GetStringUTFChars(env, jlib_path, 0);

//    LOGV("Attempting to set DNS: %s\n",dns_addr_str);
	typedef void (*set_dns_addr_str_t)();
	dlerror();
	//LOAD LIB
	if (handle == NULL) {
		loadLib(lib_path_str);
	}

	if (!handle) {
		sprintf(res_msg, "%s: Error opening lib: %s :%s", __func__,
				lib_path_str, dlerror());
		LOGV(res_msg);
		return (*env)->NewStringUTF(env, res_msg);
	}
	dlerror();
	set_dns_addr_str_t set_dns_addr_str = (set_dns_addr_str_t) dlsym(handle,
			"set_dns_addr_str");
	const char *dlsym_error = dlerror();
	if (dlsym_error) {
		LOGV("Cannot load symbol 'set_dns_addr_str': %s\n", dlsym_error);
//        dlclose(handle);
//        handle = NULL;
		return (*env)->NewStringUTF(env, res_msg);
	}

	set_dns_addr_str(dns_addr_str);

//    sprintf(res_msg, "DNS Address Changed to: %s", dns_addr_str);
	LOGV(res_msg);

	return (*env)->NewStringUTF(env, res_msg);
}

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_VMExecutor_changedev(
		JNIEnv* env, jobject thiz) {
	char res_msg[MSG_BUFSIZE + 1] = { 0 };

	if (handle == NULL)
		return (*env)->NewStringUTF(env, "VM not running");

	jclass c = (*env)->GetObjectClass(env, thiz);
	jfieldID fid = (*env)->GetFieldID(env, c, "qemu_dev", "Ljava/lang/String;");
	jstring jdev = (*env)->GetObjectField(env, thiz, fid);
	const char *dev = (*env)->GetStringUTFChars(env, jdev, 0);

	fid = (*env)->GetFieldID(env, c, "qemu_dev_value", "Ljava/lang/String;");
	jstring jdev_value = (*env)->GetObjectField(env, thiz, fid);
	const char *dev_value = (*env)->GetStringUTFChars(env, jdev_value, 0);

	typedef void (*change_dev_t)();
	dlerror();
	change_dev_t change_dev = (change_dev_t) dlsym(handle, "change_dev");
	const char *dlsym_error = dlerror();
	if (dlsym_error) {
		LOGV("Cannot load symbol 'change_dev': %s\n", dlsym_error);
//        dlclose(handle);
//        handle = NULL;
		return (*env)->NewStringUTF(env, res_msg);
	}

	printf(res_msg, "Changing Device: %s to: %s", dev, dev_value);
	change_dev(dev, dev_value);

	sprintf(res_msg, "Device %s changed to: %s", dev, dev_value);
	LOGV(res_msg);

	return (*env)->NewStringUTF(env, res_msg);
}

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_VMExecutor_ejectdev(
		JNIEnv* env, jobject thiz) {
	char res_msg[MSG_BUFSIZE + 1] = { 0 };

	if (handle == NULL)
		return (*env)->NewStringUTF(env, "VM not running");

	jclass c = (*env)->GetObjectClass(env, thiz);
	jfieldID fid = (*env)->GetFieldID(env, c, "qemu_dev", "Ljava/lang/String;");
	jstring jdev = (*env)->GetObjectField(env, thiz, fid);
	const char *dev = (*env)->GetStringUTFChars(env, jdev, 0);

	typedef void (*eject_dev_t)();
	dlerror();
	eject_dev_t eject_dev = (eject_dev_t) dlsym(handle, "eject_dev");
	const char *dlsym_error = dlerror();
	if (dlsym_error) {
		LOGV("Cannot load symbol 'eject_dev': %s\n", dlsym_error);
//        dlclose(handle);
//        handle = NULL;
		return (*env)->NewStringUTF(env, res_msg);
	}

	printf(res_msg, "Ejecting Device: %s", dev);
	eject_dev(dev);

	sprintf(res_msg, "Device %s ejected", dev);
	LOGV(res_msg);

	return (*env)->NewStringUTF(env, res_msg);
}

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_VMExecutor_getsavestate(
		JNIEnv* env, jobject thiz) {
	char res_msg[MSG_BUFSIZE + 1] = { 0 };

	if (handle == NULL)
		return (*env)->NewStringUTF(env, "VM not running");

	typedef int (*get_save_state_t)();
	dlerror();
	get_save_state_t get_save_state = (get_save_state_t) dlsym(handle,
			"get_save_state");
	const char *dlsym_error = dlerror();
	if (dlsym_error) {
		LOGV("Cannot load symbol 'get_save_state': %s\n", dlsym_error);
//        dlclose(handle);
//        handle = NULL;
		return (*env)->NewStringUTF(env, res_msg);
	}

	int state = 0;
	state = get_save_state();
	//    LOGV("%s:%s: State: %d", __FILE__, __func__, state);

	if (state)
		sprintf(res_msg, "SAVING");
	else
		sprintf(res_msg, "DONE");
	//    LOGV(res_msg);

	return (*env)->NewStringUTF(env, res_msg);
}

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_VMExecutor_getstate(
		JNIEnv* env, jobject thiz) {
	char res_msg[MSG_BUFSIZE + 1] = { 0 };

	if (handle == NULL)
		return (*env)->NewStringUTF(env, "VM not running");

	typedef int (*get_state_t)();
	dlerror();
	get_state_t get_state = (get_state_t) dlsym(handle, "get_state");
	const char *dlsym_error = dlerror();
	if (dlsym_error) {
		LOGV("Cannot load symbol 'get_state': %s\n", dlsym_error);
//        dlclose(handle);
//        handle = NULL;
		return (*env)->NewStringUTF(env, res_msg);
	}

	int state = 0;
	state = get_state();
	//    LOGV("%s:%s: State: %d", __FILE__, __func__, state);

	if (state)
		sprintf(res_msg, "RUNNING");
	else
		sprintf(res_msg, "READY");
	//    LOGV(res_msg);

	return (*env)->NewStringUTF(env, res_msg);
}

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_VMExecutor_stop(
		JNIEnv* env, jobject thiz) {
	char res_msg[MSG_BUFSIZE + 1] = { 0 };

	if (handle == NULL)
		return (*env)->NewStringUTF(env, "VM not running");

	jclass c = (*env)->GetObjectClass(env, thiz);
	jfieldID fid = (*env)->GetFieldID(env, c, "restart", "I");
	int restart_int = (*env)->GetIntField(env, thiz, fid);

	typedef void (*stop_vm_t)();
	dlerror();
	stop_vm_t stop_vm = (stop_vm_t) dlsym(handle, "stop_vm");
	const char *dlsym_error = dlerror();
	if (dlsym_error) {
		LOGV("Cannot load symbol 'stop_vm': %s\n", dlsym_error);
//        dlclose(handle);
//        handle = NULL;
		return (*env)->NewStringUTF(env, res_msg);
	}

	stop_vm(!restart_int);

	if (restart_int)
		sprintf(res_msg, "VM Restart Request");
	else
		sprintf(res_msg, "VM Stop Request");
	LOGV(res_msg);

	started = restart_int;

	return (*env)->NewStringUTF(env, res_msg);
}

JNIEXPORT jstring JNICALL Java_com_max2idea_android_limbo_jni_VMExecutor_start(
		JNIEnv* env, jobject thiz) {
	int res;
	char res_msg[MSG_BUFSIZE + 1] = { 0 };

	if (started) {
		sprintf(res_msg, "VM Already started");
		LOGV(res_msg);
		return (*env)->NewStringUTF(env, res_msg);
	}

	LOGV("***************** INIT LIMBO ************************");

	/* Read the member values from the Java Object
	 */
	jclass c = (*env)->GetObjectClass(env, thiz);
	jfieldID fid = (*env)->GetFieldID(env, c, "cpu", "Ljava/lang/String;");
	jstring jcpu = (*env)->GetObjectField(env, thiz, fid);
	const char *cpu_str = (*env)->GetStringUTFChars(env, jcpu, 0);

	LOGV("CPU= %s", cpu_str);

	fid = (*env)->GetFieldID(env, c, "memory", "I");
	int mem = (*env)->GetIntField(env, thiz, fid);

	LOGV("MEM= %d", mem);

	fid = (*env)->GetFieldID(env, c, "disableacpi", "I");
	int disableacpi = (*env)->GetIntField(env, thiz, fid);

	fid = (*env)->GetFieldID(env, c, "disablehpet", "I");
	int disablehpet = (*env)->GetIntField(env, thiz, fid);

	fid = (*env)->GetFieldID(env, c, "usbmouse", "I");
	int usbmouse = (*env)->GetIntField(env, thiz, fid);

	fid = (*env)->GetFieldID(env, c, "enableqmp", "I");
	int enableqmp = (*env)->GetIntField(env, thiz, fid);

	fid = (*env)->GetFieldID(env, c, "enablevnc", "I");
	int enablevnc = (*env)->GetIntField(env, thiz, fid);

	fid = (*env)->GetFieldID(env, c, "vnc_allow_external", "I");
	int vnc_allow_external = (*env)->GetIntField(env, thiz, fid);

	LOGV("vnc_allow_external= %d", vnc_allow_external);

	fid = (*env)->GetFieldID(env, c, "hda_img_path", "Ljava/lang/String;");
	jstring jhda_img_path = (*env)->GetObjectField(env, thiz, fid);
	LOGV("HDA jstring= %s", jhda_img_path);
	const char * hda_img_path_str = NULL;
	if (jhda_img_path != NULL)
		hda_img_path_str = (*env)->GetStringUTFChars(env, jhda_img_path, 0);

	LOGV("HDA= %s", hda_img_path_str);

	fid = (*env)->GetFieldID(env, c, "hdb_img_path", "Ljava/lang/String;");
	jstring jhdb_img_path = (*env)->GetObjectField(env, thiz, fid);
	const char * hdb_img_path_str = NULL;
	if (jhdb_img_path != NULL)
		hdb_img_path_str = (*env)->GetStringUTFChars(env, jhdb_img_path, 0);

	fid = (*env)->GetFieldID(env, c, "cd_iso_path", "Ljava/lang/String;");
	jstring jcdrom_iso_path = (*env)->GetObjectField(env, thiz, fid);
	const char * cdrom_iso_path_str = NULL;
	if (jcdrom_iso_path != NULL)
		cdrom_iso_path_str = (*env)->GetStringUTFChars(env, jcdrom_iso_path, 0);

	fid = (*env)->GetFieldID(env, c, "fda_img_path", "Ljava/lang/String;");
	jstring jfda_img_path = (*env)->GetObjectField(env, thiz, fid);
	const char * fda_img_path_str = NULL;
	if (jfda_img_path != NULL)
		fda_img_path_str = (*env)->GetStringUTFChars(env, jfda_img_path, 0);

	fid = (*env)->GetFieldID(env, c, "fdb_img_path", "Ljava/lang/String;");
	jstring jfdb_img_path = (*env)->GetObjectField(env, thiz, fid);
	const char * fdb_img_path_str = NULL;
	if (jfdb_img_path != NULL)
		fdb_img_path_str = (*env)->GetStringUTFChars(env, jfdb_img_path, 0);

	fid = (*env)->GetFieldID(env, c, "bootdevice", "Ljava/lang/String;");
	jstring jboot_dev = (*env)->GetObjectField(env, thiz, fid);
	const char * boot_dev_str = NULL;
	if (jboot_dev != NULL)
		boot_dev_str = (*env)->GetStringUTFChars(env, jboot_dev, 0);

	fid = (*env)->GetFieldID(env, c, "net_cfg", "Ljava/lang/String;");
	jstring jnet = (*env)->GetObjectField(env, thiz, fid);
	const char * net_str = NULL;
	if (jnet != NULL)
		net_str = (*env)->GetStringUTFChars(env, jnet, 0);

	fid = (*env)->GetFieldID(env, c, "nic_driver", "Ljava/lang/String;");
	jstring jnic_driver = (*env)->GetObjectField(env, thiz, fid);
	const char * nic_driver_str = NULL;
	if (jnic_driver != NULL)
		nic_driver_str = (*env)->GetStringUTFChars(env, jnic_driver, 0);

	fid = (*env)->GetFieldID(env, c, "libqemu", "Ljava/lang/String;");
	jstring jlib_path = (*env)->GetObjectField(env, thiz, fid);
	const char * lib_path_str = NULL;
	if (jlib_path != NULL)
		lib_path_str = (*env)->GetStringUTFChars(env, jlib_path, 0);

	fid = (*env)->GetFieldID(env, c, "vga_type", "Ljava/lang/String;");
	jstring jvga_type = (*env)->GetObjectField(env, thiz, fid);
	const char * vga_type_str = NULL;
	if (jvga_type != NULL)
		vga_type_str = (*env)->GetStringUTFChars(env, jvga_type, 0);

	fid = (*env)->GetFieldID(env, c, "hd_cache", "Ljava/lang/String;");
	jstring jhd_cache = (*env)->GetObjectField(env, thiz, fid);
	const char * hd_cache_str = NULL;
	if (jhd_cache != NULL)
		hd_cache_str = (*env)->GetStringUTFChars(env, jhd_cache, 0);

	fid = (*env)->GetFieldID(env, c, "sound_card", "Ljava/lang/String;");
	jstring jsound_card = (*env)->GetObjectField(env, thiz, fid);
	const char * sound_card_str = NULL;
	if (jsound_card != NULL)
		sound_card_str = (*env)->GetStringUTFChars(env, jsound_card, 0);

	fid = (*env)->GetFieldID(env, c, "snapshot_name", "Ljava/lang/String;");
	jstring jsnapshot_name = (*env)->GetObjectField(env, thiz, fid);
	const char * snapshot_name_str = NULL;
	if (jsnapshot_name != NULL)
		snapshot_name_str = (*env)->GetStringUTFChars(env, jsnapshot_name, 0);

	fid = (*env)->GetFieldID(env, c, "vnc_passwd", "Ljava/lang/String;");
	jstring jvnc_passwd = (*env)->GetObjectField(env, thiz, fid);
	const char * vnc_passwd_str = NULL;
	if (jvnc_passwd != NULL)
		vnc_passwd_str = (*env)->GetStringUTFChars(env, jvnc_passwd, 0);

	fid = (*env)->GetFieldID(env, c, "base_dir", "Ljava/lang/String;");
	jstring jbase_dir = (*env)->GetObjectField(env, thiz, fid);
	const char * base_dir_str = NULL;
	if (jbase_dir != NULL)
		base_dir_str = (*env)->GetStringUTFChars(env, jbase_dir, 0);

	fid = (*env)->GetFieldID(env, c, "dns_addr", "Ljava/lang/String;");
	jstring jdns_addr = (*env)->GetObjectField(env, thiz, fid);
	const char * dns_addr_str = NULL;
	if (jdns_addr != NULL)
		dns_addr_str = (*env)->GetStringUTFChars(env, jdns_addr, 0);

	fid = (*env)->GetFieldID(env, c, "kernel", "Ljava/lang/String;");
	jstring jkernel = (*env)->GetObjectField(env, thiz, fid);
	const char * kernel_str = NULL;
	if (jkernel != NULL)
		kernel_str = (*env)->GetStringUTFChars(env, jkernel, 0);

	fid = (*env)->GetFieldID(env, c, "arch", "Ljava/lang/String;");
	jstring jarch = (*env)->GetObjectField(env, thiz, fid);
	const char * arch_str = NULL;
	if (jarch != NULL)
		arch_str = (*env)->GetStringUTFChars(env, jarch, 0);

	fid = (*env)->GetFieldID(env, c, "append", "Ljava/lang/String;");
	jstring jappend = (*env)->GetObjectField(env, thiz, fid);
	const char * append_str = NULL;
	if (jappend != NULL)
		append_str = (*env)->GetStringUTFChars(env, jappend, 0);

	fid = (*env)->GetFieldID(env, c, "initrd", "Ljava/lang/String;");
	jstring jinitrd = (*env)->GetObjectField(env, thiz, fid);
	const char * initrd_str = NULL;
	if (jinitrd != NULL)
		initrd_str = (*env)->GetStringUTFChars(env, jinitrd, 0);

	fid = (*env)->GetFieldID(env, c, "aiomaxthreads", "I");
	int aiomaxthreads = (*env)->GetIntField(env, thiz, fid);

	LOGV("Finished getting Java fields");

	int params = 8;

	if (hda_img_path_str != NULL) {
		params += 2;
	}
	if (hdb_img_path_str != NULL) {
		params += 2;
	}
	if (cdrom_iso_path_str != NULL) {
		params += 2;
	}
	if (fda_img_path_str != NULL) {
		params += 2;
	}
	if (fdb_img_path_str != NULL) {
		params += 2;
	}
	if (net_str != NULL && strcmp(net_str, "none") != 0) {
		params += 2;
	} else {
		nic_driver_str = NULL;
	}
	if (boot_dev_str != NULL) {
		params += 2;
	}
	if (vga_type_str != NULL && strncmp(arch_str, "arm", 3) != 0) {
		params += 2;
	}
	if (sound_card_str != NULL) {
		params += 2;
	}

	if (disableacpi) {
		params += 1;
	}

	if (disablehpet) {
		params += 1;
	}
	if (usbmouse) {
		params += 3;
	}

	if (snapshot_name_str != NULL && strcmp(snapshot_name_str, "") != 0) {
		params += 2;
	}
	if (enableqmp) {
		params += 1;
	}
	if (enablevnc) {
		params += 2;
	}

	params += 2; // For CPU option

	params += 2; //For -smp option
	params += 2; //For -M option
	params += 2; //For -k option

	if (strncmp(arch_str, "arm", 3) == 0) {
		params += 6; //For kernel, initrd, and append options
	}

	char mem_str[MAX_STRING_LEN] = "128";
	sprintf(mem_str, "%d", mem);

	int i = 0;
	LOGV("Params = %d", params);
	char ** argv = (char **) malloc(params * sizeof(*argv));
	for (i = 0; i < params; i++) {
		argv[i] = (char *) malloc(MAX_STRING_LEN * sizeof(char));
	}

	int param = 0;
	strcpy(argv[param++], lib_path_str);

	if (strncmp(arch_str, "arm", 3) == 0) {
		strcpy(argv[param++], "-kernel");
		strcpy(argv[param++], kernel_str);

		strcpy(argv[param++], "-initrd");
		strcpy(argv[param++], initrd_str);

		strcpy(argv[param++], "-append");
		strcpy(argv[param++], append_str);

	}

	strcpy(argv[param++], "-cpu");
	strcpy(argv[param++], cpu_str);

	strcpy(argv[param++], "-m");
	strcpy(argv[param++], mem_str);

	strcpy(argv[param++], "-L");
	strcpy(argv[param++], base_dir_str);

	if (hda_img_path_str != NULL) {

		strcpy(argv[param++], "-hda");
		strcpy(argv[param++], hda_img_path_str);

//		strcpy(argv[param++], "-drive");
//		strcpy(argv[param], "file=");
//		strcat(argv[param], hda_img_path_str);
//		strcat(argv[param], ",index=0,media=disk");
//
//		if(strncmp(arch_str, "arm", 3) == 0){
//			strcat(argv[param], ",if=scsi");
////			strcat(argv[param], ",if=ide");
////			strcat(argv[param], ",if=sd");
//		}else {
//			strcat(argv[param], ",if=ide");
//		}
//
//		//        strcat(argv[param], ",if=scsi,bus=0,unit=6"); //scsi interface not working
//		strcat(argv[param], ",aio=threads");
//		if (hd_cache_str != NULL && strcmp(hd_cache_str, "default") != 0) {
//			strcat(argv[param], ",cache=");
//			strcat(argv[param++], hd_cache_str);
//		} else {
//			param++;
//		}
	}
	if (hdb_img_path_str != NULL) {
		strcpy(argv[param++], "-hdb");
		strcpy(argv[param++], hdb_img_path_str);

//		//MORE OPTIONS
//		strcpy(argv[param++], "-drive");
//		strcpy(argv[param], "file=");
//		strcat(argv[param], hdb_img_path_str);
//		strcat(argv[param], ",index=1,media=disk");
//
//		if(strncmp(arch_str, "arm", 3) == 0){
//			strcat(argv[param], ",if=scsi");
//			//			strcat(argv[param], ",if=ide");
////			strcat(argv[param], ",if=sd"); // Trying SD card for arm
//		}else {
//			strcat(argv[param], ",if=ide");
//		}
//		//        strcat(argv[param], ",if=scsi,bus=0,unit=7"); //scsi interface not working
//		strcat(argv[param], ",aio=threads");
//		if (hd_cache_str != NULL && strcmp(hd_cache_str, "default") != 0) {
//			strcat(argv[param], ",cache=");
//			strcat(argv[param++], hd_cache_str);
//		} else {
//			param++;
//		}
	}
	if (cdrom_iso_path_str != NULL) {
		LOGV("Adding CD");
		strcpy(argv[param++], "-cdrom");
		strcpy(argv[param++], cdrom_iso_path_str);
	}
	if (fda_img_path_str != NULL) {
		LOGV("Adding FDA");
		strcpy(argv[param++], "-fda");
		strcpy(argv[param++], fda_img_path_str);
	}
	if (fdb_img_path_str != NULL) {
		LOGV("Adding FDB");
		strcpy(argv[param++], "-fdb");
		strcpy(argv[param++], fdb_img_path_str);
	}

	if (vga_type_str != NULL && strncmp(arch_str, "arm", 3) != 0) {
		LOGV("Adding vga: %s", vga_type_str);
		strcpy(argv[param++], "-vga");
		strcpy(argv[param++], vga_type_str); //[std|cirrus|vmware|qxl|xenfb|none] //select video card type
	}

	if (boot_dev_str != NULL) {
		LOGV("Adding boot device: %s", boot_dev_str);
		strcpy(argv[param++], "-boot");
		strcpy(argv[param++], boot_dev_str);
	}

	if (net_str != NULL) {
		LOGV("Adding Net: %s", net_str);
		strcpy(argv[param++], "-net");
		strcpy(argv[param], net_str);
		//FIXME: NOT WORKING setting DNS workaround below
//        LOGV("DNS=%s",dns_addr_str);
//        if(dns_addr_str!=NULL){
//        	strcat(argv[param], ",dns=");
//        	strcat(argv[param], dns_addr_str);
//        }
		param++;

	}

	if (nic_driver_str != NULL) {
		LOGV("Adding NIC: %s", nic_driver_str);
		strcpy(argv[param++], "-net");
		if (strncmp(arch_str, "arm", 3) == 0) {
			strcpy(argv[param], "nic");
			param++;
		} else {
			strcpy(argv[param], "nic,model=");
			strcat(argv[param++], nic_driver_str);
		}
	}

	if (sound_card_str != NULL && strcmp(sound_card_str, "None") != 0) {
		LOGV("Adding Sound: %s", sound_card_str);
		strcpy(argv[param++], "-soundhw");
		strcpy(argv[param++], sound_card_str);
	}

	if (snapshot_name_str != NULL && strcmp(snapshot_name_str, "") != 0) {
		LOGV("Adding snapshot: %s", snapshot_name_str);
		strcpy(argv[param++], "-loadvm");
		strcpy(argv[param++], snapshot_name_str);
	}

	if (usbmouse) {
		LOGV("Adding USB MOUSE");
		strcpy(argv[param++], "-usb");
		strcpy(argv[param++], "-usbdevice");
		strcpy(argv[param++], "tablet");
	}
	if (disableacpi) {
		LOGV("Disabling ACPI");
		strcpy(argv[param++], "-no-acpi"); //disable ACPI
	}
	if (disablehpet) {
		LOGV("Disabling HPET");
		strcpy(argv[param++], "-no-hpet"); //        disable HPET
	}

	if (enableqmp) { //Not working
		LOGV("Enable qmp server");
		strcpy(argv[param++], "-qmp tcp:0:4444,server"); //        disable HPET
	}

	//    strcpy(argv[param++], "-D");
	//    strcpy(argv[param++], "/sdcard/limbo/log.txt");
	//    strcpy(argv[param++], "-win2k-hack");     //use it when installing Windows 2000 to avoid a disk full bug
	//    strcpy(argv[param++], "--trace");
	//    strcpy(argv[param++], "events=/sdcard/limbo/tmp/events");
	//    strcpy(argv[param++], "--trace");
	//    strcpy(argv[param++], "file=/sdcard/limbo/tmp/trace");
	//    strcpy(argv[param++], "-nographic"); //DO NOT USE //      disable graphical output and redirect serial I/Os to console

	if (enablevnc) {
		LOGV("Enable VNC server");
		strcpy(argv[param++], "-vnc");
		if (vnc_allow_external) {
			strcpy(argv[param++], ":1,password");
			//TODO: Allow connections from External
			//this is still not secure
			// Use with x509 auth and TLS for encryption
		} else
			strcpy(argv[param++], "localhost:1"); // Allow all hosts to connect
	} else {
		LOGV("Disabling VNC server");
	}

	strcpy(argv[param++], "-smp");
	strcpy(argv[param++], "1");

	strcpy(argv[param++], "-M");
	if (strncmp(arch_str, "arm", 3) == 0) {
		strcpy(argv[param++], "versatilepb");
	} else {
		strcpy(argv[param++], "pc");
	}

	strcpy(argv[param++], "-k");
	strcpy(argv[param++], "en-us");

	argv[param] = NULL;
	int argc = params - 1;
	for (i = 0; i < argc; i++) {
		LOGV("Arg(%d): %s", i, argv[i]);
	}

	LOGV("***************** INIT QEMU ************************");
	started = 1;
	LOGV("Starting VM...");

	//LOAD LIB
	if (handle == NULL) {
		loadLib(lib_path_str);
	}

	if (!handle) {
		sprintf(res_msg, "Error opening lib: %s :%s", lib_path_str, dlerror());
		LOGV(res_msg);
		return (*env)->NewStringUTF(env, res_msg);
	}

	//Set Max AIO Threads
	typedef void (*setAIOMaxThreads_t)();
	dlerror();
	setAIOMaxThreads_t setAIOMaxThreads = (setAIOMaxThreads_t) dlsym(handle,
			"setAIOMaxThreads");
	const char *dlsym_error1 = dlerror();
	if (dlsym_error1) {
		LOGV("Cannot load symbol 'setAIOMaxThreads': %s\n", dlsym_error1);
//		handle = NULL;
		return (*env)->NewStringUTF(env, res_msg);
	}
	LOGV("Attempt to change aiomaxthreads: %d", aiomaxthreads);
	setAIOMaxThreads(aiomaxthreads);

	//Set DNS
	typedef void (*set_dns_addr_str_t)();
	dlerror();
	set_dns_addr_str_t set_dns_addr_str = (set_dns_addr_str_t) dlsym(handle,
			"set_dns_addr_str");
	const char *dlsym_error2 = dlerror();
	if (dlsym_error2) {
		LOGV("Cannot load symbol 'set_dns_addr_str': %s\n", dlsym_error2);
//        dlclose(handle);
//        handle = NULL;
		return (*env)->NewStringUTF(env, res_msg);
	}

	set_dns_addr_str(dns_addr_str);

	LOGV("Loading symbol qemu_start...\n");
	typedef void (*qemu_start_t)();

	// reset errors
	dlerror();
	qemu_start_t qemu_start = (qemu_start_t) dlsym(handle, "qemu_start");
	const char *dlsym_error = dlerror();
	if (dlsym_error) {
		LOGV("Cannot load symbol 'qemu_start': %s\n", dlsym_error);
		dlclose(handle);
		handle = NULL;
		return (*env)->NewStringUTF(env, res_msg);
	}

	qemu_start(argc, argv, NULL);
	//UNLOAD LIB
	sprintf(res_msg, "Closing lib: %s", lib_path_str);
	LOGV(res_msg);
	dlclose(handle);
	handle = NULL;

	(*env)->ReleaseStringUTFChars(env, jcdrom_iso_path, cdrom_iso_path_str);

	/* Log and return a string of success or error message.
	 * This can be enhanced semantically with codes.
	 */
	sprintf(res_msg, "VM has shutdown");
	LOGV(res_msg);

	exit(1);

//    return (*env)->NewStringUTF(env, res_msg);
}

