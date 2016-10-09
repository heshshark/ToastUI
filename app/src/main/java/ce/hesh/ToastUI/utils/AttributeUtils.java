/*
 * Copyright 2013-2016 John Persano
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ce.hesh.ToastUI.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import ce.hesh.ToastUI.Common;
import ce.hesh.ToastUI.R;
import de.robv.android.xposed.XSharedPreferences;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;

/**
 * Utils class to help with saved SuperToast attributes.
 */
public class AttributeUtils {


    public static int getXPFrame(XSharedPreferences xperf) {
        switch (xperf.getInt(Common.KEY_TOAST_FRAME,0)) {
            case 0: return Style.FRAME_LOLLIPOP;
            case 1: return Style.FRAME_STANDARD;
            case 2: return Style.FRAME_KITKAT;
            default: return Style.FRAME_LOLLIPOP;
        }
    }


    public static int getXPAnimations(XSharedPreferences xpref) {
        switch (xpref.getInt(Common.KEY_TOAST_ANIMATIONS,0)) {
            case 0: return Style.ANIMATIONS_FLY;
            case 1: return Style.ANIMATIONS_FADE;
            case 2: return Style.ANIMATIONS_SCALE;
            case 3: return Style.ANIMATIONS_POP;
            default: return Style.ANIMATIONS_FLY;
        }
    }

}