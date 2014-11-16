package com.github.blalasaadri.tricircle.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.twotoasters.watchface.gears.widget.IWatchface;
import com.twotoasters.watchface.gears.widget.Watch;
import com.github.blalasaadri.tricircle.R;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class Watchface extends FrameLayout implements IWatchface {

    @InjectView(R.id.face)              ImageView face;
    @InjectView(R.id.hand_hour)         ImageView handHour;
    @InjectView(R.id.ring_hour)         ImageView ringHour;
    @InjectView(R.id.hand_minute)       ImageView handMinute;
    @InjectView(R.id.ring_minute)       ImageView ringMinute;
    @InjectView(R.id.hand_second)       ImageView handSecond;
    @InjectView(R.id.ring_second)       ImageView ringSecond;
    @InjectView(R.id.charge_indicator)  ImageView chargeIndicator;
    @InjectView(R.id.charge_text)
    TextView chargeText;

    private static final int SHADE_COLOUR = Color.DKGRAY;

    private Watch mWatch;

    private boolean mInflated;
    private boolean mActive;

    private int overlayHour, overlayMinute, overlaySecond, overlayIndicator;

    @SuppressWarnings("unused")
    public Watchface(Context context) {
        super(context);
        init(context, null, 0);
    }

    @SuppressWarnings("unused")
    public Watchface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    @SuppressWarnings("unused")
    public Watchface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    @DebugLog
    private void init(Context context, AttributeSet attrs, int defStyle) {
        mWatch = new Watch(this);

        overlayHour = Color.RED;
        overlayMinute = Color.YELLOW;
        overlaySecond = Color.BLUE;
        overlayIndicator = Color.GREEN;
    }

    @DebugLog
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this, getRootView());
        mInflated = true;
    }

    @DebugLog
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWatch.onAttachedToWindow();
    }

    @DebugLog
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mWatch.onDetachedFromWindow();
    }

    private void rotateHands(int hour, int minute, int second) {
        int rotHr = 30 * hour;
        int rotMin = 6 * minute;
        int rotSec = 6 * second;

        handHour.setRotation(rotHr);
        handMinute.setRotation(rotMin);
        handSecond.setRotation(rotSec);
    }

    @Override
    public void onTimeChanged(Calendar time) {
        Timber.v("onTimeChanged()");

        int hr = time.get(Calendar.HOUR_OF_DAY) % 12;
        int min = time.get(Calendar.MINUTE);
        int sec = time.get(Calendar.SECOND);

        rotateHands(hr, min, sec);
        invalidate();
    }

    @Override
    public void onBatteryLevelChanged(int percentage) {
        Timber.v("onBatteryLevelChanged()", percentage);

        // Determine the colour to display the charge indicator in
        int greenValue;
        int redValue;
        if(percentage <= 20) {
            float share = percentage / 20f;

            greenValue = (int) (255 * share) | SHADE_COLOUR;
            redValue = (int) (255 * (1 - share)) | SHADE_COLOUR;
        } else {
            greenValue = 255 | SHADE_COLOUR;
            redValue = 0;
        }
        overlayIndicator = Color.rgb(redValue, greenValue, 0);
        chargeIndicator.setColorFilter(overlayIndicator);
        chargeText.setTextColor(overlayIndicator);

        // Set the Text output
        chargeText.setText(Integer.toString(percentage) + "%");

        // How much of the battery has been used so far?
        float percentageFactor = (100 - percentage) / 100f;

        // Move the image down
        int paddingTop = (int) (chargeIndicator.getHeight() * percentageFactor);
        chargeIndicator.setPadding(0, paddingTop, 0, 0);

        // And crop the image
        Bitmap bmp;
        if(mActive) {
            bmp = BitmapFactory.decodeResource(getResources(), R.drawable.charge_indicator_normal);
        } else {
            bmp = BitmapFactory.decodeResource(getResources(), R.drawable.charge_indicator_dimmed);
        }
        int bmpHeight = (int) (bmp.getHeight() * percentageFactor);
        Bitmap croppedBitmap = Bitmap.createBitmap(bmp, 0, bmpHeight, bmp.getWidth(), bmp.getHeight() - bmpHeight);
        chargeIndicator.setImageBitmap(croppedBitmap);
    }

    @Override
    @DebugLog
    public void onActiveStateChanged(boolean active) {
        this.mActive = active;
        setImageResources();
    }

    @Override
    public boolean handleSecondsInDimMode() {
        return true;
    }

    @Override
    public boolean isInEditMode() {
        return face.isInEditMode()
                || handHour.isInEditMode()
                || ringHour.isInEditMode()
                || handMinute.isInEditMode()
                || ringMinute.isInEditMode()
                || handSecond.isInEditMode()
                || ringSecond.isInEditMode()
                || chargeIndicator.isInEditMode();
    }

    @DebugLog
    private void setImageResources() {
        if (mInflated) {
            int faceImageResource;
            int handHourImageResource, ringHourImageResource;
            int handMinuteImageResource, ringMinuteImageResource;
            int handSecondImageResource, ringSecondImageResource;

            if(mActive) {
                faceImageResource = R.drawable.background_normal;
                handHourImageResource = R.drawable.hand_hour_normal;
                ringHourImageResource = R.drawable.ring_hour_normal;
                handMinuteImageResource = R.drawable.hand_minute_normal;
                ringMinuteImageResource = R.drawable.ring_minute_normal;
                handSecondImageResource = R.drawable.hand_second_normal;
                ringSecondImageResource = R.drawable.ring_second_normal;
            } else {
                faceImageResource = R.drawable.background_dimmed;
                handHourImageResource = R.drawable.hand_hour_dimmed;
                ringHourImageResource = R.drawable.ring_hour_dimmed;
                handMinuteImageResource = R.drawable.hand_minute_dimmed;
                ringMinuteImageResource = R.drawable.ring_minute_dimmed;
                handSecondImageResource = R.drawable.hand_second_dimmed;
                ringSecondImageResource = R.drawable.ring_second_dimmed;
            }
            face.setImageResource(faceImageResource);
            handHour.setImageResource(handHourImageResource);
            ringHour.setImageResource(ringHourImageResource);
            handMinute.setImageResource(handMinuteImageResource);
            ringMinute.setImageResource(ringMinuteImageResource);
            handSecond.setImageResource(handSecondImageResource);
            ringSecond.setImageResource(ringSecondImageResource);

            setColourFilters();
        }
    }

    private void setColourFilters() {
        int colourHour, colourMinute, colourSecond, colourIndicator;
        if(mActive) {
            colourHour = overlayHour;
            colourMinute = overlayMinute;
            colourSecond = overlaySecond;
            colourIndicator = overlayIndicator;
        } else {
            colourHour = overlayHour | SHADE_COLOUR;
            colourMinute = overlayMinute | SHADE_COLOUR;
            colourSecond = overlaySecond | SHADE_COLOUR;
            colourIndicator = overlayIndicator | SHADE_COLOUR;
        }
        handHour.setColorFilter(colourHour);
        ringHour.setColorFilter(colourHour);
        handMinute.setColorFilter(colourMinute);
        ringMinute.setColorFilter(colourMinute);
        handSecond.setColorFilter(colourSecond);
        ringSecond.setColorFilter(colourSecond);
        chargeIndicator.setColorFilter(colourIndicator);
    }
}
