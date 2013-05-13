include android-toolchain.mak


#x86 and ARM devices support
#ARM is currently very slow
#Possible Values=arm-softmmu,x86_64-softmmu
TARGET_LIST = x86_64-softmmu

#use coroutine
#ucontext is deprecated and also not avail in Bionic
# gthread, ucontext, sigaltstack, windows
COROUTINE=sigaltstack
 
#Enable Internal profiler
#CONFIG_PROFILER = --enable-gprof

#ENABLE SDL
CONFIG_EXTRA = --enable-sdl

#ENABLE JPEG PNG ENCODING
CONFIG_EXTRA += --enable-vnc-jpeg --enable-vnc-png 
#CONFIG_EXTRA += --disable-vnc-jpeg --disable-vnc-png 

#ENABLE SOUND VIA SDL
CONFIG_EXTRA += --audio-drv-list=sdl --enable-mixemu
#CONFIG_EXTRA += --audio-card-list= --audio-drv-list=

#DISABLE TSC PENTIUM FEATURE
LIMBO_DISABLE_TSC=LIMBO_DISABLE_TSC

ifeq ($(TARGET_ARCH), arm)
    QEMU_TARGET_CPU = armv4b
else
    QEMU_TARGET_CPU = i686
    CONFIG_EXTRA = --disable-vnc-jpeg  --disable-vnc-png --audio-card-list= --audio-drv-list=
endif

config:
	echo TOOLCHAIN DIR: $(TOOLCHAIN_DIR)
	echo NDK ROOT: $(NDK_ROOT) 
	echo NDK PLATFORM: $(NDK_PLATFORM) 
	echo USR INCLUDE: $(NDK_INCLUDE)

	cd ./qemu	; \
	./configure \
	--target-list=$(TARGET_LIST) \
	--cpu=$(QEMU_TARGET_CPU) \
	--disable-kvm --disable-curses \
	--disable-vhost-net --disable-spice \
	--disable-smartcard-nss --disable-uuid \
	--enable-vnc-thread --disable-attr \
	--disable-linux-aio --disable-zlib-test \
	--disable-smartcard --disable-smartcard-nss \
	--disable-nptl --disable-opengl --disable-pie \
	--disable-guest-agent --android --less-warnings \
	--cross-prefix=$(TOOLCHAIN_PREFIX) \
	--extra-cflags=\
	"\
	-I$(NDK_ROOT)/$(NDK_PLATFORM)/$(USR_INCLUDE) \
	-I../limbo/include -I../glib/glib -I../glib \
	-I../glib/gmodule -I../glib/io \
	-I../limbo/include -I../../glib/glib \
	-I../../glib -I../../glib/gmodule -I../../glib/io \
	-I../SDL/include -I../png -I../jpeg \
	-D$(LIMBO_DISABLE_TSC) \
	" \
	--with-coroutine=$(COROUTINE) \
	$(CONFIG_EXTRA) \
	$(CONFIG_PROFILER)

