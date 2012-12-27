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

import android.util.Log;

/**
 * Converter for WAV files to MIDI notes
 *
 * @author Max Kastanas
 */
public class ImageCreator {

    private String filename;
    private int memory = 0;
    private String lib_path = FileUtils.getDataDir() +  "/lib/libqemu-img.so";
    private String lib = "liblimbo.so";
    private final int prealloc;

    public ImageCreator(String srcfilename, int size, int prealloc) {
        this.filename = srcfilename;
        this.memory = size;
        this.prealloc = prealloc;
        loadNativeLibs(lib);
    }

    private void loadNativeLibs(String lib) {
    	
        // Load the C library
        loadNativeLib(lib, FileUtils.getDataDir() + "/lib");
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
    
    protected native String start();

    public void createImage() {
        start();
    }
}
