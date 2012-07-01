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
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

public class ViewWidgetConfigureActivity extends Activity {
    ImageAdapter adapter;
    GridView gridview;
	String folder;
	String[] items;
	Button upButton;
	boolean isFolderEmpty;
    public static String DEFAULT_APP_DIRECTORY = "";
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	static String selectedImage = "";
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.view_widget_configure);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ViewWidgetConfigureActivity.this);
        DEFAULT_APP_DIRECTORY = settings.getString("storageLocation", "/sdcard/Memo");
        setResult(RESULT_CANCELED);
        gridview = (GridView) findViewById(R.id.gridview);
        upButton = (Button) findViewById(R.id.upButton);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
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
        gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
				if (!isFolderEmpty) {
					File temp = new File(folder + "/" + items[index]);
					if (temp.isDirectory()) {
						RefreshList(folder + "/" + items[index]);
					} else {
						selectedImage = temp.getAbsolutePath();
						final Context context = ViewWidgetConfigureActivity.this;
						 AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
						 ViewWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId);
						 Intent resultValue = new Intent();
						 resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
						 setResult(RESULT_OK, resultValue);
						 finish();
					}
				}
			}
        });
        RefreshList(DEFAULT_APP_DIRECTORY);
	}
	static String GetImage(Context context, int appWidgetId) {
        return selectedImage;
    }
	public ViewWidgetConfigureActivity() {
        super();
    }
	void RefreshList(String location) {
    	isFolderEmpty = false;
        folder = location;
        setTitle(location);
        items = GetFiles(location);
        Arrays.sort(items);
        if (items.length == 0) {
        	items = new String[1];
        	items[0] = getString(R.string.empty_folder);
        	isFolderEmpty = true;
        	File[] files = new File[0];
    		adapter = new ImageAdapter(this, files, getResources());
        } else {
        	File[] files = new File[items.length];
        	for (int i = 0; i < files.length; i++) {
        		files[i] = new File(folder + "/" + items[i]);
        	}
    		adapter = new ImageAdapter(this, files, getResources());
        }
        gridview.setAdapter(adapter);
	}
    private String[] GetFiles(String location) {
    	File mFolder = new File(location);
    	String[] tempNames = mFolder.list();
    	File[] tempFiles = mFolder.listFiles();
    	int n = 0;
    	if (tempFiles == null) {
    		tempNames = new String[0];
    		return tempNames;
    	}
    	for (int i = 0; i < tempFiles.length; i++) {
    		if (!tempFiles[i].getName().startsWith(".")) {
    			n++;
    		}
    	}
    	File[] tfiles = new File[n];
    	n = 0;
    	for (int i = 0; i < tempFiles.length; i++) {
    		if (!tempFiles[i].getName().startsWith(".")) {
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