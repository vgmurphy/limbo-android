package org.libsdl.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.androidVNC.VncCanvasActivity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import com.max2idea.android.limbo.main.Const;

/**
 * SDLSurface. This is what we draw on, so we need to know when it's created in
 * order to do anything useful.
 * 
 * Because of this, that's where we set up the SDL thread
 */
public class SDLSurface extends GLSurfaceView implements
		SurfaceHolder.Callback, View.OnKeyListener, View.OnTouchListener,
		SensorEventListener {

	//
	// Sensors
	private static SensorManager mSensorManager;

	private class GestureListener extends
			GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent event) {
			// Log.v("onDown",
			// "Action=" + event.getAction() + ", X,Y=" + event.getX()
			// + "," + event.getY() + " P=" + event.getPressure());
			return true;
		}

		@Override
		public void onLongPress(MotionEvent event) {
			// Log.v("onLongPress",
			// "Action=" + event.getAction() + ", X,Y=" + event.getX()
			// + "," + event.getY() + " P=" + event.getPressure());
			SDLActivity.onNativeTouch(Const.SDL_MOUSE_LEFT, 0,
					MotionEvent.ACTION_DOWN, 0, 0, 0);
		}

		public boolean onSingleTapConfirmed(MotionEvent event) {
			// float x = e.getX();
			// float y = e.getY();

			// Log.d("onSingleTapConfirmed", "Tapped at: (" + x + "," + y +
			// ")");
			for (int i = 0; i < event.getPointerCount(); i++) {
				int action = event.getAction();
				float x = event.getX(i);
				float y = event.getY(i);
				float p = event.getPressure(i);

				// Log.v("onSingleTapConfirmed", "Action=" + action + ", X,Y=" +
				// x
				// + "," + y + " P=" + p);

				// if (x < (SDLActivity.width - SDLActivity.vm_width) / 2) {
				// return true;
				// } else if (x > SDLActivity.width - (SDLActivity.width -
				// SDLActivity.vm_width) / 2) {
				// return true;
				// }
				//
				// if (y < (SDLActivity.height - SDLActivity.vm_height) / 2) {
				// return true;
				// } else if (y > SDLActivity.height - (SDLActivity.height -
				// SDLActivity.vm_height) / 2) {
				// return true;
				// }
				// TODO: Anything else we need to pass?
				// SDLActivity.onNativeTouch(action, x, y, p);
				// if(action == MotionEvent.ACTION_MOVE)
				SDLActivity.singleClick(event, i);

			}
			return true;

		}

		// event when double tap occurs
		@Override
		public boolean onDoubleTap(MotionEvent event) {
			// float x = e.getX();
			// float y = e.getY();

			// Log.d("onDoubleTap", "Tapped at: (" + x + "," + y + ")");

			for (int i = 0; i < event.getPointerCount(); i++) {
				int action = event.getAction();
				float x = event.getX(i);
				float y = event.getY(i);
				float p = event.getPressure(i);

				// Log.v("onDoubleTap", "Action=" + action + ", X,Y=" + x + ","
				// + y + " P=" + p);
				doubleClick(event, i);
			}

			return true;
		}
	}

	private void doubleClick(final MotionEvent event, final int i) {
		// TODO Auto-generated method stub
		Thread t = new Thread(new Runnable() {
			public void run() {
				SDLActivity.onNativeTouch(Const.SDL_MOUSE_LEFT, i,
						MotionEvent.ACTION_DOWN, 0, 0, 0);
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					Log.v("doubletap", "Could not sleep");
				}
				SDLActivity.onNativeTouch(Const.SDL_MOUSE_LEFT, i,
						MotionEvent.ACTION_UP, 0, 0, 0);
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					Log.v("doubletap", "Could not sleep");
				}
				SDLActivity.onNativeTouch(Const.SDL_MOUSE_LEFT, i,
						MotionEvent.ACTION_DOWN, 0, 0, 0);
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					Log.v("doubletap", "Could not sleep");
				}
				SDLActivity.onNativeTouch(Const.SDL_MOUSE_LEFT, i,
						MotionEvent.ACTION_UP, 0, 0, 0);
			}
		});
		t.start();
	}

	GestureDetector gestureDetector;

	// Startup
	public SDLSurface(Context context) {
		super(context);
		setEGLContextClientVersion(2);
		getHolder().addCallback(this);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		setOnKeyListener(this);
		setOnTouchListener(this);
		// this.setOnGenericMotionListener(this);
		this.setWillNotDraw(false);

		mSensorManager = (SensorManager) context.getSystemService("sensor");

		gestureDetector = new GestureDetector(context, new GestureListener());
	}

	// Called when we have a valid drawing surface
	public void surfaceCreated(SurfaceHolder holder) {
		// Log.v("SDL", "surfaceCreated()");
		enableSensor(Sensor.TYPE_ACCELEROMETER, true);
	}

	// Called when we lose the surface
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v("SDL", "surfaceDestroyed()");
		// SDLActivity.nativePause();
		enableSensor(Sensor.TYPE_ACCELEROMETER, false);
	}

	// Called when the surface is resized
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		SDLActivity.width = width;
		SDLActivity.height = height;
		Log.v("SDL", "surfaceChanged(" + width + "," + height + ")");

		int sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565 by default
		switch (format) {
		case PixelFormat.A_8:
			Log.v("SDL", "pixel format A_8");
			break;
		case PixelFormat.LA_88:
			Log.v("SDL", "pixel format LA_88");
			break;
		case PixelFormat.L_8:
			Log.v("SDL", "pixel format L_8");
			break;
		case PixelFormat.RGBA_4444:
			Log.v("SDL", "pixel format RGBA_4444");
			sdlFormat = 0x85421002; // SDL_PIXELFORMAT_RGBA4444
			break;
		case PixelFormat.RGBA_5551:
			Log.v("SDL", "pixel format RGBA_5551");
			sdlFormat = 0x85441002; // SDL_PIXELFORMAT_RGBA5551
			break;
		case PixelFormat.RGBA_8888:
			Log.v("SDL", "pixel format RGBA_8888");
			sdlFormat = 0x86462004; // SDL_PIXELFORMAT_RGBA8888
			break;
		case PixelFormat.RGBX_8888:
			Log.v("SDL", "pixel format RGBX_8888");
			sdlFormat = 0x86262004; // SDL_PIXELFORMAT_RGBX8888
			break;
		case PixelFormat.RGB_332:
			Log.v("SDL", "pixel format RGB_332");
			sdlFormat = 0x84110801; // SDL_PIXELFORMAT_RGB332
			break;
		case PixelFormat.RGB_565:
			Log.v("SDL", "pixel format RGB_565");
			sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565
			break;
		case PixelFormat.RGB_888:
			Log.v("SDL", "pixel format RGB_888");
			// Not sure this is right, maybe SDL_PIXELFORMAT_RGB24 instead?
			sdlFormat = 0x86161804; // SDL_PIXELFORMAT_RGB888
			break;
		default:
			Log.v("SDL", "pixel format unknown " + format);
			break;
		}
		SDLActivity.onNativeResize(width, height, sdlFormat);
		Log.v("SDL", "Window size:" + width + "x" + height);
		SDLActivity.startApp();
	}

	@Override
	public void onDraw(Canvas canvas) {
		Log.v("Draw", "Drawing...");
		// canvas.scale((float) 0.5, (float) 0.5);
		super.onDraw(canvas);
	}

	public boolean once = false;
	public float old_x = 0;
	public float old_y = 0;
	private boolean mouseUp = true;
	private boolean firstTouch = false;
	private float sensitivity_mult = (float) 1.0;
	private boolean stretchToScreen = false;

	// Key events
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_MENU
				|| event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
				|| event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP)
			return false;

		if (event.getAction() == KeyEvent.ACTION_DOWN) {
//			Log.v("SDL", "key down: " + keyCode);
			if (keyCode == 77) {
				SDLActivity.onNativeKeyDown(59);
				SDLActivity.onNativeKeyDown(9);
			} else if (keyCode == 17) {
				SDLActivity.onNativeKeyDown(59);
				SDLActivity.onNativeKeyDown(15);
			} else if (keyCode == 18) {
				SDLActivity.onNativeKeyDown(59);
				SDLActivity.onNativeKeyDown(10);
			} else
				SDLActivity.onNativeKeyDown(keyCode);
			return true;
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
//			Log.v("SDL", "key up: " + keyCode);
			if (keyCode == 77) { //@ key
				SDLActivity.onNativeKeyUp(59);
				SDLActivity.onNativeKeyUp(9);
			} else if (keyCode == 17) { //* key
				SDLActivity.onNativeKeyUp(59);
				SDLActivity.onNativeKeyUp(15);
			} else if (keyCode == 18) { //# key
				SDLActivity.onNativeKeyUp(59);
				SDLActivity.onNativeKeyUp(10);
			} else
				SDLActivity.onNativeKeyUp(keyCode);
			return true;
		}

		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Log.v("onTouchEvent",
		// "Action=" + event.getAction() + ", X,Y=" + event.getX() + ","
		// + event.getY() + " P=" + event.getPressure());
		if (!firstTouch) {
			SDLActivity.onNativeTouch(0, 0, MotionEvent.ACTION_MOVE, 0, 0, 0);
			firstTouch = true;
		}
		if (event.getPointerCount() > 1) {

			// Log.v("Right Click",
			// "Action=" + event.getAction() + ", X,Y=" + event.getX()
			// + "," + event.getY() + " P=" + event.getPressure());
			rightClick(event);
			return true;
		} else
			return gestureDetector.onTouchEvent(event);
	}

	public boolean rightClick(final MotionEvent e) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				// remoteMouseStayPut(e);
				// One
				// Log.v("Double Click", "One");
				SDLActivity.onNativeTouch(Const.SDL_MOUSE_RIGHT, 0,
						MotionEvent.ACTION_DOWN, 0, 0, 0);
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					Logger.getLogger(VncCanvasActivity.class.getName()).log(
							Level.SEVERE, null, ex);
				}
				SDLActivity.onNativeTouch(Const.SDL_MOUSE_RIGHT, 0,
						MotionEvent.ACTION_UP, 0, 0, 0);
			}
		});
		// t.setPriority(Thread.MAX_PRIORITY);
		t.start();
		return true;

	}

	// Touch events
	public boolean onTouch(View v, MotionEvent event) {
		// Log.v("onTouch",
		// "Action=" + event.getAction() + ", X,Y=" + event.getX() + ","
		// + event.getY() + " P=" + event.getPressure());
		if (event.getAction() == MotionEvent.ACTION_MOVE) {

			// for (int i = 0; i < event.getPointerCount(); i++) {
			int action = event.getAction();
			float x = event.getX(0);
			float y = event.getY(0);
			float p = event.getPressure(0);

			// TODO: Anything else we need to pass?
			// SDLActivity.onNativeTouch(action, x, y, p);
			if (mouseUp) {
				old_x = x;
				old_y = y;
				mouseUp = false;
			}
			if (action == MotionEvent.ACTION_MOVE)
				SDLActivity.onNativeTouch(0, 0, MotionEvent.ACTION_MOVE,
						(x - old_x) * sensitivity_mult, (y - old_y)
								* sensitivity_mult, p);
			// Log.v("onTouch", "Moving by=" + action + ", X,Y=" + (x - old_x)
			// + "," + (y - old_y) + " P=" + p);
			// save current
			old_x = x;
			old_y = y;
			// }
		} else if (event.getAction() == event.ACTION_UP) {
			SDLActivity.onNativeTouch(Const.SDL_MOUSE_LEFT, 0,
					MotionEvent.ACTION_UP, 0, 0, 0);
			// SDLActivity.onNativeTouch(Const.SDL_MOUSE_RIGHT, 0,
			// MotionEvent.ACTION_UP, 0, 0, 0);
			mouseUp = true;
		}
		return false;
	}

	// Sensor events
	public void enableSensor(int sensortype, boolean enabled) {
		// TODO: This uses getDefaultSensor - what if we have >1 accels?
		if (enabled) {
			mSensorManager.registerListener(this,
					mSensorManager.getDefaultSensor(sensortype),
					SensorManager.SENSOR_DELAY_GAME, null);
		} else {
			mSensorManager.unregisterListener(this,
					mSensorManager.getDefaultSensor(sensortype));
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			SDLActivity.onNativeAccel(event.values[0], event.values[1],
					event.values[2]);
		}
	}

}
