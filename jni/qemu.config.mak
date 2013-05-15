include android-config.mak

#x86 and ARM devices support
#ARM is currently very slow
#Possible Values=arm-softmmu,x86_64-softmmu
QEMU_TARGET_LIST = x86_64-softmmu

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
#LIMBO_DISABLE_TSC=-DLIMBO_DISABLE_TSC

ifeq ($(APP_ABI), armeabi)
    QEMU_HOST_CPU = armv4b
else ifeq ($(APP_ABI), armeabi-v7a)
    QEMU_HOST_CPU = armv4b
else
    QEMU_HOST_CPU = i686   
endif

config:
	echo TOOLCHAIN DIR: $(TOOLCHAIN_DIR)
	echo NDK ROOT: $(NDK_ROOT) 
	echo NDK PLATFORM: $(NDK_PLATFORM) 
	echo USR INCLUDE: $(NDK_INCLUDE)

	cd ./qemu	; \
	./configure \
	--target-list=$(QEMU_TARGET_LIST) \
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
	-I../../glib/android -I../glib/android \
	-I../SDL/include -I../png -I../jpeg \
	$(LIMBO_DISABLE_TSC) \
	" \
	--with-coroutine=$(COROUTINE) \
	$(CONFIG_EXTRA) \
	$(CONFIG_PROFILER)

