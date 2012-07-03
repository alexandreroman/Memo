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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.view.ContextMenu;  
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ContextMenu.ContextMenuInfo;  
import android.view.WindowManager;

public class ListActivity extends Activity {
    ImageAdapter adapter;
    GridView gridview;
    FolderAdapter folders;
    String folder = "";
    OnNavigationListener mOnNavigationListener;
    boolean showAll = true;
    private static final int SELECT_PICTURE = 1341;
    private static final int CAMERA_REQUEST = 1888;
    int spinnerSelection = 0;
    String DEFAULT_APP_DIRECTORY = "";
    ActionBar actionBar;
    File[] fileFolders;
	List<File[]> images = new ArrayList<File[]>();
	public static String addLocation = "";
    private void AddAll() {
    	fileFolders = GetAllFolders();
    	images = new ArrayList<File[]>();
		File[] temptFiles = GetFiles(new File(DEFAULT_APP_DIRECTORY));
		images.add(temptFiles);
    	for (int i = 0; i < fileFolders.length; i++) {
    		File[] tempFiles = GetFiles(fileFolders[i]);
    		images.add(tempFiles);
    	}
    }
    private File[] GetFiles(File folder) {
    	File[] tempFiles = folder.listFiles();
    	if (tempFiles == null) {
    		tempFiles = new File[0];
    		return tempFiles;
    	}
    	int n = 0;
    	for (int i = 0; i < tempFiles.length; i++) {
    		if (!tempFiles[i].isDirectory() && !tempFiles[i].getName().startsWith(".")) {
    			n++;
    		}
    	}
    	File[] tfiles = new File[n];
    	n = 0;
    	for (int i = 0; i < tempFiles.length; i++) {
    		if (!tempFiles[i].isDirectory() && !tempFiles[i].getName().startsWith(".")) {
    			tfiles[n] = tempFiles[i];
    			n++;
    		}
    	}
    	return tfiles;
    }
    private File[] GetAllFolders() {
    	File mFolder = new File(DEFAULT_APP_DIRECTORY);
    	File[] tempFiles = mFolder.listFiles();
    	int n = 0;
    	if (tempFiles == null) {
    		tempFiles = new File[0];
    		return tempFiles;
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
    	return tfiles;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ListActivity.this);
        DEFAULT_APP_DIRECTORY = settings.getString("storageLocation", "/sdcard/Memo");
        gridview = (GridView) findViewById(R.id.gridview);
        registerForContextMenu(gridview);
        AddAll();
    	ReloadGrid();
        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), MemoActivity.class);
                intent.putExtra("location", adapter.getFile(position).getAbsolutePath());
                intent.putExtra("isFromGallery", false);
                startActivityForResult(intent, 0);
            }
        });
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);
        mOnNavigationListener = new OnNavigationListener() {
              public boolean onNavigationItemSelected(int position, long itemId) {
            	  if (position == 0) {
            		  showAll = true;
            		  folder = "";
                	  spinnerSelection = actionBar.getSelectedNavigationIndex();
            		  ReloadGrid();
            	  } else if (position == 1) {
            		  showAll = false;
            		  folder = "";
                	  spinnerSelection = actionBar.getSelectedNavigationIndex();
            		  ReloadGrid();
            	  } else if (position == folders.getCount() - 1) {
            		  AlertDialog.Builder alert = new AlertDialog.Builder(ListActivity.this);
            		  alert.setTitle(getString(R.string.add_folder));
            		  final EditText input = new EditText(ListActivity.this);
            		  alert.setView(input);
            		  alert.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
            		  public void onClick(DialogInterface dialog, int whichButton) {
            		    String value = input.getText().toString();
            		    if (value.length() > 0) {
            		    	File t = new File(DEFAULT_APP_DIRECTORY + "/" + value);
            		    	if (t.exists()) {
                        		new AlertDialog.Builder(ListActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle(getString(R.string.already_exists_title))
                                .setMessage(getString(R.string.already_exists_description))
                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        		    public void onClick(DialogInterface dialog, int whichButton) {
                                	      actionBar.setSelectedNavigationItem(spinnerSelection);
                          		    }
                          		  })
                                .show();
            		    	} else if (t.getName().startsWith(".")) {
                        		new AlertDialog.Builder(ListActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle(getString(R.string.invalid_filename_title))
                                .setMessage(getString(R.string.invalid_filename_description))
                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        		    public void onClick(DialogInterface dialog, int whichButton) {
                                	      actionBar.setSelectedNavigationItem(spinnerSelection);
                          		    }
                          		  })
                                .show();
            		    	} else {
                		    	t.mkdirs();
                		    	AddAll();
                		    	RefreshFolders();
                		    	File[] f = folders.getFolders();
                		    	for (int i = 0; i < f.length; i++) {
                		    		if (f[i].getAbsolutePath().equals(t.getAbsolutePath())) {
                		    			int n = i + 2;
                	            	      actionBar.setSelectedNavigationItem(n);
                	                	  spinnerSelection = n;
                	            		  showAll = false;
                	            		  folder = folders.getFile(n).getAbsolutePath();
                	            		  ReloadGrid();
                	            	      break;
                		    		}
                		    	}
            		    	}
            		    }
            		  }
            		  });
            		  alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            		    public void onClick(DialogInterface dialog, int whichButton) {
                  	      actionBar.setSelectedNavigationItem(spinnerSelection);
            		    }
            		  });
              	      actionBar.setSelectedNavigationItem(spinnerSelection);
            		  final AlertDialog dialog = alert.show();
            		  input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            			    public void onFocusChange(View v, boolean hasFocus) {
            			        if (hasFocus) {
            			        	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            			        }
            			    }
            			});

            	  } else {
            		  showAll = false;
            		  folder = folders.getFile(position).getAbsolutePath();
                	  spinnerSelection = actionBar.getSelectedNavigationIndex();
            		  ReloadGrid();
            	  }
                return true;
              }
            };
	    	RefreshFolders();
    }
    public void RefreshFolders() {
        folders = new FolderAdapter(getLayoutInflater(), fileFolders, this);
        getActionBar().setListNavigationCallbacks(folders, mOnNavigationListener);
	    actionBar.setSelectedNavigationItem(0);
  	    spinnerSelection = 0;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuNew:
                Intent intent = new Intent(getApplicationContext(), MemoActivity.class);
                intent.putExtra("location", "");
                intent.putExtra("folder", folder);
                startActivity(intent);
                return true;
            case R.id.menuOpen:
            	Intent intent1 = new Intent();
                intent1.setType("image/*");
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent1, SELECT_PICTURE);
            	return true;
            case R.id.menuCapture:
                startActivityForResult(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST); 
            	return true;
            case R.id.menuRefresh:
            	AddAll();
            	RefreshFolders();
        		ReloadGrid();
            	return true;
            case R.id.menuDeleteFolder:
            	if (spinnerSelection == 0 || spinnerSelection == 1) {
            		new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.cant_delete_title))
                    .setMessage(getString(R.string.cant_delete_description))
                    .setPositiveButton(getString(R.string.ok), null)
                    .show();
            	} else {
                	new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(String.format(getString(R.string.delete_title), fileFolders[spinnerSelection - 2].getName()))
                    .setMessage(getString(R.string.delete_confirm))
                    .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        	DeleteFolder();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
            	}
            	return true;
            case R.id.menuSettings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void DeleteFolder() {
    	final File f = new File(fileFolders[spinnerSelection - 2].getAbsolutePath());
    	final File[] im = GetFiles(f);
    	if (im.length == 0) {
    		f.delete();
    		AddAll();
    		RefreshFolders();
    		ReloadGrid();
    	} else {
    		new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(getString(R.string.move_images_title))
            .setMessage(getString(R.string.move_images_description))
            .setPositiveButton(getString(R.string.move), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	for (int i = 0; i < im.length; i++) {
                    	File newFile = new File(DEFAULT_APP_DIRECTORY + "/" + im[i].getName());
                    	if (newFile.exists()) {
                        	File mFolder = new File(DEFAULT_APP_DIRECTORY);
                        	String filename = mFolder.getPath() + '/' + FileUtilities.getUniqueFilename(mFolder, "image", "png");
                        	newFile = new File(filename);
                    	}
                		im[i].renameTo(newFile);
                	}
            		f.delete();
            		AddAll();
            		RefreshFolders();
            		ReloadGrid();
                }
            })
            .setNegativeButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	for (int i = 0; i < im.length; i++) {
                		im[i].delete();
                	}
            		f.delete();
            		AddAll();
            		RefreshFolders();
            		ReloadGrid();
                }
            })
            .show();
    	}
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                String selectedImagePath = getPath(selectedImageUri);
                System.out.println("Image Path : " + selectedImagePath);
                Intent intent = new Intent(getApplicationContext(), MemoActivity.class);
                intent.putExtra("location", selectedImagePath);
                intent.putExtra("isFromGallery", true);
                startActivityForResult(intent, 0);
            } else if (requestCode == CAMERA_REQUEST) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                try {
                	File mFolder = new File(DEFAULT_APP_DIRECTORY);
                	if (!mFolder.exists()) {
                		mFolder.mkdirs();
                        File[] t = new File[fileFolders.length + 1];
                        for (int i = 0; i < fileFolders.length; i++) {
                        	t[i] = fileFolders[i];
                        }
                        t[t.length - 1] = mFolder;
                        fileFolders = t;
                        images.add(new File[0]);
                        RefreshFolders();
                	}
                	String filename = mFolder.getPath() + '/' + FileUtilities.getUniqueFilename(mFolder, "temp", "png");
                    FileOutputStream out = new FileOutputStream(filename);
                    photo.compress(Bitmap.CompressFormat.PNG, 100, out);
                    Intent intent = new Intent(getApplicationContext(), MemoActivity.class);
                    intent.putExtra("location", filename);
                    intent.putExtra("isFromCamera", true);
                    startActivityForResult(intent, 0);
             } catch (Exception e) {
                    e.printStackTrace();
             }
            }
        }
    }
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String temp = cursor.getString(column_index);
        cursor.close();
        return temp;
    }
    @Override
    protected void onResume(){
    	super.onResume();
    	if (!addLocation.equals("")) {
    		for (File[] folder : images) {
    			for (File image : folder) {
    				if (image.getAbsolutePath().equals(addLocation)) {
    					return;
    				}
    			}
    		}
    		File newFile = new File(addLocation);
    		int i = 0;
    		for (File[] tempArray : images) {
       			if (tempArray.length == 0) {
           			if (images.size() == 1 || fileFolders.length == 0 || newFile.getParent().equals(DEFAULT_APP_DIRECTORY)) {
       					File[] tempFile = new File[1];
   						tempFile[0] = newFile;
   						images.set(0, tempFile);
   				    	ReloadGrid();
   						break;
       				} else if (i > 0) {
       					if (newFile.getParent().equals(fileFolders[i - 1].getAbsolutePath())) {
       						File[] tempFile = new File[1];
       						tempFile[0] = newFile;
       						images.set(i, tempFile);
       				    	ReloadGrid();
       						break;
       					}
       				}
       			} else {
       	    		if (newFile.getParent().equals(tempArray[0].getParent())) {
       					boolean add = true;
       	    			for (File image : tempArray) {
       	    				if (image.getAbsolutePath().equals(newFile.getAbsolutePath())) {
       	    					add = false;
       	    					break;
       	    				}
       	    			}
       					if (add) {
       						File[] tempFile = new File[tempArray.length + 1];
       						int n = 0;
       						for (File f : tempArray) {
       							tempFile[n] = f;
       							n++;
       						}
       						tempFile[n] = newFile;
       						images.set(i, tempFile);
       				    	ReloadGrid();
       				    	break;
       					}
       	    		}
       	    	}
       			i++;
    		}
    	}
    }
    private void ReloadGrid() {
    	if (spinnerSelection == 0) {
    		int n = 0;
    		for (int i = 0; i < images.size(); i++) {
    			n += images.get(i).length;
    		}
    		File[] t = new File[n];
    		n = 0;
    		for (int i = 0; i < images.size(); i++) {
        		for (int c = 0; c < images.get(i).length; c++) {
        			t[n] = images.get(i)[c];
        			n++;
        		}
    		}
    		adapter = new ImageAdapter(this, t, getResources());
    	} else {
    		adapter = new ImageAdapter(this, images.get(spinnerSelection - 1), getResources());
    	}
        gridview.setAdapter(adapter);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	menu.setHeaderTitle(getString(R.string.image));
    	menu.add(0, v.getId(), 0, getString(R.string.edit));
    	menu.add(0, v.getId(), 0, getString(R.string.delete));
    	menu.add(0, v.getId(), 0, getString(R.string.share));
    	if (folders.getFolders().length > 0) {
    		menu.add(0, v.getId(), 0, getString(R.string.move));
    	}
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	final File selected = adapter.getFile((int)info.position);
        if (item.getTitle() == getString(R.string.edit)) {
            Intent intent = new Intent(getApplicationContext(), MemoActivity.class);
            intent.putExtra("location", selected.getAbsolutePath());
            intent.putExtra("isFromGallery", false);
            startActivityForResult(intent, 0);
        } else if (item.getTitle() == getString(R.string.delete)) {
        	new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(getString(R.string.delete_confirm_title))
            .setMessage(getString(R.string.delete_confirm_description))
            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	selected.delete();
                	AddAll();
                	ReloadGrid();
                }
            })
            .setNegativeButton(getString(R.string.no), null)
            .show();
        } else if (item.getTitle() == getString(R.string.share)) {
        	Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        	sharingIntent.setType("image/*");
        	Uri uri = Uri.fromFile(selected);
        	sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
        	startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_image_title)));
        } else if (item.getTitle() == getString(R.string.move)) {
        	final File[] f1 = folders.getFolders();
        	final File[] f = new File[f1.length];
        	final CharSequence[] items = new CharSequence[f1.length];
        	if (!selected.getParentFile().getAbsolutePath().equals(DEFAULT_APP_DIRECTORY)) {
        		items[0] = getString(R.string.misc);
        		f[0] = new File(DEFAULT_APP_DIRECTORY);
        	}
        	for (int i = 0; i < f1.length; i++) {
        		if (!selected.getParentFile().getAbsolutePath().equals(f1[i].getAbsolutePath())) {
        			items[i] = f1[i].getName();
            		f[i] = f1[i];
        		}
        	}
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setTitle(getString(R.string.choose_folder));
        	builder.setItems(items, new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog, int item) {
                	File newFile = new File(f[item].getAbsolutePath() + "/" + selected.getName());
                	if (newFile.exists()) {
                    	File mFolder = new File(f[item].getAbsolutePath());
                    	String filename = mFolder.getPath() + '/' + FileUtilities.getUniqueFilename(mFolder, "image", "png");
                    	newFile = new File(filename);
                	}
            		selected.renameTo(newFile);
                	AddAll();
                	ReloadGrid();
        	    }
        	});
        	AlertDialog alert = builder.create();
        	alert.show();
        } else {
        	return false;
        }
        return true;
    }
}