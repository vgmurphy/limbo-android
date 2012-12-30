/**
 * Copyright (C) 2009 Michael A. MacDonald
 */
package android.androidVNC;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.antlersoft.android.bc.BCFactory;

/**
 * An AbstractInputHandler that uses GestureDetector to detect standard gestures in touch events
 * 
 * @author Michael A. MacDonald
 */
abstract class AbstractGestureInputHandler extends GestureDetector.SimpleOnGestureListener implements AbstractInputHandler {
	protected GestureDetector gestures;
	private VncCanvasActivity activity;
	
	float xInitialFocus;
	float yInitialFocus;
	boolean inScaling;
	
	private static final String TAG = "AbstractGestureInputHandler";
	
	AbstractGestureInputHandler(VncCanvasActivity c)
	{
		activity = c;
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent evt) {
//		return gestures.onTouchEvent(evt);
		return false;
	}

	
}
