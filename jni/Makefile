all:  
	$(MAKE) -f Makefile.zlib all V=1
	$(MAKE) -f Makefile.glib all V=1
	$(MAKE) -f Makefile.qemu all V=1
	$(MAKE) -f Makefile.limbo all V=1

config:
	$(MAKE) -f Makefile.config V=1

clean:
	$(MAKE) -f Makefile.zlib clean V=1
	$(MAKE) -f Makefile.glib clean V=1
	$(MAKE) -f Makefile.qemu clean V=1
	$(MAKE) -f Makefile.limbo clean V=1

veryclean:
	$(MAKE) -f Makefile.zlib veryclean V=1
	$(MAKE) -f Makefile.glib veryclean V=1
	$(MAKE) -f Makefile.qemu veryclean V=1
	$(MAKE) -f Makefile.limbo veryclean V=1

distclean: 
	cd qemu && $(MAKE) distclean

ultraclean: distclean veryclean clean
	-find . -name "*.d" -exec rm -rf {} \;
	-find . -name "*.a" -exec rm -rf {} \;
	-find . -name "*.so" -exec rm -rf {} \;

