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
package com.max2idea.android.limbo.main;

import android.androidVNC.COLORMODEL;
import android.androidVNC.ConnectionBean;
import android.androidVNC.VncCanvasActivity;
import android.androidVNC.VncConstants;
import android.androidVNC.VncDatabase;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpConnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.max2idea.android.limbo.jni.ImageCreator;

import com.max2idea.android.limbo.jni.VMExecutor;
import com.max2idea.android.limbo.utils.FavOpenHelper;
import com.max2idea.android.limbo.utils.FileInstaller;
import com.max2idea.android.limbo.utils.FileUtils;
import com.max2idea.android.limbo.utils.Machine;
import com.max2idea.android.limbo.utils.MachineOpenHelper;
import com.mopub.mobileads.MoPubView;
import com.mopub.mobileads.MoPubView.OnAdLoadedListener;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class LimboActivity extends Activity {

    private static Installer a;
    public String TAG = "LIMBO";
    public View parent;
    public TextView mOutput;
    private boolean vmStarted = false;
    public static Activity activity = null;
    public static VMExecutor vmexecutor;
    public boolean userPressedCPU = false;
    public boolean userPressedMachine = false;
    public boolean userPressedRAM = false;
    public boolean userPressedCDROM = false;
    protected boolean userPressedHDCfg = false;
    protected boolean userPressedSndCfg = false;
    protected boolean userPressedVGACfg = false;
    protected boolean userPressedNicCfg = false;
    protected boolean userPressedNetCfg = false;
    protected boolean userPressedBootDev = false;
    protected boolean userPressedFDB = false;
    protected boolean userPressedFDA = false;
    protected boolean userPressedHDB = false;
    protected boolean userPressedHDA = false;
    protected boolean userPressedACPI = false;
    protected boolean userPressedHPET = false;
    protected boolean userPressedUSBMouse = false;
    protected boolean userPressedSnapshot = false;
    protected boolean userPressedVNC = false;
    private static final int HELP = 0;
    private static final int QUIT = 1;
    private static final int INSTALL = 2;
    private ImageView mStatus;
    private EditText mDNS;
    private boolean timeQuit = false;
    private Object lockTime = new Object();
    private Object currStatus = "READY";
    private TextView mStatusText;
    private WakeLock mWakeLock;
    private WifiLock wlock;
    private final boolean enableAds = true;

    public void setUserPressed(boolean pressed) {
        userPressedCPU = pressed;
        userPressedMachine = pressed;
        userPressedRAM = pressed;
        userPressedCDROM = pressed;
        userPressedHDCfg = pressed;
        userPressedSndCfg = pressed;
        userPressedVGACfg = pressed;
        userPressedNicCfg = pressed;
        userPressedNetCfg = pressed;
        userPressedBootDev = pressed;
        userPressedFDB = pressed;
        userPressedFDA = pressed;
        userPressedHDB = pressed;
        userPressedHDA = pressed;
        userPressedACPI = pressed;
        userPressedHPET = pressed;
        userPressedUSBMouse = pressed;
        userPressedSnapshot = pressed;

    }
    // Generic Dialog box

    public static void UIAlert(String title, String body, Activity activity) {
        AlertDialog ad;
        ad = new AlertDialog.Builder(activity).create();
        ad.setTitle(title);
        ad.setMessage(body);
        ad.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        ad.show();
    }
    private String output;
    private Spinner mMachine;
    private Spinner mCPU;
    private Spinner mHDA;
    private Spinner mHDB;
    private Spinner mCD;
    private Spinner mFDA;
    private Spinner mFDB;
    private Spinner mRamSize;
    private Spinner mBootDevices;
    private Spinner mNetDevices;
    private Spinner mNetConfig;
    private Spinner mVGAConfig;
    private Spinner mSoundCardConfig;
    private Spinner mHDCacheConfig;
    private CheckBox mACPI;
    private CheckBox mHPET;
//	private CheckBox mSnapshot;
    private CheckBox mUSBmouse;
    private CheckBox mVNC;
    private Spinner mSnapshot;
    private Button mStart;
    private Button mStop;
    private Button mRestart;
    private Button mSave;
//	private Button mResume;
    private FavOpenHelper favDB;
    private MachineOpenHelper machineDB;
    //ADS
    private MoPubView mAdView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Const.NOT_ICS)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // Declare an instance variable for your MoPubView.

        activity = this;

        // DB init
        favDB = new FavOpenHelper(activity);
        machineDB = new MachineOpenHelper(activity);

        // Setup UI
        this.setContentView(R.layout.main);
        this.setupWidgets();
        this.enableOptions(false);

        this.populateMachines();
        this.populateCPUs();
        this.populateRAM();
        this.populateHD("hda");
        this.populateHD("hdb");
        this.populateCDRom("cd");
        this.populateFloppy("fda");
        this.populateFloppy("fdb");
        this.populateBootDevices();
        this.populateNet();
        this.populateNetDevices();
        this.populateVGA();
        this.populateSoundcardConfig();
        this.populateHDCacheConfig();
        this.populateSnapshot();

        if (enableAds) {
            setupAds();
        }

        execTimeListener();

        if (this.isFirstLaunch()) {
            onFirstLaunch();
        }


//        acquireLocks();
    }

    public void onFirstLaunch() {
        this.onHelp();
    }

    static protected boolean isFirstLaunch() {
        PackageInfo pInfo = null;

        try {
            pInfo = activity.getPackageManager().getPackageInfo(activity.getClass().getPackage().getName(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean firstTime = prefs.getBoolean("firstTime"+pInfo.versionName, true);
//        UIUtils.log("Getting First time: " + firstTime);
        return firstTime;
    }

    static protected void setFirstLaunch() {
        PackageInfo pInfo = null;

        try {
            pInfo = activity.getPackageManager().getPackageInfo(activity.getClass().getPackage().getName(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("firstTime"+pInfo.versionName, false);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }

    static private void install() {
        progDialog = ProgressDialog.show(activity, "Please Wait", "Installing Files...", true);
        a = new Installer();
        a.execute();
    }
    private static final String WAKELOCK_KEY = "LIMBO";

    private void releaseLocks() {
        if (mWakeLock != null
                && mWakeLock.isHeld()) {
            Log.v(TAG, "Release wake lock...");
            mWakeLock.release();
        }
        if (wlock != null
                && wlock.isHeld()) {
            Log.v(TAG, "Release wifi lock...");
            wlock.release();
        }
    }

    private void acquireLocks() {
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,
                    WAKELOCK_KEY);
            mWakeLock.setReferenceCounted(false);

        }

        //Creates a new WifiLock by getting the WifiManager as a service.
//This object creates a lock tagged "lock".
        if (wlock == null) {
            WifiManager wmanager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
            wlock = wmanager.createWifiLock("lock");
            wlock.setReferenceCounted(false);
        }
        if (!mWakeLock.isHeld()) {
            Log.v(TAG, "Acquire wake lock...");
            mWakeLock.acquire();
        }
        if (!wlock.isHeld()) {
            Log.v(TAG, "Acquire wifi lock...");
            wlock.acquire();
        }
    }

    private static class Installer extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            //Get files from last dir
            onInstall();

            try {
                progDialog.dismiss();
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void test) {
//            Toast.makeText(activity, "Setting Google DNS servers in /sdcard/limbo/etc/resolv.conf.\n You can modify resolv.conf to use your own DNS server if you wish.", Toast.LENGTH_LONG).show();
        }
    }
    // Define the Handler that receives messages from the thread and update the
    // progress
    public Handler handler = new Handler() {
        @Override
        public synchronized void handleMessage(Message msg) {
            Bundle b = msg.getData();
            Integer messageType = (Integer) b.get("message_type");

            if (messageType != null && messageType == Const.VM_PAUSED) {
                // Show a progress while saving
                Toast.makeText(activity, "VM Paused", Toast.LENGTH_LONG).show();

            }
            if (messageType != null && messageType == Const.VM_RESUMED) {
                Toast.makeText(activity, "VM Resumed", Toast.LENGTH_LONG)
                        .show();
            }
            if (messageType != null && messageType == Const.VM_STARTED) {
                if (!vmStarted) {
                    Toast.makeText(activity, "VM Started\nDon't forget to Save/Load State so you don't have to boot again!", Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(activity, "VM Resumed", Toast.LENGTH_LONG)
                            .show();
                }
                enableDevOptions(false);
                mStart.setText("Resume");
                vmStarted = true;
            }
            if (messageType != null && messageType == Const.VM_STOPPED) {
                Toast.makeText(activity, "VM Shutdown", Toast.LENGTH_LONG)
                        .show();
                mStart.setText("Start");
                vmStarted = false;
            }
            if (messageType != null && messageType == Const.VM_RESTARTED) {
                Toast.makeText(activity, "VM Reset", Toast.LENGTH_LONG).show();
            }
            if (messageType != null && messageType == Const.VM_SAVED) {
                Toast.makeText(activity, "VM Saved", Toast.LENGTH_LONG).show();
            }
            if (messageType != null && messageType == Const.VM_NO_QCOW2) {
                Toast.makeText(activity, "Couldn't find a QCOW2 image\nPlease attach an HDA or HDB image first!", Toast.LENGTH_LONG).show();
            }
            if (messageType != null && messageType == Const.VM_NOTRUNNING) {
                Toast.makeText(activity, "VM not Running", Toast.LENGTH_SHORT)
                        .show();
            }
            if (messageType != null && messageType == Const.VM_CREATED) {
                String machineValue = (String) b.get("machine_name");
                currMachine = new Machine(machineValue);
                machineDB.insertMachine(currMachine);
                machineDB.update(currMachine, MachineOpenHelper.CPU, "pentium3");
                machineDB.update(currMachine, MachineOpenHelper.MEMORY, "64");
                Toast.makeText(activity, "VM Created: " + machineValue,
                        Toast.LENGTH_SHORT).show();
                populateMachines();
                setMachine(machineValue);

            }
            if (messageType != null && messageType == Const.IMG_CREATED) {
                String hdValue = (String) b.get("hd");
                String imageValue = (String) b.get("image_name");
                progDialog.dismiss();
                Toast.makeText(activity, "Image Created: " + imageValue + ".qcow2",
                        Toast.LENGTH_SHORT).show();
                setDriveAttr(hdValue, Const.machinedir + currMachine.machinename + "/" + imageValue + ".qcow2");


            }
            if (messageType != null && messageType == Const.SNAPSHOT_CREATED) {

                String imageValue = (String) b.get("snapshot_name");
                savevm(imageValue);

            }
            if (messageType != null && messageType == Const.VNC_PASSWORD) {

                String imageValue = (String) b.get("vnc_passwd");


            }
            if (messageType != null && messageType == Const.UIUTILS_SHOWALERT_HELP) {
                String title = (String) b.get("title");
                String body = (String) b.get("body");
                UIAlertHelp(title, body, activity);
            }
            if (messageType != null && messageType == Const.UIUTILS_SHOWALERT_HTML) {
                String title = (String) b.get("title");
                String body = (String) b.get("body");
                UIAlertHtml(title, body, activity);
            }
            if (messageType != null && messageType == Const.STATUS_CHANGED) {
                String status_changed = (String) b.get("status_changed");
                if (status_changed.equals("RUNNING")) {
                    mStatus.setImageResource(R.drawable.btn_radio_on);
                    mStatusText.setText("Running");
                } else if (status_changed.equals("READY") || status_changed.equals("STOPPED")) {
                    mStatus.setImageResource(R.drawable.btn_radio_off);
                    mStatusText.setText("Ready");
                } else if (status_changed.equals("SAVING")) {
                    mStatus.setImageResource(R.drawable.btn_radio_on_selected);
                    mStatusText.setText("Saving State");
                }
            }
        }
    };

    public static void UIAlertHelp(String title, String html, final Activity activity) {

        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        WebView webview = new WebView(activity);
        webview.setBackgroundColor(Color.BLACK);
        webview.loadData("<font color=\"FFFFFF\">" + html + " </font>", "text/html", "UTF-8");
        alertDialog.setView(webview);

//        alertDialog.setMessage(body);
        alertDialog.setButton("I Acknowledge", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (isFirstLaunch()) {
                    install();
                }
                setFirstLaunch();
                return;
            }
        });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (isFirstLaunch()) {
                    if (activity.getParent() != null) {
                        activity.getParent().finish();
                    } else {
                        activity.finish();
                    }
                }
            }
        });
        alertDialog.show();
    }

    public static void UIAlertHtml(String title, String html, Activity activity) {

        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        WebView webview = new WebView(activity);
        webview.setBackgroundColor(Color.BLACK);
        webview.loadData("<font color=\"FFFFFF\">" + html + " </font>", "text/html", "UTF-8");
        alertDialog.setView(webview);

//        alertDialog.setMessage(body);
        alertDialog.setButton("I Acknowledge", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alertDialog.show();
    }

    private void createImage(String imageValue, String hdValue, int imgSize, boolean prealloc) {
        String image = imageValue + ".qcow2";
        String dir = Const.machinedir + currMachine.machinename + "/";
        File dirf = new File(dir);
        if (!dirf.exists()) {
            dirf.mkdir();
        } else if (dirf.exists() && !dirf.isDirectory()) {
            Toast.makeText(activity, "Could not create dir: " + dir,
                    Toast.LENGTH_SHORT).show();
        }
        String imagef = dir + image;
        ImageCreator i = new ImageCreator(imagef, imgSize, prealloc ? 1 : 0);
        i.createImage();
    }

    public void enableOptions(boolean flag) {

        this.mCPU.setEnabled(flag); // Disabled for now

        this.mRamSize.setEnabled(flag); // Disabled for now

        this.mHDA.setEnabled(flag); // Disabled for now

        this.mHDB.setEnabled(flag); // Disabled for now

        this.mCD.setEnabled(flag); // Disabled for now

        this.mFDA.setEnabled(flag); // Disabled for now

        this.mFDB.setEnabled(flag); // Disabled for now

        this.mBootDevices.setEnabled(flag); // Disabled for now

        this.mNetConfig.setEnabled(flag); // Disabled for now

		this.mNetDevices.setEnabled(flag); // Enabled conditionally for now

        this.mVGAConfig.setEnabled(flag); // Disabled for now

//        this.mSoundCardConfig.setEnabled(flag); // Disabled for now

//        this.mHDCacheConfig.setEnabled(flag); // Disabled for now

        this.mACPI.setEnabled(flag); // Disabled for now

        this.mHPET.setEnabled(flag); // Disabled for now

        this.mUSBmouse.setEnabled(flag); // Disabled for now

//        this.mSnapshot.setEnabled(true); // Disabled for now

    }

    // Generic Message to update UI
    public static void sendHandlerMessage(Handler handler, int message_type,
            String message_var, String message_value) {
        Message msg1 = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("message_type", message_type);
        b.putString(message_var, message_value);
        msg1.setData(b);
        handler.sendMessage(msg1);
    }

    public static void sendHandlerMessage(Handler handler, int message_type, String[] message_var, String[] message_value) {
        Message msg1 = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("message_type", message_type);
        for (int i = 0; i < message_var.length; i++) {
            b.putString(message_var[i], message_value[i]);
        }
        msg1.setData(b);
        handler.sendMessage(msg1);
    }
    static private ProgressDialog progDialog;

    // Another Generic Messanger
    public static void sendHandlerMessage(Handler handler, int message_type) {
        Message msg1 = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("message_type", message_type);
        msg1.setData(b);
        handler.sendMessage(msg1);
    }

    private void onHelp() {
        PackageInfo pInfo = null;


        try {
            pInfo = getPackageManager().getPackageInfo(getClass().getPackage().getName(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        FileUtils fileutils = new FileUtils();
        try {
            showAlertHelp(Const.APP_NAME + " v" + pInfo.versionName, fileutils.LoadFile(activity, "HELP", false), handler);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void showAlertHelp(String title, String message, Handler handler) {
        Message msg1 = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("message_type", Const.UIUTILS_SHOWALERT_HELP);
        b.putString("title", title);
        b.putString("body", message);
        msg1.setData(b);
        handler.sendMessage(msg1);
    }

    public static void showAlertHtml(String title, String message, Handler handler) {
        Message msg1 = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("message_type", Const.UIUTILS_SHOWALERT_HTML);
        b.putString("title", title);
        b.putString("body", message);
        msg1.setData(b);
        handler.sendMessage(msg1);
    }

    private void exit() {
        onStopButton(true);
    }

    private void enableDevOptions(boolean b) {

        //Everything Except removable devices
        this.mMachine.setEnabled(b);
        this.mCPU.setEnabled(b);
        this.mRamSize.setEnabled(b);
        mHDA.setEnabled(b);
        mHDB.setEnabled(b);
        this.mCD.setEnabled(true);
        this.mFDA.setEnabled(true);
        this.mFDB.setEnabled(true);

        this.mBootDevices.setEnabled(b);
        this.mNetConfig.setEnabled(b);
        this.mNetDevices.setEnabled(b);
        this.mVGAConfig.setEnabled(b);
//        this.mSoundCardConfig.setEnabled(b);
//        this.mHDCacheConfig.setEnabled(b);

        this.mACPI.setEnabled(b);
        this.mHPET.setEnabled(b);
        this.mUSBmouse.setEnabled(b);

//        this.mVNC.setEnabled(b);
        this.mSnapshot.setEnabled(b);

    }

    private void setupAds() {
        mAdView = (MoPubView) findViewById(R.id.adview);
        FileUtils fileutils = new FileUtils();
        String adunit = "";
        try {
            adunit = fileutils.LoadFile(activity, "ADUNIT", false);
        } catch (IOException ex) {
            Logger.getLogger(LimboActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
        mAdView.setAdUnitId(adunit); // Enter your Ad Unit ID from www.mopub.com
        mAdView.loadAd();

        mAdView.setOnAdLoadedListener(new OnAdLoadedListener() {
            public void OnAdLoaded(MoPubView mpv) {
//                Toast.makeText(getApplicationContext(),
//                        "Ad successfully loaded.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    static private void onInstall() {
        FileInstaller.installFiles(activity);
    }

    public class AutoScrollView extends ScrollView {

        public AutoScrollView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public AutoScrollView(Context context) {
            super(context);
        }
    }
    public AutoScrollView mLyricsScroll;
    private ArrayAdapter cpuAdapter;
    private ArrayAdapter machineAdapter;
    private ArrayAdapter ramAdapter;
    private ArrayAdapter cdromAdapter;
    private ArrayAdapter vgaAdapter;
    private ArrayAdapter netAdapter;
    private ArrayAdapter bootDevAdapter;
    private ArrayAdapter hdCacheAdapter;
    private ArrayAdapter sndAdapter;
    private ArrayAdapter nicCfgAdapter;
    private boolean userPressedHDCacheCfg;
    private boolean userPressedSoundcardCfg;
    private ArrayAdapter fdaAdapter;
    private ArrayAdapter fdbAdapter;
    private ArrayAdapter hdaAdapter;
    private ArrayAdapter hdbAdapter;
    private ArrayAdapter snapshotAdapter;
	

    // Main event function
    // Retrives values from saved preferences
    private void onStartButton() {

        if (this.mMachine.getSelectedItemPosition() == 0) {
            Toast.makeText(getApplicationContext(),
                    "Select or Create a Virtual Machine first", Toast.LENGTH_LONG).show();
            return;
        }

        if (!vmStarted) {
            Thread tvm = new Thread(new Runnable() {
                public void run() {
                    startvm();
                }
            });
//            tvm.setPriority(Thread.MAX_PRIORITY);
            tvm.start();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(LimboActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
        Thread tvnc = new Thread(new Runnable() {
            public void run() {
                startvnc();

            }
        });
        


        // t.setPriority(Thread.MAX_PRIORITY);
        tvnc.start();
        
        Thread tdns = new Thread(new Runnable() {
            public void run() {
            	setDNSaddr();
            }
        });
        tdns.start();

    }

    private void setDNSaddr() {
    	Thread t = new Thread(new Runnable() {
        public void run() {
        	String dns_addr = mDNS.getText().toString();
            if ( dns_addr!= null && !dns_addr.equals("")) {
//            	Log.v("LimboDNS", "Setting DNS: " + dns_addr);
                vmexecutor.change_dns_addr();
            }
        }
    });
    // t.setPriority(Thread.MAX_PRIORITY);
    t.start();
		
	}
	private void onStopButton(boolean exit) {
        stopVM(exit);
    }

    private void onRestartButton() {

        // TODO: This probably has no effect
        Thread t = new Thread(new Runnable() {
            public void run() {
                restartvm();
            }
        });
        // t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    private void onSaveButton() {

        // TODO: This probably has no effect
        Thread t = new Thread(new Runnable() {
            public void run() {
                promptStateName(activity);
            }
        });
        // t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    private void onResumeButton() {

        // TODO: This probably has no effect
        Thread t = new Thread(new Runnable() {
            public void run() {
                resumevm();
            }
        });
        // t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    // Setting up the UI
    public void setupWidgets() {

        this.mStatus = (ImageView) findViewById(R.id.statusVal);
        this.mStatus.setImageResource(R.drawable.btn_radio_off);

        this.mStatusText = (TextView) findViewById(R.id.statusStr);

        this.mDNS = (EditText) findViewById(R.id.dnsval);
        this.mDNS.setFocusableInTouchMode(true);
        this.mDNS.setFocusable(true);
        this.mDNS.setText(SettingsManager.getDNSServer(activity));
        
        this.mMachine = (Spinner) findViewById(R.id.machineval);

        this.mCPU = (Spinner) findViewById(R.id.cpuval);
        this.mRamSize = (Spinner) findViewById(R.id.rammemval);
        this.mHDA = (Spinner) findViewById(R.id.hdimgval);
        this.mHDB = (Spinner) findViewById(R.id.hdbimgval);
        this.mCD = (Spinner) findViewById(R.id.cdromimgval);
        this.mFDA = (Spinner) findViewById(R.id.floppyimgval);
        this.mFDB = (Spinner) findViewById(R.id.floppybimgval);
        this.mBootDevices = (Spinner) findViewById(R.id.bootfromval);
        this.mNetConfig = (Spinner) findViewById(R.id.netcfgval);
        this.mNetDevices = (Spinner) findViewById(R.id.netDevicesVal);
        this.mVGAConfig = (Spinner) findViewById(R.id.vgacfgval);
        this.mSoundCardConfig = (Spinner) findViewById(R.id.soundcfgval);
        this.mSoundCardConfig.setEnabled(false); // Disabled for now
        this.mHDCacheConfig = (Spinner) findViewById(R.id.hdcachecfgval);
        this.mHDCacheConfig.setEnabled(false); // Disabled for now
        this.mACPI = (CheckBox) findViewById(R.id.acpival);
        this.mHPET = (CheckBox) findViewById(R.id.hpetval);
        this.mUSBmouse = (CheckBox) findViewById(R.id.usbmouseval);
        this.mVNC = (CheckBox) findViewById(R.id.vncexternalval); //No external connections
//        mVNC.setChecked(SettingsManager.getVNCAllowExternal(activity));
        
        this.mSnapshot = (Spinner) findViewById(R.id.snapshotval);

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        mStart = (Button) findViewById(R.id.startvm);
        mStart.setFocusableInTouchMode(true);
        mStart.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                onStartButton();

            }
        });

        mStop = (Button) findViewById(R.id.stopvm);
        mStop.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Log.v(TAG, "Stopping VM...");
                onStopButton(false);

            }
        });

        mRestart = (Button) findViewById(R.id.restartvm);
        mRestart.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Log.v(TAG, "Restarting VM...");
                onRestartButton();

            }
        });

//        mSave = (Button) findViewById(R.id.savevm);
//        mSave.setOnClickListener(new OnClickListener() {
//            public void onClick(View view) {
//                Log.v(TAG, "Restarting VM...");
//                onSaveButton();
//
//            }
//        });

//		mResume = (Button) findViewById(R.id.resumevm);
//		mResume.setOnClickListener(new OnClickListener() {
//
//			public void onClick(View view) {
//				Log.v(TAG, "Resuming VM...");
//				onResumeButton();
//
//			}
//		});

        mMachine.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                    View selectedItemView, int position, long id) {
                // your code here
//                Log.v(TAG, "Position " + position);
                if (position == 0) {
                } else if (position == 1) {
                    promptMachineName(activity);
//                    Log.v(TAG, "Promtp for Machine createion");
                } else {
                    String machine = (String) ((ArrayAdapter) mMachine
                            .getAdapter()).getItem(position);
//                    Log.v(TAG, "Machine selected: " + machine);
                    loadMachine(machine, "");
                    populateSnapshot();

                }
            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });

        mCPU.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                    View selectedItemView, int position, long id) {
                // your code here

                String cpu = (String) ((ArrayAdapter) mCPU.getAdapter())
                        .getItem(position);
//                Log.v(TAG, "Position " + position + " CPU = " + cpu
//                        + " userPressed = " + userPressedCPU);
                // SettingsManager.setLastCPU(activity,cpu);
                if (userPressedCPU) {
                    currMachine.cpu = cpu;
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.CPU, cpu);
                }
                userPressedCPU = true;
//                Log.v("CPU List", "reset userPressed = " + userPressedCPU);
            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
                userPressedCPU = true;
//                Log.v("CPU none", "reset userPressed = " + userPressedCPU);
            }
        });

        mRamSize.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                    View selectedItemView, int position, long id) {
                String ram = (String) ((ArrayAdapter) mRamSize.getAdapter())
                        .getItem(position);
//                Log.v(TAG, "Position " + position + " RAM = " + ram
//                        + " userPressed = " + userPressedRAM);
                // SettingsManager.setLastCPU(activity,cpu);
                if (userPressedRAM) {
                    currMachine.memory = Integer.parseInt(ram);
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.MEMORY, ram);
                }

                userPressedRAM = true;
//                Log.v("Ram list", "reset userPressed = " + userPressedRAM);

            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
                userPressedRAM = true;
//                Log.v("Ram none", "reset userPressed = " + userPressedRAM);
            }
        });

        mHDA.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                    View selectedItemView, int position, long id) {
                String hda = (String) ((ArrayAdapter) mHDA.getAdapter())
                        .getItem(position);
                if (userPressedHDA && position == 0) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.HDA, null);
                    currMachine.hda_img_path = hda;
                } else if (userPressedHDA && position == 1) {
                    promptImageName(activity, "hda");
                    Log.v(TAG, "Promtp for Image createion");
                } else if (userPressedHDA && position == 2) {
                    browse("hda");
                } else if (userPressedHDA && position > 2) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.HDA, hda);
                    currMachine.hda_img_path = hda;
                }
                if (currStatus.equals("RUNNING")) {
                    vmexecutor.change_dev("ide0-hd0", currMachine.hda_img_path);
                }
                userPressedHDA = true;
//                Log.v("HDA List", "reset userPressed = " + userPressedHDA);
            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });

        mHDB.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                    View selectedItemView, int position, long id) {
                String hdb = (String) ((ArrayAdapter) mHDB.getAdapter())
                        .getItem(position);
//                Log.v(TAG, "Position " + position + " HDB = " + hdb
//                        + " userPressed = " + userPressedHDB);
                // SettingsManager.setLastCPU(activity,cpu);
                if (userPressedHDB && position == 0) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.HDB, null);
                    currMachine.hdb_img_path = hdb;
                } else if (userPressedHDB && position == 1) {
                    promptImageName(activity, "hdb");
//                    Log.v(TAG, "Promtp for Image createion");

                } else if (userPressedHDB && position == 2) {
                    browse("hdb");
                } else if (userPressedHDB && position > 2) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.HDB, hdb);
                    currMachine.hdb_img_path = hdb;
                }
                if (currStatus.equals("RUNNING")) {
                    vmexecutor.change_dev("ide0-hd1", currMachine.hdb_img_path);
                }

                userPressedHDB = true;
//                Log.v("HDB List", "reset userPressed = " + userPressedHDB);
            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });

        mSnapshot.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                    View selectedItemView, int position, long id) {
                String snapshot_name = (String) ((ArrayAdapter) mSnapshot.getAdapter())
                        .getItem(position);
                if (userPressedSnapshot && position == 0) {
                    currMachine.snapshot_name = "";
                    userPressedSnapshot = false;
                    loadMachine(currMachine.machinename, currMachine.snapshot_name);
                    mStart.setText("Start");
                    enableDevOptions(true);
                    mSnapshot.setEnabled(true);
                } else if (userPressedSnapshot && position > 0) {
                    currMachine.snapshot_name = snapshot_name;
                    userPressedSnapshot = false;
                    loadMachine(currMachine.machinename, currMachine.snapshot_name);
                    mStart.setText("Resume");
                    enableOptions(false);
                    mSnapshot.setEnabled(true);
                } else {
                    userPressedSnapshot = true;
                }


//                Log.v("Snapshot List", "reset userPressed = " + userPressedSnapshot);
            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });

        mCD.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                    View selectedItemView, int position, long id) {
                String cd = (String) ((ArrayAdapter) mCD.getAdapter())
                        .getItem(position);
                if (userPressedCDROM && position == 0) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.CDROM, null);
                    currMachine.cd_iso_path = null;
                } else if (userPressedCDROM && position == 1) {
                    browse("cd");
                } else if (userPressedCDROM && position > 1) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.CDROM, cd);
                    currMachine.cd_iso_path = cd;
                    //TODO: If Machine is running eject and set floppy img
                }
                if (currStatus.equals("RUNNING") && position > 1) {
                    vmexecutor.change_dev("ide1-cd0", currMachine.cd_iso_path);
                }
                userPressedCDROM = true;
            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });

        mFDA.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                    View selectedItemView, int position, long id) {
                String fda = (String) ((ArrayAdapter) mFDA.getAdapter())
                        .getItem(position);
                if (userPressedFDA && position == 0) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.FDA, null);
                    currMachine.fda_img_path = null;
                } else if (userPressedFDA && position == 1) {
                    browse("fda");
                } else if (userPressedFDA && position > 1) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.FDA, fda);
                    currMachine.fda_img_path = fda;
                    //TODO: If Machine is running eject and set floppy img
                }
                if (currStatus.equals("RUNNING") && position > 1) {
                    vmexecutor.change_dev("floppy0", currMachine.fda_img_path);
                }

                userPressedFDA = true;
            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });

        mFDB.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                    View selectedItemView, int position, long id) {
                String fdb = (String) ((ArrayAdapter) mFDB.getAdapter())
                        .getItem(position);
                if (userPressedFDB && position == 0) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.FDB, null);
                    currMachine.fdb_img_path = null;
                } else if (userPressedFDB && position == 1) {
                    browse("fdb");
                } else if (userPressedFDB && position > 1) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.FDB, fdb);
                    currMachine.fdb_img_path = fdb;
                    //TODO: If Machine is running eject and set floppy img
                }
                if (currStatus.equals("RUNNING") && position > 1) {
                    vmexecutor.change_dev("floppy1", currMachine.fdb_img_path);
                }
                userPressedFDB = true;
            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });

        mBootDevices.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                    View selectedItemView, int position, long id) {
                String bootDev = (String) ((ArrayAdapter) mBootDevices
                        .getAdapter()).getItem(position);
//                Log.v(TAG, "Position " + position + " bootDev = " + bootDev
//                        + " userPressed = " + userPressedBootDev);
                // SettingsManager.setLastCPU(activity,cpu);
                if (userPressedBootDev) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.BOOT_CONFIG, bootDev);

                    currMachine.bootdevice = bootDev;

                }

                userPressedBootDev = true;
//                Log.v("BootDev List", "reset userPressed = "
//                        + userPressedBootDev);

            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });

        this.mNetConfig.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                    View selectedItemView, int position, long id) {
                String netfcg = (String) ((ArrayAdapter) mNetConfig
                        .getAdapter()).getItem(position);
//                Log.v(TAG, "Position " + position + " netfcg = " + netfcg
//                        + " userPressed = " + userPressedNetCfg);
                // SettingsManager.setLastCPU(activity,cpu);
                if (userPressedNetCfg) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.NET_CONFIG, netfcg);

                    currMachine.net_cfg = netfcg;
                }
                if (position > 0) {
                    mNetDevices.setEnabled(true);
                } else {
                    mNetDevices.setEnabled(false);
                }

                userPressedNetCfg = true;
//                Log.v("Net CFG List", "reset userPressed = "
//                        + userPressedNetCfg);

            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });

        this.mNetDevices.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                    View selectedItemView, int position, long id) {
                String niccfg = (String) ((ArrayAdapter) mNetDevices
                        .getAdapter()).getItem(position);
//                Log.v(TAG, "Position " + position + " nicfcg = "
//                        + niccfg + " userPressed = "
//                        + userPressedNicCfg);
                // SettingsManager.setLastCPU(activity,cpu);
                if (position < 0) {
                    Toast.makeText(getApplicationContext(),
                            "Not a valid card, using ne2k_pci instead", Toast.LENGTH_LONG).show();
                    userPressedNicCfg = true;
                    mNetDevices.setSelection(4);
                    return;
                }
                if (userPressedNicCfg) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.NIC_CONFIG, niccfg);
                    currMachine.nic_driver = niccfg;
                }

                userPressedNicCfg = true;
//                Log.v("BootDev List", "reset userPressed = "
//                        + userPressedNicCfg);

            }

            public void onNothingSelected(
                    final AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });

        mVGAConfig.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                    View selectedItemView, int position, long id) {
                // your code here
                String vgacfg = (String) ((ArrayAdapter) mVGAConfig
                        .getAdapter()).getItem(position);
//                Log.v(TAG, "Position " + position + " vgafcg = " + vgacfg
//                        + " userPressed = " + userPressedVGACfg);
                // SettingsManager.setLastCPU(activity,cpu);
                if (userPressedVGACfg) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.VGA, vgacfg);

                    currMachine.vga_type = vgacfg;

                }

                userPressedVGACfg = true;
//                Log.v("BootDev List", "reset userPressed = "
//                        + userPressedVGACfg);

            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });

        this.mSoundCardConfig
                .setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                    View selectedItemView, int position, long id) {
                String sndcfg = (String) ((ArrayAdapter) mSoundCardConfig
                        .getAdapter()).getItem(position);
//                Log.v(TAG, "Position " + position + " sndfcg = "
//                        + sndcfg + " userPressed = "
//                        + userPressedSndCfg);
                // SettingsManager.setLastCPU(activity,cpu);
                if (userPressedSndCfg) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.SOUNDCARD_CONFIG, sndcfg);

                    currMachine.soundcard = sndcfg;

                }

                userPressedSndCfg = true;
//                Log.v("Snd List", "reset userPressed = "
//                        + userPressedSndCfg);

            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });

        mHDCacheConfig.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                    View selectedItemView, int position, long id) {
                String hdcfg = (String) ((ArrayAdapter) mHDCacheConfig
                        .getAdapter()).getItem(position);
//                Log.v(TAG, "Position " + position + " sndfcg = " + hdcfg
//                        + " userPressed = " + userPressedHDCfg);
                // SettingsManager.setLastCPU(activity,cpu);
                if (userPressedHDCfg) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.HDCACHE_CONFIG, hdcfg);

                    currMachine.hd_cache = hdcfg;

                }

                userPressedHDCfg = true;
//                Log.v("HDCache List", "reset userPressed = " + userPressedHDCfg);

            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });

        mACPI.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton viewButton, boolean isChecked) {


//                Log.v(TAG, "ACPI checked: " + isChecked + " userPressed = " + userPressedACPI);
                // SettingsManager.setLastCPU(activity,cpu);
                if (userPressedACPI) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.DISABLE_ACPI, ((isChecked ? 1 : 0) + ""));
                }

                userPressedACPI = true;
//                Log.v("ACPI ", "reset userPressed = " + userPressedACPI);

            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });



        mHPET.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton viewButton, boolean isChecked) {


//                Log.v(TAG, "ACPI checked: " + isChecked + " userPressed = " + userPressedHPET);
                // SettingsManager.setLastCPU(activity,cpu);
                if (userPressedHPET) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.DISABLE_HPET, ((isChecked ? 1 : 0) + ""));
                }

                userPressedHPET = true;
//                Log.v("ACPI ", "reset userPressed = " + userPressedHPET);

            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });

        this.mUSBmouse.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton viewButton, boolean isChecked) {


//                Log.v(TAG, "ACPI checked: " + isChecked + " userPressed = " + userPressedUSBMouse);
                // SettingsManager.setLastCPU(activity,cpu);
                if (userPressedUSBMouse) {
                    int ret = machineDB.update(currMachine,
                            MachineOpenHelper.ENABLE_USBMOUSE, ((isChecked ? 1 : 0) + ""));
                }

                userPressedUSBMouse = true;
//                Log.v("ACPI ", "reset userPressed = " + userPressedUSBMouse);

            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });

		mDNS.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				SettingsManager.setDNSServer(activity, mDNS.getText().toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
//				Log.d("seachScreen", "beforeTextChanged");
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
//				Log.d("seachScreen", "onTextChanged");
			}
		});
            
//
        mVNC.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton viewButton, boolean isChecked) {


                if (isChecked) {
                    promptVNC(activity);
                } else {
                    vnc_passwd = null;
                    vnc_allow_external = 0;
//                    SettingsManager.setVNCAllowExternal(activity, false);
                }

            }

            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
//                Log.v(TAG, "Nothing selected");
            }
        });
    }
    private String vnc_passwd = null;
    private int vnc_allow_external = 0;

    //    This is easier: traverse the interfaces and get the local IPs
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress.getHostAddress().toString().contains(".")) {
                        Log.v("Internal ip", inetAddress.getHostAddress().toString());
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Internal IP", ex.toString());
        }
        return null;
    }

    public void promptVNC(final Activity activity) {
        final AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle("Server: " + this.getLocalIpAddress() + ":" + "5901"
                + " VNC is not secure even with a password! Make sure you're on a private network!");
        EditText passwdView = new EditText(activity);
        passwdView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwdView.setHint("Password");
        passwdView.setEnabled(true);
        passwdView.setVisibility(View.VISIBLE);
        passwdView.setId(11111);
        passwdView.setSingleLine();
        alertDialog.setView(passwdView);
        final Handler handler = this.handler;

        // alertDialog.setMessage(body);
        alertDialog.setButton("Set", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                // UIUtils.log("Searching...");
                EditText a = (EditText) alertDialog.findViewById(11111);

                if (a.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "Password cannot be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    sendHandlerMessage(handler, Const.VNC_PASSWORD, "vnc_passwd", "passwd");
                    vnc_passwd = a.getText().toString();
                    vnc_allow_external = 1;
//                    SettingsManager.setVNCAllowExternal(activity, true);
                }

            }
        });
        alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                vnc_passwd = null;
                vnc_allow_external = 0;
//                SettingsManager.setVNCAllowExternal(activity, false);
                return;
            }
        });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mVNC.setChecked(false);
                vnc_passwd = null;
                vnc_allow_external = 0;
            }
        });
        alertDialog.show();

    }

    private void loadMachine(String machine, String snapshot) {
        // TODO Auto-generated method stub

//        Log.v(TAG, "Loading attribs for machine: " + machine);

        // Load machine from DB
        this.currMachine = machineDB.getMachine(machine, snapshot);

        this.setCPU(currMachine.cpu, false);
        this.setRAM(currMachine.memory, false);
        setCDROM(currMachine.cd_iso_path, false);
        this.setFDA(currMachine.fda_img_path, false);
        this.setFDB(currMachine.fdb_img_path, false);
        this.setHDA(currMachine.hda_img_path, false);
        this.setHDB(currMachine.hdb_img_path, false);

        this.setBootDevice(currMachine.bootdevice, false);
        this.setNetCfg(currMachine.net_cfg, false);
        this.setNicDevice(currMachine.nic_driver, false);
        this.setVGA(currMachine.vga_type, false);
        this.setHDCache(currMachine.hd_cache, false);
        this.setSoundcard(currMachine.soundcard, false);

        this.userPressedACPI = false;
        this.mACPI.setChecked(currMachine.disableacpi == 1 ? true : false);
        this.userPressedHPET = false;
        this.mHPET.setChecked(currMachine.disablehpet == 1 ? true : false);
        this.userPressedUSBMouse = false;
        this.mUSBmouse.setChecked(currMachine.usbmouse == 1 ? true : false);
        if (this.currMachine == null) {
            return;
        }
        this.setUserPressed(true);
        enableOptions(true);


    }
    static Machine currMachine = null;

    public void promptMachineName(final Activity activity) {
        final AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle("Machine Name");
        EditText searchView = new EditText(activity);
        searchView.setEnabled(true);
        searchView.setVisibility(View.VISIBLE);
        searchView.setId(201012010);
        searchView.setSingleLine();
        alertDialog.setView(searchView);
        final Handler handler = this.handler;

        // alertDialog.setMessage(body);
        alertDialog.setButton("Create", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                // UIUtils.log("Searching...");
                EditText a = (EditText) alertDialog.findViewById(201012010);
                sendHandlerMessage(handler, Const.VM_CREATED, "machine_name", a
                        .getText().toString());
                return;
            }
        });
        alertDialog.show();

    }

    public void promptImageName(final Activity activity, String hd) {
        final String hd_string = hd;
        final AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle("Image Name");

        RelativeLayout mLayout = new RelativeLayout(this);
        mLayout.setId(12222);

        EditText imageNameView = new EditText(activity);
        imageNameView.setEnabled(true);
        imageNameView.setVisibility(View.VISIBLE);
        imageNameView.setId(201012010);
        imageNameView.setSingleLine();
        RelativeLayout.LayoutParams searchViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        mLayout.addView(imageNameView, searchViewParams);


        final Spinner size = new Spinner(this);
        RelativeLayout.LayoutParams setPlusParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        size.setId(201012044);

        String[] arraySpinner = new String[10];
        for (int i = 0; i < arraySpinner.length; i++) {

            if (i >= 5) {
                arraySpinner[i] = ((i - 4) * 1000) + "MB";
            } else {
                arraySpinner[i] = ((i + 1) * 100) + "MB";
            }

        }
        ArrayAdapter sizeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arraySpinner);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        size.setAdapter(sizeAdapter);
        setPlusParams.addRule(RelativeLayout.BELOW, imageNameView.getId());
        mLayout.addView(size, setPlusParams);

        final TextView preallocText = new TextView(this);
        preallocText.setText("Preallocate? ");
        preallocText.setTextSize(15);
        RelativeLayout.LayoutParams preallocTParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        preallocTParams.addRule(RelativeLayout.BELOW, size.getId());
        mLayout.addView(preallocText, preallocTParams);
        preallocText.setId(64512044);

        final CheckBox prealloc = new CheckBox(this);
        RelativeLayout.LayoutParams preallocParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        preallocParams.addRule(RelativeLayout.BELOW, size.getId());
        preallocParams.addRule(RelativeLayout.RIGHT_OF, preallocText.getId());
        preallocParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, preallocText.getId());
        mLayout.addView(prealloc, preallocParams);
        prealloc.setId(64512344);

        alertDialog.setView(mLayout);

        final Handler handler = this.handler;

        // alertDialog.setMessage(body);
        alertDialog.setButton("Create", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                int sizeSel = size.getSelectedItemPosition();
                int sizeInt = 100;
                if (sizeSel >= 5) {
                    sizeInt = ((sizeSel - 4) * 1000);
                } else {
                    sizeInt = ((sizeSel + 1) * 100);
                }


                // UIUtils.log("Searching...");
                EditText a = (EditText) alertDialog.findViewById(201012010);
                progDialog = ProgressDialog.show(activity, "Please Wait", "Creating HD Image...", true);
                CreateImage createImg = new CreateImage(a.getText().toString(), hd_string, sizeInt, prealloc.isChecked());
                createImg.execute();

            }
        });
        alertDialog.show();

    }

    private class CreateImage extends AsyncTask<Void, Void, Void> {

        String imageName;
        String hd;
        int size;
        boolean prealloc;

        private CreateImage(String imageName, String hd_string, int sizeInt, boolean prealloc) {
            this.imageName = imageName;
            this.hd = hd_string;
            this.size = sizeInt;
            this.prealloc = prealloc;

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            createImage(imageName, hd, size, prealloc);
            return null;
        }

        @Override
        protected void onPostExecute(Void test) {
            try {
                sendHandlerMessage(handler, Const.IMG_CREATED,
                        new String[]{"image_name", "hd"},
                        new String[]{imageName, hd});
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true; // return
        }

        return false;
    }

    public void promptStateName(final Activity activity) {
        final AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle("Snapshot/State Name");
        EditText searchView = new EditText(activity);
        searchView.setEnabled(true);
        searchView.setVisibility(View.VISIBLE);
        searchView.setId(201012010);
        searchView.setSingleLine();
        alertDialog.setView(searchView);
        final Handler handler = this.handler;

        // alertDialog.setMessage(body);
        alertDialog.setButton("Create", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                // UIUtils.log("Searching...");
                EditText a = (EditText) alertDialog.findViewById(201012010);
                sendHandlerMessage(handler, Const.SNAPSHOT_CREATED,
                        new String[]{"snapshot_name"},
                        new String[]{a.getText().toString()});
                return;
            }
        });
        alertDialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.v(TAG, "RET CODE: " + resultCode);
        if (resultCode == Const.FILEMAN_RETURN_CODE) {
            // Read from activity
            String currDir = SettingsManager.getLastDir(this);
            String file = "";
            String fileType = "";
            Bundle b = data.getExtras();
            fileType = b.getString("fileType");
            file = b.getString("file");
            currDir = b.getString("currDir");
//            Log.v(TAG, "Got New Dir: " + currDir);
//            Log.v(TAG, "Got File Type: " + fileType);
//            Log.v(TAG, "Got New File: " + file);
            if (currDir != null && !currDir.trim().equals("")) {
                SettingsManager.setLastDir(this, currDir);
            }
            setDriveAttr(fileType, file);

        } else if (resultCode == Const.VNC_RESET_RESULT_CODE) {
            Toast.makeText(getApplicationContext(), "Resizing Display", Toast.LENGTH_SHORT).show();
            this.startvnc();


        }

        // Check if says open

    }

    private void setDriveAttr(String fileType, String file) {
        // TODO Auto-generated method stub
        this.addDriveToList(file, fileType);
        if (fileType != null && fileType.startsWith("hd") && file != null
                && !file.trim().equals("")) {

            if (fileType.startsWith("hda")) {
                int ret = machineDB.update(currMachine, MachineOpenHelper.HDA, file);
                if (this.hdaAdapter.getPosition(file) < 0) {
                    this.hdaAdapter.add(file);
                }
                this.setHDA(file, false);
            } else if (fileType.startsWith("hdb")) {
                int ret = machineDB.update(currMachine, MachineOpenHelper.HDB, file);
                if (this.hdbAdapter.getPosition(file) < 0) {
                    this.hdbAdapter.add(file);
                }
                this.setHDB(file, false);
            }
        } else if (fileType != null && fileType.startsWith("cd")
                && file != null && !file.trim().equals("")) {
            int ret = machineDB.update(currMachine, MachineOpenHelper.CDROM, file);
            if (this.cdromAdapter.getPosition(file) < 0) {
                this.cdromAdapter.add(file);
            }
            setCDROM(file, false);
        } else if (fileType != null && fileType.startsWith("fd")
                && file != null && !file.trim().equals("")) {
            if (fileType.startsWith("fda")) {
                int ret = machineDB.update(currMachine, MachineOpenHelper.FDA, file);
                if (this.fdaAdapter.getPosition(file) < 0) {
                    this.fdaAdapter.add(file);
                }
                this.setFDA(file, false);
            } else if (fileType.startsWith("fdb")) {
                int ret = machineDB.update(currMachine, MachineOpenHelper.FDB, file);
                if (this.fdbAdapter.getPosition(file) < 0) {
                    this.fdbAdapter.add(file);
                }
                this.setFDB(file, false);
            }
        }
        int res = this.mHDA.getSelectedItemPosition();
        if (res == 1) {
            this.mHDA.setSelection(0);
        }
        res = this.mHDB.getSelectedItemPosition();
        if (res == 1) {
            this.mHDB.setSelection(0);
        }

        res = this.mCD.getSelectedItemPosition();
        if (res == 1) {
            this.mCD.setSelection(0);
        }

        res = this.mFDA.getSelectedItemPosition();
        if (res == 1) {
            this.mFDA.setSelection(0);
        }

        res = this.mFDB.getSelectedItemPosition();
        if (res == 1) {
            this.mFDB.setSelection(0);

        }

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
//        this.releaseLocks();
        super.onDestroy();
        this.stopTimeListener();
        if (mAdView != null) {
            mAdView.destroy();
        }

    }
    private VncDatabase database;
    private ConnectionBean selected;

    private void startvnc() {
//        updateSelectedFromView();
//        saveAndWriteRecent();
//        Intent intent = new Intent(this, VncCanvasActivity.class);

        Thread t = new Thread(new Runnable() {
            public void run() {
                if (vnc_passwd != null && !vnc_passwd.equals("")) {
                    vmexecutor.change_vnc_password();
                }
            }
        });
        // t.setPriority(Thread.MAX_PRIORITY);
        t.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(LimboActivity.class.getName()).log(Level.SEVERE, null, ex);
        }



        Intent intent = new Intent(this, LimboVNCActivity.class);
        android.content.ContentValues values = new android.content.ContentValues();
        values.put("_id", Long.toString(0));
        values.put("NICKNAME", "limbo");
        values.put("ADDRESS", "localhost");
        values.put("PORT", Integer.toString(5901));
        if (this.vnc_passwd != null && !this.vnc_passwd.equals("")) {
            values.put("PASSWORD", this.vnc_passwd);
        } else {
            values.put("PASSWORD", "");
        }
        values.put("COLORMODEL", COLORMODEL.C24bit.nameString());
        values.put("FORCEFULL", Long.toString(0));
        values.put("REPEATERID", 0);
        values.put("INPUTMODE", VncCanvasActivity.TOUCHPAD_MODE);
        values.put("SCALEMODE", ScaleType.MATRIX.toString());
        values.put("USELOCALCURSOR", "0");
        values.put("KEEPPASSWORD", "0");
        values.put("FOLLOWMOUSE", "0");
        values.put("USEREPEATER", "0");
        values.put("METALISTID", Long.toString(0));
        values.put("LAST_META_KEY_ID", Long.toString(0));
        values.put("FOLLOWPAN", "0");
        values.put("USERNAME", "limbouser");
        values.put("SECURECONNECTIONTYPE", "None");
        values.put("SHOWZOOMBUTTONS", ("0"));
//        values.put("DOUBLE_TAP_ACTION", "DOUBLE_CLICK");

        intent.putExtra(VncConstants.CONNECTION, values);
        startActivityForResult(intent, Const.VNC_REQUEST_CODE);
    }

//    private void updateSelectedFromView() {
//        selected = new ConnectionBean();
//        if (selected == null) {
//            return;
//        }
//        selected.setAddress("192.168.1.100");
//        try {
//            selected.setPort(Integer.parseInt("5900".toString()));
//        } catch (NumberFormatException nfe) {
//        }
//        selected.setNickname("limbo");
//        selected.setUserName("limbouser");
//        selected.setForceFull(BitmapImplHint.FULL); // or : BitmapImplHint.TILE
//        //TODO: Set password only for external VNC clients
//        //TODO: add x509 verification option also
//        selected.setPassword("");
//        selected.setKeepPassword(false);
//        
//        selected.setUseLocalCursor(true);
//        selected.setColorModel("64 colors (1bpp)");
////        if (repeaterTextSet) {
////            selected.setRepeaterId(repeaterText.getText().toString());
////            selected.setUseRepeater(true);
////        } else {
//            selected.setUseRepeater(false);
//        //}
//    }
//    private void saveAndWriteRecent() {
//        SQLiteDatabase db = database.getWritableDatabase();
//        db.beginTransaction();
//        try {
//            selected.save(db);
//            MostRecentBean mostRecent = getMostRecent(db);
//            if (mostRecent == null) {
//                mostRecent = new MostRecentBean();
//                mostRecent.setConnectionId(selected.get_Id());
//                mostRecent.Gen_insert(db);
//            } else {
//                mostRecent.setConnectionId(selected.get_Id());
//                mostRecent.Gen_update(db);
//            }
//            db.setTransactionSuccessful();
//        } finally {
//            db.endTransaction();
//        }
//    }
    // Start calling the JNI interface
    public void startvm() {
        vmexecutor = null;
        if(Const.debug){
        	vnc_passwd = "test";
        	vnc_allow_external = 1;
        }
        
        vmexecutor = new VMExecutor(currMachine);
        vmexecutor.vnc_allow_external = vnc_allow_external;
        vmexecutor.vnc_passwd = vnc_passwd;
        vmexecutor.dns_addr = mDNS.getText().toString();
        vmexecutor.print();
        output = "Starting VM...";
        sendHandlerMessage(handler, Const.VM_STARTED);
        vmexecutor.startvm();
        sendHandlerMessage(handler, Const.VM_STOPPED);
    }

    public void restartvm() {
        if (vmexecutor != null) {
            Log.v(TAG, "Restarting the VM...");
            output = vmexecutor.stopvm(1);
            sendHandlerMessage(handler, Const.VM_RESTARTED);
        } else {
            Log.v(TAG, "Not running VM...");
            sendHandlerMessage(handler, Const.VM_NOTRUNNING);
        }

    }

    public void savevm(String name) {
        if (vmexecutor != null) {
            if ((currMachine.hda_img_path == null || currMachine.hda_img_path.equals(""))
                    && (currMachine.hdb_img_path == null || currMachine.hdb_img_path.equals(""))) {
                sendHandlerMessage(handler, Const.VM_NO_QCOW2);
            } else {
                Log.v(TAG, "Saving State of the VM...");
                output = vmexecutor.savevm("test_snapshot");
                sendHandlerMessage(handler, Const.VM_SAVED);
            }
        } else {
            Log.v(TAG, "Not running VM...");
            sendHandlerMessage(handler, Const.VM_NOTRUNNING);
        }

    }

    public void resumevm() {
        if (vmexecutor != null) {
            Log.v(TAG, "Resuming the VM...");
            output = vmexecutor.resumevm();
            sendHandlerMessage(handler, Const.VM_RESTARTED);
        } else {
            Log.v(TAG, "Not running VM...");
            sendHandlerMessage(handler, Const.VM_NOTRUNNING);
        }

    }

    // Not needed
    public static String sendHttpGet(String url) {
        HttpConnection hcon = null;
        DataInputStream dis = null;
        java.net.URL URL = null;
        try {
            URL = new java.net.URL(url);
        } catch (MalformedURLException ex) {
            Logger.getLogger(LimboActivity.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
        StringBuffer responseMessage = new StringBuffer();

        try {
            // obtain a DataInputStream from the HttpConnection
            dis = new DataInputStream(URL.openStream());

            // retrieve the response from the server
            int ch;
            while ((ch = dis.read()) != -1) {
                responseMessage.append((char) ch);
            }// end while ( ( ch = dis.read() ) != -1 )
        } catch (Exception e) {
            e.printStackTrace();
            responseMessage.append(e.getMessage());
        } finally {
            try {
                if (hcon != null) {
                    hcon.close();
                }
                if (dis != null) {
                    dis.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }// end try/catch
        }// end try/catch/finally
        return responseMessage.toString();
    }// end sendHttpGet( String )

    // Set Hard Disk
    private void populateRAM() {
        this.userPressedRAM = false;

        String[] arraySpinner = new String[32];

        for (int i = 0; i < arraySpinner.length; i++) {
            arraySpinner[i] = (i + 1) * 8 + "";
        }
        ;

        ramAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        ramAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.mRamSize.setAdapter(ramAdapter);
        this.userPressedRAM = false;
        this.mRamSize.invalidate();
    }

    // Set Hard Disk
    private void setRAM(int ram, boolean userPressed) {
        this.userPressedRAM = userPressed;
//        Log.v("DB", "UserPressed: " + userPressedRAM + " RAM=" + ram);
        if (ram != 0) {
            int pos = ramAdapter.getPosition(ram + "");
//            Log.v("DB", "Got pos: " + pos + " for RAM=" + ram);
            mRamSize.setSelection(pos);
        } else {
            this.userPressedRAM = true;
//            Log.v("RAM", "reset userPressed = " + userPressedRAM);
        }
    }

    // Set Hard Disk
    private void populateBootDevices() {

        String[] arraySpinner = {"Default", "CD Rom", "Floppy", "Hard Disk"};

        bootDevAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        bootDevAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.mBootDevices.setAdapter(bootDevAdapter);
        this.mBootDevices.invalidate();
    }

    // Set Net Cfg
    private void populateNet() {
        String[] arraySpinner = {"None", "User"};
        netAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arraySpinner);
        netAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.mNetConfig.setAdapter(netAdapter);
        this.mNetConfig.invalidate();
    }

    // Set VGA Cfg
    private void populateVGA() {

        String[] arraySpinner = {"std", "cirrus", "vmware", "qxl", "xenfb",
            "none"};

        vgaAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        vgaAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.mVGAConfig.setAdapter(vgaAdapter);
        this.mVGAConfig.invalidate();
    }

    private void populateSoundcardConfig() {

        String[] arraySpinner = {"None", "sb16", "ac97", "es1370", "hda"};

        sndAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        sndAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.mSoundCardConfig.setAdapter(sndAdapter);
        this.mSoundCardConfig.invalidate();
    }

    // Set Cache Cfg
    private void populateHDCacheConfig() {

        String[] arraySpinner = {"default", "none", "writeback",
            "writethrough"};

        hdCacheAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        hdCacheAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.mHDCacheConfig.setAdapter(hdCacheAdapter);
        this.mHDCacheConfig.invalidate();
    }

    // Set Hard Disk
    private void populateNetDevices() {
        String[] arraySpinner = {"e1000", "pcnet", "rtl8139",
            "ne2k_pci", "i82551", "i82557b", "i82559er", "virtio"};
        nicCfgAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arraySpinner);
        nicCfgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.mNetDevices.setAdapter(nicCfgAdapter);
        this.mNetDevices.invalidate();
    }

    private void setMachine(String machine) {
        if (machine != null) {
            int pos = machineAdapter.getPosition(machine);
            this.mMachine.setSelection(pos);
        } else {
            userPressedMachine = true;
//            Log.v("Mach", "reset userPressed = " + userPressedMachine);
        }
        mStart.requestFocus();
    }

    // Set Hard Disk
    private void populateMachines() {
        this.userPressedMachine = false;
        // Add from History
        ArrayList<String> machines = machineDB.getMachines();
        int length = 0;
        if (machines == null || machines.size() == 0) {
//            Log.v(TAG, "No machine in DB");
            length = 0;
        } else {
//            Log.v("PopMachines", "Found " + length + " machines in DB");
            length = machines.size();
        }

        String[] arraySpinner = new String[machines.size() + 2];
        arraySpinner[0] = "Open";
        arraySpinner[1] = "New";
        int index = 2;
        Iterator i = machines.iterator();
        while (i.hasNext()) {
            String file = (String) i.next();
            if (file != null) {
                arraySpinner[index++] = file;
            }
        }

        machineAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        machineAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.mMachine.setAdapter(machineAdapter);
        this.mMachine.invalidate();

    }

    // Set Hard Disk
    private void setCPU(String cpu, boolean userPressed) {
        this.userPressedCPU = userPressed;
//        Log.v("DB", "UserPressed: " + userPressedCPU + " CPU=" + cpu);
        if (cpu != null) {
            int pos = cpuAdapter.getPosition(cpu);
//            Log.v("DB", "Got pos: " + pos + " for CPU=" + cpu);
            mCPU.setSelection(pos);
        } else {
            this.userPressedCPU = true;
//            Log.v("CPU", "reset userPressed = " + this.userPressedCPU);
        }
    }

    private void setCDROM(String cdrom, boolean userPressed) {
        this.userPressedCDROM = userPressed;
        this.currMachine.cd_iso_path = cdrom;
//        Log.v("DB", "UserPressed: " + userPressedCDROM + " CDROM=" + cdrom);
        if (cdrom != null) {
            int pos = cdromAdapter.getPosition(cdrom);
//            Log.v("DB", "Got pos: " + pos + " for CDROM=" + cdrom);
            if (pos > 1) {
                mCD.setSelection(pos);
            } else {
                mCD.setSelection(0);
            }
        } else {
            mCD.setSelection(0);
//            Log.v("CDROM", "reset userPressed = " + this.userPressedCDROM);
        }
    }

    private void setHDA(String hda, boolean userPressed) {
        this.userPressedHDA = userPressed;
//        Log.v("DB", "UserPressed: " + userPressedHDA + " HDA=" + hda);
        currMachine.hda_img_path = hda;
        if (hda != null) {
            int pos = hdaAdapter.getPosition(hda);
//            Log.v("DB", "Got pos: " + pos + " for HDA=" + hda);
            if (pos >= 0) {
                mHDA.setSelection(pos);
            } else {
                mHDA.setSelection(0);
            }
        } else {
            mHDA.setSelection(0);
//            Log.v("HDA", "reset userPressed = " + this.userPressedHDA);
        }
    }

    private void setHDB(String hdb, boolean userPressed) {
        this.userPressedHDB = userPressed;
        this.currMachine.hdb_img_path = hdb;
//        Log.v("DB", "UserPressed: " + userPressedHDB + " HDB=" + hdb);
        if (hdb != null) {
            int pos = hdbAdapter.getPosition(hdb);
//            Log.v("DB", "Got pos: " + pos + " for HDB=" + hdb);
            if (pos >= 0) {
                mHDB.setSelection(pos);
            } else {
                mHDB.setSelection(0);
            }
        } else {
            mHDB.setSelection(0);
//            Log.v("HDB", "reset userPressed = " + this.userPressedHDB);
        }
    }

    private void setFDA(String fda, boolean userPressed) {
        this.userPressedFDA = userPressed;
        this.currMachine.fda_img_path = fda;
//        Log.v("DB", "UserPressed: " + userPressedFDA + " FDA=" + fda);
        if (fda != null) {
            int pos = fdaAdapter.getPosition(fda);
//            Log.v("DB", "Got pos: " + pos + " for FDA=" + fda);
            if (pos >= 0) {
                mFDA.setSelection(pos);
            } else {
                mFDA.setSelection(0);
            }
        } else {
            mFDA.setSelection(0);
//            Log.v("FDA", "reset userPressed = " + this.userPressedFDA);
        }
    }

    private void setFDB(String fdb, boolean userPressed) {
        this.userPressedFDB = userPressed;
        this.currMachine.fdb_img_path = fdb;
//        Log.v("DB", "UserPressed: " + userPressedFDB + " FDB=" + fdb);
        if (fdb != null) {
            int pos = fdbAdapter.getPosition(fdb);
//            Log.v("DB", "Got pos: " + pos + " for FDB=" + fdb);
            if (pos >= 0) {
                mFDB.setSelection(pos);
            } else {
                mFDB.setSelection(0);
            }
        } else {
            mFDB.setSelection(0);
//            Log.v("FDB", "reset userPressed = " + this.userPressedFDB);
        }
    }

    private void setHDCache(String hdcache, boolean userPressed) {
        this.userPressedHDCacheCfg = userPressed;
//        Log.v("DB", "UserPressed: " + userPressedHDCacheCfg + " HDCache="
//                + hdcache);
        if (hdcache != null) {
            int pos = this.hdCacheAdapter.getPosition(hdcache);
//            Log.v("DB", "Got pos: " + pos + " for HDCache=" + hdcache);
            if (pos >= 0) {
                this.mHDCacheConfig.setSelection(pos);
            } else {
                mHDCacheConfig.setSelection(0);
            }
        } else {
            mHDCacheConfig.setSelection(0);
//            Log.v("VGA", "reset userPressed = " + this.userPressedHDCacheCfg);
        }
    }

    private void setSoundcard(String soundcard, boolean userPressed) {
        this.userPressedSoundcardCfg = userPressed;
//        Log.v("DB", "UserPressed: " + userPressedSoundcardCfg + " Soundcard="
//                + soundcard);
        if (soundcard != null) {
            int pos = this.sndAdapter.getPosition(soundcard);
//            Log.v("DB", "Got pos: " + pos + " for Soundcard=" + soundcard);
            if (pos >= 0) {
                this.mSoundCardConfig.setSelection(pos);
            } else {
                mSoundCardConfig.setSelection(0);
            }
        } else {
            mSoundCardConfig.setSelection(0);
//            Log.v("VGA", "reset userPressed = " + this.userPressedSoundcardCfg);
        }
    }

    private void setVGA(String vga, boolean userPressed) {
        this.userPressedVGACfg = userPressed;
//        Log.v("DB", "UserPressed: " + userPressedVGACfg + " VGA=" + vga);
        if (vga != null) {
            int pos = vgaAdapter.getPosition(vga);
//            Log.v("DB", "Got pos: " + pos + " for VGA=" + vga);
            if (pos >= 0) {
                this.mVGAConfig.setSelection(pos);
            } else {
                mVGAConfig.setSelection(0);
            }
        } else {
            mVGAConfig.setSelection(0);
//            Log.v("VGA", "reset userPressed = " + this.userPressedVGACfg);
        }
    }

    private void setNetCfg(String net, boolean userPressed) {
        this.userPressedNetCfg = userPressed;
//        Log.v("DB", "UserPressed: " + userPressedNetCfg + " Net=" + net);
        if (net != null) {
            int pos = this.netAdapter.getPosition(net);
//            Log.v("DB", "Got pos: " + pos + " for Net=" + net);
            if (pos >= 0) {
                this.mNetConfig.setSelection(pos);
            } else {
                mNetConfig.setSelection(0);
            }
        } else {
            mNetConfig.setSelection(0);
//            Log.v("NET", "reset userPressed = " + this.userPressedNetCfg);
        }
    }

    private void setBootDevice(String bootDevice, boolean userPressed) {
        this.userPressedBootDev = userPressed;
//        Log.v("DB", "UserPressed: " + userPressedBootDev + " Boot Dev=" + bootDevice);
        if (bootDevice != null) {
            int pos = this.bootDevAdapter.getPosition(bootDevice);
//            Log.v("DB", "Got pos: " + pos + " for BootDev=" + bootDevice);
            if (pos >= 0) {
                this.mBootDevices.setSelection(pos);
            } else {
                mBootDevices.setSelection(0);
            }
        } else {
            mBootDevices.setSelection(0);
//            Log.v("NET", "reset userPressed = " + this.userPressedBootDev);
        }
    }

    private void setSnapshot(String snapshot, boolean userPressed) {
        this.userPressedSnapshot = userPressed;
//        Log.v("DB", "UserPressed: " + userPressedBootDev + " Boot Dev=" + snapshot);
        if (snapshot != null && !snapshot.equals("")) {
            int pos = this.snapshotAdapter.getPosition(snapshot);
//            Log.v("DB", "Got pos: " + pos + " for BootDev=" + snapshot);
            if (pos >= 0) {
                this.mSnapshot.setSelection(pos);
                this.mSnapshot.invalidate();
            } else {
                mSnapshot.setSelection(0);
            }
        } else {
            mSnapshot.setSelection(0);
//            Log.v("NET", "reset userPressed = " + this.userPressedBootDev);
        }
        mStart.requestFocus();
    }

    private void setNicDevice(String nic, boolean userPressed) {
        this.userPressedNicCfg = userPressed;
//        Log.v("DB", "UserPressed: " + userPressedNicCfg + " Nic=" + nic);
        if (nic != null) {
            int pos = this.nicCfgAdapter.getPosition(nic);
//            Log.v("DB", "Got pos: " + pos + " for Nic=" + nic);
            if (pos >= 0) {
                this.mNetDevices.setSelection(pos);
            } else {
                mNetDevices.setSelection(3);
            }
        } else {
            mNetDevices.setSelection(3);
//            Log.v("NIC", "reset userPressed = " + this.userPressedNicCfg);
        }
    }

    private void populateCPUs() {
        this.userPressedCPU = false;

        String[] arraySpinner = {
            //x86 32bit
            "qemu32", "coreduo", "486", "pentium", "pentium2",
            "pentium3", "athlon", "n270",
            //x86 (64Bit)
            "qemu64 (64Bit)", "phenom (64Bit)", "core2duo (64Bit)",
            "kvm64 (64Bit)"
        // TODO: Needs qemu-system-arm.so
        // ,"arm926", "arm946","arm1026","arm1136","arm1136-r2",
        // "arm1176"
        };

        cpuAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        cpuAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.mCPU.setAdapter(cpuAdapter);
        this.mCPU.invalidate();
    }

    // Set Hard Disk
    private void populateHD(String fileType) {
        // Add from History
        ArrayList<String> oldHDs = favDB.getFavURL(fileType);
        int length = 0;
        if (oldHDs == null || oldHDs.size() == 0) {
            length = 0;
        } else {
            length = oldHDs.size();
        }

        ArrayList<String> arraySpinner = new ArrayList<String>();
        arraySpinner.add("None");
        arraySpinner.add("New");
        arraySpinner.add("Open");
        Iterator i = oldHDs.iterator();
        while (i.hasNext()) {
            String file = (String) i.next();
            if (file != null) {
                arraySpinner.add(file);
            }
        }

        if (fileType.equals("hda")) {

            hdaAdapter = new ArrayAdapter(this,
                    android.R.layout.simple_spinner_item, arraySpinner);
            hdaAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.mHDA.setAdapter(hdaAdapter);
            this.mHDA.invalidate();
        } else if (fileType.equals("hdb")) {
            hdbAdapter = new ArrayAdapter(this,
                    android.R.layout.simple_spinner_item, arraySpinner);
            hdbAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.mHDB.setAdapter(hdbAdapter);
            this.mHDB.invalidate();
        }
    }

    private void populateSnapshot() {
        // Add from History
        ArrayList<String> oldSnapshots = null;
        if (currMachine != null) {
            oldSnapshots = machineDB.getSnapshots(currMachine);
        }

        int length = 0;
        if (oldSnapshots == null) {
            length = 0;
        } else {
            length = oldSnapshots.size();
        }

        ArrayList<String> arraySpinner = new ArrayList<String>();
        arraySpinner.add("None");
        if (oldSnapshots != null) {
            Iterator i = oldSnapshots.iterator();
            while (i.hasNext()) {
                String file = (String) i.next();
                if (file != null) {
                    arraySpinner.add(file);
                }
            }
        }
        this.userPressedSnapshot = false;

        snapshotAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arraySpinner);
        snapshotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.mSnapshot.setAdapter(snapshotAdapter);
        this.mSnapshot.invalidate();

        if (oldSnapshots == null) {
            this.mSnapshot.setEnabled(false);
        } else {
            this.mSnapshot.setEnabled(true);
        }




    }

    // Set Hard Disk
    private void populateCDRom(String fileType) {
        this.userPressedCDROM = false;
        // Add from History
        ArrayList<String> oldCDs = favDB.getFavURL(fileType);
        int length = 0;
        if (oldCDs == null || oldCDs.size() == 0) {
            length = 0;
        } else {
            length = oldCDs.size();
        }

        ArrayList<String> arraySpinner = new ArrayList<String>();
        arraySpinner.add("None");
        arraySpinner.add("Open");
        if (oldCDs != null) {
            Iterator i = oldCDs.iterator();
            while (i.hasNext()) {
                String file = (String) i.next();
                if (file != null) {
                    arraySpinner.add(file);
                }
            }
        }
        cdromAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        cdromAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.mCD.setAdapter(cdromAdapter);
        this.mCD.invalidate();
    }

    // Set Hard Disk
    private void populateFloppy(String fileType) {
        // Add from History
        ArrayList<String> oldFDs = favDB.getFavURL(fileType);
        int length = 0;
        if (oldFDs == null || oldFDs.size() == 0) {
            length = 0;
        } else {
            length = oldFDs.size();
        }

        ArrayList<String> arraySpinner = new ArrayList<String>();
        arraySpinner.add("None");
        arraySpinner.add("Open");
        if (oldFDs != null) {
            Iterator i = oldFDs.iterator();
            while (i.hasNext()) {
                String file = (String) i.next();
                if (file != null) {
                    arraySpinner.add(file);
                }
            }
        }

        if (fileType.equals("fda")) {
            fdaAdapter = new ArrayAdapter(this,
                    android.R.layout.simple_spinner_item, arraySpinner);
            fdaAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.mFDA.setAdapter(fdaAdapter);
            this.mFDA.invalidate();
        } else if (fileType.equals("fdb")) {
            fdbAdapter = new ArrayAdapter(this,
                    android.R.layout.simple_spinner_item, arraySpinner);
            fdbAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.mFDB.setAdapter(fdbAdapter);
            this.mFDB.invalidate();
        }
    }

    public void browse(String fileType) {
        // Check if SD card is mounted
//        Log.v(TAG, "Browsing: " + fileType);
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(getApplicationContext(),
                    "Error: SD card is not mounted", Toast.LENGTH_LONG).show();
            return;
        }

        String dir = null;
        // GET THE LAST ACCESSED DIR FROM THE REG
        String lastDir = SettingsManager.getLastDir(this);
        try {
            Intent i = null;
            i = getFileManIntent();
            Bundle b = new Bundle();
            b.putString("lastDir", lastDir);
            b.putString("fileType", fileType);
            i.putExtras(b);
            // Log.v("**PASS** ", lastDir);
            startActivityForResult(i, Const.FILEMAN_REQUEST_CODE);

        } catch (Exception e) {
//            Log.v(TAG, "Error while starting Filemanager: " + e.getMessage());
        }
    }

    public Intent getFileManIntent() {
        return new Intent(LimboActivity.this,
                com.max2idea.android.limbo.main.PFileManager.class);
    }

    public Intent getVNCIntent() {
        return new Intent(LimboActivity.this,
                com.max2idea.android.limbo.main.LimboVNCActivity.class);
    }

    private void addDriveToList(String file, String type) {
        // Check if exists
//        Log.v(TAG, "Adding To list: " + type + ":" + file);
        int res = favDB.getFavUrlSeq(file, type);
        if (res == -1) {
            if (type.equals("hda")) {
                this.mHDA.getAdapter().getCount();
            } else if (type.equals("hdb")) {
                this.mHDB.getAdapter().getCount();
            } else if (type.equals("cd")) {
                this.mCD.getAdapter().getCount();
            } else if (type.equals("fda")) {
                this.mFDA.getAdapter().getCount();
            } else if (type.equals("fdb")) {
                this.mFDB.getAdapter().getCount();
            }
            favDB.insertFavURL(file, type);
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        menu.add(0, INSTALL, 0, "Install Roms").setIcon(android.R.drawable.ic_menu_agenda);
        menu.add(0, HELP, 0, "Help").setIcon(android.R.drawable.ic_menu_help);
        menu.add(0, QUIT, 0, "Exit").setIcon(android.R.drawable.ic_lock_power_off);

        return true;


    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        super.onOptionsItemSelected(item);
        if (item.getItemId() == this.INSTALL) {
            this.install();
        }
        if (item.getItemId() == this.HELP) {
            this.onHelp();
        } else if (item.getItemId() == this.QUIT) {
            this.exit();
        }
        return true;
    }

    public void stopVM(boolean exit) {
        if (vmexecutor == null && !exit) {
            Log.v(TAG, "Not running VM...");
            sendHandlerMessage(handler, Const.VM_NOTRUNNING);
            return;
        }

        new AlertDialog.Builder(this).setTitle("Shutdown VM")
                .setMessage("To avoid any corrupt data make sure you "
                + "have already shutdown the Operating system from within the VM. Continue?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (LimboActivity.vmexecutor != null) {
                    LimboActivity.vmexecutor.stopvm(0);
                } else if (activity.getParent() != null) {
                    activity.getParent().finish();
                } else {
                    activity.finish();
                }
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        }).show();
    }

    public void saveStateDB(String snapshot_name) {
        currMachine.snapshot_name = snapshot_name;
        int ret = machineDB.deleteMachine(currMachine);
        ret = machineDB.insertMachine(currMachine);
        if (this.snapshotAdapter.getPosition(snapshot_name) < 0) {
            this.snapshotAdapter.add(snapshot_name);
        }
    }

    public void stopTimeListener() {
//        Log.v("Limbo", "Stopping Listener");
        synchronized (this.lockTime) {
            this.timeQuit = true;
            this.lockTime.notifyAll();
        }
    }

    public void onPause() {
        super.onPause();
        Log.v("Limbo", "Limbo Console Pause");
        this.stopTimeListener();
    }

    public void onResume() {
        super.onResume();
        Log.v("Limbo", "Limbo Console Resume");
        execTimeListener();
    }

    public void timeListener() {
        while (timeQuit != true) {
            if (vmexecutor != null) {
                String status = checkStatus();
                if (!status.equals(currStatus)) {
                    currStatus = status;
                    Log.v("Inside", "Status changed: " + status);
                    sendHandlerMessage(handler, Const.STATUS_CHANGED, "status_changed", status);
                }
            }
//            Log.v("Inside", "Status: " + currStatus);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Log.v("Limbo", "Could not sleep");
            }
        }
//        Log.v("Limbo", "Limbo Stopped");

    }

    void execTimeListener() {
//        Log.v("Limbo", "ExecTImeListener");
        Thread t = new Thread(new Runnable() {
            public void run() {
                startTimeListener();
            }
        });
        t.start();
    }

    public void startTimeListener() {
        this.stopTimeListener();

        timeQuit = false;
        try {
//            Log.v("Listener", "Time Listener Started...");
            timeListener();
            synchronized (lockTime) {
                while (timeQuit == false) {
                    lockTime.wait();
                }
                lockTime.notifyAll();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.v("Limbo", "Time listener thread error: " + ex.getMessage());
        }
//        Log.v("Listener", "Time listener thread exited...");

    }

    private String checkStatus() {
        String state = "READY";
        if (vmexecutor != null && vmexecutor.get_state().equals("RUNNING")) {
            state = "RUNNING";
        } else if (vmexecutor != null) {
            String save_state = vmexecutor.get_save_state();
//            Log.v("Listener", "Save State: " + save_state);
            if (save_state.equals("SAVING")) {
                state = save_state;
            } else {
                state = "READY";
            }
        } else {
            state = "READY";
        }
        return state;
    }
}
