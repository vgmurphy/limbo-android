/*
  Simple DirectMedia Layer
  Copyright (C) 1997-2012 Sam Lantinga <slouken@libsdl.org>

  This software is provided 'as-is', without any express or implied
  warranty.  In no event will the authors be held liable for any damages
  arising from the use of this software.

  Permission is granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely, subject to the following restrictions:

  1. The origin of this software must not be misrepresented; you must not
     claim that you wrote the original software. If you use this software
     in a product, an acknowledgment in the product documentation would be
     appreciated but is not required.
  2. Altered source versions must be plainly marked as such, and must not be
     misrepresented as being the original software.
  3. This notice may not be removed or altered from any source distribution.
*/
#include "SDL_config.h"

#if SDL_VIDEO_DRIVER_ANDROID

/* We're going to do this by default */
#define SDL_ANDROID_BLOCK_ON_PAUSE  1

#include "SDL_androidevents.h"
#include "SDL_events.h"

static int once = 1;

void
Android_PumpEvents(_THIS)
{
    static int isPaused = 0;
#if SDL_ANDROID_BLOCK_ON_PAUSE
    static int isPausing = 0;
#endif
    /* No polling necessary */

    /*
     * Android_ResumeSem and Android_PauseSem are signaled from Java_org_libsdl_app_SDLActivity_nativePause and Java_org_libsdl_app_SDLActivity_nativeResume
     * When the pause semaphore is signaled, if SDL_ANDROID_BLOCK_ON_PAUSE is defined the event loop will block until the resume signal is emitted.
     * When the resume semaphore is signaled, SDL_GL_CreateContext is called which in turn calls Java code
     * SDLActivity::createGLContext -> SDLActivity:: initEGL -> SDLActivity::createEGLSurface -> SDLActivity::createEGLContext
     */

#if SDL_ANDROID_BLOCK_ON_PAUSE
    if (isPaused && !isPausing) {
        if(SDL_SemWait(Android_ResumeSem) == 0) {
#else
    if (isPaused) {
        if(SDL_SemTryWait(Android_ResumeSem) == 0) {
#endif
            isPaused = 0;
            /* TODO: Should we double check if we are on the same thread as the one that made the original GL context?
             * This call will go through the following chain of calls in Java:
             * SDLActivity::createGLContext -> SDLActivity:: initEGL -> SDLActivity::createEGLSurface -> SDLActivity::createEGLContext
             * SDLActivity::createEGLContext will attempt to restore the GL context first, and if that fails it will create a new one
             * If a new GL context is created, the user needs to restore the textures manually (TODO: notify the user that this happened with a message)
             */
            LOGV("%s Resume SDL_GL_CreateContext", __func__);
//            SDL_GL_CreateContext(Android_Window);
            Android_JNI_Resume();

        }
    }
    else {
#if SDL_ANDROID_BLOCK_ON_PAUSE
        if( (isPausing || SDL_SemTryWait(Android_PauseSem) == 0 )
//        		&& once
        		) {
            /* We've been signaled to pause, but before we block ourselves, we need to make sure that
            SDL_WINDOWEVENT_FOCUS_LOST and SDL_WINDOWEVENT_MINIMIZED have reached the app */
            if (SDL_HasEvent(SDL_WINDOWEVENT)) {
            	LOGV("%s Pausing", __func__);
            	isPausing = 1;
            }
            else {
                isPausing = 0;
                isPaused = 1;
                LOGV("%s Paused", __func__);
//                once = 0;
            }

        }
#else
        if(SDL_SemTryWait(Android_PauseSem) == 0) {
            /* If we fall in here, the system is/was paused */
            isPaused = 1;
        }
#endif
    }
}
//static int AND_Paused = 0;
//
//void Android_PumpEvents(_THIS)
//{
//    while(AND_Paused)   // implement here pausing the program on lost focus
//    {
//        sched_yield();
//    }
//}


#endif /* SDL_VIDEO_DRIVER_ANDROID */

/* vi: set ts=4 sw=4 expandtab: */
