PACKAGE = tunctl
VERSION = 1.5
BIN = $(PACKAGE)
MANS = 8
MAN = $(PACKAGE).$(MANS)

DIST = Makefile $(PACKAGE).spec $(PACKAGE).c $(PACKAGE).sgml ChangeLog

CFLAGS = -g -Wall

BIN_DIR ?= /usr/sbin
MAN_DIR ?= /usr/share/man/man$(MANS)

all : $(BIN) $(MAN)

$(BIN) : $(BIN).c
	$(CC) $(CFLAGS) -o $(BIN) $(BIN).c

$(MAN) : $(PACKAGE).sgml
	docbook2man $(PACKAGE).sgml

clean : 
	rm -f $(BIN) $(OBJS) $(MAN) *~ manpage.*

install : $(BIN) $(MAN)
	install -d $(DESTDIR)$(BIN_DIR)
	install $(BIN) $(DESTDIR)$(BIN_DIR)
	install -d $(DESTDIR)$(MAN_DIR)
	install $(MAN) $(DESTDIR)$(MAN_DIR)

.PHONY: dist
dist: distcheck
	rm -rf dist/$(PACKAGE)-$(VERSION)
	mkdir -p dist/$(PACKAGE)-$(VERSION)
	cp -p $(DIST) dist/$(PACKAGE)-$(VERSION)
	tar -C dist -zcf $(PACKAGE)-$(VERSION).tar.gz $(PACKAGE)-$(VERSION)

distcheck:
	@if test "`awk '/^Version $(VERSION)($$|:)/ {print}' ChangeLog`" = ""; then \
	    echo "ERROR: Spec file ChangeLog not updated"; \
	    false; \
	fi
	@if test `awk '/^Version:/ {print $$2}' $(PACKAGE).spec` != $(VERSION); then \
	    echo "ERROR: Spec file version not updated"; \
	    false; \
	fi
	@if test "`awk '/^\*.* $(VERSION)-/ {print}' $(PACKAGE).spec`" = ""; then \
	    echo "ERROR: Spec file ChangeLog not updated"; \
	    false; \
	fi
