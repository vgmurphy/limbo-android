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

extern void AndroidGetWindowSize(int *width, int *height) {
	SDL_GetWindowSize(Android_Window, width, height);
//	LOGV("Android window size=%dx%d", *width, *height);

}

static x_curr_pos =0, y_curr_pos = 0;
void Android_OnTouchMouseReset(int touch_device_id_in, int pointer_finger_id_in, int action, float x, float y, float p) {
	x_curr_pos=0;
	y_curr_pos=0;
	Android_OnTouch(touch_device_id_in, pointer_finger_id_in, action, x, y, p);
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
				x_curr_pos = x;
				y_curr_pos = y;
				SDL_SendMouseMotion(Android_Window, 0, x_curr_pos, y_curr_pos);
			}
			else { //Relative position
				x_curr_pos += (int) x;
				y_curr_pos += (int) y;
				SDL_SendMouseMotion(Android_Window, 1, x, y);
			}
//        	LOGV("%s, SDL Move: x=(%d), y=(%d)", __func__, x_pos , y_pos);
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
