package com.playtime.sdk.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.playtime.sdk.R;
import com.playtime.sdk.activity.PlaytimeOfferWallActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
                dialog1.setContentView(R.layout.dialog_terms);
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
                dialog1.setContentView(R.layout.popup_notify);
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
        //AppLogger.getInstance().e("URL openUrlInChrome :============", url);
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
        dialogLoaderOffer.setContentView(R.layout.popup_progressbar);

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
                //AppLogger.getInstance().e("finalUrl11--)", "" + finalUrl);
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
}
