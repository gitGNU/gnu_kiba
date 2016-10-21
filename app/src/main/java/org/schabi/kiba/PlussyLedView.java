package org.schabi.kiba;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Christian Schabesberger on 13.09.15.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
 * PlussyLedView.java is part of KIBA.
 *
 * KIBA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KIBA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KIBA.  If not, see <http://www.gnu.org/licenses/>.
 */

public class PlussyLedView extends View {
    private static final String TAG = PlussyLedView.class.toString();

    public static final int R_MASK = (0xFF);
    public static final int G_MASK = (0xFF << 8);
    public static final int B_MASK = (0xFF << 16);

    private class LedTuple {
        public PointF position;
        public int ledNumber;
        public int color;
    }

    private Paint ledPaint;
    private Paint cursorPaint;
    private float width;
    private float height;
    private float length;
    private float groupLength;
    private float ledAlign;
    private float ledRadius;

    private float cursorRadius;
    private PointF cursorPos = new PointF();
    private int selectedLed;
    private static final int cursorColour = 0xffffffff;
    private int colorAtCursor = 0xffff0000;

    private LedTuple ledGuiMap[] = new LedTuple[20];
    private int mapping[] = new int[20];

    public interface OnLedChangedListener {
        void onChange(int led, int color);
    }

    private OnLedChangedListener listener;

    public PlussyLedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        for(int i = 0; i < 20; i++) {
            ledGuiMap[i] = new LedTuple();
            ledGuiMap[i].color = 0xff009900;
        }

        // init paint
        ledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ledPaint.setStyle(Paint.Style.FILL);

        cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cursorPaint.setStyle(Paint.Style.FILL);
        cursorPaint.setColor(Color.WHITE);
    }

    private void mapGroup(float x, float y, int startIndex, int tl, int tr, int bl, int br) {
        ledGuiMap[startIndex].ledNumber = tl;
        ledGuiMap[startIndex].position = new PointF(x - (groupLength/4), y - (groupLength/4));
        ledGuiMap[startIndex + 1].ledNumber = tr;
        ledGuiMap[startIndex + 1].position = new PointF(x + (groupLength/4), y - (groupLength/4));
        ledGuiMap[startIndex + 2].ledNumber = bl;
        ledGuiMap[startIndex + 2].position = new PointF(x - (groupLength/4), y + (groupLength/4));
        ledGuiMap[startIndex + 3].ledNumber = br;
        ledGuiMap[startIndex + 3].position = new PointF(x + (groupLength/4), y + (groupLength/4));
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w;
        height = h;
        length = width > height ? height : width;
        groupLength = length/3;
        ledAlign = length/100;
        cursorRadius = groupLength/4;
        ledRadius = cursorRadius - ledAlign/2;

        // top group
        mapGroup(width / 2, height / 2 - groupLength, 0, mapping[0], mapping[1], mapping[2], mapping[3]);
        // left group
        mapGroup(width / 2 - groupLength, height / 2, 4, mapping[4], mapping[5], mapping[10], mapping[11]);
        // middle group
        mapGroup(width / 2, height / 2, 8, mapping[6], mapping[7], mapping[12], mapping[13]);
        // right group
        mapGroup(width / 2 + groupLength, height / 2, 12, mapping[8], mapping[9], mapping[14], mapping[15]);
        // bottom group
        mapGroup(width / 2, height / 2 + groupLength, 16, mapping[16], mapping[17], mapping[18], mapping[19]);

        cursorPos = ledGuiMap[0].position;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY &&
                MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }else if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST &&
                MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                    MeasureSpec.getSize(heightMeasureSpec));
        } else if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY &&
                MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                    MeasureSpec.getSize(widthMeasureSpec));
        } else if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST &&
                MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            setMeasuredDimension(MeasureSpec.getSize(heightMeasureSpec),
                    MeasureSpec.getSize(heightMeasureSpec));
        } else {
            Log.e(TAG, "Measurment mode unknown please use other options on layout_width and layout_height");
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        cursorPaint.setColor(cursorColour);
        canvas.drawCircle(cursorPos.x, cursorPos.y, cursorRadius, cursorPaint);
        for(int i = 0; i < 20; i++) {
            LedTuple ledm = ledGuiMap[i];
            ledPaint.setColor(ledm.color);
            canvas.drawCircle(ledm.position.x, ledm.position.y, ledRadius, ledPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float tx = event.getX();
        float ty = event.getY();
        for(int i = 0; i < 20; i++) {
            LedTuple led = ledGuiMap[i];
            if((led.position.x - ledRadius < tx && tx < led.position.x + ledRadius) &&
                    (led.position.y - ledRadius < ty && ty < led.position.y + ledRadius)) {
                PointF lastCursorPos = cursorPos;
                cursorPos = led.position;
                selectedLed = i;
                ledGuiMap[selectedLed].color = colorAtCursor;
                if(lastCursorPos != cursorPos) {
                    this.invalidate();
                    if(listener != null) {
                        listener.onChange(ledGuiMap[selectedLed].ledNumber, ledGuiMap[selectedLed].color);
                    }
                }
                break;
            }
        }
        return true;
    }

    public void setColourAtCursor(int color) {
        colorAtCursor = color;
        ledGuiMap[selectedLed].color = colorAtCursor;
        if(listener != null) {
            listener.onChange(ledGuiMap[selectedLed].ledNumber, ledGuiMap[selectedLed].color);
        }
        invalidate();
    }

    public void updateMatrix(int color[]) {
        for(int i = 0; i < 20; i++) {
            ledGuiMap[i].color = (0xff << 24) | (color[ledGuiMap[i].ledNumber] & 0xffffff);
        }
        invalidate();
    }

    public void setMapping(int mapping[]) {
        this.mapping = mapping;
        invalidate();
    }

    public void setOnLedChangedListener(OnLedChangedListener listener) {
        this.listener = listener;
    }
}
