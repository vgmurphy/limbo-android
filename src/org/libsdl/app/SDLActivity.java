package org.libsdl.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.max2idea.android.limbo.main.Const;
import com.max2idea.android.limbo.main.LimboActivity;
import com.max2idea.android.limbo.main.LimboVNCActivity;
//import com.max2idea.android.limbo.main.R;
import com.max2idea.android.limbo.main.R;
import com.max2idea.android.limbo.main.LimboSettingsManager;
import com.max2idea.android.limbo.utils.*;

/**
 * SDL Activity
 */
public class SDLActivity extends Activity {

	public static final int KEYBOARD = 10000;
	public static final int QUIT = 10001;
	public static final int HELP = 10002;
	private boolean monitorMode = false;
	private boolean mouseOn = false;
	private Object lockTime = new Object();
	private boolean timeQuit = false;
	private Thread timeListenerThread;
	private ProgressDialog progDialog;
	private Activity activity = this;

	public String cd_iso_path = null;
	public String hda_img_path = null;
	public String fda_img_path = null;
	public String hdb_img_path = null;
	public String fdb_img_path = null;
	public String cpu = null;
	public String TAG = "VMExecutor";

	public int aiomaxthreads = 1;
	// Default Settings
	public int memory = 128;
	public String bootdevice = null;
	// net
	public String net_cfg = "None";
	public int nic_num = 1;
	public String vga_type = "std";
	public String hd_cache = "default";
	public String nic_driver = null;
	public String lib = "liblimbo.so";
	public String lib_path = null;
	public int restart = 0;
	public String snapshot_name = "limbo";
	public int disableacpi = 0;
	public int disablehpet = 0;
	public static int enablebluetoothmouse = 0;
	public int enableqmp = 0;
	public int enablevnc = 0;
	public String vnc_passwd = null;
	public int vnc_allow_external = 0;
	public String qemu_dev = null;
	public String qemu_dev_value = null;
	public String base_dir = Const.basefiledir;
	public String dns_addr = null;
	private boolean once = true;
	private boolean zoomable = false;
	private String status = null;

	public static Handler handler;

	// This is what SDL runs in. It invokes SDL_main(), eventually
	private static Thread mSDLThread;

	// EGL private objects
	private static EGLContext mEGLContext;
	private static EGLSurface mEGLSurface;
	private static EGLDisplay mEGLDisplay;
	private static EGLConfig mEGLConfig;
	private static int mGLMajor, mGLMinor;

	public static int vm_width;
	public static int vm_height;
	public static int width;
	public static int height;
	public static int screen_width;
	public static int screen_height;
	public static float width_mult = (float) 1.0;
	public static float height_mult = (float) 1.0;

	private static Activity activity1;

	public static void showTextInput(int x, int y, int w, int h) {
		// Transfer the task to the main thread as a Runnable
		// mSingleton.commandHandler.post(new ShowTextInputHandler(x, y, w, h));
	}

	public static void singleClick(final MotionEvent event, final int i) {
		// TODO Auto-generated method stub
		Thread t = new Thread(new Runnable() {
			public void run() {
				SDLActivity.onNativeTouch(Const.SDL_MOUSE_LEFT, i,
						MotionEvent.ACTION_DOWN, 0, 0, 0);
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					Log.v("singletap", "Could not sleep");
				}
				SDLActivity.onNativeTouch(Const.SDL_MOUSE_LEFT, i,
						MotionEvent.ACTION_UP, 0, 0, 0);
			}
		});
		t.start();
	}

	private void promptBluetoothMouse(final Activity activity) {
		// TODO Auto-generated method stub

		final AlertDialog alertDialog;
		alertDialog = new AlertDialog.Builder(activity).create();
		alertDialog.setTitle("Enable Bluetooth Mouse");

		RelativeLayout mLayout = new RelativeLayout(this);
		mLayout.setId(12222);

		TextView textView = new TextView(activity);
		textView.setVisibility(View.VISIBLE);
		textView.setId(201012010);
		textView.setText("Step 1: Disable Mouse Acceleration inside the Guest OS.\n\tFor DSL use command: dsl@box:/>xset m 1\n"
				+ "Step 2: Pair your Bluetooth Mouse and press OK!\n"
				+ "Step 3: Move your mouse pointer to all desktop corners to calibrate.\n");

		RelativeLayout.LayoutParams searchViewParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		mLayout.addView(textView, searchViewParams);
		alertDialog.setView(mLayout);

		final Handler handler = this.handler;

		// alertDialog.setMessage(body);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				MotionEvent a = MotionEvent.obtain(0, 0,
						MotionEvent.ACTION_DOWN, 0, 0, 0);
				SDLActivity.singleClick(a, 0);
				SDLActivity.onNativeMouseReset(0, 0, MotionEvent.ACTION_MOVE,
						vm_width / 2, vm_height / 2, 0);
				SDLActivity.enablebluetoothmouse = 1;

			}
		});
		alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SDLActivity.enablebluetoothmouse = 0;
				return;
			}
		});
		alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				SDLActivity.enablebluetoothmouse = 0;
				return;

			}
		});
		alertDialog.show();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Log.v(TAG, "RET CODE: " + resultCode);
		if (resultCode == Const.FILEMAN_RETURN_CODE) {
			// Read from activity
			String currDir = LimboSettingsManager.getLastDir(this);
			String file = "";
			String fileType = "";
			Bundle b = data.getExtras();
			fileType = b.getString("fileType");
			file = b.getString("file");
			currDir = b.getString("currDir");
			// Log.v(TAG, "Got New Dir: " + currDir);
			// Log.v(TAG, "Got File Type: " + fileType);
			// Log.v(TAG, "Got New File: " + file);
			if (currDir != null && !currDir.trim().equals("")) {
				LimboSettingsManager.setLastDir(this, currDir);
			}
			if (fileType != null && file != null) {
				DrivesDialogBox.setDriveAttr(fileType, file, true);
			}

		}

		// Check if says open

	}

	public void setParams(Machine machine) {

		if (machine == null) {
			return;
		}
		memory = machine.memory;
		vga_type = machine.vga_type;
		hd_cache = machine.hd_cache;
		snapshot_name = machine.snapshot_name;
		disableacpi = machine.disableacpi;
		disablehpet = machine.disablehpet;
		// enablebluetoothmouse = machine.bluetoothmouse;
		enableqmp = machine.enableqmp;
		enablevnc = machine.enablevnc;

		if (machine.cpu.endsWith("(64Bit)")) {
			// lib_path = FileUtils.getDataDir() +
			// "/lib/libqemu-system-x86_64.so";
			cpu = machine.cpu.split(" ")[0];
		} else {
			cpu = machine.cpu;
			// x86_64 can run 32bit as well as no need for the extra lib
			// lib_path = FileUtils.getDataDir() +
			// "/lib/libqemu-system-x86_64.so";
		}
		// Add other archs??

		// Load VM library
		// loadNativeLibs("libSDL.so");
		// loadNativeLibs("libSDL_image.so");
		// loadNativeLibs("libmikmod.so");
		// loadNativeLibs("libSDL_mixer.so");
		// loadNativeLibs("libSDL_ttf.so");
		// loadNativeLibs(lib);

		if (machine.cd_iso_path == null || machine.cd_iso_path.equals("None")) {
			cd_iso_path = null;
		} else {
			cd_iso_path = machine.cd_iso_path;
		}
		if (machine.hda_img_path == null || machine.hda_img_path.equals("None")) {
			hda_img_path = null;
		} else {
			hda_img_path = machine.hda_img_path;
		}

		if (machine.hdb_img_path == null || machine.hdb_img_path.equals("None")) {
			hdb_img_path = null;
		} else {
			hdb_img_path = machine.hdb_img_path;
		}

		if (machine.fda_img_path == null || machine.fda_img_path.equals("None")) {
			fda_img_path = null;
		} else {
			fda_img_path = machine.fda_img_path;
		}

		if (machine.fdb_img_path == null || machine.fdb_img_path.equals("None")) {
			fdb_img_path = null;
		} else {
			fdb_img_path = machine.fdb_img_path;
		}
		if (machine.bootdevice == null) {
			bootdevice = null;
		} else if (machine.bootdevice.equals("Default")) {
			bootdevice = null;
		} else if (machine.bootdevice.equals("CD Rom")) {
			bootdevice = "d";
		} else if (machine.bootdevice.equals("Floppy")) {
			bootdevice = "a";
		} else if (machine.bootdevice.equals("Hard Disk")) {
			bootdevice = "c";
		}

		if (machine.net_cfg == null || machine.net_cfg.equals("None")) {
			net_cfg = "none";
			nic_driver = null;
		} else if (machine.net_cfg.equals("User")) {
			net_cfg = "user";
			nic_driver = machine.nic_driver;
		}

	}

	public static void sendCtrlAtlKey(int code) {

		SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_CTRL_LEFT);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_ALT_LEFT);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SDLActivity.onNativeKeyDown(code);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_CTRL_LEFT);
		SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_ALT_LEFT);
		SDLActivity.onNativeKeyUp(code);
	}

	public void stopTimeListener() {
		Log.v("SaveVM", "Stopping Listener");
		synchronized (this.lockTime) {
			this.timeQuit = true;
			this.lockTime.notifyAll();
		}
	}

	public void onDestroy() {

		// Now wait for the SDL thread to quit
		Log.v("LimboSDL", "Waiting for SDL thread to quit");
		if (mSDLThread != null) {
			try {
				mSDLThread.join();
			} catch (Exception e) {
				Log.v("SDL", "Problem stopping thread: " + e);
			}
			mSDLThread = null;

			Log.v("SDL", "Finished waiting for SDL thread");
		}
		this.stopTimeListener();
		super.onDestroy();
	}

	// EGL functions
	public static boolean initEGL(int majorVersion, int minorVersion) {
		Log.v("initEGL", "Version: " + majorVersion + "." + minorVersion);
		EGL10 egl1 = (EGL10) EGLContext.getEGL();
		if (mEGLDisplay != null && mEGLContext != null) {
			Log.v("initEGL", "Destroying EGL Context");
			egl1.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE,
					EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
			egl1.eglDestroyContext(mEGLDisplay, mEGLContext);
			mEGLContext = null;
		}

		if (mEGLDisplay != null && mEGLSurface != null) {
			Log.v("initEGL", "Destroying Surface");
			egl1.eglDestroySurface(mEGLDisplay, mEGLSurface);
			mEGLSurface = null;

		}

		Log.v("initEGL", "Destroying Display");
		SDLActivity.mEGLDisplay = null;

		if (SDLActivity.mEGLDisplay == null) {
			Log.v("initEGL", "Starting up OpenGL ES " + majorVersion + "."
					+ minorVersion);

			try {
				EGL10 egl = (EGL10) EGLContext.getEGL();

				EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

				int[] version = new int[2];
				egl.eglInitialize(dpy, version);

				int EGL_OPENGL_ES_BIT = 1;
				int EGL_OPENGL_ES2_BIT = 4;
				int renderableType = 0;
				if (majorVersion == 2) {
					renderableType = EGL_OPENGL_ES2_BIT;
				} else if (majorVersion == 1) {
					renderableType = EGL_OPENGL_ES_BIT;
				}
				int[] configSpec = {
						// EGL10.EGL_DEPTH_SIZE, 16,
						EGL10.EGL_RENDERABLE_TYPE, renderableType,
						EGL10.EGL_NONE };
				EGLConfig[] configs = new EGLConfig[1];
				int[] num_config = new int[1];
				if (!egl.eglChooseConfig(dpy, configSpec, configs, 1,
						num_config) || num_config[0] == 0) {
					Log.e("initEGL", "No EGL config available");
					return false;
				}
				EGLConfig config = configs[0];

				/*
				 * int EGL_CONTEXT_CLIENT_VERSION=0x3098; int contextAttrs[] =
				 * new int[] { EGL_CONTEXT_CLIENT_VERSION, majorVersion,
				 * EGL10.EGL_NONE }; EGLContext ctx = egl.eglCreateContext(dpy,
				 * config, EGL10.EGL_NO_CONTEXT, contextAttrs);
				 * 
				 * if (ctx == EGL10.EGL_NO_CONTEXT) { Log.e("SDL",
				 * "Couldn't create context"); return false; }
				 * LimboSDLActivity.mEGLContext = ctx;
				 */
				SDLActivity.mEGLDisplay = dpy;
				SDLActivity.mEGLConfig = config;
				SDLActivity.mGLMajor = majorVersion;
				SDLActivity.mGLMinor = minorVersion;

				SDLActivity.createEGLSurface();
			} catch (Exception e) {
				Log.v("initEGL", e + "");
				for (StackTraceElement s : e.getStackTrace()) {
					Log.v("initEGL", s.toString());
				}
			}
		} else
			SDLActivity.createEGLSurface();

		// Set Fit to Screen by Default for smaller devices
		// so it doesn't crash
		if (fitToScreen)
			SDLActivity.setFitToScreen();
		else if (stretchToScreen)
			SDLActivity.setStretchToScreen();
		return true;
	}

	public static boolean createEGLContext() {
		EGL10 egl = (EGL10) EGLContext.getEGL();
		int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
		int contextAttrs[] = new int[] { EGL_CONTEXT_CLIENT_VERSION,
				SDLActivity.mGLMajor, EGL10.EGL_NONE };
		SDLActivity.mEGLContext = egl.eglCreateContext(SDLActivity.mEGLDisplay,
				SDLActivity.mEGLConfig, EGL10.EGL_NO_CONTEXT, contextAttrs);
		if (SDLActivity.mEGLContext == EGL10.EGL_NO_CONTEXT) {
			Log.e("createEGLContext", "Couldn't create context");
			return false;
		}
		return true;
	}

	public static boolean createEGLSurface() {
		if (SDLActivity.mEGLDisplay != null && SDLActivity.mEGLConfig != null) {
			EGL10 egl = (EGL10) EGLContext.getEGL();
			if (SDLActivity.mEGLContext == null)
				createEGLContext();

			Log.v("createEGLSurface", "Creating new EGL Surface");
			EGLSurface surface = egl.eglCreateWindowSurface(
					SDLActivity.mEGLDisplay, SDLActivity.mEGLConfig,
					SDLActivity.mSurface, null);
			if (surface == EGL10.EGL_NO_SURFACE) {
				Log.e("createEGLSurface", "Couldn't create surface");
				return false;
			}
			Log.v("createEGLSurface", "Making Current");
			if (!egl.eglMakeCurrent(SDLActivity.mEGLDisplay, surface, surface,
					SDLActivity.mEGLContext)) {
				Log.e("createEGLSurface",
						"Old EGL Context doesnt work, trying with a new one");
				createEGLContext();
				if (!egl.eglMakeCurrent(SDLActivity.mEGLDisplay, surface,
						surface, SDLActivity.mEGLContext)) {
					Log.e("createEGLSurface",
							"Failed making EGL Context current");
					return false;
				}
			}
			SDLActivity.mEGLSurface = surface;
			return true;
		}
		return false;
	}

	// EGL buffer flip
	public static void flipEGL() {
		try {
			// LimboSDLActivity.mSurface.setScaleX((float) 0.2);
			// LimboSDLActivity.mSurface.setScaleY((float) 0.2);
			EGL10 egl = (EGL10) EGLContext.getEGL();

			egl.eglWaitNative(EGL10.EGL_CORE_NATIVE_ENGINE, null);

			// drawing here
			// Log.v("FlipEGL", "Drawing");

			egl.eglWaitGL();

			egl.eglSwapBuffers(SDLActivity.mEGLDisplay, SDLActivity.mEGLSurface);

		} catch (Exception e) {
			Log.v("flipEGL", "Exception: " + e);
			for (StackTraceElement s : e.getStackTrace()) {
				Log.v("flipEGL", s.toString());
			}
		}
	}

	public void timeListener() {
		while (timeQuit != true) {
			status = checkCompletion();
			// Log.v("timeListener", "Status: " + status);
			if (status == null || status.equals("") || status.equals("DONE")) {
				Log.v("Inside", "Saving state is done: " + status);
				stopTimeListener();
				return;
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ex) {
				Log.v("SaveVM", "Could not sleep");
			}
		}
		Log.v("SaveVM", "Save state complete");

	}

	public void startTimeListener() {
		this.stopTimeListener();
		timeQuit = false;
		try {
			Log.v("Listener", "Time Listener Started...");
			timeListener();
			synchronized (lockTime) {
				while (timeQuit == false) {
					lockTime.wait();
				}
				lockTime.notifyAll();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.v("SaveVM", "Time listener thread error: " + ex.getMessage());
		}
		Log.v("Listener", "Time listener thread exited...");

	}

	public DrivesDialogBox drives = null;

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Log.v("Limbo", "Inside Options Check");
		super.onOptionsItemSelected(item);
		if (item.getItemId() == R.id.itemDrives) {
			// Show up removable devices dialog
			drives = new DrivesDialogBox(activity1, R.style.Transparent,this);
			drives.show();
		} else if (item.getItemId() == R.id.itemShutdown) {
			stopVM(false);
		} else if (item.getItemId() == R.id.itemMouse) {
			promptMouse();
		} else if (item.getItemId() == this.KEYBOARD
				|| item.getItemId() == R.id.itemKeyboard) {
			this.onKeyboard();
		} else if (item.getItemId() == R.id.itemMonitor) {
			if (this.monitorMode) {
				this.onVMConsole();
			} else {
				this.onMonitor();
			}
		} else if (item.getItemId() == R.id.itemExternalMouse) {
			if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				this.promptBluetoothMouse(activity);
			} else {
				Toast.makeText(this.getApplicationContext(),
						"External Mouse support only for ICS and above",
						Toast.LENGTH_LONG).show();
			}

		} else if (item.getItemId() == R.id.itemSaveState) {
			this.promptStateName(activity);
		} else if (item.getItemId() == R.id.itemFitToScreen) {
			setFitToScreen();
		} else if (item.getItemId() == R.id.itemStretchToScreen) {
			setStretchToScreen();
		} else if (item.getItemId() == R.id.itemZoomIn) {
			this.setZoomIn();
		} else if (item.getItemId() == R.id.itemZoomOut) {
			this.setZoomOut();
		} else if (item.getItemId() == R.id.itemCtrlAltDel) {
			this.onCtrlAltDel();
		} else if (item.getItemId() == R.id.itemCtrlC) {
			this.onCtrlC();
		} else if (item.getItemId() == R.id.itemOneToOne) {
			this.setOneToOne();
		} else if (item.getItemId() == R.id.itemZoomable) {
			this.setZoomable();
		} else if (item.getItemId() == this.QUIT) {
		} else if (item.getItemId() == R.id.itemHelp) {
			this.onMenuHelp();
		}
		// this.canvas.requestFocus();
		return true;
	}

	private static void onMenuHelp() {
		String url = "http://code.google.com/p/limbo-android/wiki/LimboAndroid";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		LimboActivity.activity.startActivity(i);

	}

	private void promptMouse() {

		MotionEvent a = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0,
				0);
		SDLActivity.singleClick(a, 0);
		SDLActivity.onNativeMouseReset(0, 0, MotionEvent.ACTION_MOVE,
				vm_width / 2, vm_height / 2, 0);
		Toast.makeText(this.getApplicationContext(),
				"Mouse Trackpad Mode enabled", Toast.LENGTH_LONG).show();
	}

	private void onCtrlAltDel() {
		// TODO Auto-generated method stub
		SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_CTRL_RIGHT);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_ALT_RIGHT);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_FORWARD_DEL);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_FORWARD_DEL);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_ALT_RIGHT);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_CTRL_RIGHT);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void onCtrlC() {
		// TODO Auto-generated method stub
		SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_CTRL_RIGHT);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_C);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_C);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_CTRL_RIGHT);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void stopVM(boolean exit) {

		new AlertDialog.Builder(this)
				.setTitle("Shutdown VM")
				.setMessage(
						"To avoid any corrupt data make sure you "
								+ "have already shutdown the Operating system from within the VM. Continue?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								new Thread(new Runnable() {
									public void run() {
										Log.v("SDL", "VM is stopped");
										nativeQuit();
									}
								}).start();

							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
	}

	private static void setStretchToScreen() {
		// TODO Auto-generated method stub

		new Thread(new Runnable() {
			public void run() {
				SDLActivity.stretchToScreen = true;
				SDLActivity.fitToScreen = false;
				sendCtrlAtlKey(KeyEvent.KEYCODE_6);
			}
		}).start();

	}

	private static void setFitToScreen() {
		// TODO Auto-generated method stub

		new Thread(new Runnable() {
			public void run() {
				SDLActivity.stretchToScreen = false;
				SDLActivity.fitToScreen = true;
				sendCtrlAtlKey(KeyEvent.KEYCODE_5);

			}
		}).start();

	}

	private void setOneToOne() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			public void run() {
				SDLActivity.stretchToScreen = false;
				SDLActivity.fitToScreen = false;
				sendCtrlAtlKey(KeyEvent.KEYCODE_U);
			}
		}).start();

	}

	private void setFullScreen() {
		// TODO Auto-generated method stub

		new Thread(new Runnable() {
			public void run() {
				sendCtrlAtlKey(KeyEvent.KEYCODE_F);
			}
		}).start();

	}

	private void setZoomIn() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			public void run() {
				SDLActivity.stretchToScreen = false;
				SDLActivity.fitToScreen = false;
				sendCtrlAtlKey(KeyEvent.KEYCODE_4);
			}
		}).start();

	}

	private void setZoomOut() {
		// TODO Auto-generated method stub

		new Thread(new Runnable() {
			public void run() {
				SDLActivity.stretchToScreen = false;
				SDLActivity.fitToScreen = false;
				sendCtrlAtlKey(KeyEvent.KEYCODE_3);

			}
		}).start();

	}

	private void setZoomable() {
		// TODO Auto-generated method stub
		zoomable = true;

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		// Log.v("Limbo", "Inside Options Created");
		getMenuInflater().inflate(R.menu.sdlactivitymenu, menu);

		// if (vncCanvas.scaling != null) {
		// menu.findItem(vncCanvas.scaling.getId()).setChecked(true);
		// }

		// if (this.monitorMode) {
		// menu.findItem(R.id.itemMonitor).setTitle("VM Console");
		//
		// } else {
		// menu.findItem(R.id.itemMonitor).setTitle("Monitor Console");
		//
		// }
		//
		// // Menu inputMenu = menu.findItem(R.id.itemInputMode).getSubMenu();
		//
		// if (this.mouseOn) { // Panning is disable for now
		// menu.findItem(R.id.itemMouse).setTitle("Pan (Mouse Off)");
		// menu.findItem(R.id.itemMouse).setIcon(R.drawable.pan);
		// } else {
		// menu.findItem(R.id.itemMouse).setTitle("Enable Mouse");
		// menu.findItem(R.id.itemMouse).setIcon(R.drawable.mouse);
		//
		// }

		return true;

	}

	private void onMonitor() {
		new Thread(new Runnable() {
			public void run() {
				monitorMode = true;
				sendCtrlAtlKey(KeyEvent.KEYCODE_2);
				Log.v("Limbo", "Monitor On");
			}
		}).start();

	}

	private void onVMConsole() {
		monitorMode = false;
		sendCtrlAtlKey(KeyEvent.KEYCODE_1);
	}

	private void onSaveState(final String stateName) {
		// onMonitor();
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException ex) {
		// Logger.getLogger(LimboVNCActivity.class.getName()).log(
		// Level.SEVERE, null, ex);
		// }
		// vncCanvas.sendText("savevm " + stateName + "\n");
		// Toast.makeText(this.getApplicationContext(),
		// "Please wait while saving VM State", Toast.LENGTH_LONG).show();
		new Thread(new Runnable() {
			public void run() {
				Log.v("SDL", "Saving VM1");
				nativePause();
				// LimboActivity.vmexecutor.saveVM1(stateName);

				nativeResume();

			}
		}).start();

		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException ex) {
		// Logger.getLogger(LimboVNCActivity.class.getName()).log(
		// Level.SEVERE, null, ex);
		// }
		// onSDL();
		((LimboActivity) LimboActivity.activity).saveStateDB(stateName);

		progDialog = ProgressDialog.show(activity, "Please Wait",
				"Saving VM State...", true);
		SaveVM a = new SaveVM();
		a.execute();

	}

	public void saveStateDB(String snapshot_name) {
		LimboActivity.currMachine.snapshot_name = snapshot_name;
		int ret = LimboActivity.machineDB
				.deleteMachine(LimboActivity.currMachine);
		ret = LimboActivity.machineDB.insertMachine(LimboActivity.currMachine);

	}

	private void onSaveState1(String stateName) {
		// Log.v("onSaveState1", stateName);
		monitorMode = true;
		sendCtrlAtlKey(KeyEvent.KEYCODE_2);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ex) {
			Logger.getLogger(LimboVNCActivity.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		sendText("savevm " + stateName + "\n");
		saveStateDB(stateName);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ex) {
			Logger.getLogger(LimboVNCActivity.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		sendCommand(COMMAND_SAVEVM, "vm");

	}

	private static void sendText(String string) {
		// TODO Auto-generated method stub
		// Log.v("sendText", string);
		KeyCharacterMap keyCharacterMap = KeyCharacterMap
				.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
		KeyEvent[] keyEvents = keyCharacterMap.getEvents(string.toCharArray());
		for (int i = 0; i < keyEvents.length; i++) {

			if (keyEvents[i].getAction() == KeyEvent.ACTION_DOWN) {
				// Log.v("sendText", "Up: " + keyEvents[i].getKeyCode());
				SDLActivity.onNativeKeyDown(keyEvents[i].getKeyCode());
			} else if (keyEvents[i].getAction() == KeyEvent.ACTION_UP) {
				// Log.v("sendText", "Down: " + keyEvents[i].getKeyCode());
				SDLActivity.onNativeKeyUp(keyEvents[i].getKeyCode());
			}
		}
	}

	private class SaveVM extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			// Log.v("handler", "Save VM");
			startTimeListener();
			return null;
		}

		@Override
		protected void onPostExecute(Void test) {
			try {
				if (progDialog.isShowing()) {
					progDialog.dismiss();
				}
				monitorMode = false;
				sendCtrlAtlKey(KeyEvent.KEYCODE_1);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private void fullScreen() {
		// AbstractScaling.getById(R.id.itemFitToScreen).setScaleTypeForActivity(
		// this);
		// showPanningState();
	}

	public void promptStateName(final Activity activity) {
		// Log.v("promptStateName", "ask");
		if ((LimboActivity.currMachine.hda_img_path == null || !LimboActivity.currMachine.hda_img_path
				.contains(".qcow2"))
				&& (LimboActivity.currMachine.hdb_img_path == null || !LimboActivity.currMachine.hdb_img_path
						.contains(".qcow2")))

		{
			Toast.makeText(
					activity.getApplicationContext(),
					"No HDD image found, please create a qcow2 image from Limbo console",
					Toast.LENGTH_LONG).show();
			return;
		}
		final AlertDialog alertDialog;
		alertDialog = new AlertDialog.Builder(activity).create();
		alertDialog.setTitle("Snapshot/State Name");
		EditText stateView = new EditText(activity);
		if (LimboActivity.currMachine.snapshot_name != null) {
			stateView.setText(LimboActivity.currMachine.snapshot_name);
		}
		stateView.setEnabled(true);
		stateView.setVisibility(View.VISIBLE);
		stateView.setId(201012010);
		stateView.setSingleLine();
		alertDialog.setView(stateView);

		// alertDialog.setMessage(body);
		alertDialog.setButton("Create", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				// UIUtils.log("Searching...");
				final EditText a = (EditText) alertDialog
						.findViewById(201012010);
				progDialog = ProgressDialog.show(activity, "Please Wait",
						"Saving VM State...", true);
				new Thread(new Runnable() {
					public void run() {
						// Log.v("promptStateName", a.getText().toString());
						onSaveState1(a.getText().toString());
					}
				}).start();

				return;
			}
		});
		alertDialog.show();

	}

	private String checkCompletion() {
		String save_state = "";
		if (LimboActivity.vmexecutor != null) {
			save_state = LimboActivity.vmexecutor.get_save_state();
		}
		return save_state;
	}

	// Main components
	public static SDLActivity mSingleton;
	public static SDLSurface mSurface;

	// Audio
	private static Thread mAudioThread;
	private static AudioTrack mAudioTrack;
	private static boolean fitToScreen = Const.enable_qemu_fullScreen;
	private static boolean stretchToScreen = false; // Start with fitToScreen

	// Setup
	protected void onCreate(Bundle savedInstanceState) {
		// Log.v("SDL", "onCreate()");
		super.onCreate(savedInstanceState);

		Log.v("SDL", "Max Mem = " + Runtime.getRuntime().maxMemory());
		this.handler = commandHandler;
		this.activity1 = this;

		if (Const.enable_fullscreen
				|| android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		if (LimboSettingsManager.getOrientationReverse(this))
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);

		if (LimboActivity.currMachine == null) {
			Log.v("SDLAcivity", "No VM selected!");
		}

		setParams(LimboActivity.currMachine);

		// So we can call stuff from static callbacks
		mSingleton = this;

		// Set up the surface
		mSurface = getSDLSurface();
		mSurface.setRenderer(new ClearRenderer());
		// mSurface.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		// setContentView(mSurface);
		createUI(0, 0);
		SurfaceHolder holder = mSurface.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
		setScreenSize();

		Toast toast = Toast.makeText(activity, "2-Finger Tap for Right Click",
				Toast.LENGTH_LONG);
		toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
		toast.show();
	}

	public SDLSurface getSDLSurface() {
		// TODO Auto-generated method stub
		mSurface = new SDLSurface(getApplication());
		return mSurface;
	}

	private void setScreenSize() {
		// TODO Auto-generated method stub
		WindowManager wm = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		this.screen_width = display.getWidth();
		this.screen_height = display.getHeight();

	}

	private void createUI(int w, int h) {
		// TODO Auto-generated method stub
		int width = w;
		int height = h;
		if (width == 0) {
			width = RelativeLayout.LayoutParams.WRAP_CONTENT;
		}
		if (height == 0) {
			height = RelativeLayout.LayoutParams.WRAP_CONTENT;
		}

		setContentView(R.layout.main_sdl);
		FrameLayout mLayout = (FrameLayout) findViewById(R.id.sdl);
		FrameLayout.LayoutParams surfaceParams = new FrameLayout.LayoutParams(
				width, height);
		// surfaceParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
		// mLayout.getId());
		// surfaceParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
		// mLayout.getId());
		mLayout.addView(mSurface, surfaceParams);
	}

	// Events
	protected void onPause() {
		Log.v("SDL", "onPause()");
		SDLActivity.nativePause();
		super.onPause();

	}

	private void onKeyboard() {
		InputMethodManager inputMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMgr.toggleSoftInput(0, 0);
	}

	protected void onResume() {
		Log.v("SDL", "onResume()");
		if (status == null || status.equals("") || status.equals("DONE"))
			SDLActivity.nativeResume();
		super.onResume();
		// if (this.mEGLContext != null) {
		// new Thread(new Runnable() {
		// public void run() {
		// Log.v("SDL", "waiting for 5 secs");
		// try {
		// Thread.sleep(5000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// Log.v("SDL", "Toggle Full Screen");
		// LimboActivity.vmexecutor.toggleFullScreen();
		//
		// }
		// }).start();
		//
		// }

	}

	static void resume() {
		Log.v("Resume", "Resuming -> Full Screeen");
		if (SDLActivity.fitToScreen)
			SDLActivity.setFitToScreen();
		if (SDLActivity.stretchToScreen)
			SDLActivity.setStretchToScreen();
		else
			LimboActivity.vmexecutor.toggleFullScreen();
		// sendCtrlAtlKey(KeyEvent.KEYCODE_F);
		// sendCtrlAtlKey(KeyEvent.KEYCODE_F);
	}

	// Messages from the SDLMain thread
	static int COMMAND_CHANGE_TITLE = 1;
	static int COMMAND_SAVEVM = 2;

	// Handler for the messages
	Handler commandHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.arg1 == COMMAND_CHANGE_TITLE) {
				setTitle((String) msg.obj);
			} else if (msg.arg1 == COMMAND_SAVEVM) {
				// Log.v("handler", "Save VM");
				SaveVM sv = new SaveVM();
				sv.execute();
			}

		}
	};

	public static void startApp() {
		// Start up the C app thread
		if (mSDLThread == null) {
			mSDLThread = new Thread(new SDLMain(), "SDLThread");
			mSDLThread.start();
		}
		// else {
		// LimboSDLActivity.nativeResume();
		// }
	}

	// Send a message from the SDLMain thread
	public static void sendCommand(int command, Object data) {
		Message msg = handler.obtainMessage();
		msg.arg1 = command;
		msg.obj = data;
		handler.sendMessage(msg);
	}

	// C functions we call
	public static native void nativeInit();

	public static native void nativePause();

	public static native void nativeStop();

	public static native void nativeResume();

	public static native void nativeQuit();

	public static native void onNativeResize(int x, int y, int format);

	public static native void onNativeKeyDown(int keycode);

	public static native void onNativeKeyUp(int keycode);

	public static native void onNativeTouch(int touch_device_id_in,
			int pointer_finger_id_in, int action, float x, float y, float p);

	public static native void onNativeMouseReset(int touch_device_id_in,
			int pointer_finger_id_in, int action, float x, float y, float p);

	public static native void onNativeAccel(float x, float y, float z);

	public static native void nativeRunAudioThread();

	// Java functions called from C

	public static boolean createGLContext(int majorVersion, int minorVersion) {
		// return mSurface.initEGL(majorVersion, minorVersion);
		return initEGL(majorVersion, minorVersion);
	}

	public static void flipBuffers() {
		// mSurface.flipEGL();
		flipEGL();
	}

	public static void setSDLResolution(int width, int height) {

		if (SDLActivity.vm_width == 0)
			SDLActivity.vm_width = width;

		if (SDLActivity.vm_height == 0)
			SDLActivity.vm_height = height;

		// Update multiplier for Mouse positioning

		SDLActivity.width_mult = (float) SDLActivity.vm_width / (float) width
				* SDLActivity.width_mult;

		SDLActivity.height_mult = (float) SDLActivity.vm_height
				/ (float) height * SDLActivity.height_mult;

		vm_width = width;
		vm_height = height;

		Log.v("setSDLResolution", "Scaling to " + vm_width + "x" + vm_height
				+ ", width_mult = " + SDLActivity.width_mult
				+ ", height_mult = " + SDLActivity.height_mult);

	}

	public static void setActivityTitle(String title) {
		// Called from SDLMain() thread and can't directly affect the view
		mSingleton.sendCommand(COMMAND_CHANGE_TITLE, title);
	}

	// Audio
	private static Object buf;

	public static Object audioInit(int sampleRate, boolean is16Bit,
			boolean isStereo, int desiredFrames) {
		Log.v("SDLAudio", "audioInit");
		int channelConfig = isStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO
				: AudioFormat.CHANNEL_CONFIGURATION_MONO;
		int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT
				: AudioFormat.ENCODING_PCM_8BIT;
		int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);

		Log.v("SDL", "SDL audio: wanted " + (isStereo ? "stereo" : "mono")
				+ " " + (is16Bit ? "16-bit" : "8-bit") + " "
				+ ((float) sampleRate / 1000f) + "kHz, " + desiredFrames
				+ " frames buffer");

		// Let the user pick a larger buffer if they really want -- but ye
		// gods they probably shouldn't, the minimums are horrifyingly high
		// latency already
		desiredFrames = Math.max(
				desiredFrames,
				(AudioTrack.getMinBufferSize(sampleRate, channelConfig,
						audioFormat) + frameSize - 1)
						/ frameSize);
		// desiredFrames *= 8;

		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
				channelConfig, audioFormat, desiredFrames * frameSize,
				AudioTrack.MODE_STREAM);

		audioStartThread();

		Log.v("SDL",
				"SDL audio: got "
						+ ((mAudioTrack.getChannelCount() >= 2) ? "stereo"
								: "mono")
						+ " "
						+ ((mAudioTrack.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) ? "16-bit"
								: "8-bit") + " "
						+ ((float) mAudioTrack.getSampleRate() / 1000f)
						+ "kHz, " + desiredFrames + " frames buffer");

		if (is16Bit) {
			buf = new short[desiredFrames * (isStereo ? 2 : 1)];
		} else {
			buf = new byte[desiredFrames * (isStereo ? 2 : 1)];
		}
		return buf;
	}

	public static void audioStartThread() {
		Log.v("SDLAudio", "audioStartThread");

		if (LimboActivity.vmexecutor.sound_card != null
				&& !LimboActivity.vmexecutor.sound_card.equals("None")) {

			mAudioThread = new Thread(new Runnable() {
				public void run() {
					mAudioTrack.play();
					nativeRunAudioThread();
				}
			});
			// Use Max priority is better
			mAudioThread.setPriority(Thread.MIN_PRIORITY);
			mAudioThread.start();
			Log.v("SDLAudio", "Audio Thread started");
		} else {
			Log.v("SDLAudio", "Audio disabled");
		}

	}

	public static void audioWriteShortBuffer(short[] buffer) {
		// Log.v("SDLAudio", "audioWriteShortBuffer");
		for (int i = 0; i < buffer.length;) {
			int result = mAudioTrack.write(buffer, i, buffer.length - i);
			if (result > 0) {
				i += result;
			} else if (result == 0) {
				// try {
				// Thread.sleep(1);
				// } catch (InterruptedException e) {
				// // Nom nom
				// }
			} else {
				Log.w("SDL", "SDL audio: error return from write(short)");
				return;
			}
		}
	}

	public static void audioWriteByteBuffer(byte[] buffer) {
		// Log.v("SDLAudio", "audioWriteShortBuffer");
		for (int i = 0; i < buffer.length;) {
			int result = mAudioTrack.write(buffer, i, buffer.length - i);
			if (result > 0) {
				i += result;
			} else if (result == 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// Nom nom
				}
			} else {
				Log.w("SDL", "SDL audio: error return from write(short)");
				return;
			}
		}
	}

	public static void audioQuit() {
		Log.v("SDLAudio", "audioQuit");
		if (mAudioThread != null) {
			try {
				mAudioThread.join();
			} catch (Exception e) {
				Log.v("SDL", "Problem stopping audio thread: " + e);
			}
			mAudioThread = null;

			// Log.v("SDL", "Finished waiting for audio thread");
		}

		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack = null;
		}
	}
}

/**
 * Simple nativeInit() runnable
 */
// class SDLMain implements Runnable {
// public void run() {
// // Runs SDL_main()
// LimboSDLActivity.nativeInit();
//
// //Log.v("SDL", "SDL thread terminated");
// }
// }

/**
 * Simple nativeInit() runnable
 */
class SDLMain implements Runnable {

	public void run() {
		// Runs SDL_main()
		SDLActivity.nativeInit();
		// Go via VMExecutor
		LimboActivity.startvm(Const.UI_SDL);
		Log.v("SDL", "SDL thread terminated");
	}

}

class ClearRenderer implements GLSurfaceView.Renderer {
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// Do nothing special.
		Log.v("onSurfaceCreated", "...");
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
		Log.v("onSurfaceChanged", "...");
		gl.glViewport(0, 0, w, h);
	}

	public void onDrawFrame(GL10 gl) {
		Log.v("onDrawFrame", "...");
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

	}
}
