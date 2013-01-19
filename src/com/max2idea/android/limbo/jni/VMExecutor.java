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

import com.max2idea.android.limbo.utils.FileUtils;
import com.max2idea.android.limbo.utils.Machine;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.Editable;
import android.util.Log;
import com.max2idea.android.limbo.main.Const;
import com.max2idea.android.limbo.main.LimboActivity;

public class VMExecutor {

	private final String cd_iso_path;
	private final String hda_img_path;
	private final String fda_img_path;
	private final String hdb_img_path;
	private final String fdb_img_path;
	private String cpu;

	private final String kernel;
	private final String initrd;

	private final String TAG = "VMExecutor";

	public int aiomaxthreads = 1;
	// Default Settings
	private int memory = 128;
	private int cpuNum = 128;
	private String bootdevice = null;
	// net
	private String net_cfg = "None";
	private int nic_num = 1;
	private String vga_type = "std";
	private String hd_cache = "default";
	public String sound_card;
	private String nic_driver = null;
	private String liblimbo = "limbo";
	private String libqemu = null;
	private int restart = 0;
	private String snapshot_name = "limbo";
	private int disableacpi = 0;
	private int disablehpet = 0;
	private int usbmouse = 0; // for -usb -usbdevice tablet - fixes mouse
								// positioning
	private int enableqmp;
	public int enablevnc;
	public String vnc_passwd = null;
	public int vnc_allow_external = 0;
	private String qemu_dev;
	private String qemu_dev_value;
	public String base_dir = Const.basefiledir;
	public String dns_addr;
	private int width;
	private int height;
	private String arch = "x86";
	public String append = "";
	public boolean busy = false;
	private String machine_type;
	public boolean libLoaded = false;


	/**
     */
	public VMExecutor(Machine machine) {

		this.memory = machine.memory;
		this.cpuNum = machine.cpuNum;
		this.vga_type = machine.vga_type;
		this.hd_cache = machine.hd_cache;
		this.snapshot_name = machine.snapshot_name;
		this.disableacpi = machine.disableacpi;
		this.disablehpet = machine.disablehpet;
		this.enableqmp = machine.enableqmp;
		this.enablevnc = machine.enablevnc;
		this.sound_card = machine.soundcard;
		this.kernel = machine.kernel;
		this.initrd = machine.initrd;
		this.cpu = machine.cpu;
		
		

		
		if (machine.cpu.endsWith("(arm)")) {
			this.libqemu = FileUtils.getDataDir()
					+ "/lib/libqemu-system-arm.so";
			this.cpu = machine.cpu.split(" ")[0];
			this.arch = "arm";
			this.machine_type = machine.machine_type.split(" ")[0];
		} else if (machine.cpu.endsWith("(64Bit)")) {
			this.libqemu = FileUtils.getDataDir()
					+ "/lib/libqemu-system-x86_64.so";
			this.cpu = machine.cpu.split(" ")[0];
			this.arch = "x86_64";
			this.machine_type = "pc";
		} else {
			this.cpu = machine.cpu;
			// x86_64 can run 32bit as well as no need for the extra lib
			this.libqemu = FileUtils.getDataDir()
					+ "/lib/libqemu-system-x86_64.so";
			this.cpu = machine.cpu.split(" ")[0];
			this.arch = "x86";
			this.machine_type = "pc";
		}

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

		if (this.arch.equals("arm")) {
			this.bootdevice = null;
			this.append = "\"root=/dev/sda1\"";
		}
		if (machine.net_cfg == null || machine.net_cfg.equals("None")) {
			this.net_cfg = "none";
			this.nic_driver = null;
		} else if (machine.net_cfg.equals("User")) {
			this.net_cfg = "user";
			this.nic_driver = machine.nic_driver;
		}

		if (machine.soundcard != null && machine.soundcard.equals("None")) {
			this.sound_card = null;
		}

		// //Load SDL libraries
		if (Const.enable_SDL) {
			System.loadLibrary("SDL2");
			System.loadLibrary("SDL2_image");
		}
		if (Const.enable_sound) {
			System.loadLibrary("mikmod");
			System.loadLibrary("SDL2_mixer");
			// System.loadLibrary("SDL_ttf");

		}
		if (Const.enable_SDL) {
			System.loadLibrary("main");
		}

	}

	private void loadNativeLibs() {

		// Load the C library
		// FIXME: use standard load without the hardcoded path
		// loadNativeLib(this., FileUtils.getDataDir() + "/lib");
		System.loadLibrary("limbo");
		
		if(arch.equals("x86") || arch.equals("x86_64")){
			System.loadLibrary("qemu-system-x86_64");
		}else if(arch.equals("arm")){
			System.loadLibrary("qemu-system-arm");
		}
		libLoaded=true;
		

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

	protected native void scale();

	protected native String getsavestate();

	protected native void resize();

	protected native void togglefullscreen();

	protected native void savevm1();

	protected native void savevm2();

	protected native void savevm3();

	protected native String getstate();

	protected native String changedev();

	protected native String ejectdev();

	/**
	 * Converting the PCM file to a WAV file
	 */
	public String startvm() {
		Log.v(TAG, "Starting the VM");
		loadNativeLibs();
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

	public void change_dev(String dev, String image_path) {
		this.busy = true;
		this.qemu_dev = dev;
		this.qemu_dev_value = image_path;
		if (qemu_dev_value == null || qemu_dev_value.trim().equals("")) {
			Log.v("Limbo", "Ejecting Dev: " + dev);
			this.ejectdev();
		} else {
			Log.v("Limbo", "Changing Dev: " + dev + " to: " + image_path);
			this.changedev();
		}
		this.busy = false;

	}

	public void resizeScreen() {
		// TODO Auto-generated method stub
		this.resize();

	}

	public void toggleFullScreen() {
		// TODO Auto-generated method stub
		this.togglefullscreen();

	}

	public void saveVM1(String stateName) {
		// TODO Auto-generated method stub
		this.snapshot_name = stateName;
		this.savevm3();

	}

	public void screenScale(int width, int height) {
		// TODO Auto-generated method stub
		this.width = width;
		this.height = height;
		this.scale();

	}
}
