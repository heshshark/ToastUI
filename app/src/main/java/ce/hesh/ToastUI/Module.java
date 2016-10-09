package ce.hesh.ToastUI;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XModuleResources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.Toast;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperToast;

import ce.hesh.ToastUI.utils.AttributeUtils;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedBridge.log;


public class Module extends XC_MethodHook implements IXposedHookLoadPackage,IXposedHookInitPackageResources,IXposedHookZygoteInit {
    public static String MODULE_PATH;
    public  static  XModuleResources modRes;
    public static boolean enabled;
    public static final String PACKAGE_NAME;
    private Context context = null;
    private CharSequence toastMsg = null;
    private int Duration_type = 0;

    private String tempMesssage = "just a tempmessage";
    private long tempTime = 0;

    static XSharedPreferences xpref;

    static {
        enabled = false;
        PACKAGE_NAME = Module.class.getPackage().getName();
        MODULE_PATH = null;
    }


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        xpref.reload();

        if(xpref.getBoolean(Common.KEY_TOAST_SWITCH,true)) {

            XposedHelpers.findAndHookMethod(Toast.class, "makeText", Context.class, CharSequence.class, Integer.TYPE, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    context = (Context) param.args[0];
                    toastMsg = (CharSequence) param.args[1];
                    Duration_type = (int) param.args[2];
                }
            });

            XposedHelpers.findAndHookMethod(Toast.class, "show", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Toast t = (Toast) param.thisObject;
                    t.cancel();

                    xpref.reload();

                    if(!tempMesssage.equals(toastMsg.toString())){
                        showSuperToast();
                        tempTime = System.currentTimeMillis();
                    }else {
                        if (System.currentTimeMillis() - tempTime > 2000) {
                            tempTime = System.currentTimeMillis();
                            showSuperToast();
                        }
                    }

                    tempMesssage = toastMsg.toString();

                }


            });
        }


    }

    private void showSuperToast() {
            Drawable applicationIcon = context.getPackageManager().getApplicationIcon(context.getApplicationInfo());

            int Duration_time;
            if (Duration_type == 0) {
                Duration_time = Style.DURATION_SHORT;
            } else {
                Duration_time = Style.DURATION_MEDIUM;
            }

            SuperToast superToast = new SuperToast(context, modRes);
            superToast.setText(toastMsg.toString())
                    .setTextSize(xpref.getInt(Common.KEY_TOAST_TEXTSIZE,14))
                    .setTextColor(xpref.getInt(Common.KEY_FONT_COLOR, Color.WHITE))
                .setDuration(Duration_time)
                .setFrame(AttributeUtils.getXPFrame(xpref))
                .setColor(xpref.getInt(Common.KEY_BACKGROUND_COLOR,Color.parseColor("#ff".concat("F44335"))))
                .setTransParence(xpref.getFloat(Common.KEY_TOAST_TRANSPERENCY,100.0f)/100)
                .setAnimations(AttributeUtils.getXPAnimations(xpref));

        if(xpref.getBoolean(Common.KEY_TOAST_ENABLE_ICON,true)){
            superToast.setDrawable(applicationIcon)
                    .setIconSize(xpref.getInt(Common.KEY_TOAST_ICONSIZE,100));
        }
        System.out.println();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            TypedArray array = context.getTheme().obtainStyledAttributes(new int[]{
                    android.R.attr.colorPrimaryDark,
            });
            int backgroudcolor = array.getColor(0,xpref.getInt(Common.KEY_BACKGROUND_COLOR,Color.parseColor("#ff".concat("F44335"))));

            if(BuildConfig.DEBUG){
                log(String.valueOf(backgroudcolor));
            }
            if (backgroudcolor!=Color.TRANSPARENT &&backgroudcolor!=Color.BLACK){
                superToast.setColor(backgroudcolor);
            }

            /*System.out.println(context.getApplicationInfo().className);
            if (context.getPackageName().equals("com.tencent.mm")){

            }*/
            array.recycle();
        }
        superToast.show();

    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam initPackageResourcesParam) throws Throwable {
        modRes = XModuleResources.createInstance(MODULE_PATH, initPackageResourcesParam.res);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        xpref = new XSharedPreferences(Common.PREF_PACKAGE,Common.PREFS);
        xpref.makeWorldReadable();
        log("[ToastUI] Pref Init.");
    }

}
