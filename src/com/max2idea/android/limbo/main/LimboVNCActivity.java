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

import android.androidVNC.AbstractScaling;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dev
 */
public class LimboVNCActivity extends android.androidVNC.VncCanvasActivity {

    public static final int KEYBOARD = 10000;
    public static final int QUIT = 10001;
    private boolean qmpMode = false;
    private boolean mouseOn = false;
    private Object lockTime = new Object();
    private boolean timeQuit = false;
    private Thread timeListenerThread;
    private ProgressDialog progDialog;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        Toast toast = Toast.makeText(activity, "2-Finger Long Press for Right Click", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
        toast.show();
        this.vncCanvas.setFocusableInTouchMode(true);
//        fullScreen();
    }

    public void stopTimeListener() {
        Log.v("SaveVM", "Stopping Listener");
        synchronized (this.lockTime) {
            this.timeQuit = true;
            this.lockTime.notifyAll();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.stopTimeListener();
    }

    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
    }

    public void timeListener() {
        while (timeQuit != true) {
            String status = checkCompletion();
//            Log.v("Inside", "Status: " + status);
            if (status == null || status.equals("")
                    || status.equals("DONE")) {
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

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Log.v("Limbo", "Inside Options Check");
        super.onOptionsItemSelected(item);
        if (item.getItemId() == this.KEYBOARD
                || item.getItemId() == R.id.itemKeyboard) {
            this.onKeyboard();
        } else if (item.getItemId() == R.id.itemMonitor) {
            if (this.qmpMode) {
                this.onVNC();
            } else {
                this.onQMP();
            }
        } else if (item.getItemId() == R.id.itemSaveState) {
            this.promptStateName(activity);
        } else if (item.getItemId() == R.id.itemFitToScreen){
        	input1 = getInputHandlerById(R.id.itemInputTouchpad);
            if (input1 != null) {
                inputHandler = input1;
                connection.setInputMode(input1.getName());
                if (mouseOn == false) {
                    connection.setFollowMouse(true);
                    mouseOn = true;
                } else {
                    mouseOn = false;
                }
                showPanningState();
                connection.save(database.getWritableDatabase());
                return true;
            }
        } else if (item.getItemId() == this.QUIT) {
        } else if (item.getItemId() == R.id.itemCenterMouse) {
            if (mouseOn == false) {
                input1 = getInputHandlerById(R.id.itemInputTouchpad);
            } else {
                input1 = getInputHandlerById(R.id.itemInputTouchPanZoomMouse);
            }
            if (input1 != null) {
                inputHandler = input1;
                connection.setInputMode(input1.getName());
                if (mouseOn == false) {
                    connection.setFollowMouse(true);
                    mouseOn = true;
                } else {
                    mouseOn = false;
                }
                showPanningState();
                connection.save(database.getWritableDatabase());
                return true;
            }
        }
        this.vncCanvas.requestFocus();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        Log.v("Limbo", "Inside Options Created");
        getMenuInflater().inflate(R.menu.vnccanvasactivitymenu, menu);

        if (vncCanvas.scaling != null) {
            menu.findItem(vncCanvas.scaling.getId()).setChecked(true);
        }

        if (this.qmpMode) {
            menu.findItem(R.id.itemMonitor).setTitle("VM Console");

        } else {
            menu.findItem(R.id.itemMonitor).setTitle("Monitor Console");

        }

        Menu inputMenu = menu.findItem(R.id.itemInputMode).getSubMenu();

        inputModeMenuItems = new MenuItem[inputModeIds.length];
        for (int i = 0; i < inputModeIds.length; i++) {
            inputModeMenuItems[i] = inputMenu.findItem(inputModeIds[i]);
        }
        updateInputMenu();
        menu.findItem(R.id.itemFollowMouse).setChecked(
                connection.getFollowMouse());
        menu.findItem(R.id.itemFollowPan).setChecked(connection.getFollowPan());

        if (this.mouseOn) {
            menu.findItem(R.id.itemCenterMouse).setTitle("Pan (Mouse Off)");
            menu.findItem(R.id.itemCenterMouse).setIcon(R.drawable.pan);
        } else {
            menu.findItem(R.id.itemCenterMouse).setTitle("Mouse (Pan Off)");
            menu.findItem(R.id.itemCenterMouse).setIcon(R.drawable.mouse);

        }
        menu.removeItem(menu.findItem(R.id.itemFollowPan).getItemId());
        menu.removeItem(menu.findItem(R.id.itemFollowMouse).getItemId());
//        menu.removeItem(menu.findItem(R.id.itemInputMode).getItemId());
        menu.removeItem(menu.findItem(R.id.itemInfo).getItemId());
        menu.removeItem(menu.findItem(R.id.itemArrowUp).getItemId());
        menu.removeItem(menu.findItem(R.id.itemArrowDown).getItemId());
        menu.removeItem(menu.findItem(R.id.itemArrowLeft).getItemId());
        menu.removeItem(menu.findItem(R.id.itemArrowRight).getItemId());


        return true;


    }

    private void onKeyboard() {
        InputMethodManager inputMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMgr.toggleSoftInput(0, 0);
    }

    private void onQMP() {
        qmpMode = true;
        vncCanvas.sendMetaKey1(50, 6);

    }

    private void onVNC() {
        qmpMode = false;
        vncCanvas.sendMetaKey1(49, 6);
    }

    private void onSaveState(String stateName) {
        onQMP();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(LimboVNCActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
        vncCanvas.sendText("savevm " + stateName + "\n");
//        Toast.makeText(this.getApplicationContext(), "Please wait while saving VM State", Toast.LENGTH_LONG).show();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(LimboVNCActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
        onVNC();
        ((LimboActivity) LimboActivity.activity).saveStateDB(stateName);


        progDialog = ProgressDialog.show(activity, "Please Wait", "Saving VM State...", true);
        SaveVM a = new SaveVM();
        a.execute();

    }

    private class SaveVM extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            startTimeListener();
            return null;
        }

        @Override
        protected void onPostExecute(Void test) {
            try {
                progDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void fullScreen() {
        AbstractScaling.getById(R.id.itemFitToScreen).setScaleTypeForActivity(
                this);
        showPanningState();
    }

    public void promptStateName(final Activity activity) {
        if ((LimboActivity.currMachine.hda_img_path == null
                || !LimboActivity.currMachine.hda_img_path.contains(".qcow2"))
                &&(LimboActivity.currMachine.hdb_img_path == null
                || !LimboActivity.currMachine.hdb_img_path.contains(".qcow2"))
                )
                
                {
            Toast.makeText(activity.getApplicationContext(), "No HDD image found, please create a qcow2 image from Limbo console", Toast.LENGTH_LONG).show();
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
                EditText a = (EditText) alertDialog.findViewById(201012010);
                onSaveState(a.getText().toString());
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
}
