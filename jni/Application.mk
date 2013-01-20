ifeq ($(TARGET_ARCH),arm)
	APP_ABI=armeabi
else
	APP_ABI=x86
endif

include jni/android-toolchain.mak
include jni/android-device-config.mak