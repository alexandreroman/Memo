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
import java.io.InputStream;
import java.io.OutputStream;

import com.ngarside.memo.ColorPickerDialog.OnColorChangedListener;
import com.samsung.sdraw.CanvasView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingsActivity extends PreferenceActivity {
    public static String DEFAULT_APP_DIRECTORY;
	ListView list;
	String moveStorageLocation = "";
    private static final int SELECT_PICTURE = 1341;
    int FOLDER_RETURN = 214;
    boolean justOpened = false;
	String[] moveLocationArray;
	Spinner moveStorageLocationSpinner;
	Spinner backgroundTypeSpinner;
    boolean copyToNewStorageLocation = false;
    boolean deleteOldStorageLocation = false;
    int bgcolor = 0;
    SeekBar seekbar;
    ImageView colourView;
    SharedPreferences settings;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		addPreferencesFromResource(R.xml.preferences);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		settings = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        DEFAULT_APP_DIRECTORY = settings.getString("storageLocation", "/sdcard/Memo");
        moveLocationArray = new String[] {DEFAULT_APP_DIRECTORY, getString(R.string.choose_new_location)};
		findPreference("offset").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
	    		final Dialog dialog = new Dialog(SettingsActivity.this);
	    		dialog.setContentView(R.layout.offset_dialog);
	    		dialog.setTitle(getString(R.string.custom_offset_title));
	    		final TextView textX = (TextView) dialog.findViewById(R.id.editText1);
	    		final TextView textY = (TextView) dialog.findViewById(R.id.editText2);
	    		textY.setOnEditorActionListener(new EditText.OnEditorActionListener() {
					public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_DONE) {
		    	    	    SharedPreferences.Editor editor = settings.edit();
		    	    	    if (textX.getText().length() > 0) {
		    	    	    	editor.putInt("offsetX", Integer.parseInt(textX.getText().toString()));
		    	    	    }
		    	    	    if (textY.getText().length() > 0) {
		    	    	    	editor.putInt("offsetY", Integer.parseInt(textY.getText().toString()));
		    	    	    }
		    	    	    editor.commit();
		    	    	    dialog.dismiss();
						}
						return false;
					}
	    		});
	    		textX.setText(Integer.toString(settings.getInt("offsetX", 0)));
	    		textY.setText(Integer.toString(settings.getInt("offsetY", 0)));
	    		dialog.show();
	    		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	    		Button btn = (Button) dialog.findViewById(R.id.button1);
	    		btn.setOnClickListener(new OnClickListener() {
	    	        public void onClick(View v) {
	    	    	    SharedPreferences.Editor editor = settings.edit();
	    	    	    if (textX.getText().length() == 0) {
	    	    	    	editor.putInt("offsetX", 0);
	    	    	    } else {
	    	    	    	editor.putInt("offsetX", Integer.parseInt(textX.getText().toString()));
	    	    	    }
	    	    	    if (textY.getText().length() == 0) {
	    	    	    	editor.putInt("offsetY", 0);
	    	    	    } else {
	    	    	    	editor.putInt("offsetY", Integer.parseInt(textY.getText().toString()));
	    	    	    }
	    	    	    editor.commit();
	    	    	    dialog.dismiss();
	    	        }
	    	    });
	    		Button btn2 = (Button) dialog.findViewById(R.id.button2);
	    		btn2.setOnClickListener(new OnClickListener() {
	    	        public void onClick(View v) {
	    	        	dialog.dismiss();
	    	        }
	    	    });
				return true;
			}
		});
		findPreference("spenButton").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
    	        int selected = 0;
    	        if (settings.getInt("penButtonAction", -1) == CanvasView.PEN_MODE) {
    	        	selected = 1;
    	        } else if (settings.getInt("penButtonAction", -1) == CanvasView.ERASER_MODE) {
    	        	selected = 2;
    	        }else if (settings.getInt("penButtonAction", -1) == -3) {
    	        	selected = 3;
    	        }
    			AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
    			builder.setTitle(getString(R.string.choose_action_title));
    			builder.setSingleChoiceItems(R.array.spen_button_array, selected, new DialogInterface.OnClickListener() {
    			    public void onClick(DialogInterface dialog, int item) {
    		    	    SharedPreferences.Editor editor = settings.edit();
    		    	    if (item == 0) {
    		    	    	editor.putInt("penButtonAction", -1);
    		    	    } else if (item == 1) {
    		    	    	editor.putInt("penButtonAction", CanvasView.PEN_MODE);
    		    	    } else if (item == 2) {
    		    	    	editor.putInt("penButtonAction", CanvasView.ERASER_MODE);
    		    	    } else {
    		    	    	editor.putInt("penButtonAction", -3);
    		    	    }
    		    	    editor.commit();
    		    	    dialog.dismiss();
    			    }
    			});
    			builder.setNegativeButton(getString(R.string.cancel), null);
    			AlertDialog alert = builder.create();
    			alert.show();
				return true;
			}
		});
		findPreference("finger").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
    	        int selected = 0;
    	        if (settings.getInt("fingerAction", -2) == -2) {
    	        	selected = 1;
    	        } else if (settings.getInt("fingerAction", -2) == CanvasView.PEN_MODE) {
    	        	selected = 2;
    	        } else if (settings.getInt("fingerAction", -2) == CanvasView.ERASER_MODE) {
    	        	selected = 3;
    	        } else if (settings.getInt("fingerAction", -2) == -3) {
    	        	selected = 4;
    	        }
    			AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
    			builder.setTitle(getString(R.string.choose_action_title));
    			builder.setSingleChoiceItems(R.array.finger_array, selected, new DialogInterface.OnClickListener() {
    			    public void onClick(DialogInterface dialog, int item) {
    		    	    SharedPreferences.Editor editor = settings.edit();
    		    	    if (item == 0) {
    		    	    	editor.putInt("fingerAction", -1);
    		    	    } else if (item == 1) {
    		    	    	editor.putInt("fingerAction", -2);
    		    	    } else if (item == 2) {
    		    	    	editor.putInt("fingerAction", CanvasView.PEN_MODE);
    		    	    } else if (item == 3) {
    		    	    	editor.putInt("fingerAction", CanvasView.ERASER_MODE);
    		    	    } else {
    		    	    	editor.putInt("fingerAction", -3);
    		    	    }
    		    	    editor.commit();
    		    	    dialog.dismiss();
    			    }
    			});
    			builder.setNegativeButton(getString(R.string.cancel), null);
    			AlertDialog alert = builder.create();
    			alert.show();
				return true;
			}
		});
		findPreference("dominantHand").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
    	        int selected = settings.getInt("dominantHand", 0);
    			AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
    			builder.setTitle(getString(R.string.set_dominant_hand_title));
    			builder.setSingleChoiceItems(R.array.dominant_hand_array, selected, new DialogInterface.OnClickListener() {
    			    public void onClick(DialogInterface dialog, int item) {
    		    	    SharedPreferences.Editor editor = settings.edit();
    		    	    editor.putInt("dominantHand", item);
    		    	    editor.commit();
    		    	    dialog.dismiss();
    			    }
    			});
    			builder.setNegativeButton(getString(R.string.cancel), null);
    			AlertDialog alert = builder.create();
    			alert.show();
				return true;
			}
		});
		findPreference("defaultBackground").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
	    		final Dialog dialog = new Dialog(SettingsActivity.this);
	    		dialog.setContentView(R.layout.background_dialog);
	    		dialog.setTitle(getString(R.string.background));
	    		dialog.show();
	            bgcolor = settings.getInt("backgroundColour", 0);
	    		backgroundTypeSpinner = (Spinner) dialog.findViewById(R.id.typeSpinner);
	    		seekbar = (SeekBar) dialog.findViewById(R.id.seekBar);
	    		seekbar.setProgress(settings.getInt("backgroundTileSize", 20));
	    		ArrayAdapter<String> backgroundTypeAdapter = new ArrayAdapter<String>(SettingsActivity.this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.background_type_array));
	    		backgroundTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    		backgroundTypeSpinner.setAdapter(backgroundTypeAdapter);
	    		backgroundTypeSpinner.setSelection(settings.getInt("backgroundType", 0));
	    		colourView = (ImageView) dialog.findViewById(R.id.colourView);
	    		colourView.setBackgroundColor(bgcolor);
	    		colourView.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						new ColorPickerDialog(SettingsActivity.this, new OnColorChangedListener() {
							public void colorChanged(String key, int color) {
								bgcolor = color;
					    		colourView.setBackgroundColor(bgcolor);
							}
					    }, "backgroundColour", bgcolor, bgcolor).show();
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
	    	    	    SharedPreferences.Editor editor = settings.edit();
	    		    	editor.putInt("backgroundType", backgroundTypeSpinner.getSelectedItemPosition());
	    		    	int bgSize = seekbar.getProgress();
	    		    	if (bgSize < 2) {
	    		    		bgSize = 2;
	    		    	}
	    		    	editor.putInt("backgroundTileSize", bgSize);
	    		    	editor.putInt("backgroundColour", bgcolor);
	    	    	    editor.commit();
	    	    	    dialog.dismiss();
	    	        }
	    		});
				return true;
			}
		});
		findPreference("fullscreen").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
    			AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
    			builder.setTitle(getString(R.string.fullscreen_dialog_title));
    			builder.setSingleChoiceItems(R.array.fullscreen_array, settings.getInt("useFullscreen", 0), new DialogInterface.OnClickListener() {
    			    public void onClick(DialogInterface dialog, int item) {
    		    	    SharedPreferences.Editor editor = settings.edit();
    		    	    editor.putInt("useFullscreen", item);
    		    	    editor.commit();
    		    	    dialog.dismiss();
    			    }
    			});
    			builder.setNegativeButton(getString(R.string.cancel), null);
    			AlertDialog alert21 = builder.create();
    			alert21.show();
				return true;
			}
		});
		findPreference("orientation").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
    			AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
    			builder.setTitle(getString(R.string.orientation_dialog_title));
    			builder.setSingleChoiceItems(R.array.orientation_array, settings.getInt("orientation", 0), new DialogInterface.OnClickListener() {
    			    public void onClick(DialogInterface dialog, int item) {
    		    	    SharedPreferences.Editor editor = settings.edit();
    		    	    editor.putInt("orientation", item);
    		    	    editor.commit();
    		    	    dialog.dismiss();
    			    }
    			});
    			builder.setNegativeButton(getString(R.string.cancel), null);
    			AlertDialog alert211 = builder.create();
    			alert211.show();
				return true;
			}
		});
		findPreference("deleteTempFiles").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
    	    	new AlertDialog.Builder(SettingsActivity.this)
    	        .setTitle(getString(R.string.here_be_dragons))
    	        .setMessage(getString(R.string.delete_temp_files_warning))
    	        .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                		File tempDirectory = new File(DEFAULT_APP_DIRECTORY + "/.temp");
                		File[] tempFiles = tempDirectory.listFiles();
                		for (File temp : tempFiles) {
                			temp.delete();
                		}
                		tempDirectory.delete();
                    }
                })
    	        .setNegativeButton(getString(R.string.cancel), null)
    	        .show();
				return true;
			}
		});
		findPreference("setStorageLocation").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
    		    copyToNewStorageLocation = false;
    		    deleteOldStorageLocation = false;
	    		final Dialog dialog = new Dialog(SettingsActivity.this);
	    		dialog.setContentView(R.layout.move_storage_dialog);
	    		dialog.setTitle(getString(R.string.move_storage));
	    		dialog.show();
	    		Button btn2112 = (Button) dialog.findViewById(R.id.btnCancel);
	    		btn2112.setOnClickListener(new OnClickListener() {
	    	        public void onClick(View v) {
	    	        	dialog.dismiss();
	    	        }
	    	    });
	    		Button btn2111 = (Button) dialog.findViewById(R.id.btnExport);
	    		btn2111.setOnClickListener(new OnClickListener() {
	    	        public void onClick(View v) {
	    	        	String newLocation = moveLocationArray[moveStorageLocationSpinner.getSelectedItemPosition()];
	    	        	if (newLocation == DEFAULT_APP_DIRECTORY) {
	    	    	    	new AlertDialog.Builder(SettingsActivity.this)
	    	    	        .setIcon(R.drawable.ic_launcher)
	    	    	        .setTitle(getString(R.string.error))
	    	    	        .setMessage(getString(R.string.move_storage_error))
	    	    	        .setNegativeButton(getString(R.string.dismiss), null)
	    	    	        .show();
	    	        	} else {
	    		    	    SharedPreferences.Editor editor1 = settings.edit();
	    		    	    editor1.putString("storageLocation", newLocation);
	    		    	    editor1.commit();
	    	        		File oldRoot = new File(DEFAULT_APP_DIRECTORY);
	    	        		File newRoot = new File(newLocation);
	    	        		newRoot.mkdirs();
	    	        		if (copyToNewStorageLocation) {
		    	        		for (File file : oldRoot.listFiles()) {
		    	        			File newFile = new File(newRoot.getAbsolutePath() + "/" + file.getName());
		    	        			if (file.isDirectory()) {
		    	        				newFile.mkdirs();
		    	        				for (File image : file.listFiles()) {
		    	        					File newImage = new File(newFile.getAbsolutePath() + "/" + image.getName());
		    	        					copyFile(image, newImage);
		    	        				}
		    	        			} else {
		    	        				copyFile(file, newFile);
		    	        			}
		    	        		}
	    	        		}
	    	        		if (deleteOldStorageLocation) {
		    	        		for (File file : oldRoot.listFiles()) {
		    	        			if (file.isDirectory()) {
		    	        				for (File image : file.listFiles()) {
		    	        					image.delete();
		    	        				}
		    	        			}
		    	        			file.delete();
		    	        		}
		    	        		oldRoot.delete();
	    	        		}
	    	    	    	new AlertDialog.Builder(SettingsActivity.this)
	    	    	        .setTitle(getString(R.string.move_completed_title))
	    	    	        .setMessage(getString(R.string.move_completed_info))
	    	    	        .setNegativeButton(getString(R.string.dismiss), null)
	    	    	        .show();
	    	        	}
	    	        	dialog.dismiss();
	    	        }
	    	    });
	    		CheckBox copyCheck = (CheckBox) dialog.findViewById(R.id.copyCheck);
	    		copyCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
						copyToNewStorageLocation = arg1;
					}
	    		});
	    		CheckBox deleteCheck = (CheckBox) dialog.findViewById(R.id.deleteCheck);
	    		deleteCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
						deleteOldStorageLocation = arg1;
					}
	    		});
	    		moveStorageLocationSpinner = (Spinner) dialog.findViewById(R.id.locationSpinner);
	    		ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(SettingsActivity.this, android.R.layout.simple_spinner_item, moveLocationArray);
	    		typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    		moveStorageLocationSpinner.setAdapter(typeAdapter);
	    		justOpened = true;
	    		moveStorageLocationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
						if (justOpened) {
							justOpened = false;
						} else {
							if (index == moveLocationArray.length - 1) {
				                Intent intent = new Intent(getApplicationContext(), FolderActivity.class);
				                startActivityForResult(intent, FOLDER_RETURN);
							} else {
								moveStorageLocation = moveLocationArray[index];
							}
						}
					}
					public void onNothingSelected(AdapterView<?> arg0) {
					}
	    		});
				return true;
			}
		});
		findPreference("about").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
    	    	new AlertDialog.Builder(SettingsActivity.this)
    	        .setIcon(R.drawable.ic_launcher)
    	        .setTitle(getString(R.string.about_dialog_title))
    	        .setMessage(getString(R.string.about_dialog_info))
    	        .setNegativeButton("Dismiss", null)
    	        .show();
				return true;
			}
		});
		findPreference("changeLog").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
	    		final Dialog dialog = new Dialog(SettingsActivity.this);
	    		dialog.setContentView(R.layout.changelog_dialog);
	    		dialog.setTitle(getString(R.string.change_log_title));
	    		Button btn = (Button) dialog.findViewById(R.id.button2);
	    		WebView web = (WebView) dialog.findViewById(R.id.textView1);
	    		btn.setOnClickListener(new OnClickListener() {
	    	        public void onClick(View v) {
	    	        	dialog.dismiss();
	    	        }
	    	    });
	    		web.loadUrl("file:///android_asset/changelog.htm");
	    		dialog.show();
				return true;
			}
		});
		findPreference("share").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
    			Intent shareIntent = new Intent(Intent.ACTION_SEND);
    			shareIntent.setType("text/*");
    			shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.ngarside.memo");
    			startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
				return true;
			}
		});
		findPreference("rateAndReview").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
    			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.ngarside.memo")));
				return true;
			}
		});
		findPreference("contribute").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
    			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ngarside/Memo")));
				return true;
			}
		});
		findPreference("license").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
	    		final Dialog dialog = new Dialog(SettingsActivity.this);
	    		dialog.setContentView(R.layout.changelog_dialog);
	    		dialog.setTitle(getString(R.string.gpl_title));
	    		Button btn = (Button) dialog.findViewById(R.id.button2);
	    		WebView web = (WebView) dialog.findViewById(R.id.textView1);
	    		btn.setOnClickListener(new OnClickListener() {
	    	        public void onClick(View v) {
	    	        	dialog.dismiss();
	    	        }
	    	    });
	    		web.loadUrl("file:///android_asset/gpl.htm");
	    		dialog.show();
				return true;
			}
		});
		findPreference("nathanGarside").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
    			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/showthread.php?p=26998582")));
				return true;
			}
		});
		findPreference("wallec").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
    			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://wwalczyszyn.deviantart.com/")));
				return true;
			}
		});
	}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	Intent intent = new Intent(this, ListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
        	if (requestCode == FOLDER_RETURN) {
        		String customFolder = (String) data.getExtras().get("folder");
        		int index = -1;
        		for (int i = 0; i < moveLocationArray.length; i++) {
        			if (moveLocationArray[i].equals(customFolder)) {
        				index = i;
        			}
        		}
        		if (index == -1) {
            		index = 0;
            		String[] temp = new String[moveLocationArray.length + 1];
            		temp[0] = customFolder;
            		for (int i = 0; i < moveLocationArray.length; i++) {
            			temp[i + 1] = moveLocationArray[i];
            		}
            		moveLocationArray = temp;
        		}
	    		ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(SettingsActivity.this, android.R.layout.simple_spinner_item, moveLocationArray);
	    		typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    		moveStorageLocationSpinner.setAdapter(typeAdapter);
	    		moveStorageLocationSpinner.setSelection(index);
        	} else if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                String selectedImagePath = getPath(selectedImageUri);
	    	    SharedPreferences.Editor editor = settings.edit();
	    	    editor.putString("customPageBackground", selectedImagePath);
	    	    editor.commit();
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
    public void copyFile(File oldFile, File newFile) {
    	if (!newFile.exists()) {
    		try {
				newFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	copyfile(oldFile.getAbsolutePath(), newFile.getAbsolutePath());
    }
    void copyfile(String srFile, String dtFile) {
    	try {
    		File f1 = new File(srFile);
    		File f2 = new File(dtFile);
    		InputStream in = new FileInputStream(f1);
    		OutputStream out = new FileOutputStream(f2);
    		byte[] buf = new byte[1024];
    		int len;
    		while ((len = in.read(buf)) > 0) {
    			out.write(buf, 0, len);
    		}
    		in.close();
    		out.close();
    	} catch(FileNotFoundException ex) {
    		System.exit(0);
    	} catch(IOException e) {}
    }
}