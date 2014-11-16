package com.github.blalasaadri.tricircle.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.twotoasters.watchface.gears.widget.IWatchface;
import com.twotoasters.watchface.gears.widget.Watch;
import com.github.blalasaadri.tricircle.R;

import org.joda.time.DateTime;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hugo.weaving.DebugLog;
import timber.log.Timber;

import static org.joda.time.DateTimeConstants.HOURS_PER_DAY;
import static org.joda.time.DateTimeConstants.MINUTES_PER_HOUR;
import static org.joda.time.DateTimeConstants.SECONDS_PER_MINUTE;

public class Watchface extends FrameLayout implements IWatchface {

    private static final int SHADE_COLOUR = Color.DKGRAY;

    @InjectView(R.id.face)
    ImageView face;
    @InjectView(R.id.hour_arc)
    ArcView hourArc;
    @InjectView(R.id.minute_arc)
    ArcView minuteArc;
    @InjectView(R.id.second_arc)
    ArcView secondArc;
    @InjectView(R.id.charge_indicator)
    ImageView chargeIndicator;
    @InjectView(R.id.charge_text)
    TextView chargeText;

    private Watch mWatch;

    private boolean mInflated;
    private boolean mActive;

    private int overlayHour, overlayMinute, overlaySecond, overlayIndicator;

    @SuppressWarnings("unused")
    public Watchface(Context context) {
        super(context);
        init();
    }

    @SuppressWarnings("unused")
    public Watchface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressWarnings("unused")
    public Watchface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @DebugLog
    private void init() {
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

    @Override
    public void onTimeChanged(@NonNull DateTime time) {
        int hour = time.getHourOfDay() % 12;
        int minute = time.getMinuteOfHour();
        int second = time.getSecondOfMinute();
        Timber.i("Setting time to %d:%d:%d", hour, minute, second);

        hourArc.setValue(hour, HOURS_PER_DAY / 2, (minute == 59) && (second == 59));
        minuteArc.setValue(minute, MINUTES_PER_HOUR, (second == 59));
        secondArc.setValue(second, SECONDS_PER_MINUTE, true);
        invalidate();
    }

    @Override
    public void onBatteryLevelChanged(int percentage) {
        Timber.d("battery level changed to %d", percentage);

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
                || hourArc.isInEditMode()
                || minuteArc.isInEditMode()
                || secondArc.isInEditMode()
                || chargeIndicator.isInEditMode();
    }

    @DebugLog
    private void setImageResources() {
        if (mInflated) {
            int faceImageResource;

            if(mActive) {
                faceImageResource = R.drawable.background_normal;
            } else {
                faceImageResource = R.drawable.background_dimmed;
            }
            face.setImageResource(faceImageResource);

            setColourFilters();
        }
    }

    private void setColourFilters() {
        int colorHour, colorMinute, colorSecond, colorIndicator;
        if(mActive) {
            colorHour = overlayHour;
            colorMinute = overlayMinute;
            colorSecond = overlaySecond;
            colorIndicator = overlayIndicator;
        } else {
            colorHour = overlayHour | SHADE_COLOUR;
            colorMinute = overlayMinute | SHADE_COLOUR;
            colorSecond = overlaySecond | SHADE_COLOUR;
            colorIndicator = overlayIndicator | SHADE_COLOUR;
        }
        hourArc.setColor(colorHour);
        minuteArc.setColor(colorMinute);
        secondArc.setColor(colorSecond);
        chargeIndicator.setColorFilter(colorIndicator);
    }
}
