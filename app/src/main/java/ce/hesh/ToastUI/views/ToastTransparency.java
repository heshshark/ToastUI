package ce.hesh.ToastUI.views;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import ce.hesh.ToastUI.Common;
import ce.hesh.ToastUI.R;

public class ToastTransparency extends DialogPreference implements OnSeekBarChangeListener {
    public SeekBar mSeekBar;
    private TextView mValue;

    public ToastTransparency(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.pref_app_icon_size);
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.mValue = (TextView) view.findViewById(R.id.value);
        this.mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        this.mSeekBar.setOnSeekBarChangeListener(this);
    }

    protected void showDialog(Bundle state) {
        super.showDialog(state);
        float value = getSharedPreferences().getFloat(Common.KEY_TOAST_TRANSPERENCY, 100f) ;
        this.mSeekBar.setMax(100);
        this.mSeekBar.setProgress((int) value);
    }

    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            int realValue = this.mSeekBar.getProgress() + 0;
            Editor editor = getEditor();
            editor.putFloat(Common.KEY_TOAST_TRANSPERENCY,(float)realValue);
            editor.commit();
        }
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        this.mValue.setText((progress ) + "%");
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}