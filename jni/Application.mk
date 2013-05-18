LOCAL_ARM_MODE := arm
APP_ABI := armeabi

LIMBO_JNI_ROOT := $(CURDIR)/jni

include jni/android-config.mak

$(warning ARCH_CFLAGS = $(ARCH_CFLAGS))
$(warning LOCAL_ARM_MODE = $(LOCAL_ARM_MODE))
$(warning NDK_DEBUG = $(NDK_DEBUG))