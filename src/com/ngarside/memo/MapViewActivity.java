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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class MapViewActivity extends MapActivity {
	private MapController myMapController;
	String file = "";
    public static String DEFAULT_APP_DIRECTORY = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MapViewActivity.this);
        DEFAULT_APP_DIRECTORY = settings.getString("storageLocation", "/sdcard/Memo");
		getActionBar().setDisplayHomeAsUpEnabled(true);
		MapView mapView = (MapView) findViewById(R.id.mapview);
		myMapController = mapView.getController();
	    mapView.setBuiltInZoomControls(true);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.insert, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
			public boolean onQueryTextChange(String text) {
				return false;
			}
			public boolean onQueryTextSubmit(String text) {
				Geocoder geo = new Geocoder(getApplicationContext());
				try {
					List<Address> add = geo.getFromLocationName(text, 1);
					if (add.size() > 0) {
						GeoPoint point = new GeoPoint((int) (add.get(0).getLatitude()*1000000), (int) (add.get(0).getLongitude()*1000000));
						myMapController.animateTo(point);
						myMapController.setZoom(20);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}
        });
        return true;
    }
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
	@Override
	public void finish() {
		Bundle bundle = new Bundle();
	    bundle.putString("file", file);
	    Intent intent = new Intent();
	    intent.putExtras(bundle);
	    setResult(RESULT_OK, intent);
	    super.finish();
	}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case android.R.id.home:
        		finish();
        		return true;
            case R.id.menuInsert:
            	MapView browserView = (MapView) findViewById(R.id.mapview);
                browserView.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(browserView.getDrawingCache());
                browserView.setDrawingCacheEnabled(false);
            	File mFolder = new File(DEFAULT_APP_DIRECTORY + "/.temp");
                if (!(mFolder.exists())) {
                    mFolder.mkdirs();
                }
            	String filename = mFolder.getPath() + '/' + FileUtilities.getUniqueFilename(mFolder, "image", "png");
                File imageFile = new File(filename);
                OutputStream fout = null;
                try {
                    fout = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                    fout.flush();
                    fout.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                	file = filename;
                }
            	finish();
        		return true;
            case R.id.menuCancel:
            	finish();
        		return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}