package com.github.blalasaadri.tricircle.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.github.blalasaadri.tricircle.R;

/**
 * A View which will display a partial ring and a hand to display a numerical value.
 */
public class ArcView extends View {

    private static final int DEFAULT_STROKE_WIDTH = 5;

    private final Paint ringPaint, handPaint;

    private RectF oval;
    private float angle, scale;
    private float width;
    private int lineWidth, handRadius;
    private float padding;

    public ArcView(Context context, AttributeSet attrs) {
        super(context, attrs);

        ringPaint = new Paint();
        handPaint = new Paint();

        TypedArray attributes =
                context.getTheme().obtainStyledAttributes(attrs, R.styleable.ArcView, 0, 0);
        try {
            angle = attributes.getFloat(R.styleable.ArcView_angle, 0);
            lineWidth = attributes.getInteger(R.styleable.ArcView_lineWidth, DEFAULT_STROKE_WIDTH);
            handRadius = attributes.getInteger(R.styleable.ArcView_handRadius, DEFAULT_STROKE_WIDTH);
            scale = attributes.getFloat(R.styleable.ArcView_scale, 1f);
            ringPaint.setColor(attributes.getColor(R.styleable.ArcView_color, Color.WHITE));
            handPaint.setColor(attributes.getColor(R.styleable.ArcView_color, Color.WHITE));
        } finally {
            attributes.recycle();
        }
        init();
    }

    private void init() {
        ringPaint.setStrokeWidth(lineWidth);
        ringPaint.setStyle(Paint.Style.STROKE);
        handPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    /**
     * Sets the angle up to which the arc should be drawn.
     *
     * @param angle an angle (0 <= angle <= 360) which the arc should draw
     */
    public void setAngle(float angle) {
        // only change if the difference is greater than 1 degree
        if(Math.abs(angle - this.angle) > 1f) {
            this.angle = angle;
            invalidate();
            requestLayout();
        }
    }

    /**
     * Set the color in which the ring and hand should be drawn.
     *
     * @param color the color to draw the ring and hand in
     */
    public void setColor(int color) {
        ringPaint.setColor(color);
        handPaint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Calculate what is needed for the circle section
        if(oval == null) {
            int minOfWidthAndHeight = (canvas.getWidth() < canvas.getHeight()) ? canvas.getWidth() : canvas.getHeight();
            width = minOfWidthAndHeight * scale;
            padding = (minOfWidthAndHeight - width) / 2;
            oval = new RectF(padding, padding, padding + width, padding + width);
        }
        // Draw the sircle section
        canvas.drawArc(oval, -90f, angle, false, ringPaint);

        // Calculate what is needed for the hand
        double radius = width / 2d;
        double radian = Math.PI * (angle - 90d) / 180d;
        float x = (float) (Math.cos(radian) * radius);
        float y = (float) (Math.sin(radian) * radius);
        float handPadding = (float) (padding + radius - handRadius / 2);
        float left = handPadding + x;
        float top = handPadding + y;
        RectF handOval = new RectF(left, top, left + handRadius, top + handRadius);
        // Draw the hand
        canvas.drawArc(handOval, 0, 360, false, handPaint);
    }
}
