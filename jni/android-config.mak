# Base definitions for Android toolchain.
# This is the only part of the file you need to change before when compiling.

#PLATFORM
NDK_ROOT = /home/dev/tools/android-ndk-r8e
NDK_PLATFORM = platforms/android-14

# ANDROID DEVICE CONFIGURATION

# ARMv5 Generic DEBUG
#include $(LIMBO_JNI_ROOT)/android-device-config/android-generic-armv5te-softfp-noopt.mak

# ARMv5 Generic
#include $(LIMBO_JNI_ROOT)/android-device-config/android-generic-armv5te-softfp.mak

# ARMv7 Generic
include $(LIMBO_JNI_ROOT)/android-device-config/android-generic-armv7a-vfpv3d16.mak

# x86 Generic
#include $(LIMBO_JNI_ROOT)/android-device-config/android-ndkr8-x86.mak

# Nexus 1
#include $(LIMBO_JNI_ROOT)/android-device-config/android-nexus1-armv7-QSD8250-vfpv3.mak

# Asus tf101
#include $(LIMBO_JNI_ROOT)/android-device-config/android-asustf101-armv7a-cortex9-vfpv3d16.mak

# Nexus 4
#include $(LIMBO_JNI_ROOT)/android-device-config/android-nexus4-armv7a-krait-neon.mak

################ No modifications below this line are necessary #####################
TARGET_ARCH = 

ifeq ($(APP_ABI),armeabi)
    EABI = arm-linux-androideabi-$(NDK_TOOLCHAIN_VERSION)
    TOOLCHAIN_PREFIX = arm-linux-androideabi-
    TARGET_ARCH=arm
else ifeq ($(APP_ABI),armeabi-v7a)
    EABI = arm-linux-androideabi-$(NDK_TOOLCHAIN_VERSION)
    TOOLCHAIN_PREFIX = arm-linux-androideabi-
    TARGET_ARCH=arm
else ifeq ($(APP_ABI),x86)
    EABI = x86-$(NDK_TOOLCHAIN_VERSION)
    TOOLCHAIN_PREFIX = i686-linux-android-
    TARGET_ARCH=x86
endif

TOOLCHAIN_DIR = $(NDK_ROOT)/toolchains/$(EABI)/prebuilt/linux-x86
TOOLCHAIN_PREFIX := $(TOOLCHAIN_DIR)/bin/$(TOOLCHAIN_PREFIX)

# ANDROID NDK TOOLCHAIN, doesn't support hard float so it's slow


CC = $(TOOLCHAIN_PREFIX)gcc
AR = $(TOOLCHAIN_PREFIX)ar
LNK = $(TOOLCHAIN_PREFIX)g++
STRIP = $(TOOLCHAIN_PREFIX)strip
AR_FLAGS = crs
SYS_ROOT = --sysroot=$(NDK_ROOT)/$(NDK_PLATFORM)/arch-$(TARGET_ARCH)
NDK_INCLUDE = $(NDK_ROOT)/$(NDK_PLATFORM)/arch-$(TARGET_ARCH)/usr/include

# INCLUDE_FIXED contains overrides for include files found under the toolchain's /usr/include.
# Hoping to get rid of those one day, when newer NDK versions are released.
INCLUDE_FIXED = $(LIMBO_JNI_ROOT)/include-fixed

# The logutils header is injected into all compiled files in order to redirect
# output to the Android console, and provide debugging macros.
LOGUTILS = $(LIMBO_JNI_ROOT)/logutils.h

USR_LIB = \
-L$(TOOLCHAIN_DIR)//lib

# INCLUDE_FIXED
SYSTEM_INCLUDE = \
    -I$(INCLUDE_FIXED) \
    -I./qemu/linux-headers \
    -I$(TOOLCHAIN_DIR)/$(EABI)/include \
    -I$(NDK_INCLUDE) \
    -include $(LIMBO_JNI_ROOT)/logutils.h



