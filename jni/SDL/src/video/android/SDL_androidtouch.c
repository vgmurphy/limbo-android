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

#include <android/log.h>

#include "SDL_events.h"
#include "../../events/SDL_mouse_c.h"
#include "../../events/SDL_touch_c.h"

#include "SDL_androidtouch.h"
#include "../logutils.h"


#define ACTION_DOWN 0
#define ACTION_UP 1
#define ACTION_MOVE 2
#define ACTION_CANCEL 3
#define ACTION_OUTSIDE 4
// The following two are deprecated but it seems they are still emitted (instead the corresponding ACTION_UP/DOWN) as of Android 3.2
#define ACTION_POINTER_1_DOWN 5
#define ACTION_POINTER_1_UP 6

static SDL_FingerID leftFingerDown = 0;

static void Android_GetWindowCoordinates(float x, float y,
                                         int *window_x, int *window_y)
{
    int window_w, window_h;

    SDL_GetWindowSize(Android_Window, &window_w, &window_h);
    *window_x = (int)(x * window_w);
    *window_y = (int)(y * window_h);
}

//void Android_OnTouch(int touch_device_id_in, int pointer_finger_id_in, int action, float x, float y, float p)
//{
//	LOGV("%s dev=%d, pntr=%d, Action=%d, X,Y=%f,%f P=%f", __func__, touch_device_id_in, pointer_finger_id_in, action, x, y, p);
//    SDL_TouchID touchDeviceId = 0;
//    SDL_FingerID fingerId = 0;
//    int window_x, window_y;
//
//    if (!Android_Window) {
//        return;
//    }
//
//    touchDeviceId = (SDL_TouchID)touch_device_id_in;
//    if (!SDL_GetTouch(touchDeviceId)) {
//        SDL_Touch touch;
//        memset( &touch, 0, sizeof(touch) );
//        touch.id = touchDeviceId;
//        touch.x_min = 0.0f;
//        touch.x_max = 1.0f;
//        touch.native_xres = touch.x_max - touch.x_min;
//        touch.y_min = 0.0f;
//        touch.y_max = 1.0f;
//        touch.native_yres = touch.y_max - touch.y_min;
//        touch.pressure_min = 0.0f;
//        touch.pressure_max = 1.0f;
//        touch.native_pressureres = touch.pressure_max - touch.pressure_min;
//        if (SDL_AddTouch(&touch, "") < 0) {
//             SDL_Log("error: can't add touch %s, %d", __FILE__, __LINE__);
//        }
//    }
//
//    fingerId = (SDL_FingerID)pointer_finger_id_in;
//    switch (action) {
//        case ACTION_DOWN:
//        case ACTION_POINTER_1_DOWN:
//            if (!leftFingerDown) {
//                Android_GetWindowCoordinates(x, y, &window_x, &window_y);
//
//                /* send moved event */
//                SDL_SendMouseMotion(NULL, 0, window_x, window_y);
//
//                /* send mouse down event */
//                SDL_SendMouseButton(NULL, SDL_PRESSED, SDL_BUTTON_LEFT);
//
//                leftFingerDown = fingerId;
//            }
//            SDL_SendFingerDown(touchDeviceId, fingerId, SDL_TRUE, x, y, p);
//            break;
//        case ACTION_MOVE:
//            if (!leftFingerDown) {
//                Android_GetWindowCoordinates(x, y, &window_x, &window_y);
//
//                /* send moved event */
//                SDL_SendMouseMotion(NULL, 0, window_x, window_y);
//            }
//            SDL_SendTouchMotion(touchDeviceId, fingerId, SDL_FALSE, x, y, p);
//            break;
//        case ACTION_UP:
//        case ACTION_POINTER_1_UP:
//            if (fingerId == leftFingerDown) {
//                /* send mouse up */
//                SDL_SendMouseButton(NULL, SDL_RELEASED, SDL_BUTTON_LEFT);
//                leftFingerDown = 0;
//            }
//            SDL_SendFingerDown(touchDeviceId, fingerId, SDL_FALSE, x, y, p);
//            break;
//        default:
//            break;
//    }
//}

extern void AndroidGetWindowSize(int *width, int *height){
	SDL_GetWindowSize(Android_Window, width, height);
//	LOGV("Android window size=%dx%d", *width, *height);

}

static x_pos =0, y_pos = 0;
void Android_OnTouchMouseReset(int touch_device_id_in, int pointer_finger_id_in, int action, float x, float y, float p){
	x_pos=0;
	y_pos=0;
	Android_OnTouch(touch_device_id_in, pointer_finger_id_in,  action,  x,  y,  p);
}

void Android_OnTouch(int touch_device_id_in, int pointer_finger_id_in, int action, float x, float y, float p)
{
    if (!Android_Window) {
        return;
    }
    int window_w, window_h;
    SDL_GetWindowSize(Android_Window, &window_w, &window_h);

    if ((action != ACTION_CANCEL)
    		&& (action != ACTION_OUTSIDE)
    		) {
        SDL_SetMouseFocus(Android_Window);

        //Use with External mouse only, otherwise trackpad
        //SDL_SendMouseMotion(Android_Window, 0, (int)x, (int)y);

        switch(action) {
        case ACTION_MOVE:


//        	LOGV("%s, SDL Move: width=%d, height=%d", __func__, window_w, window_h);
//        	LOGV("%s, SDL Move: x=(%d)+(%d), y=(%d)+(%d)", __func__, x_pos , (int)x, y_pos, (int) y);

        	if(pointer_finger_id_in==1) { //Absolute position
				x_pos = x;
				y_pos = y;

			}
			else { //Relative position
				x_pos += (int) x;
				if(x_pos > window_w) {
					x_pos = window_w;
				} else if (x_pos < 0)
				x_pos = 0;
				y_pos += (int) y;
				if(y_pos > window_h) {
					y_pos = window_h;
				} else if (y_pos < 0) {
					y_pos =0;
				}
			}
//        	LOGV("%s, SDL Move: x=(%d), y=(%d)", __func__, x_pos , y_pos);
        	SDL_SendMouseMotion(Android_Window, 0, x_pos, y_pos);
            break;
        case ACTION_UP:
        	if(touch_device_id_in==SDL_BUTTON_LEFT) {
//				LOGV("%s, SDL Left Button Released: %d,%d", __func__, x_pos, y_pos);
				SDL_SendMouseButton(Android_Window, SDL_RELEASED, SDL_BUTTON_LEFT);
			} else if(touch_device_id_in==SDL_BUTTON_RIGHT) {
//				LOGV("%s, SDL Right Button Released: %d,%d", __func__, x_pos, y_pos);
				SDL_SendMouseButton(Android_Window, SDL_RELEASED, SDL_BUTTON_RIGHT);
			}
            break;

        case ACTION_DOWN:
        	if(touch_device_id_in==SDL_BUTTON_LEFT) {
//				LOGV("%s, SDL Left Button Pressed: %d,%d", __func__, x_pos, y_pos);
				SDL_SendMouseButton(Android_Window, SDL_PRESSED, SDL_BUTTON_LEFT);
			} else if(touch_device_id_in==SDL_BUTTON_RIGHT) {
//				LOGV("%s, SDL Right Button Pressed: %d,%d", __func__, x_pos, y_pos);
				SDL_SendMouseButton(Android_Window, SDL_PRESSED, SDL_BUTTON_RIGHT);
			}
			break;
        }
    } else {
        SDL_SetMouseFocus(NULL);
    }
}

#endif /* SDL_VIDEO_DRIVER_ANDROID */

/* vi: set ts=4 sw=4 expandtab: */
