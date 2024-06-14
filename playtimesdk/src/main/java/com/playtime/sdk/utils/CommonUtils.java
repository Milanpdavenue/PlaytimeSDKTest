package com.playtime.sdk.utils;

import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import com.playtime.sdk.R;
import com.playtime.sdk.activity.PlaytimeOfferWallActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class CommonUtils {
    private static Dialog dialogLoader;

    public static void showProgressLoader(Context activity) {
        try {
            if (dialogLoader == null || !dialogLoader.isShowing()) {
                dialogLoader = new Dialog(activity, android.R.style.Theme_Light);
                dialogLoader.getWindow().setBackgroundDrawableResource(R.color.black_transparent);
                dialogLoader.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogLoader.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                dialogLoader.setCancelable(true);
                dialogLoader.setCanceledOnTouchOutside(true);
                dialogLoader.setContentView(R.layout.dialog_playtimeads_progressbar);
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

//    public static void Notify(final Context activity, String title, String message, boolean isFinish) {
//        try {
//            if (activity != null) {
//                final Dialog dialog1 = new Dialog(activity, android.R.style.Theme_Light);
//                dialog1.getWindow().setBackgroundDrawableResource(R.color.black_transparent);
//                dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                dialog1.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//                dialog1.setContentView(R.layout.popup_notify);
//                dialog1.setCancelable(false);
//
//                Button btnOk = dialog1.findViewById(R.id.btnOk);
//                TextView tvTitle = dialog1.findViewById(R.id.tvTitle);
//                tvTitle.setText(title);
//
//                TextView tvMessage = dialog1.findViewById(R.id.tvMessage);
//                tvMessage.setText(message);
//                btnOk.setOnClickListener(v -> {
//                    dialog1.dismiss();
//                    if (isFinish && activity instanceof Activity && !((Activity) activity).isFinishing()) {
//                        ((Activity) activity).finish();
//                    }
//                });
//                dialog1.show();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static void showConsentPopup(final Context activity, String title, String message) {
        try {
            if (activity != null) {
                final Dialog dialog1 = new Dialog(activity, android.R.style.Theme_Light);
                dialog1.getWindow().setBackgroundDrawableResource(R.color.black_transparent);
                dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog1.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                dialog1.setContentView(R.layout.dialog_playtimeads_terms);
                dialog1.setCancelable(false);

                Button btnOk = dialog1.findViewById(R.id.btnOk);
                Button btnCancel = dialog1.findViewById(R.id.btnCancel);

                TextView tvTitle = dialog1.findViewById(R.id.tvTitle);
                tvTitle.setText(title);

                TextView tvMessage = dialog1.findViewById(R.id.tvMessage);
                tvMessage.setText(Html.fromHtml(message));
                tvMessage.setMovementMethod(LinkMovementMethod.getInstance());

                btnOk.setOnClickListener(v -> {
                    SharePrefs.getInstance(activity).putBoolean(SharePrefs.IS_CONSENT_GIVEN, true);
                    if (activity instanceof PlaytimeOfferWallActivity) {
                        ((PlaytimeOfferWallActivity) activity).askUsagePermissionAndResumePlaytimeUsage();
                    }
                    dialog1.dismiss();
                });
                btnCancel.setOnClickListener(v -> {
                    dialog1.dismiss();
                    showExitConfirmationPopup(activity);
                });
                dialog1.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showExitConfirmationPopup(Context activity) {
        try {
            if (activity != null) {
                final Dialog dialog1 = new Dialog(activity, android.R.style.Theme_Light);
                dialog1.getWindow().setBackgroundDrawableResource(R.color.black_transparent);
                dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog1.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                dialog1.setContentView(R.layout.dialog_playtimeads_notify_);
                dialog1.setCancelable(false);

                Button btnOk = dialog1.findViewById(R.id.btnOk);
                Button btnCancel = dialog1.findViewById(R.id.btnCancel);

                TextView tvTitle = dialog1.findViewById(R.id.tvTitle);
                tvTitle.setText("Hey, don't miss out");

                TextView tvMessage = dialog1.findViewById(R.id.tvMessage);
                tvMessage.setText("Do you really want to go back to App without collecting any rewards?");

                btnOk.setOnClickListener(v -> {
                    dialog1.dismiss();
                    CommonUtils.showConsentPopup(activity, SharePrefs.getInstance(activity).getString(SharePrefs.CONSENT_TITLE), SharePrefs.getInstance(activity).getString(SharePrefs.CONSENT_MESSAGE));
                });
                btnCancel.setOnClickListener(v -> {
                    dialog1.dismiss();
                    if (activity instanceof Activity && !((Activity) activity).isFinishing()) {
                        ((Activity) activity).finish();
                    }
                });
                dialog1.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showPopup(Context activity, String message) {
        try {
            if (activity != null) {
                final Dialog dialog1 = new Dialog(activity, android.R.style.Theme_Light);
                dialog1.getWindow().setBackgroundDrawableResource(R.color.black_transparent);
                dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog1.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                dialog1.setContentView(R.layout.dialog_playtimeads_notify_);
                dialog1.setCancelable(false);

                Button btnOk = dialog1.findViewById(R.id.btnOk);
                btnOk.setText("Ok");
                Button btnCancel = dialog1.findViewById(R.id.btnCancel);
                btnCancel.setVisibility(View.GONE);
                TextView tvTitle = dialog1.findViewById(R.id.tvTitle);
                tvTitle.setText(activity.getString(R.string.sdk_app_name));

                TextView tvMessage = dialog1.findViewById(R.id.tvMessage);
                tvMessage.setText(Html.fromHtml(message));

                btnOk.setOnClickListener(v -> {
                    dialog1.dismiss();
                });
                dialog1.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean launchApp(Context context, String packageName) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            context.startActivity(launchIntent);//null pointer check in case package name was not found
            return true;
        }

        //invite to install
        Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(
                Uri.parse("market://details?id=" + packageName));
        if (marketIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(marketIntent);
            return true;
        }
        return false;
    }

    public static boolean isStringNullOrEmpty(String text) {
        return (text == null || text.trim().equals("null") || text.trim()
                .length() == 0);
    }

    public static void openUrl(Context c, String url) {
        if (!isStringNullOrEmpty(url)) {
            if (url.contains("/t.me/") || url.contains("telegram") || url.contains("facebook.com") || url.contains("instagram.com") || url.contains("youtube.com") || url.contains("play.google.com/store/apps/details") || url.contains("market.android.com/details")) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    c.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    openUrlInChrome(c, url);
                }
            } else {
                openUrlInChrome(c, url);
            }
        }
    }

    public static void openUrlInChrome(Context c, String url) {
        if (!isStringNullOrEmpty(url)) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                intent.setPackage("com.android.chrome");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    intent.setPackage(null);
                    c.startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    setToast(c, "No application found to handle this url");
                }
            }
        }
    }

    private static String finalUrl = "";
    private static Activity activityLoad;
    private static WebView webLoader;
    private static Handler handler;
    private static Dialog dialogLoaderOffer;

    public static void loadOffer(Activity activity, String str) {
        activityLoad = activity;
        dialogLoaderOffer = new Dialog(activity, android.R.style.Theme_Light);
        dialogLoaderOffer.getWindow().setBackgroundDrawableResource(R.color.black_transparent);
        dialogLoaderOffer.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogLoaderOffer.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        dialogLoaderOffer.setCancelable(true);
        dialogLoaderOffer.setCanceledOnTouchOutside(true);
        dialogLoaderOffer.setContentView(R.layout.dialog_playtimeads_progressbar);

        webLoader = dialogLoaderOffer.findViewById(R.id.webloader);

        if (!activity.isFinishing() && !dialogLoaderOffer.isShowing()) {
            dialogLoaderOffer.show();
        }

        webLoader.getSettings().setJavaScriptEnabled(true);
        webLoader.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView webView, String str) {
            }

            public boolean shouldOverrideUrlLoading(WebView webView, String str) {
                webView.loadUrl(str);
                return true;
            }

            public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
                super.onPageStarted(webView, str, bitmap);
                if (str.startsWith("market://") || str.startsWith("intent://") || str.startsWith("http://") || str.startsWith("https://")) {
                    finalUrl = str;
                }
                if (str.startsWith("market://") || str.startsWith("intent://")) {
                    if (handler != null) {
                        handler.removeCallbacksAndMessages(null);
                    }
                    openWebPage();
                }
            }
        });
        webLoader.loadUrl(str);
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        startTimer();
    }

    public static void openWebPage() {
        try {
            if (dialogLoaderOffer != null && !activityLoad.isFinishing()) {
                dialogLoaderOffer.dismiss();
            }

            if (finalUrl != null) {
                if (finalUrl.startsWith("intent:")) {
                    try {
                        Intent intent = Intent.parseUri(finalUrl, Intent.URI_INTENT_SCHEME);
                        if (intent.resolveActivity(activityLoad.getPackageManager()) != null) {
                            activityLoad.startActivity(intent);
                            return;
                        }
                        //try to find fallback url
                        String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                        if (fallbackUrl != null) {
                            webLoader.loadUrl(fallbackUrl);
                            return;
                        }
                        //invite to install
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(
                                Uri.parse("market://details?id=" + intent.getPackage()));
                        if (marketIntent.resolveActivity(activityLoad.getPackageManager()) != null) {
                            activityLoad.startActivity(marketIntent);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (finalUrl.startsWith("market://")) {
                    try {
                        //invite to install
                        String packageName = finalUrl.substring("market://details?id=".length());
                        if (packageName.contains("&")) {
                            packageName = packageName.substring(0, packageName.indexOf("&"));
                        }
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=" + packageName));
                        if (marketIntent.resolveActivity(activityLoad.getPackageManager()) != null) {
                            activityLoad.startActivity(marketIntent);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                openUrl(activityLoad, finalUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        finalUrl = "";
        activityLoad = null;
        webLoader = null;
        handler = null;
        dialogLoaderOffer = null;
    }

    private static void startTimer() {
        handler = new Handler();
        handler.postDelayed(CommonUtils::openWebPage, 8000);
    }

    public static final String DATE_TIME_FORMAT_STANDARDIZED_UTC = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_STANDARDIZED_UTC = "yyyy-MM-dd";

    public static Date formatDate(String datetime) throws ParseException {
        return new SimpleDateFormat(DATE_TIME_FORMAT_STANDARDIZED_UTC).parse(datetime);
    }

    public static Date formatOnlyDate(String datetime) throws ParseException {
        return new SimpleDateFormat(DATE_FORMAT_STANDARDIZED_UTC).parse(datetime);
    }

    public static String getStringDate(long time) {
        return new SimpleDateFormat(DATE_FORMAT_STANDARDIZED_UTC).format(new Date(time));
    }

    public static String getStringDateTime(long time) {
        return new SimpleDateFormat(DATE_TIME_FORMAT_STANDARDIZED_UTC).format(new Date(time));
    }

    public static boolean isPackageInstalled(Context c, String targetPackage) {
        PackageManager pm = c.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    public static void openInstalledApp(Context context, String packageName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);

            if (intent == null) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
            }
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            setToast(context, "Application not found");
        }
    }

    public static void openPlayStore(Context context, String appPackage) {
        try {
            Intent intents = context.getPackageManager().getLaunchIntentForPackage(appPackage);

            if (intents == null) {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + appPackage))
                        .setPackage(Constants.PLAY_STORE_PACKAGE_NAME)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
            } else {
                intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                context.startActivity(intents);
            }
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + appPackage)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
        }
    }

    public static boolean isUsageStatsPermissionGranted(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        boolean granted;
        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return granted;
    }

    private static Dialog dialog;

    public static void requestUsageStatsPermission(Context context, String applicationName, String message) {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        Drawable appIconBitmap = null;
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            applicationName = packageManager.getApplicationLabel(applicationInfo).toString();
            appIconBitmap = packageManager.getApplicationIcon(applicationInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (dialog == null) {
            dialog = new Dialog(context, android.R.style.Theme_Light);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.layout_playtimeads_permission);
            dialog.getWindow().setBackgroundDrawableResource(R.color.black_transparent);
            Button ok = dialog.findViewById(R.id.btnok);
            AppCompatButton dialogBtn_cancel = dialog.findViewById(R.id.btncancel);
            TextView tvApplicationName = dialog.findViewById(R.id.tv_applicationName);
            TextView tvMessage = dialog.findViewById(R.id.tvMessage);
            tvMessage.setText(message);
            ImageView ivApplicationIcon = dialog.findViewById(R.id.iv_applicationIcon);
            tvApplicationName.setText(applicationName);
            if (appIconBitmap != null) {
                ivApplicationIcon.setImageDrawable(appIconBitmap);
            }
            ok.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                        if (context instanceof Activity) {
                            context.startActivity(intent);
                        } else {
                            CommonUtils.setToast(context, "Please allow app usage permission");
                        }
                        dialog.dismiss();
                    } catch (ActivityNotFoundException var4) {
                        try {
                            Intent intentx = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            if (context instanceof Activity) {
                                context.startActivity(intentx);
                            } else {
                                CommonUtils.setToast(context, "Please allow app usage permission");
                            }
                            dialog.dismiss();
                        } catch (Exception e) {
                            CommonUtils.setToast(context, "Not able to open settings screen");
                        }
                    }
                }
            });
            dialogBtn_cancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

    public static String getTopPackageName(Context context) {

        String topPackageName = "";
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            List<UsageStats> stats =
                    mUsageStatsManager.queryUsageStats(
                            UsageStatsManager.INTERVAL_DAILY,
                            System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
                            System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
            if (stats != null) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : stats) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (!mySortedMap.isEmpty()) {
                    topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            } else {
                topPackageName = mActivityManager.getRunningAppProcesses().get(0).processName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return topPackageName;
    }
}
