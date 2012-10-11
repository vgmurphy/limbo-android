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
#ifndef _STDLIB_H
#define	_STDLIB_H

#ifdef	__cplusplus
extern "C" {
#endif

//USE WITH ANDROID NDK
#include <android/log.h>
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "liblimbo",__VA_ARGS__)
#define printf(...) __android_log_print(ANDROID_LOG_VERBOSE, "liblimbo", __VA_ARGS__)
#define fprintf(stdout, ...) __android_log_print(ANDROID_LOG_VERBOSE, "liblimbo", __VA_ARGS__)

#ifdef	__cplusplus
}
#endif

#endif	/* _STDLIB_H */

