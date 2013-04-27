#NDK_TOOLCHAIN_VERSION := 4.4.3
#NDK_TOOLCHAIN_VERSION=4.6
NDK_TOOLCHAIN_VERSION=4.7
APP_ABI := armeabi
APP_OPTIM := debug

ifeq ($(TARGET_ARCH),arm)
	APP_ABI=armeabi
else
	APP_ABI=x86
endif

include jni/android-toolchain.mak
include jni/android-device-config.mak