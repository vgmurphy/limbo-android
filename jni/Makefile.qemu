all:
	cd qemu && $(MAKE) all V=1 && $(MAKE) qemu-img.so V=1

clean:
	cd qemu && $(MAKE) clean V=1

veryclean:
	-find . -name *.o -exec rm -rf {} \;
