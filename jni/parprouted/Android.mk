LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS := -L$(LOCAL_PATH)/lib -llog -g

LOCAL_C_INCLUDES := bionic
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include

LOCAL_SRC_FILES:= arp.c parprouted.c

LOCAL_MODULE := parprouted
LOCAL_ARM_MODE := $(ARM_MODE)

include $(BUILD_EXECUTABLE)