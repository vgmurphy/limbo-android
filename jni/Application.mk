LOCAL_ARM_MODE=arm
APP_ABI=armeabi

LIMBO_JNI_ROOT := $(CURDIR)/jni

include jni/android-config.mak

LOCAL_ARM_MODE=$(ARM_MODE)

$(warning ARCH_CFLAGS = $(ARCH_CFLAGS))
$(warning LOCAL_ARM_MODE = $(LOCAL_ARM_MODE))
$(warning LOCAL_ARM_NEON = $(LOCAL_ARM_NEON))
$(warning NDK_DEBUG = $(NDK_DEBUG))
$(warning APP_OPTIM = $(APP_OPTIM))
$(warning APP_ABI = $(APP_ABI))
