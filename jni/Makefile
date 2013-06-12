.PHONY: createdirs sdl qemu limbo

NDK_PROJECT_PATH := $(CURDIR)/../
LIMBO_JNI_ROOT := $(CURDIR)

include $(LIMBO_JNI_ROOT)/android-config.mak
	
sdlbuild=cd $(NDK_PROJECT_PATH) && ndk-build V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)

CREATE_OBJDIR=mkdir -p $(NDK_PROJECT_PATH)/obj/local/$(APP_ABI)/
CREATE_LIBSDIR=mkdir -p $(NDK_PROJECT_PATH)/libs/$(APP_ABI)/
DEL_LIBSDIR=rm -rf $(NDK_PROJECT_PATH)/libs/armeabi \
	../libs/armeabi-v7a \
	../libs/x86 ../libs/mips \   
INSTALL_LIBS=cp -f $(NDK_PROJECT_PATH)/obj/local/$(APP_ABI)/*.so \
	../libs/$(APP_ABI)/
RENAME_TUNCTL=mv -f $(NDK_PROJECT_PATH)/libs/$(APP_ABI)/tunctl \
	$(NDK_PROJECT_PATH)/libs/$(APP_ABI)/libtunctl.so
RENAME_PARPROUTED=mv -f $(NDK_PROJECT_PATH)/libs/$(APP_ABI)/parprouted \
	$(NDK_PROJECT_PATH)/libs/$(APP_ABI)/libparprouted.so

ifneq ($(NDK_DEBUG),1)
	STRIP_LIBS=$(STRIP) --strip-unneeded $(NDK_PROJECT_PATH)/libs/$(APP_ABI)/*.so
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
	$(MAKE) -f $(LIMBO_JNI_ROOT)/qemu.mak all V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	
limbo: sdl config qemu
	$(MAKE) -f $(LIMBO_JNI_ROOT)/limbo.mak all V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	
config:
	$(MAKE) -f $(LIMBO_JNI_ROOT)/qemu.config.mak V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)

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
	-cd $(LIMBO_JNI_ROOT) && ndk-build clean LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	$(MAKE) -f $(LIMBO_JNI_ROOT)/qemu.mak clean V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	$(MAKE) -f $(LIMBO_JNI_ROOT)/limbo.mak clean V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)

veryclean:
	$(MAKE) -f $(LIMBO_JNI_ROOT)/qemu.mak veryclean V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	$(MAKE) -f $(LIMBO_JNI_ROOT)/limbo.mak veryclean V=1 LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)
	-rm -rf $(NDK_PROJECT_PATH)/obj/local/

distclean: 
	cd $(LIMBO_JNI_ROOT)/qemu && $(MAKE) distclean LIMBO_JNI_ROOT=$(LIMBO_JNI_ROOT)

ultraclean: distclean veryclean clean
	-find . -name "*.d" -exec rm -rf {} \;
	-find . -name "*.a" -exec rm -rf {} \;
	-find . -name "*.so" -exec rm -rf {} \;

	


