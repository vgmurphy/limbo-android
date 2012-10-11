LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := limbo   
LOCAL_CFLAGS    :=
LOCAL_SRC_FILES := 
LOCAL_LDLIBS    := 

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := qemu-system-x86_64   
LOCAL_CFLAGS    :=
LOCAL_SRC_FILES := 
LOCAL_LDLIBS    := 

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := qemu-img   
LOCAL_CFLAGS    :=
LOCAL_SRC_FILES := 
LOCAL_LDLIBS    := 

include $(BUILD_SHARED_LIBRARY)