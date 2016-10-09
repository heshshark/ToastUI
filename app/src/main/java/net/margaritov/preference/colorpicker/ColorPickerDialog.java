package net.margaritov.preference.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.internal.view.SupportMenu;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import ce.hesh.ToastUI.R;

public class ColorPickerDialog extends Dialog implements ColorPickerView.OnColorChangedListener, OnClickListener {
    private ColorPickerView mColorPicker;
    private ColorStateList mHexDefaultTextColor;
    private boolean mHexInternalTextChange;
    private EditText mHexVal;
    private boolean mHexValueEnabled;
    private OnColorChangedListener mListener;
    private ColorPickerPanelView mNewColor;
    private ColorPickerPanelView mOldColor;

    public interface OnColorChangedListener {
        void onColorChanged(int i);
    }

    public ColorPickerDialog(Context context, int initialColor) {
        super(context);
        this.mHexValueEnabled = false;
        init(initialColor);
    }

    private void init(int color) {
        getWindow().setFormat(1);
        setUp(color);
    }

    private void setUp(int color) {
        View layout = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_color_picker, null);
        setContentView(layout);
        setTitle(R.string.dialog_color_picker);
        this.mColorPicker = (ColorPickerView) layout.findViewById(R.id.color_picker_view);
        this.mOldColor = (ColorPickerPanelView) layout.findViewById(R.id.old_color_panel);
        this.mNewColor = (ColorPickerPanelView) layout.findViewById(R.id.new_color_panel);
        this.mHexVal = (EditText) layout.findViewById(R.id.hex_val);
        this.mHexDefaultTextColor = this.mHexVal.getTextColors();
        this.mHexVal.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (ColorPickerDialog.this.mHexValueEnabled && !ColorPickerDialog.this.mHexInternalTextChange) {
                    if (s.length() > 5 || s.length() < 10) {
                        try {
                            int c = ColorPickerPreference.convertToColorInt(s.toString());
                            ColorPickerDialog.this.mHexInternalTextChange = true;
                            ColorPickerDialog.this.mColorPicker.setColor(c, true);
                            ColorPickerDialog.this.mHexInternalTextChange = false;
                            ColorPickerDialog.this.mHexVal.setTextColor(ColorPickerDialog.this.mHexDefaultTextColor);
                            return;
                        } catch (NumberFormatException e) {
                            ColorPickerDialog.this.mHexVal.setTextColor(SupportMenu.CATEGORY_MASK);
                            return;
                        }
                    }
                    ColorPickerDialog.this.mHexVal.setTextColor(SupportMenu.CATEGORY_MASK);
                }
            }
        });
        setHexValueEnabled(true);
        ((LinearLayout) this.mOldColor.getParent()).setPadding(Math.round(this.mColorPicker.getDrawingOffset()), 0, Math.round(this.mColorPicker.getDrawingOffset()), 0);
        this.mOldColor.setOnClickListener(this);
        this.mNewColor.setOnClickListener(this);
        this.mColorPicker.setOnColorChangedListener(this);
        this.mOldColor.setColor(color);
        this.mColorPicker.setColor(color, true);
    }

    public void onColorChanged(int color) {
        this.mNewColor.setColor(color);
        if (this.mHexValueEnabled) {
            updateHexValue(color);
        }
    }

    public void setHexValueEnabled(boolean enable) {
        this.mHexValueEnabled = enable;
        if (enable) {
            this.mHexVal.setVisibility(View.VISIBLE);
            updateHexLengthFilter();
            updateHexValue(getColor());
            return;
        }
        this.mHexVal.setVisibility(View.GONE);
    }

    public boolean getHexValueEnabled() {
        return this.mHexValueEnabled;
    }

    private void updateHexLengthFilter() {
        if (getAlphaSliderVisible()) {
            this.mHexVal.setFilters(new InputFilter[]{new LengthFilter(9)});
            return;
        }
        this.mHexVal.setFilters(new InputFilter[]{new LengthFilter(7)});
    }

    private void updateHexValue(int color) {
        if (!this.mHexInternalTextChange) {
            this.mHexInternalTextChange = true;
            if (getAlphaSliderVisible()) {
                this.mHexVal.setText(ColorPickerPreference.convertToARGB(color));
            } else {
                this.mHexVal.setText(ColorPickerPreference.convertToRGB(color));
            }
            this.mHexInternalTextChange = false;
        }
    }

    public void setAlphaSliderVisible(boolean visible) {
        this.mColorPicker.setAlphaSliderVisible(visible);
        if (this.mHexValueEnabled) {
            updateHexLengthFilter();
            updateHexValue(getColor());
        }
    }

    public boolean getAlphaSliderVisible() {
        return this.mColorPicker.getAlphaSliderVisible();
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.mListener = listener;
    }

    public int getColor() {
        return this.mColorPicker.getColor();
    }

    public void onClick(View v) {
        if (v.getId() == R.id.new_color_panel && this.mListener != null) {
            this.mListener.onColorChanged(this.mNewColor.getColor());
        }
        dismiss();
    }

    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt("old_color", this.mOldColor.getColor());
        state.putInt("new_color", this.mNewColor.getColor());
        return state;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mOldColor.setColor(savedInstanceState.getInt("old_color"));
        this.mColorPicker.setColor(savedInstanceState.getInt("new_color"), true);
    }
}
