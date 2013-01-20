.PHONY: glib sdl

include android-toolchain.mak

sdlbuild=cd .. && ndk-build V=1 TARGET_ARCH=arm 

ifneq ($(NDK_DEBUG),1)
	STRIP_LIBS=$(STRIP) --strip-unneeded ../libs/$(TARGET_ARCH_ABI)/*
endif

ifeq ($(TARGET_ARCH),x86)
	sdlbuild=
endif
	
all: config glib sdl limbo qemu
	$(STRIP_LIBS) 

glib:
	$(MAKE) -f Makefile.glib V=1

sdl:
	$(sdlbuild)

qemu: glib sdl
	$(MAKE) -f Makefile.qemu all V=1
	
limbo: qemu glib sdl
	$(MAKE) -f Makefile.limbo all V=1
	
config:
	$(MAKE) -f Makefile.config V=1

debug-arm:
	$(MAKE) ultraclean
	$(MAKE) config TARGET_ARCH=arm
	$(MAKE) all TARGET_ARCH=arm NDK_DEBUG=1
	
debug-x86:
	$(MAKE) ultraclean
	$(MAKE) config TARGET_ARCH=x86
	$(MAKE) all TARGET_ARCH=x86 NDK_DEBUG=1
	
release-arm:
	$(MAKE) ultraclean
	$(MAKE) config TARGET_ARCH=arm
	$(MAKE) all TARGET_ARCH=arm
	$(STRIP_LIBS) 
	

release-x86:
	$(MAKE) ultraclean
	$(MAKE) config TARGET_ARCH=x86
	$(MAKE) all TARGET_ARCH=x86
	$(STRIP_LIBS) 
	
release: release-x86 release-arm

clean:
	-cd .. && ndk-build clean
	$(MAKE) -f Makefile.glib clean V=1
	$(MAKE) -f Makefile.qemu clean V=1
	$(MAKE) -f Makefile.limbo clean V=1

veryclean:
	$(MAKE) -f Makefile.glib veryclean V=1
	$(MAKE) -f Makefile.qemu veryclean V=1
	$(MAKE) -f Makefile.limbo veryclean V=1

distclean: 
	cd qemu && $(MAKE) distclean

ultraclean: distclean veryclean clean
	-find . -name "*.d" -exec rm -rf {} \;
	-find . -name "*.a" -exec rm -rf {} \;
	-find . -name "*.so" -exec rm -rf {} \;

	


