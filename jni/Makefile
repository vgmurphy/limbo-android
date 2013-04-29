.PHONY: createdirs glib sdl qemu glib limbo

include android-toolchain.mak

sdlbuild=cd .. && ndk-build V=1 TARGET_ARCH=arm

CREATE_DIRS=mkdir -p ../obj/local/$(TARGET_ARCH_ABI)/
INSTALL_LIBS=cp -f ../obj/local/$(TARGET_ARCH_ABI)/*.so ../libs/$(TARGET_ARCH_ABI)/
RENAME_TUNCTL=mv -f ../libs/$(TARGET_ARCH_ABI)/tunctl ../libs/$(TARGET_ARCH_ABI)/libtunctl.so
RENAME_PARPROUTED=mv -f ../libs/$(TARGET_ARCH_ABI)/parprouted ../libs/$(TARGET_ARCH_ABI)/libparprouted.so

ifneq ($(NDK_DEBUG),1)
	STRIP_LIBS=$(STRIP) --strip-unneeded ../libs/$(TARGET_ARCH_ABI)/*
endif

ifeq ($(TARGET_ARCH),x86)
	sdlbuild=
endif
	
all: createdirs sdl config qemu glib limbo
	$(INSTALL_LIBS)
	$(RENAME_TUNCTL)
	$(RENAME_PARPROUTED)
	$(STRIP_LIBS) 
	
createdirs:
	$(CREATE_DIRS)

glib:
	$(MAKE) -f glib.mak V=1

sdl:
	$(sdlbuild)

qemu: glib sdl
	$(MAKE) -f qemu.mak all V=1
	
limbo: qemu glib sdl
	$(MAKE) -f limbo.mak all V=1
	
config:
	$(MAKE) -f qemu.config.mak V=1

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
	$(MAKE) -f glib.mak clean V=1
	$(MAKE) -f qemu.mak clean V=1
	$(MAKE) -f limbo.mak clean V=1

veryclean:
	$(MAKE) -f glib.mak veryclean V=1
	$(MAKE) -f qemu.mak veryclean V=1
	$(MAKE) -f limbo.mak veryclean V=1
	-rm -rf ../obj/local/

distclean: 
	cd qemu && $(MAKE) distclean

ultraclean: distclean veryclean clean
	-find . -name "*.d" -exec rm -rf {} \;
	-find . -name "*.a" -exec rm -rf {} \;
	-find . -name "*.so" -exec rm -rf {} \;

	


