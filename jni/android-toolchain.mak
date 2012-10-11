# ANDROID NDK TOOLCHAIN, doesn't support hard float so it's slow

NDK_BASE=/home/dev/tools/android-ndk-r8b
NDK_PLATFORM=platforms/android-14
TOOLCHAIN_DIR=/home/dev/tools/android-ndk-r8b/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86
CC = $(TOOLCHAIN_DIR)/bin/arm-linux-androideabi-gcc
AR = $(TOOLCHAIN_DIR)/bin/arm-linux-androideabi-ar
LNK = $(TOOLCHAIN_DIR)/bin/arm-linux-androideabi-g++
STRIP = $(TOOLCHAIN_DIR)/bin/arm-linux-androideabi-strip
AR_FLAGS = crs
SYS_ROOT = --sysroot=$(NDK_BASE)/$(NDK_PLATFORM)/arch-arm
NDK_INCLUDE=arch-arm/usr/include
#GCC_STATIC = \
	$(TOOLCHAIN_DIR)/lib/gcc/arm-linux-androideabi/4.6.3/armv7-a/thumb/libgcc.a 

#	$(TOOLCHAIN_DIR)/lib/gcc/arm-eabi/4.6.3/fpu/libgcc.a

#$(TOOLCHAIN_DIR)/lib/gcc/arm-eabi/4.6.3/fpu/libgcc.a
#$(TOOLCHAIN_DIR)/lib/gcc/arm-linux-androideabi/4.6.3/libgcc.a
#$(TOOLCHAIN_DIR)/lib/gcc/arm-linux-androideabi/4.6.3/armv7-a/libgcc.a
#$(TOOLCHAIN_DIR)/lib/gcc/arm-linux-androideabi/4.6.3/armv7-a/thumb/libgcc.a
#$(NDK_BASE)/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/lib/gcc/arm-linux-androideabi/4.4.3/armv7-a/thumb/libgcc.a

USR_LIB = \
-L$(TOOLCHAIN_DIR)/arm-linux-androideabi/lib

#-L$(NDK_BASE)/$(NDK_PLATFORM)/arch-arm/usr/lib

SYSTEM_INCLUDE = \
    -I./qemu/linux-headers \
    -I$(TOOLCHAIN_DIR)/arm-linux-androideabi/include \
    -I$(NDK_BASE)/$(NDK_PLATFORM)/arch-arm/usr/include 
    
    #-I$(NDK_BASE)/$(NDK_PLATFORM)/arch-arm/usr/include/linux
     #-I$(TOOLCHAIN_DIR)/arm-linux-androideabi/include 

ANDROID_DEBUG_FLAGS = -g

ANDROID_CFLAGS = $(ANDROID_DEBUG_FLAGS)

ANDROID_CFLAGS += -O3

#ANDROID_CFLAGS +=

