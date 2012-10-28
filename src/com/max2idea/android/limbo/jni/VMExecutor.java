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
package com.max2idea.android.limbo.jni;

import com.max2idea.android.limbo.utils.Machine;

import android.text.Editable;
import android.util.Log;
import com.max2idea.android.limbo.main.Const;

public class VMExecutor {

    private final String cd_iso_path;
    private final String hda_img_path;
    private final String fda_img_path;
    private final String hdb_img_path;
    private final String fdb_img_path;
    private final String cpu;
    private final String TAG = "VMExecutor";
    
    public int aiomaxthreads = 1;
    // Default Settings
    private int memory = 128;
    private String bootdevice = null;
    // net
    private String net_cfg = "None";
    private int nic_num = 1;
    private String vga_type = "std";
    private String hd_cache = "default";
    private String nic_driver = null;
    private String lib = "liblimbo.so";
    private String lib_path = null;
    private int restart = 0;
    private String snapshot_name = "limbo";
    private int disableacpi = 0;
    private int disablehpet = 0;
    private int usbmouse = 0;
    private int enableqmp;
    private int enablevnc;
    public String vnc_passwd = null;
    public int vnc_allow_external = 0;
    private String qemu_dev;
    private String qemu_dev_value;
    public String base_dir = Const.basefiledir;
    public String dns_addr;

    /**
     */
    public VMExecutor(Machine machine) {


        this.memory = machine.memory;
        this.vga_type = machine.vga_type;
        this.hd_cache = machine.hd_cache;
        this.snapshot_name = machine.snapshot_name;
        this.disableacpi = machine.disableacpi;
        this.disablehpet = machine.disablehpet;
        this.usbmouse = machine.usbmouse;
        this.enableqmp = machine.enableqmp;
        this.enablevnc = machine.enablevnc;

        if (machine.cpu.endsWith("(64Bit)")) {
            this.lib_path = "/data/data/com.max2idea.android.limbo.main/lib/libqemu-system-x86_64.so";
            this.cpu = machine.cpu.split(" ")[0];
        } else {
            this.cpu = machine.cpu;
            //x86_64 can run 32bit as well as no need for the extra lib
            //this.lib_path = "/data/data/com.max2idea.android.limbo.main/lib/libqemu-system-i386.so";
            this.lib_path = "/data/data/com.max2idea.android.limbo.main/lib/libqemu-system-x86_64.so";
        }
        //Add other archs??

        // Load VM library
//                loadNativeLibs("libSDL.so");
//                loadNativeLibs("libSDL_image.so");
//                loadNativeLibs("libmikmod.so");
//                loadNativeLibs("libSDL_mixer.so");
//                loadNativeLibs("libSDL_ttf.so");
        loadNativeLibs(lib);

        if (machine.cd_iso_path == null || machine.cd_iso_path.equals("None")) {
            this.cd_iso_path = null;
        } else {
            this.cd_iso_path = machine.cd_iso_path;
        }
        if (machine.hda_img_path == null || machine.hda_img_path.equals("None")) {
            this.hda_img_path = null;
        } else {
            this.hda_img_path = machine.hda_img_path;
        }

        if (machine.hdb_img_path == null || machine.hdb_img_path.equals("None")) {
            this.hdb_img_path = null;
        } else {
            this.hdb_img_path = machine.hdb_img_path;
        }

        if (machine.fda_img_path == null || machine.fda_img_path.equals("None")) {
            this.fda_img_path = null;
        } else {
            this.fda_img_path = machine.fda_img_path;
        }

        if (machine.fdb_img_path == null || machine.fdb_img_path.equals("None")) {
            this.fdb_img_path = null;
        } else {
            this.fdb_img_path = machine.fdb_img_path;
        }
        if (machine.bootdevice == null) {
            this.bootdevice = null;
        } else if (machine.bootdevice.equals("Default")) {
            this.bootdevice = null;
        } else if (machine.bootdevice.equals("CD Rom")) {
            this.bootdevice = "d";
        } else if (machine.bootdevice.equals("Floppy")) {
            this.bootdevice = "a";
        } else if (machine.bootdevice.equals("Hard Disk")) {
            this.bootdevice = "c";
        }

        if (machine.net_cfg == null || machine.net_cfg.equals("None")) {
            this.net_cfg = "none";
            this.nic_driver = null;
        } else if (machine.net_cfg.equals("User")) {
            this.net_cfg = "user";
            this.nic_driver = machine.nic_driver;
        }

    }

    private void loadNativeLibs(String lib) {
        // Load the C library
        loadNativeLib(lib, "/data/data/com.max2idea.android.limbo.main/lib");
    }

    // Load the shared lib
    private void loadNativeLib(String lib, String destDir) {
        if (true) {
            String libLocation = destDir + "/" + lib;
            try {
                System.load(libLocation);
            } catch (Exception ex) {
                Log.e("JNIExample", "failed to load native library: " + ex);
            }
        }

    }

    public void print() {
        Log.v(TAG, "CPU: " + this.cpu);
        Log.v(TAG, "MEM: " + this.memory);
        Log.v(TAG, "HDA: " + this.hda_img_path);
        Log.v(TAG, "HDB: " + this.hdb_img_path);
        Log.v(TAG, "CD: " + this.cd_iso_path);
        Log.v(TAG, "FDA: " + this.fda_img_path);
        Log.v(TAG, "FDB: " + this.fdb_img_path);
        Log.v(TAG, "Boot Device: " + this.bootdevice);
        Log.v(TAG, "NET: " + this.net_cfg);
        Log.v(TAG, "NIC: " + this.nic_driver);
    }

    /**
     * JNI interface for converting PCM file to a WAV file
     */
    protected native String start();

    /**
     * JNI interface for converting PCM file to a WAV file
     */
    protected native String stop();

    protected native String save();

    protected native void vncchangepassword();
    
    protected native void dnschangeaddr();

    protected native String getsavestate();
    
    protected native String getstate();
    
    protected native String changedev();

    /**
     * Converting the PCM file to a WAV file
     */
    public String startvm() {
        Log.v(TAG, "Starting the VM");
        return this.start();
    }

    public String stopvm(int restart) {
        Log.v(TAG, "Stopping the VM");
        this.restart = restart;
        return this.stop();
    }

    public String savevm(String statename) {
        // Set to delete previous snapshots after vm resumed
        Log.v(TAG, "Save the VM");
        this.snapshot_name = statename;
        return this.save();
    }

    public String resumevm() {
        // Set to delete previous snapshots after vm resumed
        Log.v(TAG, "Resume the VM");
        return this.start();
    }

    public void change_vnc_password() {
        this.vncchangepassword();
    }
    
    public void change_dns_addr() {
        this.dnschangeaddr();
    }

    public String get_save_state() {
        return this.getsavestate();
    }

    public String get_state() {
        return this.getstate();
    }

    public void change_dev(String ide1cd0, String cd_iso_path) {
        this.qemu_dev = ide1cd0;
        this.qemu_dev_value = cd_iso_path;
        this.changedev();
    }
}
