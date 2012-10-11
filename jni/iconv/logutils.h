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
#ifndef _LOGUTILS_H
#define	_LOGUTILS_H

#ifdef	__cplusplus
extern "C" {
#endif

    //Define DEBUG before we include stdio.h
//    #define DEBUG_ANDROID_AIO 1
//    #define DEBUG_ANDROID_AIO_COMPAT 1

    //USE WITH ANDROID NDK
#ifdef __ANDROID__
#include <android/log.h>
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "liblimbo",__VA_ARGS__)
#define printf(...) __android_log_print(ANDROID_LOG_VERBOSE, "liblimbo", __VA_ARGS__)
#define fprintf(stdout, ...) __android_log_print(ANDROID_LOG_VERBOSE, "liblimbo", __VA_ARGS__)
#define perror(...) __android_log_print(ANDROID_LOG_VERBOSE, "liblimbo", __VA_ARGS__)
    
//DEBUG
//#define LOGD_AIO(...) __android_log_print(ANDROID_LOG_VERBOSE, "liblimbo",__VA_ARGS__)
//#define LOGD_MLP(...) __android_log_print(ANDROID_LOG_VERBOSE, "liblimbo",__VA_ARGS__)
//#define LOGD_TRD(...) __android_log_print(ANDROID_LOG_VERBOSE, "liblimbo",__VA_ARGS__)
//#define LOGD_CPUS(...) __android_log_print(ANDROID_LOG_VERBOSE, "liblimbo",__VA_ARGS__)
//#define LOGD_VL(...) __android_log_print(ANDROID_LOG_VERBOSE, "liblimbo",__VA_ARGS__)
//#define LOGD_TMR(...) __android_log_print(ANDROID_LOG_VERBOSE, "liblimbo",__VA_ARGS__)
    
#define LOGD_AIO(...) 
#define LOGD_MLP(...)
#define LOGD_TRD(...)
#define LOGD_CPUS(...)
#define LOGD_VL(...)
#define LOGD_TMR(...)

#endif



#ifdef	__cplusplus
}
#endif

#endif	/* _LOGUTILS_H */

