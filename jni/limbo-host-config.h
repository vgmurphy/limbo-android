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

#ifndef _LIMBO_HOST_CONFIG_H
#define	_LIMBO_HOST_CONFIG_H

#ifdef	__cplusplus
extern "C" {
#endif

    //#define UTIME_NOW	((1l << 30) - 1l)
    //#define UTIME_OMIT	((1l << 30) - 2l)
#define G_ATOMIC_ARM
#undef G_ATOMIC_I486
//#undef CONFIG_PIPE2
//#undef HAVE_TIMEGM
//#define IOV_MAX		1024
//#undef CONFIG_UUID
//#undef CONFIG_ACCEPT4
//#undef __linux__
//#define F_ULOCK 0	/* Unlock a previously locked region.  */
//#define F_LOCK  1	/* Lock a region for exclusive use.  */
//#define F_TLOCK 2	/* Test and lock a region for exclusive use.  */
//#define F_TEST  3	/* Test a region for other processes locks.  */

//#undef  PR_SET_NAME


//#include "./qemu/i386-softmmu/config-target.h"
//#include "./iconv/config.h"
// TODO: Needs qemu-system-arm.so
//#include "./qemu/arm-softmmu/config-target.h"

#ifdef __ENABLE_NEON__
#warning "Enabling Neon"
#include "arm_neon.h"
#endif //NEON

#ifdef	__cplusplus
}
#endif


#endif	/* _LIMBO_HOST_CONFIG_H */

