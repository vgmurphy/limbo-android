package com.max2idea.android.limbo.utils;

import java.util.ArrayList;
import java.util.Iterator;

import org.libsdl.app.SDLActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.max2idea.android.limbo.main.Const;
import com.max2idea.android.limbo.main.LimboActivity;
import com.max2idea.android.limbo.main.R;
import com.max2idea.android.limbo.main.LimboSettingsManager;

public class DrivesDialogBox extends Dialog {
	private static Activity activity;


	public DrivesDialogBox(Context context, int theme, Activity activity1) {
		super(context, theme);
		activity = activity1;
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
				WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		setContentView(R.layout.dev_dialog);
		this.setTitle("Device Manager");
		getWidgets();
		setupListeners();
		initUI();
		 
	}

	@Override
	public void onBackPressed() {
		this.dismiss();
	}

	public static Spinner mCD;
	public static Spinner mFDA;
	public static Spinner mFDB;
	public static Button mOK;

	public static boolean userPressedCDROM = true;
	public static boolean userPressedFDA = true;
	public static boolean userPressedFDB = true;

	
	private void getWidgets() {
		mCD = (Spinner) findViewById(R.id.cdromimgval);
		mFDA = (Spinner) findViewById(R.id.floppyimgval);
		mFDB = (Spinner) findViewById(R.id.floppybimgval);
		mOK = (Button) findViewById(R.id.okButton);
	}

	private void setupListeners() {
		mOK.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});
		mCD
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parentView,
							View selectedItemView, int position, long id) {
						String cd = (String) ((ArrayAdapter) mCD
								.getAdapter()).getItem(position);

						if (userPressedCDROM && position == 0) {
							int ret = LimboActivity.machineDB.update(
									LimboActivity.currMachine,
									MachineOpenHelper.CDROM, null);
							LimboActivity.currMachine.cd_iso_path = null;
						} else if (userPressedCDROM
								&& position == 1) {
							browse("cd");
						} else if (userPressedCDROM
								&& position > 1) {
							int ret = LimboActivity.machineDB.update(
									LimboActivity.currMachine,
									MachineOpenHelper.CDROM, cd);
							LimboActivity.currMachine.cd_iso_path = cd;
							// TODO: If Machine is running eject and set
							// floppy img
						}
						if (userPressedCDROM
								&& LimboActivity.vmexecutor != null
								&& position > 1
								&& !LimboActivity.vmexecutor.busy) {
							LimboActivity.vmexecutor.change_dev("ide1-cd0",
									LimboActivity.currMachine.cd_iso_path);
						} else if (userPressedCDROM
								&& LimboActivity.vmexecutor != null
								&& position == 0
								&& !LimboActivity.vmexecutor.busy) {
							LimboActivity.vmexecutor.change_dev("ide1-cd0",
									null); // Eject
						}
						userPressedCDROM = true;
					}

					public void onNothingSelected(AdapterView<?> parentView) {
						// your code here
						// Log.v(TAG, "Nothing selected");
					}
				});

		mFDA
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parentView,
							View selectedItemView, int position, long id) {
						String fda = (String) ((ArrayAdapter) mFDA
								.getAdapter()).getItem(position);
						if (userPressedFDA && position == 0) {
							int ret = LimboActivity.machineDB.update(
									LimboActivity.currMachine,
									MachineOpenHelper.FDA, null);
							LimboActivity.currMachine.fda_img_path = null;
						} else if (userPressedFDA
								&& position == 1) {
							browse("fda");
						} else if (userPressedFDA
								&& position > 1) {
							int ret = LimboActivity.machineDB.update(
									LimboActivity.currMachine,
									MachineOpenHelper.FDA, fda);
							LimboActivity.currMachine.fda_img_path = fda;
							// TODO: If Machine is running eject and set
							// floppy img
						}
						if (userPressedFDA
								&& LimboActivity.vmexecutor != null
								&& position > 1) {
							LimboActivity.vmexecutor.change_dev("floppy0",
									LimboActivity.currMachine.fda_img_path);
						} else if (userPressedFDA
								&& LimboActivity.vmexecutor != null
								&& position == 0) {
							LimboActivity.vmexecutor.change_dev("floppy0",
									null); // Eject
						}

						userPressedFDA = true;
					}

					public void onNothingSelected(AdapterView<?> parentView) {
						// your code here
						// Log.v(TAG, "Nothing selected");
					}
				});

		mFDB
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parentView,
							View selectedItemView, int position, long id) {
						String fdb = (String) ((ArrayAdapter) mFDB
								.getAdapter()).getItem(position);
						if (userPressedFDB && position == 0) {
							int ret = LimboActivity.machineDB.update(
									LimboActivity.currMachine,
									MachineOpenHelper.FDB, null);
							LimboActivity.currMachine.fdb_img_path = null;
						} else if (userPressedFDB
								&& position == 1) {
							browse("fdb");
						} else if (userPressedFDB
								&& position > 1) {
							int ret = LimboActivity.machineDB.update(
									LimboActivity.currMachine,
									MachineOpenHelper.FDB, fdb);
							LimboActivity.currMachine.fdb_img_path = fdb;
							// TODO: If Machine is running eject and set
							// floppy img
						}
						if (userPressedFDB
								&& LimboActivity.vmexecutor != null
								&& position > 1) {
							LimboActivity.vmexecutor.change_dev("floppy1",
									LimboActivity.currMachine.fdb_img_path);
						} else if (userPressedFDB
								&& LimboActivity.vmexecutor != null
								&& position == 0) {
							LimboActivity.vmexecutor.change_dev("floppy1",
									null); // Eject
						}
						userPressedFDB = true;
					}

					public void onNothingSelected(AdapterView<?> parentView) {
						// your code here
						// Log.v(TAG, "Nothing selected");
					}
				});

	}

	private void initUI() {
		// Set spinners to values from currmachine
		populateCDRom("cd");
		populateFloppy("fda");
		populateFloppy("fdb");
		setCDROM(LimboActivity.currMachine.cd_iso_path, false);
		setFDA(LimboActivity.currMachine.fda_img_path, false);
		setFDB(LimboActivity.currMachine.fdb_img_path, false);
	}
	
	// Set CDROM
	private static void populateCDRom(String fileType) {
		userPressedCDROM = false;
		// Add from History
		ArrayList<String> oldCDs = LimboActivity.favDB.getFavURL(fileType);
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
		ArrayAdapter cdromAdapter = new ArrayAdapter(activity,
				R.layout.custom_spinner_item, arraySpinner);
		cdromAdapter
				.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
		mCD.setAdapter(cdromAdapter);
		mCD.invalidate();
	}

	// Set Hard Disk
	private static void populateFloppy(String fileType) {
		// Add from History
		ArrayList<String> oldFDs = LimboActivity.favDB.getFavURL(fileType);
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
			ArrayAdapter fdaAdapter = new ArrayAdapter(activity,
					R.layout.custom_spinner_item, arraySpinner);
			fdaAdapter
					.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
			mFDA.setAdapter(fdaAdapter);
			mFDA.invalidate();
		} else if (fileType.equals("fdb")) {
			ArrayAdapter fdbAdapter = new ArrayAdapter(activity,
					R.layout.custom_spinner_item, arraySpinner);
			fdbAdapter
					.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
			mFDB.setAdapter(fdbAdapter);
			mFDB.invalidate();
		}
	}

	public static void setDriveAttr(String fileType, String file,
			boolean userPressed) {
		// TODO Auto-generated method stub
		addDriveToList(file, fileType);
		if (fileType != null && fileType.startsWith("cd") && file != null
				&& !file.trim().equals("")) {
			int ret = LimboActivity.machineDB.update(LimboActivity.currMachine,
					MachineOpenHelper.CDROM, file);
			if (((ArrayAdapter) mCD.getAdapter()).getPosition(file) < 0) {
				((ArrayAdapter) mCD.getAdapter()).add(file);
			}
			setCDROM(file, userPressed);
		} else if (fileType != null && fileType.startsWith("fd")
				&& file != null && !file.trim().equals("")) {
			if (fileType.startsWith("fda")) {
				int ret = LimboActivity.machineDB.update(
						LimboActivity.currMachine, MachineOpenHelper.FDA, file);
				if (((ArrayAdapter) mFDA.getAdapter())
						.getPosition(file) < 0) {
					((ArrayAdapter) mFDA.getAdapter()).add(file);
				}
				setFDA(file, userPressed);
			} else if (fileType.startsWith("fdb")) {
				int ret = LimboActivity.machineDB.update(
						LimboActivity.currMachine, MachineOpenHelper.FDB, file);
				if (((ArrayAdapter) mFDB.getAdapter())
						.getPosition(file) < 0) {
					((ArrayAdapter) mFDB.getAdapter()).add(file);
				}
				setFDB(file, userPressed);
			}
		}

		int res = mCD.getSelectedItemPosition();
		if (res == 1) {
			mCD.setSelection(0);
		}

		res = mFDA.getSelectedItemPosition();
		if (res == 1) {
			mFDA.setSelection(0);
		}

		res = mFDB.getSelectedItemPosition();
		if (res == 1) {
			mFDB.setSelection(0);

		}

	}

	private static void setCDROM(String cdrom, boolean userPressed) {
		userPressedCDROM = userPressed;
		LimboActivity.currMachine.cd_iso_path = cdrom;
		// Log.v("DB", "UserPressed: " + userPressedCDROM + " CDROM=" + cdrom);
		if (cdrom != null) {
			int pos = ((ArrayAdapter) mCD.getAdapter())
					.getPosition(cdrom);
			// Log.v("DB", "Got pos: " + pos + " for CDROM=" + cdrom);
			if (pos > 1) {
				mCD.setSelection(pos);
			} else {
				mCD.setSelection(0);
			}
		} else {
			mCD.setSelection(0);
			// Log.v("CDROM", "reset userPressed = " + this.userPressedCDROM);
		}
	}

	private static void setFDA(String fda, boolean userPressed) {
		userPressedFDA = userPressed;
		LimboActivity.currMachine.fda_img_path = fda;
		// Log.v("DB", "UserPressed: " + userPressedFDA + " FDA=" + fda);
		if (fda != null) {
			int pos = ((ArrayAdapter) mFDA.getAdapter())
					.getPosition(fda);
			// Log.v("DB", "Got pos: " + pos + " for FDA=" + fda);
			if (pos >= 0) {
				mFDA.setSelection(pos);
			} else {
				mFDA.setSelection(0);
			}
		} else {
			mFDA.setSelection(0);
			// Log.v("FDA", "reset userPressed = " + this.userPressedFDA);
		}
	}

	private static void setFDB(String fdb, boolean userPressed) {
		userPressedFDB = userPressed;
		LimboActivity.currMachine.fdb_img_path = fdb;
		// Log.v("DB", "UserPressed: " + userPressedFDB + " FDB=" + fdb);
		if (fdb != null) {
			int pos = ((ArrayAdapter) mFDA.getAdapter())
					.getPosition(fdb);
			// Log.v("DB", "Got pos: " + pos + " for FDB=" + fdb);
			if (pos >= 0) {
				mFDB.setSelection(pos);
			} else {
				mFDB.setSelection(0);
			}
		} else {
			mFDB.setSelection(0);
			// Log.v("FDB", "reset userPressed = " + this.userPressedFDB);
		}
	}

	private static void addDriveToList(String file, String type) {
		// Check if exists
		// Log.v(TAG, "Adding To list: " + type + ":" + file);
		int res = LimboActivity.favDB.getFavUrlSeq(file, type);
		if (res == -1) {
			if (type.equals("cd")) {
				mCD.getAdapter().getCount();
			} else if (type.equals("fda")) {
				mFDA.getAdapter().getCount();
			} else if (type.equals("fdb")) {
				mFDB.getAdapter().getCount();
			}
			LimboActivity.favDB.insertFavURL(file, type);
		}

	}

	public static void browse(String fileType) {
		// Check if SD card is mounted
		// Log.v(TAG, "Browsing: " + fileType);
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			Toast.makeText(activity.getApplicationContext(),
					"Error: SD card is not mounted", Toast.LENGTH_LONG).show();
			return;
		}

		String dir = null;
		// GET THE LAST ACCESSED DIR FROM THE REG
		String lastDir = LimboSettingsManager.getLastDir(activity);
		try {
			Intent i = null;
			i = getFileManIntent();
			Bundle b = new Bundle();
			b.putString("lastDir", lastDir);
			b.putString("fileType", fileType);
			i.putExtras(b);
			// Log.v("**PASS** ", lastDir);
			activity.startActivityForResult(i, Const.FILEMAN_REQUEST_CODE);

		} catch (Exception e) {
			// Log.v(TAG, "Error while starting Filemanager: " +
			// e.getMessage());
		}
	}
	
	public static Intent getFileManIntent() {
		return new Intent(activity,
				com.max2idea.android.limbo.main.LimboFileManager.class);
	}

}

