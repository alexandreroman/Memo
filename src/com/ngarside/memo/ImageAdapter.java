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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private File[] files = null;
    private Bitmap[] bs = null;
    private Bitmap folder = null;
    public static final String DEFAULT_APP_DIRECTORY = "/sdcard/Memo";
    public static final String DEFAULT_APP_IMAGEDATA_DIRECTORY = DEFAULT_APP_DIRECTORY;
    private int px = 0;
    private int py = 0;
    public ImageAdapter(Context c, File[] files, Resources resources) {
        mContext = c;
        this.files = files;
        px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, resources.getDisplayMetrics());
        py = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 170, resources.getDisplayMetrics());
        bs = new Bitmap[files.length];
        for (int i = 0; i < files.length; i++) {
        	try {
        		bs[i] = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(files[i].getAbsolutePath()), px, py, false);
        	} catch (NullPointerException e) {
        		e.printStackTrace();
        	}
        }
    	try {
    		folder = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.folder), px, py, false);
    	} catch (NullPointerException e) {
    		e.printStackTrace();
    	}
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
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(px, py));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        if (files[position].isDirectory()) {
        	imageView.setImageBitmap(folder);
        } else {
        	imageView.setImageBitmap(bs[position]);
        }
        return imageView;
    }
	public long getItemId(int position) {
		return 0;
	}
}