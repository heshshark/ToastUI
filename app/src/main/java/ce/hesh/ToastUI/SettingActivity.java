package ce.hesh.ToastUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.Toast;

import ce.hesh.ToastUI.views.AppIconSize;



@SuppressLint({"WorldReadableFiles"})
public class SettingActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String URL = "https://github.com/heshshark/ToastUI";

    private ListPreference mStyle;
    private ListPreference mAnimation;
    private SwitchPreference mSwith;
    private CheckBoxPreference mEnable_icon;
    private Preference mGithub;

    private SharedPreferences prefs;
    long tempTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if((keyCode == KeyEvent.KEYCODE_BACK) &&
                (event.getAction() == KeyEvent.ACTION_DOWN))
        {
//如果两次按返回键时间间隔大于2000毫秒就吐司提示，否则finish（）当前Activity
            if((System.currentTimeMillis() - tempTime) > 2000 )
            {
                tempTime = System.currentTimeMillis();
                Toast.makeText(this, R.string.click_exit, Toast.LENGTH_SHORT).show();
            }else
            {
                finish();
                System.exit(0);//0表示正常退出，非0表示不正常
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.prefs = getSharedPreferences(Common.PREFS, Context.MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.preference);

        mSwith = (SwitchPreference) findPreference("enable_toast");
        mStyle = (ListPreference) findPreference("style_key");
        mAnimation = (ListPreference) findPreference("animation_key");
        mEnable_icon = (CheckBoxPreference) findPreference("enable_icon");
        mGithub = findPreference("viewsource");


        mSwith.setOnPreferenceChangeListener(this);
        mStyle.setOnPreferenceChangeListener(this);
        mAnimation.setOnPreferenceChangeListener(this);
        mEnable_icon.setOnPreferenceChangeListener(this);
        mGithub.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key;
        boolean key_switch;
        SharedPreferences.Editor editor;
        if (preference == mSwith) {
            key_switch = (boolean) newValue;
            editor = prefs.edit();
            editor.putBoolean(Common.KEY_TOAST_SWITCH, key_switch);
            editor.apply();
            return true;
        } else if (preference == mEnable_icon) {
            key_switch = (boolean) newValue;
            editor = prefs.edit();
            editor.putBoolean(Common.KEY_TOAST_ENABLE_ICON, key_switch);
            editor.apply();
            return true;
        } else if (preference == mStyle) {
            key = (String) newValue;
            editor = prefs.edit();
            editor.putInt(Common.KEY_TOAST_FRAME, Integer.valueOf(key));
            editor.apply();
            return true;
        } else if (preference == mAnimation) {
            key = (String) newValue;
            editor = prefs.edit();
            editor.putInt(Common.KEY_TOAST_ANIMATIONS, Integer.valueOf(key));
            editor.apply();
            return true;
        }

        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference == mGithub){
            final Intent issueIntent = new Intent(Intent.ACTION_VIEW);
            issueIntent.setData(Uri.parse(URL));
            startActivity(issueIntent);
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
