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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.ngarside.memo.ColorPickerDialog.OnColorChangedListener;
import com.samsung.samm.common.SObject;
import com.samsung.samm.common.SObjectImage;
import com.samsung.samm.common.SObjectText;
import com.samsung.sdraw.AbstractSettingView;
import com.samsung.sdraw.CanvasView;
import com.samsung.sdraw.SettingView;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.HistoryUpdateListener;
import com.samsung.spensdk.applistener.SPenTouchListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

public class MemoActivity extends Activity {
    String DEFAULT_APP_DIRECTORY = "";
    String DEFAULT_APP_IMAGEDATA_DIRECTORY = DEFAULT_APP_DIRECTORY;
    String background = "";
    String location = "";
    SCanvasView mCanvasView;
    ImageButton mPenBtn;
    ImageButton mEraserBtn;
    ImageButton mUndoBtn;
    ImageButton mRedoBtn;
    ImageButton mSaveBtn;
    ImageButton shareBtn;
    ImageButton upBtn;
    ImageButton imageBtn;
    ImageButton textBtn;
    SettingView mSettingView;
    File mFolder = null;
    boolean isSaved = true;
    boolean isSharing = false;
    boolean canGoBack = true;
    boolean isFromCamera = false;
    boolean includePageBackgroundShare = false;
    boolean disableBackButton = false;
    boolean isFullscreen = false;
    boolean isLeftHanded = false;
    boolean autoSaveOnExit = false;
	boolean mbSCanvasViewInitialized = false;
    boolean handwritingRecognition = false;
    boolean preventScreenLock = false;
    boolean deleteTempOnExit = false;
    boolean lockToOrientation = false;
    boolean exportBackgroundChecked = false;
    ProgressDialog progressDialog;
    int useFullscreen = 0;
	int REQUEST_CODE_SELECT_IMAGE_OBJECT = 103;
    int CAMERA_REQUEST = 1888;
    int SELECT_PICTURE_BACKGROUND = 1341;
    int offsetX = 0;
    int offsetY = 0;
    int penButtonAction = 0;
    int fingerAction = 0;
    int previousTool = 0;
	int INSERT_IMAGE_FROM_PAGE = 768;
    int backgroundTileSize = 0;
    int pageBackground = 0;
    int backgroundType = 0;
    int tempOrientation = 0;
    int bgcolor = 0;
    int FOLDER_RETURN = 214;
    int PEN = 0;
    int FINGER = 1;
    int mode = 0;
    int penTool = CanvasView.PEN_MODE;
    LinearLayout toolbar;
    SeekBar seekbar;
    ImageView colourView;
	Spinner backgroundTypeSpinner;
    List<Point> positions = new ArrayList<Point>();
    String[] locationArray;
    Spinner exportLocationSpinner;
    SharedPreferences settings;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_activity);
        settings = PreferenceManager.getDefaultSharedPreferences(MemoActivity.this);
        DEFAULT_APP_DIRECTORY = settings.getString("storageLocation", "/sdcard/Memo");
        DEFAULT_APP_IMAGEDATA_DIRECTORY = DEFAULT_APP_DIRECTORY;
        bgcolor = settings.getInt("backgroundColour", 0);
        backgroundTileSize = settings.getInt("backgroundTileSize", 20);
        if (backgroundTileSize < 2) {
        	backgroundTileSize = 2;
        }
        backgroundType = settings.getInt("backgroundType", 0);
        autoSaveOnExit = settings.getBoolean("autoSaveOnExit", false);
        deleteTempOnExit = settings.getBoolean("deleteTempOnExit", false);
        preventScreenLock = settings.getBoolean("preventScreenLock", false);
        offsetX = settings.getInt("offsetX", 0);
        offsetY = settings.getInt("offsetY", 0);
        useFullscreen = settings.getInt("useFullscreen", 0);
        disableBackButton = settings.getBoolean("disableBackButton", false);
        includePageBackgroundShare = settings.getBoolean("includePageBackgroundShare", false);
        penButtonAction = settings.getInt("penButtonAction", -1);
        fingerAction = settings.getInt("fingerAction", -2);
        pageBackground = settings.getInt("pageBackground", 0);
        isLeftHanded = settings.getInt("dominantHand", 0) == 1;
        tempOrientation = settings.getInt("orientation", 0);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        toolbar = (LinearLayout) findViewById(R.id.toolbar);
        if (useFullscreen == 1 || useFullscreen == 3) {
        	toolbar.setVisibility(View.GONE);
        	isFullscreen = true;
        }
        if (useFullscreen > 1) {
        	this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        	isFullscreen = true;
        }
        if (preventScreenLock) {
        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        Bundle extras = getIntent().getExtras();
        if (extras.containsKey("canGoBack")) {
        	canGoBack = extras.getBoolean("canGoBack");
        }
        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
        	canGoBack = false;
        	if (extras.containsKey(Intent.EXTRA_STREAM)) {
        		Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
        		String filename = parseUriToFilename(uri);
        		if (filename != null) {
        			background = filename;
        		}
        	}
        } else if(extras != null) {
        	if (extras.getBoolean("isFromGallery")) {
        		background = extras.getString("location");
        	} else {
        		location = extras.getString("location");
        		String folder = extras.getString("folder");
        		if (folder != null) {
        			if (!folder.equals("")) {
        				DEFAULT_APP_IMAGEDATA_DIRECTORY = folder;
        			}
        		}
        		if (extras.getBoolean("isFromCamera")) {
        			isFromCamera = true;
        		}
        	}
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.saving_dialog));
        progressDialog.setCancelable(false);
        mPenBtn = (ImageButton) findViewById(R.id.settingBtn);
        mPenBtn.setOnClickListener(mPenClickListener);
        mEraserBtn = (ImageButton) findViewById(R.id.eraseBtn);
        mEraserBtn.setOnClickListener(mEraserClickListener);
        mUndoBtn = (ImageButton) findViewById(R.id.undoBtn);
        mUndoBtn.setOnClickListener(undoNredoBtnClickListener);
        mRedoBtn = (ImageButton) findViewById(R.id.redoBtn);
        mRedoBtn.setOnClickListener(undoNredoBtnClickListener);
        mSaveBtn = (ImageButton) findViewById(R.id.saveBtn);
        mSaveBtn.setOnClickListener(saveBtnClickListener);
        shareBtn = (ImageButton) findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(shareBtnClickListener);
        upBtn = (ImageButton) findViewById(R.id.upBtn);
        upBtn.setOnClickListener(upBtnClickListener);
        imageBtn = (ImageButton) findViewById(R.id.imageBtn);
        imageBtn.setOnClickListener(imageBtnClickListener);
        textBtn = (ImageButton) findViewById(R.id.textBtn);
        textBtn.setOnClickListener(textBtnClickListener);
        mCanvasView = (SCanvasView) findViewById(R.id.canvas_view);
        mSettingView = (SettingView) findViewById(R.id.setting_view);
        mCanvasView.setSettingView(mSettingView);
        mCanvasView.setHistoryUpdateListener(historyUpdateListener);
        mFolder = new File(DEFAULT_APP_IMAGEDATA_DIRECTORY);
        mUndoBtn.setEnabled(false);
        mRedoBtn.setEnabled(false);
        mSaveBtn.setEnabled(false);
        SetupTooltips();
        mCanvasView.setOnInitializeFinishListener(mInitializeFinishListener);
        mCanvasView.setSPenTouchListener(new SPenTouchListener() {
    		public boolean onTouchFinger(View view, MotionEvent event) {
    			if (useFullscreen > 0 && !isFullscreen) {
    				HideToolbars();
    				return true;
    			}
    			if (mode == PEN) {
                    mode = FINGER;
    				penTool = mCanvasView.getMode();
    			}
    			switch (fingerAction) {
    				case -1:
    					return true;
    				case CanvasView.PEN_MODE:
        				mCanvasView.changeModeTo(CanvasView.PEN_MODE);
    					break;
    				case CanvasView.ERASER_MODE:
        				mCanvasView.changeModeTo(CanvasView.ERASER_MODE);
    					break;
    			}
        		isSaved = false;
                mSaveBtn.setEnabled(true);
                event.setLocation(event.getAxisValue(MotionEvent.AXIS_X) + offsetX, event.getAxisValue(MotionEvent.AXIS_Y) + offsetY);
    			return false;
    		}
    		public boolean onTouchPen(View view, MotionEvent event) {
    			if (useFullscreen > 0 && !isFullscreen) {
    				HideToolbars();
    				return true;
    			}
    			if (mode == FINGER) {
                    mode = PEN;
    				mCanvasView.changeModeTo(penTool);
    			}
        		isSaved = false;
                mSaveBtn.setEnabled(true);
                if (handwritingRecognition) {
                	positions.add(new Point());
                	positions.get(positions.size() - 1).x = (int) event.getX();
                	positions.get(positions.size() - 1).y = (int) event.getY();
                }
                event.setLocation(event.getAxisValue(MotionEvent.AXIS_X) + offsetX, event.getAxisValue(MotionEvent.AXIS_Y) + offsetY);
    			return false;
    		}
    		public boolean onTouchPenEraser(View view, MotionEvent event) {
        		isSaved = false;
                mSaveBtn.setEnabled(true);
    			return false;
    		}
    		public void onTouchButtonDown(View view, MotionEvent event) {
    			previousTool = mCanvasView.getMode();
    			if (penButtonAction != -1) {
    				mCanvasView.changeModeTo(penButtonAction);
        			penTool = penButtonAction;
    			}
    		}
    		public void onTouchButtonUp(View view, MotionEvent event) {
    			if (penButtonAction != -1) {
    				mCanvasView.changeModeTo(previousTool);
        			penTool = previousTool;
    			}
    		}
    	});
    }
    void SetupTooltips() {
    	upBtn.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				Toast.makeText(getApplicationContext(), getString(R.string.up_tooltip), Toast.LENGTH_SHORT).show();
				return true;
			}
    	});
    	mPenBtn.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				Toast.makeText(getApplicationContext(), getString(R.string.pen_tooltip), Toast.LENGTH_SHORT).show();
				return true;
			}
    	});
    	mEraserBtn.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				Toast.makeText(getApplicationContext(), getString(R.string.eraser_tooltip), Toast.LENGTH_SHORT).show();
				return true;
			}
    	});
    	imageBtn.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				Toast.makeText(getApplicationContext(), getString(R.string.image_tooltip), Toast.LENGTH_SHORT).show();
				return true;
			}
    	});
    	textBtn.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				Toast.makeText(getApplicationContext(), getString(R.string.text_tooltip), Toast.LENGTH_SHORT).show();
				return true;
			}
    	});
    	mUndoBtn.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				Toast.makeText(getApplicationContext(), getString(R.string.undo_tooltip), Toast.LENGTH_SHORT).show();
				return true;
			}
    	});
    	mRedoBtn.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				Toast.makeText(getApplicationContext(), getString(R.string.redo_tooltip), Toast.LENGTH_SHORT).show();
				return true;
			}
    	});
    	mSaveBtn.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				Toast.makeText(getApplicationContext(), getString(R.string.save_tooltip), Toast.LENGTH_SHORT).show();
				return true;
			}
    	});
    	shareBtn.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				Toast.makeText(getApplicationContext(), getString(R.string.share_tooltip), Toast.LENGTH_SHORT).show();
				return true;
			}
    	});
    }
    void HideToolbars() {
        if (useFullscreen == 1 || useFullscreen == 3) {
        	isFullscreen = true;
        	toolbar.setVisibility(View.GONE);
        }
        if (useFullscreen > 1) {
        	isFullscreen = true;
        	this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
    Bitmap generateBackgroundTile() {
    	return generateBackgroundTile(settings.getInt("backgroundType", 0), settings.getInt("backgroundTileSize", 20), settings.getInt("backgroundColour", 0));
    }
    Bitmap generateBackgroundTile(int backgroundType, int backgroundTileSize, int backgroundColour) {
    	int[] colours = new int[backgroundTileSize * backgroundTileSize];
		int i = 0;
    	switch (backgroundType) {
    		case 0:
    			for (i = 0; i < colours.length; i++) {
    				colours[i] = backgroundColour;
    			}
    			break;
    		case 1:
    			i = 0;
    			for (int x = 0; x < backgroundTileSize; x++) {
    				for (int y = 0; y < backgroundTileSize; y++) {
    					if (x < backgroundTileSize - 1 && y < backgroundTileSize - 1) {
    						colours[i] = backgroundColour;
    					} else {
    						int colour = Color.red(backgroundColour)+Color.green(backgroundColour)+Color.blue(backgroundColour);
    						if (colour < 384 && colour > 0) {
    							colours[i] = Color.WHITE;
    						} else {
    							colours[i] = Color.BLACK;
    						}
    					}
    					i++;
    				}
    			}
    			break;
    		case 2:
    			i = 0;
    			for (int x = 0; x < backgroundTileSize; x++) {
    				for (int y = 0; y < backgroundTileSize; y++) {
    					if (x < backgroundTileSize - 1) {
    						colours[i] = backgroundColour;
    					} else {
    						int colour = Color.red(backgroundColour)+Color.green(backgroundColour)+Color.blue(backgroundColour);
    						if (colour < 384 && colour > 0) {
    							colours[i] = Color.WHITE;
    						} else {
    							colours[i] = Color.BLACK;
    						}
    					}
    					i++;
    				}
    			}
    			break;
    	}
    	return Bitmap.createBitmap(colours, backgroundTileSize, backgroundTileSize, Bitmap.Config.ARGB_8888);
    }
    void UpdateBackground(int backgroundType, int backgroundTileSize, int backgroundColour) {
		Bitmap bitmap = generateBackgroundTile(backgroundType, backgroundTileSize, backgroundColour);
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		mCanvasView.setBackgroundDrawable(bd);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.memo, menu);
    	return true;
    }
    private OnColorChangedListener colourListener = new OnColorChangedListener() {
		public void colorChanged(String key, int color) {
			bgcolor = color;
    		colourView.setBackgroundColor(bgcolor);
		}
    };
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    		case R.id.menuDeleteSelected:
    			if (!mCanvasView.deleteSelectedSObject()) {
    				mCanvasView.deleteSelectedObject();
    			}
    			break;
    		case R.id.menuDuplicateSelected:
    			if (mCanvasView.getSelectedSObjectType() == SObject.SOBJECT_LIST_TYPE_IMAGE) {
    				mCanvasView.insertSAMMImage((SObjectImage) mCanvasView.getSelectedSObject(), false);
    			} else if (mCanvasView.getSelectedSObjectType() == SObject.SOBJECT_LIST_TYPE_TEXT) {
    				mCanvasView.insertSAMMText((SObjectText) mCanvasView.getSelectedSObject(), false);
    			}
    			break;
    		case R.id.menuPageBackground:
    			final Dialog dialog = new Dialog(MemoActivity.this);
    			dialog.setContentView(R.layout.background_dialog);
    			dialog.setTitle(getString(R.string.background));
	    		dialog.show();
	    		backgroundTypeSpinner = (Spinner) dialog.findViewById(R.id.typeSpinner);
	    		seekbar = (SeekBar) dialog.findViewById(R.id.seekBar);
	    		seekbar.setProgress(backgroundTileSize);
	    		ArrayAdapter<String> backgroundTypeAdapter = new ArrayAdapter<String>(MemoActivity.this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.background_type_array));
	    		backgroundTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    		backgroundTypeSpinner.setAdapter(backgroundTypeAdapter);
	    		backgroundTypeSpinner.setSelection(backgroundType);
	    		colourView = (ImageView) dialog.findViewById(R.id.colourView);
	    		colourView.setBackgroundColor(bgcolor);
	    		colourView.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						new ColorPickerDialog(MemoActivity.this, colourListener, "backgroundColour", bgcolor, bgcolor).show();
					}
	    		});
	    		Button btn21 = (Button) dialog.findViewById(R.id.btnCancel);
	    		btn21.setOnClickListener(new OnClickListener() {
	    	        public void onClick(View v) {
	    	        	dialog.dismiss();
	    	        }
	    	    });
	    		Button btn211 = (Button) dialog.findViewById(R.id.btnExport);
	    		btn211.setOnClickListener(new OnClickListener() {
	    	        public void onClick(View v) {
	    	        	backgroundTileSize = seekbar.getProgress();
	    	            if (backgroundTileSize < 2) {
	    	            	backgroundTileSize = 2;
	    	            }
	    	            backgroundType = backgroundTypeSpinner.getSelectedItemPosition();
	    	    	    UpdateBackground(backgroundType, backgroundTileSize, bgcolor);
	    	    	    dialog.dismiss();
	    	        }
	    		});
    			break;
    		case R.id.menuToggleOrientationLock:
    			lockToOrientation = !lockToOrientation;
    			item.setChecked(lockToOrientation);
    			if (lockToOrientation) {
    				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    				} else {
    					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    				}
    			} else {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    			}
    			break;
        }
    	return super.onOptionsItemSelected(item);
    }
    public void LoadBackground() {
		Bitmap bitmap = generateBackgroundTile();
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		mCanvasView.setBackgroundDrawable(bd);
    }
    private SObjectImage getImageFromBitmap(final Bitmap bitmap, final boolean compression, final boolean scaleToCanvas) throws IOException {
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        final SObjectImage image = new SObjectImage();
        RectF rect;
        int canvasWidth = mCanvasView.getWidth();
        int canvasHeight = mCanvasView.getHeight();
        if (scaleToCanvas) { 
        	if (imageWidth > 600) {
        		imageWidth = 600;
        		imageHeight /= imageWidth / 600;
        	}
        	if (imageHeight > 800) {
        		imageHeight = 800;
        		imageWidth /= imageHeight / 800;
        	}
        }
        rect = new RectF((canvasWidth - imageWidth) / 2, (canvasHeight - imageHeight) / 2, (canvasWidth - imageWidth) / 2 + imageWidth, (canvasHeight - imageHeight) / 2 + imageHeight);
        image.setRect(rect);
        if (compression) {
            image.setImagePath(compressBitmap(bitmap).getAbsolutePath());
        } else {
            image.setImageBitmap(bitmap);
        }
        return image;
    }
    private File compressBitmap(final Bitmap bitmap) throws IOException {
    	File mFolder = new File(DEFAULT_APP_DIRECTORY + "/.temp");
        if (!(mFolder.exists())) {
            mFolder.mkdirs();
        }
    	String filename = mFolder.getPath() + '/' + FileUtilities.getUniqueFilename(mFolder, "image", "png");
        File file = new File(filename);
        OutputStream output = null;
        try {
            output = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        } finally {
            if (output != null) {
                output.close();
            }
        }
        return file;
    }
    public String parseUriToFilename(Uri uri) {
    	String selectedImagePath = null;
    	String filemanagerPath = uri.getPath();
    	String[] projection = { MediaStore.Images.Media.DATA };
    	Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
    	if (cursor != null) {
    		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    		cursor.moveToFirst();
    		selectedImagePath = cursor.getString(column_index);
    		cursor.close();
    	}
    	if (selectedImagePath != null) {
    		return selectedImagePath;
    	} else if (filemanagerPath != null) {
    		return filemanagerPath;
    	}
    	return null;
  	}
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
    	if (keyCode == KeyEvent.KEYCODE_BACK && isLeftHanded) {
    		keyCode = KeyEvent.KEYCODE_MENU;
    	} else if (keyCode == KeyEvent.KEYCODE_MENU && isLeftHanded) {
    		keyCode = KeyEvent.KEYCODE_BACK;
    	}
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if (disableBackButton) {
        		return true;
        	} else {
            	if (isSaved) {
            		ListActivity.addLocation = location;
                	MemoActivity.this.finish();
            	} else if (autoSaveOnExit) {
                	SaveCanvas();
            		ListActivity.addLocation = location;
                	MemoActivity.this.finish();
            	} else {
                	new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.save_dialog_title))
                    .setMessage(getString(R.string.save_dialog_info))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        	SaveCanvas();
                    		ListActivity.addLocation = location;
                        	MemoActivity.this.finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                    		ListActivity.addLocation = location;
                        	MemoActivity.this.finish();
                        }
                    })
                    .show();
            	}
                return true;
        	}
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
        	if (useFullscreen != 0) {
                if (useFullscreen == 1 || useFullscreen == 3) {
                	toolbar.setVisibility(View.VISIBLE);
                	isFullscreen = false;
                }
                if (useFullscreen > 1) {
                	this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                	isFullscreen = false;
                }
        	}
            openOptionsMenu();
            return true;
        } else {
        	return super.onKeyDown(keyCode, event);
        }
    }
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (deleteTempOnExit) {
			File temp = new File(DEFAULT_APP_DIRECTORY + "/.temp");
			if (temp.exists()) {
				for (File file : temp.listFiles()) {
					file.delete();
				}
				temp.delete();
			}
		}
        if (preventScreenLock) {
        	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
		mCanvasView.closeSAMMLibrary();
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus && mbSCanvasViewInitialized == false) {
			mCanvasView.createSAMMLibrary();
			mCanvasView.setAppID("Memo", 1, 0, "Debug");
			mbSCanvasViewInitialized = true;
	    	if (!background.equals("")) {
				SObjectImage sImageObject = null;
				try {
					sImageObject = getImageFromBitmap(BitmapFactory.decodeFile(background), false, true);
				} catch (IOException e) {
					e.printStackTrace();
				}
				mCanvasView.insertSAMMImage(sImageObject, true);
				background = "";
				isSaved = false;
				mSaveBtn.setEnabled(true);
	    	} else if (!location.equals("")) {
	    		if (!location.equals("")) {
	        		if (isFromCamera) {
	        			isFromCamera = false;
	        			SObjectImage sImageObject = null;
	        			try {
	        				sImageObject = getImageFromBitmap(BitmapFactory.decodeFile(location), false, true);
	        			} catch (IOException e) {
	        				e.printStackTrace();
	        			}
	        			mCanvasView.insertSAMMImage(sImageObject, true);
	        			File b = new File(location);
	        			b.delete();
	        			File a = new File(DEFAULT_APP_DIRECTORY + "/Captured");
	        			a.mkdirs();
	        			location = a.getPath() + '/' + FileUtilities.getUniqueFilename(a, "image", "png");
	    				isSaved = false;
	    				mSaveBtn.setEnabled(true);
	        		} else {
	            		if (!loadSAMMFile(location)) {
	            			java.io.FileInputStream in = null;
	                		try {
	                			in = new java.io.FileInputStream(location);
	                		} catch (FileNotFoundException e) {
	                			e.printStackTrace();
	                		}
	                        mCanvasView.setBackgroundImage(BitmapFactory.decodeStream(in));
	        				isSaved = false;
	        				mSaveBtn.setEnabled(true);
	            		}
	        		}
	        	}
	    	}
	        LoadBackground();
		}
	}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
        	if (requestCode == INSERT_IMAGE_FROM_PAGE) {
        		String imagePath = (String) data.getExtras().get("file");
        		if (imagePath.length() > 0) {
        			if(!FileUtilities.bIsValidImagePath(imagePath)) {
    					return;
    				}
    				SObjectImage sImageObject = null;
    				try {
    					sImageObject = getImageFromBitmap(BitmapFactory.decodeFile(imagePath), false, true);
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    				mCanvasView.insertSAMMImage(sImageObject, true);
    				isSaved = false;
    				mSaveBtn.setEnabled(true);
        		}
        	} else if (requestCode == SELECT_PICTURE_BACKGROUND) {
        		Uri imageFileUri = data.getData();    			
				String path = FileUtilities.getRealPathFromURI(this, imageFileUri);
				if (path != null && path.length() > 0) {
        			File file = new File(path);
        			if (file.exists() ){
            			Bitmap bitmap2 = BitmapFactory.decodeFile(path);
            			BitmapDrawable bd2 = new BitmapDrawable(bitmap2);
            			bd2.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            			mCanvasView.setBackgroundDrawable(bd2);
        			}
    			}
        	} else if (requestCode == REQUEST_CODE_SELECT_IMAGE_OBJECT) {
        		Uri imageFileUri = data.getData();    			
				String imagePath = FileUtilities.getRealPathFromURI(this, imageFileUri);
				if(!FileUtilities.bIsValidImagePath(imagePath)) {
					return;
				}
				SObjectImage sImageObject = null;
				try {
					sImageObject = getImageFromBitmap(BitmapFactory.decodeFile(imagePath), false, true);
				} catch (IOException e) {
					e.printStackTrace();
				}
				mCanvasView.insertSAMMImage(sImageObject, true);
				isSaved = false;
				mSaveBtn.setEnabled(true);
        	} else if (requestCode == CAMERA_REQUEST) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                try {
                	File mFolder = new File(DEFAULT_APP_DIRECTORY + "/.temp");
                	mFolder.mkdirs();
                	String filename = mFolder.getPath() + '/' + FileUtilities.getUniqueFilename(mFolder, "temp", "png");
                    FileOutputStream out = new FileOutputStream(filename);
                    photo.compress(Bitmap.CompressFormat.PNG, 100, out);
                    String imagePath = filename;
                    if(!FileUtilities.bIsValidImagePath(imagePath)) {
    					return;
    				}
    				SObjectImage sImageObject = null;
    				try {
    					sImageObject = getImageFromBitmap(photo, false, true);
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    				mCanvasView.insertSAMMImage(sImageObject, true);
    				isSaved = false;
    				mSaveBtn.setEnabled(true);
    				File a = new File(filename);
    				a.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
            	loadCanvas(data.getExtras().getString("filename"));
        	}
        }
    }
	RectF getDefaultRect(){
		int nScreenWidth = mCanvasView.getWidth();
		int nScreenHeight = mCanvasView.getHeight();    			
		int nBoxRadius = (nScreenWidth>nScreenHeight) ? nScreenHeight/4 : nScreenWidth/4;
		int nCenterX = nScreenWidth/2;
		int nCenterY = nScreenHeight/2;
		nCenterY -= 200;
		return new RectF(nCenterX-nBoxRadius,nCenterY-nBoxRadius,nCenterX+nBoxRadius,nCenterY+nBoxRadius);
	}
    private OnClickListener imageBtnClickListener = new OnClickListener() {
        public void onClick(View v) {
        	AlertDialog.Builder builder = new AlertDialog.Builder(MemoActivity.this);
        	builder.setTitle(getString(R.string.insert_image_title));
        	builder.setItems(R.array.insert_image_array, new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog, int item) {
        	        if (item == 0) {
                        startActivityForResult(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST); 
        	        } else if (item == 1) {
        	        	Intent galleryIntent;
        				galleryIntent = new Intent();
        				galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        				galleryIntent.setType("image/*");
        				startActivityForResult(galleryIntent, REQUEST_CODE_SELECT_IMAGE_OBJECT);
        	        } else if (item == 2) {
                        startActivityForResult(new Intent(getApplicationContext(), MapViewActivity.class), INSERT_IMAGE_FROM_PAGE);
        	        } else {
                        startActivityForResult(new Intent(getApplicationContext(), WebActivity.class), INSERT_IMAGE_FROM_PAGE);
        	        }
        	    }
        	});
        	AlertDialog alert = builder.create();
        	alert.show();
        }
    };
    private OnClickListener textBtnClickListener = new OnClickListener() {
        public void onClick(View v) {
			if (v.equals(textBtn)) {
				mCanvasView.changeModeTo(CanvasView.TEXT_MODE);
				textBtn.setSelected(true);
	            mPenBtn.setSelected(false);
	            mEraserBtn.setSelected(false);
	            if (mSettingView.isShown(AbstractSettingView.TEXT_SETTING_VIEW)) {
                    mSettingView.closeView();
	            } else {
                    mSettingView.showView(AbstractSettingView.TEXT_SETTING_VIEW);
	            }
			}
        }
    };
    public Bitmap combineImages(Bitmap c, Bitmap s) {
        Bitmap cs = null; 
        int width, height = 0; 
        if(c.getWidth() > s.getWidth()) { 
        	width = c.getWidth(); 
        } else { 
        	width = s.getWidth(); 
        } 
        if(c.getHeight() > s.getHeight()) { 
            height = c.getHeight(); 
        } else { 
            height = s.getHeight(); 
        } 
        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 
        Canvas comboImage = new Canvas(cs); 
        comboImage.drawBitmap(c, 0f, 0f, null); 
        comboImage.drawBitmap(s, 0f, 0f, null);
        return cs; 
      } 
    String SaveForSharing(boolean includeBackground) {
    	File mFolder = new File(DEFAULT_APP_DIRECTORY + "/.temp");
        if (!(mFolder.exists())) {
            mFolder.mkdirs();
        }
    	String filename = mFolder.getPath() + '/' + FileUtilities.getUniqueFilename(mFolder, "image", "png");
    	Bitmap image;
    	if (includeBackground) {
    		image = combineImages(TileBitmap(generateBackgroundTile()), mCanvasView.getBitmap(false));
    	} else {
    		image = mCanvasView.getBitmap(false);
    	}
    	try {
    	    FileOutputStream out = new FileOutputStream(filename);
    	    image.compress(Bitmap.CompressFormat.PNG, 90, out);
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
        return filename;
    }
    Bitmap TileBitmap(Bitmap b) {
    	Bitmap.Config conf = Bitmap.Config.ARGB_8888;
    	Bitmap a = Bitmap.createBitmap(mCanvasView.getWidth(), mCanvasView.getHeight(), conf);
		int[] pixels = new int[b.getWidth() * b.getHeight()];
		b.getPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
		int aw = 0;
		int ah = 0;
    	for (int x = 0; x < mCanvasView.getWidth(); x += b.getWidth()) {
    		for (int y = 0; y < mCanvasView.getHeight(); y += b.getHeight()) {
    			if (x + b.getWidth() < mCanvasView.getWidth()) {
    				aw = b.getWidth();
    			} else {
    				aw = mCanvasView.getWidth() - x;
    			}
    			if (y + b.getHeight() < mCanvasView.getHeight()) {
    				ah = b.getHeight();
    			} else {
    				ah = mCanvasView.getHeight() - y;
    			}
    			if (ah >= 0 && aw >= 0 && x >= 0 && y >= 0) {
    				a.setPixels(pixels, 0, b.getWidth(), x, y, aw, ah);
    			}
    		}
    	}
    	return a;
    }
    private OnClickListener shareBtnClickListener = new OnClickListener() {
        public void onClick(View v) {
            progressDialog.setMessage(getString(R.string.save_temp_dialog));
        	progressDialog.show();
        	shareThread = new ShareThread(shareHandler);
        	shareThread.start();
        }
    };
    void FinishSharing() {
    	String file = SaveForSharing(includePageBackgroundShare);
    	progressDialog.dismiss();
        progressDialog.setMessage(getString(R.string.saving_dialog));
    	Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
    	sharingIntent.setType("image/*");
    	Uri uri = Uri.fromFile(new File(file));
    	sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
    	startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_image_title)));
    }
    ShareThread shareThread;
    final Handler shareHandler = new Handler() {
        public void handleMessage(Message msg) {
        	progressDialog.dismiss();
        	FinishSharing();
        }
    };
    private class ShareThread extends Thread {
        Handler mHandler;
        ShareThread(Handler h) {
            mHandler = h;
        }
        public void run() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
            Message msg = mHandler.obtainMessage();
            mHandler.sendMessage(msg);
        }
    }
    void GoUp() {
		ListActivity.addLocation = location;
    	if (canGoBack) {
    		this.finish();
    	} else {
    		Intent intent = new Intent(this, ListActivity.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		startActivity(intent);
    	}
    }
    private OnClickListener upBtnClickListener = new OnClickListener() {
        public void onClick(View v) {
        	if (!isSaved) {
        		if (autoSaveOnExit) {
                	SaveCanvas();
                	GoUp();
        		} else {
        			new AlertDialog.Builder(MemoActivity.this)
        			.setIcon(android.R.drawable.ic_dialog_alert)
        			.setTitle(getString(R.string.save_dialog_title))
        			.setMessage(getString(R.string.save_dialog_info))
        			.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int which) {
        					SaveCanvas();
        					GoUp();
        				}
        			})
        			.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int which) {
        					GoUp();
        				}
        			})
        			.show();
        		}
        	} else {
            	GoUp();
        	}
        }
    };
    private OnClickListener saveBtnClickListener = new OnClickListener() {
        public void onClick(View v) {
        	SaveCanvas();
        }
    };
    public void SaveCanvas() {
        mSaveBtn.setEnabled(false);
        if (location.equals("")) {
        	location = mFolder.getPath() + '/' + FileUtilities.getUniqueFilename(mFolder, "image", "png");
        }
        ContinueSave();
    }
    ProgressThread progressThread;
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
        	progressDialog.dismiss();
            CompleteSave();
        }
    };
    private class ProgressThread extends Thread {
        Handler mHandler;
        ProgressThread(Handler h) {
            mHandler = h;
        }
        public void run() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
            Message msg = mHandler.obtainMessage();
            mHandler.sendMessage(msg);
        }
    }
    public void ContinueSave() {
    	progressDialog.show();
        progressThread = new ProgressThread(handler);
        progressThread.start();
    }
    public void CompleteSave() {
        final byte[] buffer = mCanvasView.getData();
        if (buffer == null) {
            return;
        }
        if (!(mFolder.exists())) {
            mFolder.mkdirs();
        }
        FileUtilities.writeBytedata(location, buffer);
        mCanvasView.saveSAMMFile(location);
        isSaved = true;
        progressDialog.dismiss();
        if (isSharing) {
        	isSharing = false;
        	FinishSharing();
        }
    }
    public boolean loadCanvas(String fileName) {
        String loadPath = mFolder.getPath() + '/' + fileName;
        byte[] buffer = FileUtilities.readBytedata(loadPath);
        if (buffer == null) {
            return false;
        }
        mCanvasView.setData(buffer);
        return true;
    }
    private OnClickListener undoNredoBtnClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (v == mUndoBtn) {
                mCanvasView.undo();
            } else if (v == mRedoBtn) {
                mCanvasView.redo();
            }
            mUndoBtn.setEnabled(mCanvasView.isUndoable());
            mRedoBtn.setEnabled(mCanvasView.isRedoable());
    		isSaved = false;
            mSaveBtn.setEnabled(true);
        }
    };
    OnClickListener mPenClickListener = new OnClickListener() {
        public void onClick(View v) {
        	mCanvasView.changeModeTo(CanvasView.PEN_MODE);
        	mPenBtn.setSelected(true);
        	mEraserBtn.setSelected(false);
        	textBtn.setSelected(false);
        	if (mPenBtn.isSelected()) {
        		if (mSettingView.isShown(AbstractSettingView.PEN_SETTING_VIEW)) {
        			mSettingView.closeView();
        		} else {
        			mSettingView.showView(AbstractSettingView.PEN_SETTING_VIEW);
        		}
        	} else if (mSettingView.isShown(AbstractSettingView.ERASER_SETTING_VIEW)) {
                mSettingView.closeView();
        	}
        }
    };
    OnClickListener mEraserClickListener = new OnClickListener() {
        public void onClick(View v) {
        	mCanvasView.changeModeTo(CanvasView.ERASER_MODE);
            mEraserBtn.setSelected(true);
            mPenBtn.setSelected(false);
        	textBtn.setSelected(false);
            if (mEraserBtn.isSelected()) {
                if (mSettingView.isShown(AbstractSettingView.ERASER_SETTING_VIEW)) {
                    mSettingView.closeView();
                } else {
                    mSettingView.showView(AbstractSettingView.ERASER_SETTING_VIEW);
                }
            } else {
                if (mSettingView.isShown(AbstractSettingView.PEN_SETTING_VIEW)) {
                    mSettingView.closeView();
                }
            }
        }
    };
    private HistoryUpdateListener historyUpdateListener = new HistoryUpdateListener() {
        public void onHistoryChanged(boolean bUndoable, boolean bRedoable) {
            mUndoBtn.setEnabled(bUndoable);
            mRedoBtn.setEnabled(bRedoable);
        }
    };
	boolean loadSAMMFile(String strFileName){
		if (mCanvasView != null && !mCanvasView.isAnimationMode()) {
			return mCanvasView.loadSAMMFile(strFileName, true, true, false);
		}
		return true;
	}
    CanvasView.OnInitializeFinishListener mInitializeFinishListener = new CanvasView.OnInitializeFinishListener() {
        public void onFinish() {
            lockToOrientation = tempOrientation == 1 || tempOrientation == 2;
            if (tempOrientation == 0) {
            	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            } else if (tempOrientation == 1) {
        		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
        		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    };
}