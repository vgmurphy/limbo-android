# ANDROID DEVICE CONFIGURATIONS
LIMBO_ARM_CONF = $(LIMBO_JNI_ROOT)/android-device-config/android-generic-armv5te-softfp.mak
#LIMBO_ARM_CONF = $(LIMBO_JNI_ROOT)/android-device-config/android-asustf101-armv7a-cortex9-vfpv3d16.mak
#LIMBO_ARM_CONF = $(LIMBO_JNI_ROOT)/android-device-config/android-nexus1-armv7-QSD8250-vfpv3.mak
    
#for devices with older cpus (compatible) - WORKING stable
ifeq ($(TARGET_ARCH),arm)
    include $(LIMBO_ARM_CONF)
else
    include $(LIMBO_JNI_ROOT)/android-device-config/android-ndkr8-x86.mak
endif

