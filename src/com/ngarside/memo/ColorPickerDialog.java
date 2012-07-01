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

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerDialog extends Dialog {
    public interface OnColorChangedListener {
        void colorChanged(String key, int color);
    }
    private OnColorChangedListener mListener;
    private int mInitialColor, mDefaultColor;
    static int width = 0;
    static int height = 0;
    private String mKey;
	private static class ColorPickerView extends View {
		private Paint mPaint;
		private float mCurrentHue = 0;
		private int mCurrentX = 0, mCurrentY = 0;
		private int mCurrentColor, mDefaultColor;
		private final int[] mHueBarColors = new int[258];
		private int[] mMainColors = new int[65536];
		private OnColorChangedListener mListener;
		Context context;
		ColorPickerView(Context c, OnColorChangedListener l, int color, int defaultColor) {
			super(c);
			context = c;
			mListener = l;
			mDefaultColor = defaultColor;
			float[] hsv = new float[3];
			Color.colorToHSV(color, hsv);
			mCurrentHue = hsv[0];
			updateMainColors();
			mCurrentColor = color;
			int index = 0;
			for (float i=0; i<256; i += 256/42) {
				mHueBarColors[index] = Color.rgb(255, 0, (int) i);
				index++;
			}
			for (float i=0; i<256; i += 256/42) {
				mHueBarColors[index] = Color.rgb(255-(int) i, 0, 255);
				index++;
			}
			for (float i=0; i<256; i += 256/42) {
				mHueBarColors[index] = Color.rgb(0, (int) i, 255);
				index++;
			}
			for (float i=0; i<256; i += 256/42) {
				mHueBarColors[index] = Color.rgb(0, 255, 255-(int) i);
				index++;
			}
			for (float i=0; i<256; i += 256/42) {
				mHueBarColors[index] = Color.rgb((int) i, 255, 0);
				index++;
			}
			for (float i=0; i<256; i += 256/42) {
				mHueBarColors[index] = Color.rgb(255, 255-(int) i, 0);
				index++;
			}
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setTextAlign(Paint.Align.CENTER);
			mPaint.setTextSize(25);
		}
		private int getCurrentMainColor() {
			int translatedHue = (int) mCurrentHue;
			int index = 0;
			for (float i=0; i<256; i += 256/42) {
				if (index == translatedHue) {
					return Color.rgb(255, 0, (int) i);
				}
				index++;
			}
			for (float i=0; i<256; i += 256/42) {
				if (index == translatedHue) {
					return Color.rgb(255-(int) i, 0, 255);
				}
				index++;
			}
			for (float i=0; i<256; i += 256/42) {
				if (index == translatedHue) {
					return Color.rgb(0, (int) i, 255);
				}
				index++;
			}
			for (float i=0; i<256; i += 256/42) {
				if (index == translatedHue) {
					return Color.rgb(0, 255, 255-(int) i);
				}
				index++;
			}
			for (float i=0; i<256; i += 256/42) {
				if (index == translatedHue) {
					return Color.rgb((int) i, 255, 0);
				}
				index++;
			}
			for (float i=0; i<256; i += 256/42) {
				if (index == translatedHue) {
					return Color.rgb(255, 255-(int) i, 0);
				}
				index++;
			}
			return Color.RED;
		}
		private void updateMainColors() {
			int mainColor = getCurrentMainColor();
			int index = 0;
			int[] topColors = new int[256];
			for (int y=0; y<256; y++) {
				for (int x=0; x<256; x++) {
					if (y == 0) {
						mMainColors[index] = Color.rgb(255-(255-Color.red(mainColor))*x/255, 255-(255-Color.green(mainColor))*x/255, 255-(255-Color.blue(mainColor))*x/255);
						topColors[x] = mMainColors[index];
					} else {
						mMainColors[index] = Color.rgb((255-y)*Color.red(topColors[x])/255, (255-y)*Color.green(topColors[x])/255, (255-y)*Color.blue(topColors[x])/255);
					}
					index++;
				}
			}
		}
		@Override
		protected void onDraw(Canvas canvas) {
			width = canvas.getWidth();
			int translatedHue = (int) mCurrentHue;
			for (int x=0; x<256; x++) {
				if (translatedHue != x) {
					mPaint.setColor(mHueBarColors[x]);
					mPaint.setStrokeWidth(width / 256);
				} else {
					mPaint.setColor(Color.BLACK);
					mPaint.setStrokeWidth((width / 256) * 3);
				}
				int xPosition = ((width / 256) * x) + 50;
				canvas.drawLine(xPosition, 20, xPosition, 80, mPaint);
			}
			for (int x=0; x<256; x++) {
				int xPosition = ((width / 256) * x) + 50;
				int[] colors = new int[2];
				colors[0] = mMainColors[x];
				colors[1] = Color.BLACK;
				Shader shader = new LinearGradient(0, 50, 0, 400, colors, null, Shader.TileMode.REPEAT);
				mPaint.setShader(shader);
				canvas.drawLine(xPosition, 100, xPosition, 400, mPaint);
			}
			mPaint.setShader(null);
			if (mCurrentX != 0 && mCurrentY != 0) {
				mPaint.setStyle(Paint.Style.STROKE);
				mPaint.setColor(Color.BLACK);
				canvas.drawCircle(mCurrentX, mCurrentY, 10, mPaint);
			}
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setColor(mDefaultColor);
			canvas.drawRect(50, 420, (width / 2) - 10, 500, mPaint);
			int a = Color.red(mDefaultColor)+Color.green(mDefaultColor)+Color.blue(mDefaultColor);
			if (a < 384 && a > 0) {
				mPaint.setColor(Color.WHITE);
			} else {
				mPaint.setColor(Color.BLACK);
			}
			canvas.drawText(context.getString(R.string.cancel), (width / 4) + 20, 470, mPaint);
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setColor(mCurrentColor);
			canvas.drawRect((width / 2) + 10, 420, width - 50, 500, mPaint);
			int b = Color.red(mCurrentColor)+Color.green(mCurrentColor)+Color.blue(mCurrentColor);
			if (b < 384 && b > 0) {
				mPaint.setColor(Color.WHITE);
			} else {
				mPaint.setColor(Color.BLACK);
			}
			canvas.drawText(context.getString(R.string.confirm), ((width / 4) * 3) - 20, 470, mPaint);
		}
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();
			if (x > 50 && x < width + 50 && y > 20 && y < 80) {
				mCurrentHue = 255 / (width / x);
				System.out.println(mCurrentHue);
				updateMainColors();
				int transX = mCurrentX - 50;
				int transY = mCurrentY - 20;
				int index = 256 * (transY - 1) + transX;
				if (index > 0 && index < mMainColors.length) {
					mCurrentColor = mMainColors[256 * (transY - 1) + transX];
				}
			}
			if (x > 50 && x < width - 50 && y > 100 && y < 400) {
				mCurrentX = (int) x;
				mCurrentY = (int) y;
				int transX = mCurrentX - 50;
				int transY = mCurrentY - 100;
				int index = 256 * (transY - 1) + transX;
				if (index > 0 && index < mMainColors.length) {
					mCurrentColor = mMainColors[index];
				}
			}
			if (x > 20 && x < (width / 2) - 10 && y > 420 && y < 500 && event.getAction() == MotionEvent.ACTION_DOWN) {
				mListener.colorChanged("", mDefaultColor);
			}
			if (x > (width / 2) + 10 && x < width + 50 && y > 420 && y < 500 && event.getAction() == MotionEvent.ACTION_DOWN) {
				mListener.colorChanged("", mCurrentColor);
			}
			invalidate();
			return true;
		}
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			setMeasuredDimension(widthMeasureSpec, 520);
		}
	}
    public ColorPickerDialog(Context context, OnColorChangedListener listener, String key, int initialColor, int defaultColor) {
        super(context);
        mListener = listener;
        mKey = key;
        mInitialColor = initialColor;
        mDefaultColor = defaultColor;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(String key, int color) {
                mListener.colorChanged(mKey, color);
                dismiss();
            }
        };
        setContentView(new ColorPickerView(getContext(), l, mInitialColor, mDefaultColor));
        setTitle(this.getContext().getString(R.string.colour_dialog_title));
    }
}