package org.schabi.kiba;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Christian Schabesberger on 13.09.15.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
 * ColorSeek.java is part of KIBA.
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

public class ColorSeek extends View {

    public interface OnColourSelectionChangeListener {
        void selectionChanged(int color);
    }

    private static final String TAG = ColorSeek.class.toString();

    private static final int RED_MASK = (0xff << 16);
    private static final int GREEN_MASK = (0xff << 8);
    private static final int BLUE_MASK = (0xff);

    public static final int MODE_CHROMA_COLOR_PIC = 0;
    public static final int MODE_COLOR_INTENSITY_PIC = 1;
    public static final int MODE_BRIGHTNESS_PIC = 2;
    private int mode = MODE_CHROMA_COLOR_PIC;

    private boolean isHorizontal = true;
    private final float scale = getResources().getDisplayMetrics().density;
    private final int viewHeight = (int)(40 * scale);

    private Paint stripPaint;
    private final float stripHeight = 2 * scale;
    private final float stripeAlign = 18 * scale;

    private Paint cursorPaint;
    private Paint cursorShadowPaint;
    private final float cursorRadius = 6 * scale;
    private final float cursorShadowRadius = cursorRadius + 1 * scale;
    private final int cursorShadowColor = 0x66000000;
    private float cursorPosition;
    private boolean useBigCursor = false;
    private final float bigCursorRadius = 10 * scale;
    private final float bigCursorShadowRadius = bigCursorRadius + 4 * scale;

    private int baseColor = 0xffff0000;

    private float width;
    private float height;

    private int selectedColor = 0xffff0000;
    OnColourSelectionChangeListener listener = new OnColourSelectionChangeListener() {
        @Override
        public void selectionChanged(int color) {

        }
    };

    public ColorSeek(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ColorSeek, 0, 0);

        try {
            isHorizontal = a.getInteger(R.styleable.ColorSeek_orientation, 0) == 0;
            mode = a.getInteger(R.styleable.ColorSeek_mode, 0);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w;
        height = h;

        cursorPosition = stripeAlign;

        stripPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stripPaint.setStyle(Paint.Style.FILL);
        int[] rainbow = {
                0xffff0000,
                0xffffff00,
                0xff00ff00,
                0xff00ffff,
                0xff0000ff,
                0xffff00ff,
                0xffff0000
        };
        Shader shader = new Shader();
        if(mode == MODE_CHROMA_COLOR_PIC) {
            shader = new LinearGradient(0, 0, width, height, rainbow, null, Shader.TileMode.MIRROR);
        } else if(mode == MODE_COLOR_INTENSITY_PIC ) {
            shader = new LinearGradient(0, 0, width, height, new int[]{baseColor, 0xffffffff},
                    null, Shader.TileMode.MIRROR);
            stripPaint.setShader(shader);
        } else if(mode == MODE_BRIGHTNESS_PIC ) {
            shader = new LinearGradient(0, 0, width, height, new int[]{0xff000000, baseColor},
                    null, Shader.TileMode.MIRROR);
            stripPaint.setShader(shader);
            cursorPosition = (isHorizontal ? width : height) - stripeAlign;
        }
        stripPaint.setShader(shader);

        cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cursorPaint.setStyle(Paint.Style.FILL);

        cursorShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cursorShadowPaint.setStyle(Paint.Style.FILL);
        cursorShadowPaint.setColor(cursorShadowColor);
    }

    @Override
    protected void onMeasure(int widthMesuereSpec, int heightMeasureSpec) {
        int w = 0;
        int h = 0;
        if(isHorizontal) {
            w = MeasureSpec.getSize(widthMesuereSpec);
            h = viewHeight;
        } else {
            w = viewHeight;
            h = MeasureSpec.getSize(heightMeasureSpec);
        }
        if(MeasureSpec.getMode(widthMesuereSpec) == MeasureSpec.EXACTLY) {
            w = MeasureSpec.getSize(widthMesuereSpec);
        }
        if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            h = MeasureSpec.getSize(heightMeasureSpec);
        }

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        cursorPaint.setColor(selectedColor);
        if (isHorizontal) {
            canvas.drawRect(stripeAlign, (height - stripHeight) / 2, width - stripeAlign,
                    (height - stripHeight) / 2 + stripHeight, stripPaint);

            if (useBigCursor) {
                canvas.drawCircle(cursorPosition, height / 2, bigCursorShadowRadius, cursorShadowPaint);
                canvas.drawCircle(cursorPosition, height / 2, bigCursorRadius, cursorPaint);
            } else {
                canvas.drawCircle(cursorPosition, height / 2, cursorShadowRadius, cursorShadowPaint);
                canvas.drawCircle(cursorPosition, height / 2, cursorRadius, cursorPaint);
            }
        } else {
            canvas.drawRect((width - stripHeight) / 2, stripeAlign,
                    (width - stripHeight) / 2 + stripHeight, height - stripeAlign, stripPaint);
            if (useBigCursor) {
                canvas.drawCircle(width / 2, cursorPosition, bigCursorShadowRadius, cursorShadowPaint);
                canvas.drawCircle(width / 2, cursorPosition, bigCursorRadius, cursorPaint);
            } else {
                canvas.drawCircle(width / 2, cursorPosition, cursorShadowRadius, cursorShadowPaint);
                canvas.drawCircle(width / 2, cursorPosition, cursorRadius, cursorPaint);
            }
        }
    }

    private int getColorByPosition(float position) {
        final float startPoint = stripeAlign;
        final float endPoint = (isHorizontal ? width : height) - stripeAlign;
        final float range = endPoint - startPoint;
        final float relPos = (position - stripeAlign)/range;
        final float colorRange = 1.0f / 6.0f;

        float redPart = 0;
        float greenPart = 0;
        float bluePart = 0;

        if(relPos < colorRange) {
            redPart = 1;
            greenPart = relPos/colorRange;
        } else if(relPos < colorRange * 2) {
            greenPart = 1;
            redPart = (1 - (relPos-colorRange)/colorRange);
        } else if(relPos < colorRange * 3) {
            greenPart = 1;
            bluePart = ((relPos - colorRange * 2) / colorRange);
        } else if(relPos < colorRange * 4) {
            bluePart = 1;
            greenPart = (1 - (relPos - colorRange * 3) / colorRange);
        } else if(relPos < colorRange * 5) {
            bluePart = 1;
            redPart = ((relPos - colorRange * 4) / colorRange);
        } else if(relPos <= colorRange * 6) {
            redPart = 1;
            bluePart = (1 - (relPos - colorRange * 5) / colorRange);
        }

        int color = (0xff << 24) | ((int)(redPart*0xff) << 16) | ((int)(greenPart*0xff) << 8) | (int)(bluePart*0xff);
        return color;
    }

    private int getIntensityColorByPosition(float position) {
        final float startPoint = stripeAlign;
        final float endPoint = (isHorizontal ? width : height) - stripeAlign;
        final float range = endPoint - startPoint;
        final float relPos = (position - stripeAlign)/range;

        int redVal = (baseColor & RED_MASK) >> 16;
        int greenVal = (baseColor & GREEN_MASK) >> 8;
        int blueVal = (baseColor & BLUE_MASK);

        int redDiv = 0xff - redVal;
        int greenDiv = 0xff - greenVal;
        int blueDiv = 0xff - blueVal;

        redVal += redDiv * relPos;
        greenVal += greenDiv * relPos;
        blueVal += blueDiv * relPos;

        int color = (0xff << 24) | (redVal << 16) | (greenVal << 8) | (blueVal);
        return color;
    }

    private int getBrightnessColorByPosition(float position) {
        final float startPoint = stripeAlign;
        final float endPoint = (isHorizontal ? width : height) - stripeAlign;
        final float range = endPoint - startPoint;
        final float relPos = (position - stripeAlign)/range;

        int redVal = (baseColor & RED_MASK) >> 16;
        int greenVal = (baseColor & GREEN_MASK) >> 8;
        int blueVal = (baseColor & BLUE_MASK);

        redVal *= relPos;
        greenVal *= relPos;
        blueVal *= relPos;

        int color = (0xff << 24) | (redVal << 16) | (greenVal << 8) | (blueVal);
        return color;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            useBigCursor = true;
        } else if(event.getAction() == MotionEvent.ACTION_UP) {
            useBigCursor = false;
        }

        if(isHorizontal) {
            cursorPosition = event.getX();
            if (cursorPosition < stripeAlign) {
                cursorPosition = stripeAlign;
            } else if (cursorPosition > width - stripeAlign) {
                cursorPosition = width - stripeAlign;
            }
        } else {
            cursorPosition = event.getY();
            if (cursorPosition < stripeAlign) {
                cursorPosition = stripeAlign;
            } else if (cursorPosition > height - stripeAlign) {
                cursorPosition = height - stripeAlign;
            }
        }

        if(mode == MODE_CHROMA_COLOR_PIC) {
            selectedColor = getColorByPosition(cursorPosition);
        } else if(mode == MODE_COLOR_INTENSITY_PIC) {
            selectedColor = getIntensityColorByPosition(cursorPosition);
        } else if(mode == MODE_BRIGHTNESS_PIC) {
            selectedColor = getBrightnessColorByPosition(cursorPosition);
        }
        listener.selectionChanged(selectedColor);
        invalidate();
        return true;
    }

    public void setOnColourSelectionChangeListener(OnColourSelectionChangeListener listener) {
        this.listener = listener;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setBaseColor(int baseColor) {
        this.baseColor = baseColor;
        if(mode == MODE_COLOR_INTENSITY_PIC || mode == MODE_BRIGHTNESS_PIC) {
            if(mode == MODE_COLOR_INTENSITY_PIC) {
                Shader shader = new LinearGradient(0, 0, width, height, new int[]{baseColor, 0xffffffff},
                        null, Shader.TileMode.MIRROR);
                stripPaint.setShader(shader);
                selectedColor = getIntensityColorByPosition(cursorPosition);
            } else {
                Shader shader = new LinearGradient(0, 0, width, height, new int[]{0xff000000, baseColor},
                        null, Shader.TileMode.MIRROR);
                stripPaint.setShader(shader);
                selectedColor = getBrightnessColorByPosition(cursorPosition);
            }
            listener.selectionChanged(selectedColor);
            invalidate();
        }
    }
}
