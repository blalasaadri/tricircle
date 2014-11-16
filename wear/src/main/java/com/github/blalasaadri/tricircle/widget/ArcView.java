package com.github.blalasaadri.tricircle.widget;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.github.blalasaadri.tricircle.R;

import timber.log.Timber;

/**
 * A View which will display a partial ring and a hand to display a numerical value.
 */
public class ArcView extends View {

    private static final int DEFAULT_STROKE_WIDTH = 5;

    private final Paint ringPaint, handPaint;
    private final TimeInterpolator interpolator;

    private RectF oval;
    private int angle, lastValue = -1;
    private float scale;
    private float width;
    private int lineWidth, handRadius;
    private float padding;

    public ArcView(Context context, AttributeSet attrs) {
        super(context, attrs);

        ringPaint = new Paint();
        handPaint = new Paint();

        // Read out attributes from the watchface.xml
        TypedArray attributes =
                context.getTheme().obtainStyledAttributes(attrs, R.styleable.ArcView, 0, 0);
        try {
            angle = attributes.getInteger(R.styleable.ArcView_angle, 0);
            lineWidth = attributes.getInteger(R.styleable.ArcView_lineWidth, DEFAULT_STROKE_WIDTH);
            handRadius = attributes.getInteger(R.styleable.ArcView_handRadius, DEFAULT_STROKE_WIDTH);
            scale = attributes.getFloat(R.styleable.ArcView_scale, 1f);
            ringPaint.setColor(attributes.getColor(R.styleable.ArcView_color, Color.WHITE));
            handPaint.setColor(attributes.getColor(R.styleable.ArcView_color, Color.WHITE));
        } finally {
            attributes.recycle();
        }
        init();

        // This interpolator will run an AccelerateDecelerateInterpolator in the second half of the
        // animation and return 0 before that.
        interpolator = new TimeInterpolator() {
            private AccelerateDecelerateInterpolator innerInterpolator = new AccelerateDecelerateInterpolator();

            @Override
            public float getInterpolation(float input) {
                if(input < 0.5f) {
                    return 0;
                } else {
                    return innerInterpolator.getInterpolation(input * 2f - 1f);
                }
            }
        };
    }

    private void init() {
        ringPaint.setStrokeWidth(lineWidth);
        ringPaint.setStyle(Paint.Style.STROKE);
        handPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    /**
     * Sets the value which should be displayed by this arc.
     *
     * @param value the value to which the arc should be set
     * @param maxValue the maximum value this arc can reach
     * @param render is it one second before the next value change?
     */
    public void setValue(int value, final int maxValue, boolean render) {
        if(lastValue == -1) {
            // If this is the first time the face is loaded just set it and don't animate
            lastValue = value;
            this.angle = (360 / maxValue) * (value % maxValue);
            invalidate();
            requestLayout();
        } else if(value != lastValue || render) {
            // Only change the values if the difference to the previous value is greater than 1°
            // or it is one second before the next change
            lastValue = value;
            // We start animating the next value as the animation takes one second
            int nextValue = (value + 1) % 60;
            if(nextValue == 0) {
                nextValue = 60;
            }
            int nextAngle = (360 / maxValue) * nextValue;
            int previousAngle = (360 / maxValue) * (value % maxValue);

            ValueAnimator animator;
            // The animation from (maxValue - 1) to maxValue has to be handled seperately
            if(value == maxValue - 1) {
                animator = ValueAnimator.ofInt(previousAngle, 360);
                Timber.v("animating %d (%d°) to 0 (360°)", value, previousAngle);
            } else {
                animator = ValueAnimator.ofInt(previousAngle, nextAngle);
                Timber.v("animating %d (%d°) to %d (%d°)", value, previousAngle, nextValue, nextAngle);
            }
            animator.setDuration(1000);
            animator.setInterpolator(interpolator);
            // The following Listener will update the view every time the Animator has a new value
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ArcView.this.angle = ((Number) animation.getAnimatedValue()).intValue() % 360;
                    ArcView.this.invalidate();
                    ArcView.this.requestLayout();
                }
            });
            animator.start();
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
