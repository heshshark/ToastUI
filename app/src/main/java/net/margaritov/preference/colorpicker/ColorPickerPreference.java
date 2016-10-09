package net.margaritov.preference.colorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.preference.Preference;
import android.preference.Preference.BaseSavedState;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import ce.hesh.ToastUI.Common;
import net.margaritov.preference.colorpicker.ColorPickerDialog.OnColorChangedListener;

public class ColorPickerPreference extends Preference implements OnPreferenceClickListener, OnColorChangedListener {
    private boolean mAlphaSliderEnabled;
    private float mDensity;
    ColorPickerDialog mDialog;
    private boolean mHexValueEnabled;
    private int mValue;
    View mView;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR;
        Bundle dialogBundle;

        public SavedState(Parcel source) {
            super(source);
            this.dialogBundle = source.readBundle();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBundle(this.dialogBundle);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        static {
            CREATOR = new Creator<SavedState>() {
                public SavedState createFromParcel(Parcel in) {
                    return new SavedState(in);
                }

                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            };
        }
    }

    public ColorPickerPreference(Context context) {
        super(context);
        this.mValue = ViewCompat.MEASURED_STATE_MASK;
        this.mDensity = 0.0f;
        this.mAlphaSliderEnabled = false;
        this.mHexValueEnabled = false;
        init(context, null);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mValue = ViewCompat.MEASURED_STATE_MASK;
        this.mDensity = 0.0f;
        this.mAlphaSliderEnabled = false;
        this.mHexValueEnabled = false;
        init(context, attrs);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mValue = ViewCompat.MEASURED_STATE_MASK;
        this.mDensity = 0.0f;
        this.mAlphaSliderEnabled = false;
        this.mHexValueEnabled = false;
        init(context, attrs);
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return Integer.valueOf(a.getColor(index, ViewCompat.MEASURED_STATE_MASK));
    }

    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        onColorChanged(restoreValue ? getPersistedInt(this.mValue) : ((Integer) defaultValue).intValue());
    }

    private void init(Context context, AttributeSet attrs) {
        this.mDensity = getContext().getResources().getDisplayMetrics().density;
        setOnPreferenceClickListener(this);
        if (attrs != null) {
            this.mAlphaSliderEnabled = attrs.getAttributeBooleanValue(null, "alphaSlider", false);
            this.mHexValueEnabled = attrs.getAttributeBooleanValue(null, "hexValue", false);
        }
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        this.mView = view;
        setPreviewColor();
    }

    private void setPreviewColor() {
        if (this.mView != null) {
            ImageView iView = new ImageView(getContext());
            LinearLayout widgetFrameView = (LinearLayout) this.mView.findViewById(android.R.id.widget_frame);
            if (widgetFrameView != null) {
                widgetFrameView.setVisibility(View.VISIBLE);
                widgetFrameView.setPadding(0, 0, (int) (this.mDensity * 8.0f), widgetFrameView.getPaddingBottom());
                int count = widgetFrameView.getChildCount();
                if (count > 0) {
                    widgetFrameView.removeViews(0, count);
                }
                widgetFrameView.addView(iView);
                widgetFrameView.setMinimumWidth(0);
                iView.setImageBitmap(getPreviewBitmap());
            }
        }
    }

    private Bitmap getPreviewBitmap() {
        int d = (int) (this.mDensity * 36.0f);
        int color = this.mValue;
        Bitmap bitmap = Bitmap.createBitmap(d, d, Config.ARGB_8888);
        int radius = Math.min(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth() + 8, bitmap.getHeight() + 8, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle((float) ((bitmap.getWidth() / 2) + 4), (float) ((bitmap.getHeight() / 2) + 4), (float) radius, paint);
        canvas.drawBitmap(bitmap, 4.0f, 4.0f, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
        int darkenedColor = Color.rgb((Color.red(color) * 220) / Common.LIMIT_MAX_APP_ICON_SIZE, (Color.green(color) * 220) / Common.LIMIT_MAX_APP_ICON_SIZE, (Color.blue(color) * 220) / Common.LIMIT_MAX_APP_ICON_SIZE);
        paint.setXfermode(null);
        paint.setStyle(Style.STROKE);
        paint.setColor(darkenedColor);
        paint.setStrokeWidth(3.0f);
        canvas.drawCircle((float) ((bitmap.getWidth() / 2) + 4), (float) ((bitmap.getHeight() / 2) + 4), (float) radius, paint);
        return output;
    }

    public void onColorChanged(int color) {
        if (isPersistent()) {
            persistInt(color);
        }
        this.mValue = color;
        setPreviewColor();
        try {
            getOnPreferenceChangeListener().onPreferenceChange(this, Integer.valueOf(color));
        } catch (NullPointerException e) {
        }
    }

    public int getValue() {
        return this.mValue;
    }

    public void setValue(int color) {
        if (isPersistent()) {
            persistInt(color);
        }
        this.mValue = color;
        setPreviewColor();
    }

    public boolean onPreferenceClick(Preference preference) {
        showDialog(null);
        return false;
    }

    protected void showDialog(Bundle state) {
        this.mDialog = new ColorPickerDialog(getContext(), this.mValue);
        this.mDialog.setOnColorChangedListener(this);
        if (this.mAlphaSliderEnabled) {
            this.mDialog.setAlphaSliderVisible(true);
        }
        if (this.mHexValueEnabled) {
            this.mDialog.setHexValueEnabled(true);
        }
        if (state != null) {
            this.mDialog.onRestoreInstanceState(state);
        }
        this.mDialog.show();
    }

    public void setAlphaSliderEnabled(boolean enable) {
        this.mAlphaSliderEnabled = enable;
    }

    public void setHexValueEnabled(boolean enable) {
        this.mHexValueEnabled = enable;
    }

    public static String convertToARGB(int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));
        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }
        if (red.length() == 1) {
            red = "0" + red;
        }
        if (green.length() == 1) {
            green = "0" + green;
        }
        if (blue.length() == 1) {
            blue = "0" + blue;
        }
        return "#" + alpha + red + green + blue;
    }

    public static String convertToRGB(int color) {
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));
        if (red.length() == 1) {
            red = "0" + red;
        }
        if (green.length() == 1) {
            green = "0" + green;
        }
        if (blue.length() == 1) {
            blue = "0" + blue;
        }
        return "#" + red + green + blue;
    }

    public static int convertToColorInt(String argb) throws NumberFormatException {
        int alpha;
        int red;
        int green;
        int blue;
        if (argb.startsWith("#")) {
            argb = argb.replace("#", "");
        }
        if (argb.length() == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        } else if (argb.length() == 6) {
            alpha = MotionEventCompat.ACTION_MASK;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        } else {
            throw new NumberFormatException("string " + argb + "did not meet length requirements");
        }
        return Color.argb(alpha, red, green, blue);
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (this.mDialog == null || !this.mDialog.isShowing()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.dialogBundle = this.mDialog.onSaveInstanceState();
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        showDialog(myState.dialogBundle);
    }
}
