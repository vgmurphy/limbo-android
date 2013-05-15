LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	./libcharset/lib/localcharset.c \
	./libcharset/lib/relocatable.c \
	./lib/iconv.c
    

LOCAL_MODULE:= iconv

LOCAL_CFLAGS := \
    -I$(LOCAL_PATH)/ \
    -I$(LOCAL_PATH)/lib \
    -I$(LOCAL_PATH)/include \
    -I$(LOCAL_PATH)/srclib		

ifeq ($(TARGET_ARCH), arm)
    LOCAL_CFLAGS += -DG_ATOMIC_ARM
else
    LOCAL_CFLAGS += -DG_ATOMIC_I486
endif

# COMMON VARS
LOCAL_CFLAGS += \
	  -DHAVE_CONFIG_H \
        -DSUPPORT_UCP \
        -DSUPPORT_UTF8 \
        -DNEWLINE=-1 \
        -DMATCH_LIMIT=10000000 \
        -DMATCH_LIMIT_RECURSION=8192 \
        -DMAX_NAME_SIZE=32 \
        -DMAX_NAME_COUNT=10000 \
        -DMAX_DUPLENGTH=30000 \
        -DLINK_SIZE=2 \
        -DPOSIX_MALLOC_THRESHOLD=10 \
        -DPCRE_STATIC \
        -DG_DISABLE_CAST_CHECKS \
        -DG_DISABLE_DEPRECATED \
        -DGLIB_COMPILATION \
        -DGOBJECT_COMPILATION \
        -DLIBDIR="\".\"" \
        -DLINK_SIZE=2

LOCAL_CFLAGS += $(ARCH_CFLAGS)
LOCAL_ARM_MODE := $(ARM_MODE)

include $(BUILD_SHARED_LIBRARY)

