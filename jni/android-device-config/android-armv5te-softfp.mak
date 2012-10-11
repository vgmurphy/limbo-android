## OPTIONAL OPTIMIZATION FLAGS
OPTIM = \
-ffloat-store \
-ffast-math \
-fno-rounding-math \
-fno-signaling-nans \
-fcx-limited-range \
-fno-math-errno \
-funsafe-math-optimizations \
-fassociative-math \
-freciprocal-math \
-fassociative-math \
-freciprocal-math \
-ffinite-math-only \
-fno-signed-zeros \
-fno-trapping-math \
-frounding-math \
-fsingle-precision-constant \
-fcx-limited-range \
-fcx-fortran-rules


### CONFIGURATIONS

# 9. Utilizing VFP
# This abi should try the hard float (FPU) on board but also keep compatibility
#   with Android libraries (soft)
# ANDROID NDK: Still SLOWWWWWWWWWWWWWWW
# LINARO Android toolchain supports VFP
ARCH_CFLAGS = \
-std=gnu99 \
-D__ARM_ARCH_5__ -D__ARM_ARCH_5T__ -D__ARM_ARCH_5E__ -D__ARM_ARCH_5TE__  \
-march=armv5te -mtune=xscale -msoft-float

# Suppress some warnings
ARCH_CFLAGS += -Wno-psabi

# Smaller code generation for shared libraries, usually faster
# if doesn't work use -fPIC
ARCH_CFLAGS += -fpic 

# Reduce executable size
ARCH_CFLAGS += -ffunction-sections

# Don't keep the frame pointer in a register for functions that don't need one
# Anyway enabled for -O2
#ARCH_CFLAGS += -fomit-frame-pointer 

# prevent unwanted optimizations for Qemu
#ARCH_CFLAGS += -fno-strict-aliasing

# Loop optimization might be safe
ARCH_CFLAGS += -fstrength-reduce 
ARCH_CFLAGS += -fforce-addr 

# Faster math might not be safe
ARCH_CFLAGS += -ffast-math

# anyway enabled by -O2
#ARCH_CFLAGS += -foptimize-sibling-calls

# Should not be limiting inline functions or this value should be very large
#ARCH_CFLAGS += -finline-limit=64

# Not supported
#ARCH_CFLAGS += -fforce-mem

# Fast optimizations but maybe crashing apps?
ARCH_CFLAGS += -funsafe-math-optimizations 

# Useful for IEEE non-stop floating
#ARCH_CFLAGS += -fno-trapping-math

# To suppress looking in stadnard includes for the toolchain
#ARCH_CFLAGS += -nostdinc

# for Debugging only
#ARCH_CFLAGS += -funwind-tables 

# SLows down
# ARCH_CFLAGS += -fstack-protector

# ORIGINAL CFLAGS FROM ANDROID NDK
#-D__ARM_ARCH_5__ -D__ARM_ARCH_5T__ -D__ARM_ARCH_5E__ -D__ARM_ARCH_5TE__  \
#-march=armv5te -mtune=xscale -msoft-float -mthumb \
#-Os
#-fpic -ffunction-sections -funwind-tables -fstack-protector \
#-Wno-psabi 
#-fomit-frame-pointer -fno-strict-aliasing -finline-limit=64 
