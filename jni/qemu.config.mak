include android-toolchain.mak


#x86 and ARM devices support
TARGET_LIST = x86_64-softmmu
#TARGET_LIST = arm-softmmu,x86_64-softmmu

#use coroutine
# gthread, ucontext, sigaltstack, windows
COROUTINE= --with-coroutine=ucontext
 
#Enable Internal profiler
#CONFIG_PROFILER = --enable-gprof

CONFIG_EXTRA = --enable-sdl --audio-drv-list=sdl --enable-mixemu --enable-vnc-jpeg  --enable-vnc-png
#CONFIG_EXTRA =

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
	./configure 	--disable-kvm --disable-curses --target-list=$(TARGET_LIST) \
			--disable-vhost-net --disable-spice --disable-smartcard-nss --disable-uuid \
			--enable-vnc-thread --disable-attr \
			--disable-linux-aio --disable-pie --disable-smartcard --disable-smartcard-nss \
			--disable-nptl --disable-opengl --disable-zlib-test $(COROUTINE) \
			$(CONFIG_EXTRA) \
			$(CONFIG_PROFILER) \
			--disable-guest-agent --android --less-warnings --cpu=$(QEMU_TARGET_CPU) \
			--cross-prefix=$(TOOLCHAIN_PREFIX) \
			--extra-cflags=" -I$(NDK_ROOT)/$(NDK_PLATFORM)/$(USR_INCLUDE) -I../limbo/include -I../glib/glib -I../glib -I../glib/gmodule -I../glib/io -I../limbo/include -I../../glib/glib -I../../glib -I../../glib/gmodule -I../../glib/io -I../SDL/include -I../png -I../jpeg"

