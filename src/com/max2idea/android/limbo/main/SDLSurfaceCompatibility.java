package com.max2idea.android.limbo.main;

import org.libsdl.app.SDLActivity;
import org.libsdl.app.SDLSurface;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

public class SDLSurfaceCompatibility extends SDLSurface
implements View.OnGenericMotionListener
{
	public SDLSurfaceCompatibility(Context context) {
		super(context);
		this.setOnGenericMotionListener(this);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onGenericMotion(View v, MotionEvent event) {
		// TODO Auto-generated method stub

		if (SDLActivity.enablebluetoothmouse == 0) {
			return false;
		}
		float x = event.getX();
		float y = event.getY();
		float p = event.getPressure();
		int action = event.getAction();

		if (x < (SDLActivity.width - SDLActivity.vm_width) / 2) {
			return true;
		} else if (x > SDLActivity.width
				- (SDLActivity.width - SDLActivity.vm_width) / 2) {
			return true;
		}

		if (y < (SDLActivity.height - SDLActivity.vm_height) / 2) {
			return true;
		} else if (y > SDLActivity.height
				- (SDLActivity.height - SDLActivity.vm_height) / 2) {
			return true;
		}

		if (action == MotionEvent.ACTION_HOVER_MOVE) {
//			Log.v("onGenericMotion", "Moving to (X,Y)=(" + x
//					* SDLActivity.width_mult + "," + y
//					* SDLActivity.height_mult + ")");

			SDLActivity.onNativeTouch(0, 1, MotionEvent.ACTION_MOVE, x
					* SDLActivity.width_mult, y * SDLActivity.height_mult, p);
		}

		if (event.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
//			Log.v("onGenericMotion", "Right Click (X,Y)=" + x
//					* SDLActivity.width_mult + "," + y
//					* SDLActivity.height_mult + ")");
			rightClick(event);
		}

		// save current
		old_x = x * SDLActivity.width_mult;
		old_y = y * SDLActivity.height_mult;
		return true;
	}
	

}
