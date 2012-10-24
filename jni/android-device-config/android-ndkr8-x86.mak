ARCH_CFLAGS := \
    -std=gnu99 \
    -ffunction-sections

OPTIM :=  -O3 \
                              -fomit-frame-pointer \
                              -fstrict-aliasing    \
                              -funswitch-loops     \
                              -finline-limit=300

