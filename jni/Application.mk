#NDK_TOOLCHAIN_VERSION := 4.4.3
NDK_TOOLCHAIN_VERSION=4.6
#NDK_TOOLCHAIN_VERSION=4.7
APP_ABI := armeabi

ifeq ($(TARGET_ARCH),arm)
	APP_ABI=$(TARGET_ARCH_ABI)
else
	APP_ABI=x86
endif

include jni/android-toolchain.mak
include jni/android-device-config.mak