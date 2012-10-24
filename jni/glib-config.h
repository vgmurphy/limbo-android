/*
 Copyright (C) Max Kastanas 2012

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
#ifndef _GLIB_CONFIG_H
#define	_GLIB_CONFIG_H

#ifdef	__cplusplus
extern "C" {
#endif

#undef ENABLE_NLS
#undef HAVE_LANGINFO_CODESET
#define LIB_DIR_DEF "."
#define USE_LIBICONV_GNU
#define GIO_COMPILATION
#undef HAVE_SYS_XATTR_H
#undef HAVE_XATTR
#undef HAVE_STRUCT_STAT_ST_MTIMENSEC
#undef HAVE_STRUCT_STAT_ST_MTIM_TV_NSEC
#undef HAVE_STRUCT_STAT_ST_ATIM_TV_NSEC
#undef HAVE_STRUCT_STAT_ST_CTIM_TV_NSEC
#undef HAVE_HASMNTOPT
#undef HAVE_SYS_STATVFS_H
#undef HAVE_CODESET
#define GIO_MODULE_DIR "."
#undef HAVE_STPCPY
#undef HAVE_BIND_TEXTDOMAIN_CODESET
#undef USE_CLOCK_GETTIME
#undef HAVE_POSIX_GETPWUID_R
#undef HAVE_NONPOSIX_GETPWUID_R
#undef HAVE_POSIX_GETGRGID_R
    //#undef HAVE_MNTENT_H
#undef HAVE_GETMNTENT_R
#undef HAVE_SETMNTENT
#undef HAVE_ENDMNTENT
#undef GLIB_HAVE_ALLOCA_H
#undef HAVE_TIMEGM
#undef HAVE_POSIX_MEMALIGN
#define HAVE_WORKING_O_NOFOLLOW 1


    //#include "./iconv/config.h"

    //#undef HAVE_PWD_H
    //#undef HAVE_PWD_H

#ifdef __ENABLE_NEON__
#warning "Enabling Neon"
#include "arm_neon.h"
#endif //NEON

#ifdef	__cplusplus
}
#endif

#endif	/* _GLIB_CONFIG_H */

