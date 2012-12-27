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
package com.max2idea.android.limbo.utils;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.max2idea.android.limbo.main.LimboActivity;

/**
 *
 * @author dev
 */
public class FileUtils {
    
    public String LoadFile(Activity activity, String fileName, boolean loadFromRawFolder) throws IOException {
        //Create a InputStream to read the file into
        InputStream iS;
        if (loadFromRawFolder) {
            //get the resource id from the file name
            int rID = activity.getResources().getIdentifier(getClass().getPackage().getName() + ":raw/" + fileName, null, null);
            //get the file as a stream
            iS = activity.getResources().openRawResource(rID);
        } else {
            //get the file as a stream
            iS = activity.getResources().getAssets().open(fileName);
        }

        //create a buffer that has the same size as the InputStream
        byte[] buffer = new byte[iS.available()];
        //read the text file as a stream, into the buffer
        iS.read(buffer);
        //create a output stream to write the buffer into
        ByteArrayOutputStream oS = new ByteArrayOutputStream();
        //write this buffer to the output stream
        oS.write(buffer);
        //Close the Input and Output streams
        oS.close();
        iS.close();

        //return the output stream as a String
        return oS.toString();
    }

	public static void saveFileContents(String dBFile, String machinesToExport) {
		// TODO Auto-generated method stub
		byteArrayToFile(machinesToExport.getBytes(), new File(dBFile));
	}
	
    public static void byteArrayToFile(byte[] byteData, File filePath) {

        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(byteData);
            fos.close();

        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException : " + ex);
        } catch (IOException ioe) {
            System.out.println("IOException : " + ioe);
        }

    }

	public static ArrayList<Machine> getVMs(String dBFile) {
		// TODO Auto-generated method stub
		//Read machines from csv file
		
		return null;
	}
	
	public static String getDataDir(){
		PackageManager m = LimboActivity.activity.getPackageManager();
		String packageName = LimboActivity.activity.getPackageName();
		Log.v("VMExecutor", "Found packageName: " + packageName);
		String dataDir = "";
		try {
		    PackageInfo p = m.getPackageInfo(packageName, 0);
		    dataDir = p.applicationInfo.dataDir;
		    Log.v("VMExecutor", "Found dataDir: " + dataDir);
		} catch (NameNotFoundException e) {
		    Log.w("VMExecutor", "Error Package name not found using /data/data/", e);
		    dataDir = "/data/data/" + packageName;
		}
		return dataDir; 
	}
}
