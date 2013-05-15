#NDK VERSION
NDK_TOOLCHAIN_VERSION := 4.4.3
#NDK_TOOLCHAIN_VERSION=4.6
#NDK_TOOLCHAIN_VERSION=4.7

#TARGET ARCH
APP_ABI := x86

ARCH_CFLAGS := \
    -std=gnu99 \
    -ffunction-sections

ANDROID_OPTIM_FLAGS :=  -O3 \
                              -fomit-frame-pointer \
                              -fstrict-aliasing    \
                              -funswitch-loops     \
                              -finline-limit=300

ifeq ($(NDK_DEBUG),1)
	ARCH_CFLAGS += -O0
	ARCH_CFLAGS += -g 
else
	ARCH_CFLAGS += $(ANDROID_OPTIM_FLAGS)
endif 

#LITTLE ENDIAN ONLY
ARCH_CFLAGS += -DANDROID_X86
