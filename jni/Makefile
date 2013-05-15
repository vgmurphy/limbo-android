.PHONY: createdirs sdl qemu limbo

LIMBO_JNI_ROOT := $(CURDIR)

include android-config.mak
	
sdlbuild=cd .. && ndk-build V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)

CREATE_OBJDIR=mkdir -p ../obj/local/$(APP_ABI)/
CREATE_LIBSDIR=mkdir -p ../libs/$(APP_ABI)/
DEL_LIBSDIR=rm -rf ../libs/armeabi ../libs/armeabi-v7a ../libs/x86 ../libs/mips   
INSTALL_LIBS=cp -f ../obj/local/$(APP_ABI)/*.so ../libs/$(APP_ABI)/
RENAME_TUNCTL=mv -f ../libs/$(APP_ABI)/tunctl ../libs/$(APP_ABI)/libtunctl.so
RENAME_PARPROUTED=mv -f ../libs/$(APP_ABI)/parprouted ../libs/$(APP_ABI)/libparprouted.so

ifneq ($(NDK_DEBUG),1)
	STRIP_LIBS=$(STRIP) --strip-unneeded ../libs/$(APP_ABI)/*
endif
	
all: createdirs sdl config qemu limbo
	$(INSTALL_LIBS)
	$(RENAME_TUNCTL)
	$(RENAME_PARPROUTED)
	$(STRIP_LIBS) 
	
createdirs:
	$(CREATE_OBJDIR)
	$(DEL_LIBSDIR)
	$(CREATE_LIBSDIR)

sdl:
	$(sdlbuild)

qemu: sdl config
	$(MAKE) -f qemu.mak all V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	
limbo: sdl config qemu
	$(MAKE) -f limbo.mak all V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	
config:
	$(MAKE) -f qemu.config.mak V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)

debug:
	$(MAKE) ultraclean LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	$(MAKE) config LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	$(MAKE) all NDK_DEBUG=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	
release:
	$(MAKE) ultraclean LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	$(MAKE) config LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	$(MAKE) all LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	$(STRIP_LIBS)

clean:
	-cd .. && ndk-build clean LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	$(MAKE) -f qemu.mak clean V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	$(MAKE) -f limbo.mak clean V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)

veryclean:
	$(MAKE) -f qemu.mak veryclean V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	$(MAKE) -f limbo.mak veryclean V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	-rm -rf ../obj/local/

distclean: 
	cd qemu && $(MAKE) distclean LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)

ultraclean: distclean veryclean clean
	-find . -name "*.d" -exec rm -rf {} \;
	-find . -name "*.a" -exec rm -rf {} \;
	-find . -name "*.so" -exec rm -rf {} \;

	


