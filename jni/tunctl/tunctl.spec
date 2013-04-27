Name:           tunctl
Version:        1.5
Release:        1
Summary:        Create and remove virtual network interfaces

Group:          Applications/System
License:        GPL+
URL:            http://tunctl.sourceforge.net/
Source0:        http://downloads.sourceforge.net/tunctl/tunctl-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

BuildRequires:  docbook-utils

%description
Virtual network interface manipulation tool from User Mode Linux project.

%prep
%setup

%build
make

%install
rm -rf $RPM_BUILD_ROOT
make DESTDIR=$RPM_BUILD_ROOT install

%clean
rm -rf $RPM_BUILD_ROOT


%files
%defattr(-,root,root,-)
%{_mandir}/man8/tunctl.8*
%{_sbindir}/tunctl
%doc ChangeLog

%changelog
* Wed Jul  9 2008 Henrik Nordstrom <henrik@henriknordstrom.net> 1.5-1
- Update to version 1.5

* Tue Jul  8 2008 Henrik Nordstrom <henrik@henriknordstrom.net> 1.4-3
- Packaged tunctl as a upstream package of it's own

* Tue Mar 25 2008 Lubomir Kundrak <lkundrak@redhat.com> 1.4-2
- Move to sbin (Marek Mahut, #434583)

* Fri Feb 22 2008 Lubomir Kundrak <lkundrak@redhat.com> 1.4-1
- Initial packaging
