all:  
	$(MAKE) -f Makefile.glib all V=1
	$(MAKE) -f Makefile.qemu all V=1
	$(MAKE) -f Makefile.limbo all V=1

config:
	$(MAKE) -f Makefile.config V=1

release-arm:
	$(MAKE) ultraclean
	$(MAKE) config TARGET_ARCH=arm
	$(MAKE) all TARGET_ARCH=arm

release-x86:	
	$(MAKE) ultraclean
	$(MAKE) config TARGET_ARCH=x86
	$(MAKE) all TARGET_ARCH=x86
	
release: release-x86 release-arm

clean:
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

