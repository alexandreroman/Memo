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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;

public class ViewWidgetProvider extends AppWidgetProvider {
	static String location = "";
	public static String ACTION_WIDGET_MEMO = "comNgarsideMemoActionWidgetMemo";
	public static String ACTION_WIDGET_PEN = "comNgarsideMemoActionWidgetPen";
	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		location = ViewWidgetConfigureActivity.GetImage(context, appWidgetId);
		Bitmap bitmap = BitmapFactory.decodeFile(location);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_widget_layout);
        remoteViews.setImageViewBitmap(R.id.imageView, bitmap);
		Intent memoIntent = new Intent(context, MemoActivity.class);
		memoIntent.setAction(ACTION_WIDGET_MEMO);
		memoIntent.putExtra("location", location);
        memoIntent.putExtra("isFromGallery", false);
		PendingIntent memoPendingIntent = PendingIntent.getActivity(context, 0, memoIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.iconBtn, memoPendingIntent);
		Intent penIntent = new Intent(context, ViewWidgetProvider.class);
		penIntent.setAction(ACTION_WIDGET_PEN);
		PendingIntent penPendingIntent = PendingIntent.getBroadcast(context, 0, penIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.penBtn, penPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_widget_layout);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else {
			if (intent.getAction().equals(ACTION_WIDGET_PEN)) {
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				ComponentName thisAppWidget = new ComponentName(context.getPackageName(), ViewWidgetProvider.class.getName());
				int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
				refreshAppWidget(context, appWidgetManager, appWidgetIds);
			}
			super.onReceive(context, intent);
		}
	}
	static void refreshAppWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Bitmap bitmap = BitmapFactory.decodeFile(location);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_widget_layout);
        remoteViews.setImageViewBitmap(R.id.imageView, bitmap);
		Intent memoIntent = new Intent(context, MemoActivity.class);
		memoIntent.setAction(ACTION_WIDGET_MEMO);
		memoIntent.putExtra("location", location);
        memoIntent.putExtra("isFromGallery", false);
		PendingIntent memoPendingIntent = PendingIntent.getActivity(context, 0, memoIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.iconBtn, memoPendingIntent);
		Intent penIntent = new Intent(context, ViewWidgetProvider.class);
		penIntent.setAction(ACTION_WIDGET_PEN);
		PendingIntent penPendingIntent = PendingIntent.getBroadcast(context, 0, penIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.penBtn, penPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
}