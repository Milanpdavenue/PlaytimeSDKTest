package com.playtime.sdk.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.playtime.sdk.R;

import java.util.Random;

public class CommonUtils {
    private static Dialog dialogLoader;

    public static void showProgressLoader(Context activity) {
        try {
            if (dialogLoader == null || !dialogLoader.isShowing()) {
                //AppLogger.getInstance().e("Activity Loader:", "=================LOADER===============" + activity);
                dialogLoader = new Dialog(activity, android.R.style.Theme_Light);
                dialogLoader.getWindow().setBackgroundDrawableResource(R.color.black_transparent);
                dialogLoader.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogLoader.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                dialogLoader.setCancelable(true);
                dialogLoader.setCanceledOnTouchOutside(true);
                dialogLoader.setContentView(R.layout.popup_progressbar);
                dialogLoader.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dismissProgressLoader() {
        try {
            if (dialogLoader != null && dialogLoader.isShowing()) {
                dialogLoader.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void setToast(Context _mContext, String str) {
        Toast toast = Toast.makeText(_mContext, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void setTheme(Activity context) {
        Window window = context.getWindow();
        window.setNavigationBarColor(context.getColor(R.color.white));

        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(context.getColor(android.R.color.transparent));

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    public static int getRandomNumberBetweenRange(int min, int max) {
        if (max == 0) {
            return 0;
        }
        Random r = new Random();
        int i1 = r.nextInt(max - min) + min;// min inclusive & max exclusive
        return i1;
    }

    public static void Notify(final Context activity, String title, String message, boolean isFinish) {
        try {
            if (activity != null) {
                final Dialog dialog1 = new Dialog(activity, android.R.style.Theme_Light);
                dialog1.getWindow().setBackgroundDrawableResource(R.color.black_transparent);
                dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog1.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                dialog1.setContentView(R.layout.popup_notify);
                dialog1.setCancelable(false);

                Button btnOk = dialog1.findViewById(R.id.btnOk);
                TextView tvTitle = dialog1.findViewById(R.id.tvTitle);
                tvTitle.setText(title);

                TextView tvMessage = dialog1.findViewById(R.id.tvMessage);
                tvMessage.setText(message);
                btnOk.setOnClickListener(v -> {
                    dialog1.dismiss();
                    if (isFinish && activity instanceof Activity && !((Activity) activity).isFinishing()) {
                        ((Activity) activity).finish();
                    }
                });
                dialog1.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
