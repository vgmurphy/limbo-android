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
-march=armv5te -mtune=xscale -msoft-float \
-Wno-psabi \
-fpic -ffunction-sections \
-fomit-frame-pointer -fno-strict-aliasing -finline-limit=64 \
-fstrength-reduce \
-ffast-math -fforce-addr -foptimize-sibling-calls

# Not supported
#-fforce-mem

# Fast optimizations but maybe crashing apps?
#-funsafe-math-optimizations 

# Useful for IEEE non-stop floating
#-fno-trapping-math

# To suppress looking in stadnard includes for the toolchain
#-nostdinc \

# for Debugging only
#-funwind-tables 

# SLows down
# -fstack-protector

# ORIGINAL CFLAGS FROM ANDROID NDK
#-D__ARM_ARCH_5__ -D__ARM_ARCH_5T__ -D__ARM_ARCH_5E__ -D__ARM_ARCH_5TE__  \
#-march=armv5te -mtune=xscale -msoft-float -mthumb \
#-Os
#-fpic -ffunction-sections -funwind-tables -fstack-protector \
#-Wno-psabi 
#-fomit-frame-pointer -fno-strict-aliasing -finline-limit=64 
