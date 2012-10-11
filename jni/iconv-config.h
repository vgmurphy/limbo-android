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
#ifndef _ICONV_CONFIG_H
#define	_ICONV_CONFIG_H

//#include "./../glib-config.h"

#ifdef	__cplusplus
extern "C" {
#endif

#undef HAVE_LANGINFO_CODESET
#define LIB_DIR_DEF "."
#undef ENABLE_NLS
#undef HAVE_DECL_PROGRAM_INVOCATION_SHORT_NAME
#undef HAVE_DECL_PROGRAM_INVOCATION_NAME
#undef HAVE_DECL_FFLUSH_UNLOCKED

#ifdef __ENABLE_NEON__
#warning "Enabling Neon"
#include "arm_neon.h"
#endif //NEON

#ifdef	__cplusplus
}
#endif

#endif	/* _GLIB_CONFIG_H */

