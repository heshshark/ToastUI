package ce.hesh.ToastUI.views;



import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import ce.hesh.ToastUI.Common;
import ce.hesh.ToastUI.R;



public class AppIconSize extends DialogPreference implements OnSeekBarChangeListener {
    private SeekBar mSeekBar;
    private TextView mValue;
    private TextView mtext;

    public AppIconSize(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.pref_app_icon_size);
    }

    protected void onPrepareDialogBuilder(Builder builder) {
        builder.setNeutralButton(R.string.setting_icon_size, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.mValue = (TextView) view.findViewById(R.id.value);
        this.mtext = (TextView) view.findViewById(R.id.text);
        this.mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        this.mSeekBar.setOnSeekBarChangeListener(this);
    }

    protected void showDialog(Bundle state) {
        super.showDialog(state);
        Button Defalult_button = ((AlertDialog) getDialog()).getButton(-3);
        Defalult_button.setText(R.string.setting_default_value);
        Defalult_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AppIconSize.this.mSeekBar.setProgress(100);
            }
        });
        int value = getSharedPreferences().getInt(Common.KEY_TOAST_ICONSIZE, 100) ;
        this.mSeekBar.setMax(Common.LIMIT_MAX_APP_ICON_SIZE);
        this.mSeekBar.setProgress(value);
    }

    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            int realValue = this.mSeekBar.getProgress();
            Editor editor = getEditor();
            editor.putInt(Common.KEY_TOAST_ICONSIZE, realValue);
            editor.commit();
        }
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        this.mValue.setText(progress + "dp");
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}