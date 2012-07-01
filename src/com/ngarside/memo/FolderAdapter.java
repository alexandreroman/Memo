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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FolderAdapter extends BaseAdapter {
    File[] files = null;
    LayoutInflater inflater;
    Context context;
    public FolderAdapter(LayoutInflater inflater, File[] folders, Context context) {
    	this.context = context;
        this.inflater = inflater;
    	files = new File[folders.length + 3];
        files[0] = new File("");
        files[1] = new File("");
        files[files.length - 1] = new File("");
    	for (int i = 0; i < folders.length; i++) {
    		files[i + 2] = folders[i];
    	}
    }
    public File[] getFolders() {
    	File[] tempFiles = new File[files.length - 3];
    	for (int i = 0; i < tempFiles.length; i++) {
    		tempFiles[i] = files[i + 2];
    	}
    	return tempFiles;
    }
    public int getCount() {
    	if (files == null) {
    		return 0;
    	} else {
    		return files.length;
    	}
    }
    public Object getItem(int position) {
        return null;
    }
    public File getFile(int position) {
    	return files[position];
    }
    public View getView(int position, View convertView, ViewGroup parent) {
    	View row = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
    	TextView textView = (TextView) row.findViewById(android.R.id.text1);
        if (position == 0) {
        	textView.setText(context.getString(R.string.all));
        } else if (position == 1) {
        	textView.setText(context.getString(R.string.misc));
        } else if (position == files.length - 1) {
        	textView.setText(context.getString(R.string.add_folder_spinner));
        } else {
        	textView.setText(files[position].getName());
        }
        return textView;
    }
	public long getItemId(int position) {
		return 0;
	}
}