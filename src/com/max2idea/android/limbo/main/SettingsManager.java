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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsManager extends PreferenceActivity {

    public static String getLastDir(Activity activity) {
        String lastDir = Const.basefiledir;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("lastDir", lastDir);
    }

    public static void setLastDir(Activity activity, String lastDir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lastDir", lastDir);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }

    static String getDNSServer(Activity activity) {
        String dnsServer = Const.dnsServer;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("dnsServer", dnsServer);
    }

    public static void setDNSServer(Activity activity, String dnsServer) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("dnsServer", dnsServer);
        edit.commit();
    }
    
    static String getAppend(Activity activity) {
        String dnsServer = Const.append;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("append", dnsServer);
    }

    public static void setAppend(Activity activity, String dnsServer) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("append", dnsServer);
        edit.commit();
    }
    
    static String getUI(Activity activity) {
        String ui = Const.ui;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("ui", ui);
    }

    public static void setUI(Activity activity, String ui) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("ui", ui);
        edit.commit();
    }
    
    static String getLastHDA(LimboActivity activity) {
        String lastDir = Environment.getExternalStorageDirectory().getPath();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("lastHDA", "");
    }

    public static void setLastHDA(Activity activity, String lastDir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lastHDA", lastDir);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }

    static String getLastHDB(LimboActivity activity) {
        String lastDir = Environment.getExternalStorageDirectory().getPath();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("lastHDB", "");
    }

    public static void setLastHDB(Activity activity, String lastDir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lastHDB", lastDir);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }

    static String getLastCD(LimboActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("lastCD", "");
    }

    public static void setLastCD(Activity activity, String lastDir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lastCD", lastDir);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }

    static String getLastFDA(LimboActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("lastFDA", "");
    }

    public static void setLastFDA(Activity activity, String lastDir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lastFDA", lastDir);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }

    static String getLastFDB(LimboActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("lastFDB", "");
    }

    public static void setLastFDB(Activity activity, String lastDir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lastFDB", lastDir);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }

    static String getLastBootDev(LimboActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("lastBootDev", "");
    }

    public static void setLastBootDev(Activity activity, String lastDir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lastBootDev", lastDir);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }
    static String getLastMachine(LimboActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("lastMachine", "");
    }

    public static void setLastMachine(Activity activity, String lastDir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lastMachine", lastDir);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }
    
    //FIXME: Need to save password to enable this feature
//    static boolean getVNCAllowExternal(Activity activity) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
//        return prefs.getBoolean("VNCAllowExternal", false);
//    }
//
//    public static void setVNCAllowExternal(Activity activity, boolean flag) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
//        SharedPreferences.Editor edit = prefs.edit();
//        edit.putBoolean("VNCAllowExternal", flag);
//        edit.commit();
////        UIUtils.log("Setting First time: ");
//    }

    
    static boolean getUSBMouse(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getBoolean("USBMouse", false);
    }

    public static void setUSBMouse(Activity activity, boolean flag) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("USBMouse", flag);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }
    
    static boolean getPrio(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getBoolean("HighPrio", false);
    }

    public static void setPrio(Activity activity, boolean flag) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("HighPrio", flag);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }
    
    static boolean getMultiAIO(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getBoolean("enableMultiAIO", false);
    }

    public static void setMultiAIO(Activity activity, boolean flag) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("enableMultiAIO", flag);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }
    
    static String getLastCPU(LimboActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("lastCPU", "");
    }

    public static void setLastCPU(Activity activity, String lastDir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lastCPU", lastDir);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }

    static String getLastNet(LimboActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("lastNet", "");
    }

    public static void setLastNet(Activity activity, String lastDir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lastNet", lastDir);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }

    static String getLastHDCache(LimboActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("lastHDCache", "");
    }

    public static void setLastHDCache(Activity activity, String lastDir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lastHDCache", lastDir);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }
    
    
    static String getLastVGA(LimboActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("lastVGA", "");
    }

    public static void setLastVGA(Activity activity, String lastDir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lastVGA", lastDir);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }
    
    static String getLastSoundcard(LimboActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("lastVGA", "");
    }

    public static void setLastSoundcard(Activity activity, String lastDir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lastSoundcard", lastDir);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }
    
    static String getLastNic(LimboActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("lastNic", "");
    }

    public static void setLastNic(Activity activity, String lastDir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lastNic", lastDir);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }

    static String getLastMem(LimboActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("lastMem", "8");
    }

    public static void setLastMem(Activity activity, String lastDir) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lastMem", lastDir);
        edit.commit();
//        UIUtils.log("Setting First time: ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data = new Intent();
        setResult(Const.SETTINGS_RETURN_CODE, data);
        addPrefs();
    }

    public void addPrefs() {
//        addPreferencesFromResource(R.xml.settings);
    }
}
