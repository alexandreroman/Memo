/*
(C) Copyright 2012 Nathan Garside

This file is part of Memo.

Memo is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Memo is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Memo. If not, see <http://www.gnu.org/licenses/>.
*/

package com.ngarside.memo;

import java.io.File;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class FolderActivity extends Activity {
	ListView list;
	String folder;
	String[] items;
	Button upButton;
	Button cancelButton;
	Button newButton;
	Button confirmButton;
	boolean isFolderEmpty;
    int FOLDER_RETURN = 214;
    public static String DEFAULT_APP_DIRECTORY = "";
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.folder_activity);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(FolderActivity.this);
        DEFAULT_APP_DIRECTORY = settings.getString("storageLocation", "/sdcard/Memo");
        list = (ListView) findViewById(R.id.list);
        upButton = (Button) findViewById(R.id.upButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        newButton = (Button) findViewById(R.id.newButton);
        confirmButton = (Button) findViewById(R.id.confirmButton);
        upButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				File temp = new File(folder);
				if (temp.getParent() == null) {
					Toast.makeText(getApplicationContext(), getString(R.string.at_root_directory), Toast.LENGTH_SHORT).show();
				} else {
					RefreshList(temp.getParent());
				}
			}
        });
        cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
        });
        newButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				NewFolder();
			}
        });
        confirmButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
        });
        list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
				if (!isFolderEmpty) {
					RefreshList(folder + "/" + items[index]);
				}
			}
        });
        RefreshList(DEFAULT_APP_DIRECTORY);
	}
	void NewFolder() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.add_folder));
		final EditText input = new EditText(this);
		alert.setView(input);
		alert.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				if (value.length() > 0) {
					File temp = new File(folder, value);
					if (!temp.exists()) {
						temp.mkdirs();
						RefreshList(temp.getAbsolutePath());
					}
				}
			}
		});
		alert.setNegativeButton(getString(R.string.cancel), null);
		final AlertDialog dialog = alert.show();
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			}
		});
	}
	@Override
	public void finish() {
		Bundle bundle = new Bundle();
	    bundle.putString("folder", folder);
	    Intent intent = new Intent();
	    intent.putExtras(bundle);
	    setResult(RESULT_OK, intent);
	    super.finish();
	}
	void RefreshList(String location) {
		if (location.startsWith("//")) {
			location = location.substring(1);
		}
    	isFolderEmpty = false;
        folder = location;
        setTitle(location);
        items = GetFolders(location);
        Arrays.sort(items);
        if (items.length == 0) {
        	items = new String[1];
        	items[0] = getString(R.string.empty_folder);
        	isFolderEmpty = true;
        }
		list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
	}
    private String[] GetFolders(String location) {
    	File mFolder = new File(location);
    	String[] tempNames = mFolder.list();
    	File[] tempFiles = mFolder.listFiles();
    	int n = 0;
    	if (tempFiles == null) {
    		tempNames = new String[0];
    		return tempNames;
    	}
    	for (int i = 0; i < tempFiles.length; i++) {
    		if (tempFiles[i].isDirectory() && !tempFiles[i].getName().startsWith(".")) {
    			n++;
    		}
    	}
    	File[] tfiles = new File[n];
    	n = 0;
    	for (int i = 0; i < tempFiles.length; i++) {
    		if (tempFiles[i].isDirectory() && !tempFiles[i].getName().startsWith(".")) {
    			tfiles[n] = tempFiles[i];
    			n++;
    		}
    	}
    	String[] tNames = new String[tfiles.length];
    	int i = 0;
    	for (File f : tfiles) {
    		tNames[i] = f.getName();
    		i++;
    	}
    	return tNames;
    }
}