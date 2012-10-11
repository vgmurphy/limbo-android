# ANDROID DEVICE CONFIGURATIONS

#ICS Devices vfp - NOT WORKING RELIABLY ISSUE WITH cpu-exec.c (See printf)
#include android-device-config/android-ndkr8-p14ICS_4.0-armv7a-vfp-o2.mak

#DEBUG - NOT WORKING RELIABLY ISSUE WITH cpu-exec.c (See printf)
#include android-device-config/android-ndkr8-p8_2.2-armv7a-vfp-o0.mak

#FAST - NOT WORKING RELIABLY ISSUE WITH cpu-exec.c (See printf)
#include android-device-config/android-ndkr8-p8_2.2-armv7a-vfp-o2.mak

#VERY FAST - NOT WORKING RELIABLY ISSUE WITH cpu-exec.c (See printf)
#include android-device-config/android-ndkr8-p8_2.2-armv7a-vfp-o3.mak

#for devices with multiple cpu cores (compatible) - NOT WORKING
#include android-device-config/android-ndkr8-p8_2.2-armv5te-softfp-smp-o3.mak

#for ICS devices
#include android-device-config/android-ndkr8-p14ICS-armv5te-softfp.mak

#for devices with thumb instructions - NOT WORKING cpu not supporting swp instruction
#include android-device-config/android-ndkr8-p8_2.2-armv5te-softfp-mthumb-o3.mak

#for devices with older cpus (compatible) - WORKING stable
# include android-device-config/android-ndkr8-p8_2.2-armv5te-softfp-o3.mak

#for devices with older cpus (compatible) - WORKING stable
include android-device-config/android-armv5te-softfp.mak
