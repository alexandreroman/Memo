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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class FileUtilities {
	public static String stringCheck(String str) {
		StringBuilder strbuilder = new StringBuilder();
		int size = str.length();
		for (int i = 0; i < size; i++) {
			char curChar = str.charAt(i);
			if(curChar == '\\' || curChar == '/' || curChar == ':' || curChar == '*' || curChar == '?' || curChar == '"' || curChar == '<' || curChar == '>' || curChar == '|') {
				strbuilder.append('_');
		    } else
		    	strbuilder.append(curChar);
			}
		return strbuilder.toString();
    }
	public static String getUniqueFilename(File folder, String filename, String ext) {
		if (folder == null || filename == null) {
			return null;
		}
		String curFileName;
		File curFile;
		if(filename.length() > 20){
			filename = filename.substring(0, 19);
		}
		filename = stringCheck(filename);
		int i = 1;
		do {
			curFileName = String.format("%s_%02d.%s", filename, i++, ext);
			curFile = new File(folder, curFileName);
		} while (curFile.exists());
		return curFileName;
	}
	public static byte[] readBytedata(String aFilename) {
		byte[] imgBuffer = null;
		FileInputStream fileInputStream = null;
		try {
        	File file = new File(aFilename);
        	fileInputStream = new FileInputStream(file);
        	int byteSize = (int)file.length();
        	imgBuffer = new byte[byteSize];
            if (fileInputStream.read(imgBuffer) == -1 ) {
            	Log.e("Memo", "Failed to read image");
            }
            fileInputStream.close();
		} catch (FileNotFoundException e) {
        	e.printStackTrace();            
        } catch (IOException e2) {
        	e2.printStackTrace();            
        } finally {
        	if(fileInputStream != null) {
	        	try{
	        		fileInputStream.close();
	        	} catch (IOException e) {
	            	e.printStackTrace();            
	            } 
        	}
        }
        return imgBuffer;
	}
	public static boolean writeBytedata(String aFilename, byte[] imgBuffer) {
		FileOutputStream fileOutputStream = null;
		boolean result = true;
		try {
        	File file = new File(aFilename);
        	fileOutputStream = new FileOutputStream(file);
        	fileOutputStream.write(imgBuffer);
            fileOutputStream.close();
		} catch (FileNotFoundException e) {
        	e.printStackTrace();
        	result = false;
        } catch (IOException e2) {
        	e2.printStackTrace();
        	result = false;
        } finally {
        	if(fileOutputStream != null) {
	        	try{
	        		fileOutputStream.close();
	        	} catch (IOException e) {
	            	e.printStackTrace(); 
	            	result = false;
	            } 
        	}        	
        }
		return result;
	}
	public static String getRealPathFromURI(Activity activity, Uri contentUri) { 		
		String[] proj = { MediaStore.Images.Media.DATA }; 
		CursorLoader cursorLoader = new CursorLoader(activity, contentUri, proj, null, null,null); 
		String strFileName="";
		if(cursorLoader!=null) {
			Cursor cursor = cursorLoader.loadInBackground();			
			if(cursor!=null) {
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);					
				cursor.moveToFirst();
				if(cursor.getCount() > 0) {
					strFileName=cursor.getString(column_index);
				}
				cursor.close();
			}
		}
		return strFileName; 		
	}
	public static boolean bIsValidImagePath(String strImagePath){		
		if(strImagePath == null) {			
			return false;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(strImagePath, options);
		if (options.outMimeType != null) {
			return true;
		} else {
			return false;
		}
	}
}