package com.playtime.sdk.activity;

import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.browser.customtabs.CustomTabsIntent;

import com.google.gson.Gson;
import com.playtime.sdk.AppTrackingSetup;
import com.playtime.sdk.PlaytimeSDK;
import com.playtime.sdk.R;
import com.playtime.sdk.SyncDataUtils;
import com.playtime.sdk.database.PartnerApps;
import com.playtime.sdk.repositories.PartnerAppsRepository;
import com.playtime.sdk.utils.CommonUtils;
import com.playtime.sdk.utils.Constants;
import com.playtime.sdk.utils.Logger;
import com.playtime.sdk.utils.SharePrefs;

public class PlaytimeOfferWallActivity extends AppCompatActivity {
    private WebView webViewPage;
    private String urlPage;
    private String applicationName;
    private Dialog dialog;
    private Drawable appIconBitmap;
    private static long mLastClickTime = 0;

    public PlaytimeOfferWallActivity() {
    }

    @SuppressLint({"MissingInflatedId"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtils.setTheme(PlaytimeOfferWallActivity.this);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_playtime_offerwall);

        registerPackageInstallBroadCast();
        registerDeviceStatusBroadCast();

        applicationName = getIntent().getStringExtra("applicationName");
        urlPage = getIntent().getStringExtra("url");
        Logger.getInstance().e("PlaytimeOfferWallActivity URL===", urlPage);
        webViewPage = findViewById(R.id.webviewPage);
        webViewPage.getSettings().setJavaScriptEnabled(true);
        webViewPage.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webViewPage.setWebViewClient(new WebViewClient());
        webViewPage.setVerticalScrollBarEnabled(false);
        webViewPage.setHorizontalScrollBarEnabled(false);
        webViewPage.clearCache(true);
        webViewPage.getSettings().setDomStorageEnabled(true);
        webViewPage.getSettings().setLoadsImagesAutomatically(true);
        webViewPage.getSettings().setMixedContentMode(0);
        JSInterface jsInterface = new JSInterface();
        webViewPage.addJavascriptInterface(jsInterface, "Android");
        webViewPage.loadUrl(urlPage);
        webViewPage.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                    AppLogger.getInstance().d("shouldOverrideUrlLoading", "WEB RESOURCE URL : " + request.getUrl().toString());
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                super.onReceivedHttpAuthRequest(view, handler, host, realm);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                    AppLogger.getInstance().d("shouldOverrideUrlLoading", "URL : " + url);
                try {
                    if (url.startsWith("intent://")) {
                        try {
                            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                                return true;
                            }
                            //try to find fallback url
                            String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                            if (fallbackUrl != null) {
                                webViewPage.loadUrl(fallbackUrl);
                                return true;
                            }
                            return CommonUtils.launchApp(PlaytimeOfferWallActivity.this, intent.getPackage());
                        } catch (Exception e) {
                            //not an intent uri
                        }
                    } else if (url.startsWith("market://")) {
                        try {
                            //invite to install
                            String packageName = url.substring("market://details?id=".length());
                            if (packageName.contains("&")) {
                                packageName = packageName.substring(0, packageName.indexOf("&"));
                            }
                            return CommonUtils.launchApp(PlaytimeOfferWallActivity.this, packageName);
                        } catch (Exception e) {
                            //not an intent uri
                        }
                    } else {
                        webViewPage.loadUrl(urlPage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    webViewPage.loadUrl(urlPage);
                }
                return true;
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                try {
//                  AppLogger.getInstance().e("onRenderProcessGone Main", "===========" + view.getUrl());
                    if (webViewPage.canGoBack()) {
                        //AppLogger.getInstance().e("BROWSER WINDOW : ", "page.canGoBack() : " + page.canGoBack());
                        webViewPage.goBack();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
    }

    public class JSInterface {
        @JavascriptInterface
        public void onOfferClicked(String offerId, String screenNo, String url, String offer_type, String offerDetails) {
            CommonUtils.setToast(PlaytimeOfferWallActivity.this, "Offer Clicked ScreenNo: " + screenNo + "== Url: " + url);
            Logger.getInstance().e("onOfferClicked: ", "onOfferClicked==screenNo: " + screenNo);
            Logger.getInstance().e("onOfferClicked: ", "onOfferClicked==url: " + url);
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            boolean shouldTrackUsage = offer_type.equals(Constants.OFFER_TYPE_PLAYTIME) || offer_type.equals(Constants.OFFER_TYPE_DAY);
            if (!CommonUtils.isStringNullOrEmpty(offer_type) && shouldTrackUsage && !isUsageStatsPermissionGranted()) {
                requestUsageStatsPermission();
                return;
            }
            if (!CommonUtils.isStringNullOrEmpty(offerDetails)) {
                Logger.getInstance().e("INSERT OFFER: ", "INSERT OFFER: " + offerDetails);
                PartnerApps objPartnerApp = new Gson().fromJson(offerDetails, PartnerApps.class);
                // check if install receiver is setup
                new PartnerAppsRepository(PlaytimeOfferWallActivity.this).insert(objPartnerApp);
                // check if usage tracking manager is setup
                if (objPartnerApp.offer_type_id.equals(Constants.OFFER_TYPE_PLAYTIME) || objPartnerApp.offer_type_id.equals(Constants.OFFER_TYPE_DAY)) {
                    AppTrackingSetup.startAppTracking(PlaytimeOfferWallActivity.this);
                    PlaytimeSDK.getInstance().setTimer();
                }
            }
            if (screenNo != null && !screenNo.isEmpty()) {
                registerPackageInstallBroadCast();
                switch (screenNo) {
                    case "1":
                        // Open url externally
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CommonUtils.openUrl(PlaytimeOfferWallActivity.this, url);
                            }
                        });
                        break;
                    case "2":
                        // Open url in custom tab
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Uri uri = Uri.parse(url);
                                    new CustomTabsIntent.Builder()
                                            .build()
                                            .launchUrl(PlaytimeOfferWallActivity.this, uri);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                    case "3":
                        // load offer url in hidden browser and open play store
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (url != null) {
                                    CommonUtils.loadOffer(PlaytimeOfferWallActivity.this, url);
                                }
                            }
                        });
                        break;
                    case "4":
                        // Open url in current webview for task details
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (CommonUtils.isNetworkAvailable(PlaytimeOfferWallActivity.this)) {
                                    webViewPage.loadUrl(url);
                                } else {
                                    CommonUtils.setToast(PlaytimeOfferWallActivity.this, "No internet connection");
                                }
                            }
                        });
                        break;
                    case "5":
                        break;
                }
            }
        }

        @JavascriptInterface
        public void showToast(String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CommonUtils.setToast(PlaytimeOfferWallActivity.this, message);
                }
            });
        }
    }

    private boolean isUsageStatsPermissionGranted() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        boolean granted;
        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return granted;
    }

    private void requestUsageStatsPermission() {
        PackageManager packageManager = getPackageManager();
        String packageName = getPackageName();

        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            applicationName = packageManager.getApplicationLabel(applicationInfo).toString();
            appIconBitmap = packageManager.getApplicationIcon(applicationInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (dialog == null) {
            dialog = new Dialog(this, android.R.style.Theme_Light);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.permission_layout);
            dialog.getWindow().setBackgroundDrawableResource(R.color.black_transparent);
            AppCompatButton ok = dialog.findViewById(R.id.btnok);
            AppCompatButton dialogBtn_cancel = dialog.findViewById(R.id.btncancel);
            TextView tvApplicationName = dialog.findViewById(R.id.tv_applicationName);
            ImageView ivApplicationIcon = dialog.findViewById(R.id.iv_applicationIcon);
            tvApplicationName.setText(applicationName);
            ivApplicationIcon.setImageDrawable(appIconBitmap);
            ok.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                        dialog.dismiss();
                    } catch (ActivityNotFoundException var4) {
                        try {
                            Intent intentx = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            startActivity(intentx);
                            dialog.dismiss();
                        } catch (Exception e) {
                            CommonUtils.setToast(PlaytimeOfferWallActivity.this, "Not able to open settings screen");
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && !isUsageStatsPermissionGranted()) {
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void onResume() {
        super.onResume();
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        try {
            if (webViewPage.canGoBack()) {
                //AppLogger.getInstance().e("BROWSER WINDOW : ", "page.canGoBack() : " + page.canGoBack());
                webViewPage.goBack();
            } else {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }
                this.doubleBackToExitPressedOnce = true;
                CommonUtils.setToast(PlaytimeOfferWallActivity.this, "Press BACK again to exit");

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerPackageInstallBroadCast() {
        try {
            if (PlaytimeSDK.packageInstallBroadcast == null) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
                intentFilter.addAction(Intent.ACTION_INSTALL_PACKAGE);
                intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
                intentFilter.addDataScheme("package");
                PlaytimeSDK.packageInstallBroadcast = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Logger.getInstance().e(" PlaytimeSDK.packageInstallBroadcast INSTALL", "onReceive===" + intent.getAction());
                        if (!intent.getExtras().containsKey(Intent.EXTRA_REPLACING)) {
                            try {
                                // EDIT-check if it is a partner app
                                Logger.getInstance().e("InstallPackageReceiver", "NAME: " + intent.getData().toString().replace("package:", ""));
                                new PartnerAppsRepository(PlaytimeOfferWallActivity.this).checkIsPartnerApp(intent.getData().toString().replace("package:", ""), SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getString(SharePrefs.UDID), SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getString(SharePrefs.APP_ID), SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getString(SharePrefs.GAID));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    registerReceiver(PlaytimeSDK.packageInstallBroadcast, intentFilter, RECEIVER_EXPORTED);
                } else {
                    registerReceiver(PlaytimeSDK.packageInstallBroadcast, intentFilter);
                }
                Logger.getInstance().e("PackageInstallBroadCast onCreate=======", "REGISTER");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void registerDeviceStatusBroadCast() {
        try {
            if (PlaytimeSDK.deviceStatusBroadcast == null) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
                intentFilter.addAction(Intent.ACTION_DREAMING_STARTED);
                intentFilter.addAction(Intent.ACTION_DREAMING_STOPPED);
                intentFilter.addAction(Intent.ACTION_USER_PRESENT);
                PlaytimeSDK.deviceStatusBroadcast = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        try {
                            Logger.getInstance().e("DeviceStatusBroadCast", "NAME: " + intent.getAction());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    registerReceiver(PlaytimeSDK.deviceStatusBroadcast, intentFilter, RECEIVER_EXPORTED);
                } else {
                    registerReceiver(PlaytimeSDK.deviceStatusBroadcast, intentFilter);
                }
                Logger.getInstance().e("DeviceStatusBroadCast onCreate=======", "REGISTER");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unregisterReceiver() {
        try {
            if (PlaytimeSDK.packageInstallBroadcast != null) {
                unregisterReceiver(PlaytimeSDK.packageInstallBroadcast);
                PlaytimeSDK.packageInstallBroadcast = null;
                Logger.getInstance().e("packageInstallBroadcast onDestroy=======", "UNREGISTER");
            }
            if (PlaytimeSDK.deviceStatusBroadcast != null) {
                unregisterReceiver(PlaytimeSDK.deviceStatusBroadcast);
                PlaytimeSDK.deviceStatusBroadcast = null;
                Logger.getInstance().e("DeviceStatusBroadCast onDestroy=======", "UNREGISTER");
            }
        } catch (Exception e) {
            PlaytimeSDK.packageInstallBroadcast = null;
            PlaytimeSDK.deviceStatusBroadcast = null;
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            unregisterReceiver();
        }
    }
}
