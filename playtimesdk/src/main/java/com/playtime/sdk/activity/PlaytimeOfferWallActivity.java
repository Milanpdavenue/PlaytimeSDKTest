package com.playtime.sdk.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.playtime.sdk.AppTrackingSetup;
import com.playtime.sdk.PlaytimeSDK;
import com.playtime.sdk.R;
import com.playtime.sdk.SyncDataUtils;
import com.playtime.sdk.async.ClickOfferAsync;
import com.playtime.sdk.database.PartnerApps;
import com.playtime.sdk.repositories.PartnerAppsRepository;
import com.playtime.sdk.utils.CommonUtils;
import com.playtime.sdk.utils.Constants;
import com.playtime.sdk.utils.Logger;
import com.playtime.sdk.utils.SharePrefs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class PlaytimeOfferWallActivity extends AppCompatActivity {
    private WebView webViewPage;
    private String urlPage;
    private static long mLastClickTime = 0;
    private boolean isFirstTime = true;
    private ShimmerFrameLayout shimmerLayout;
    private String applicationName;

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
        setContentView(R.layout.activity_playtimeads_offerwall);

        setViews();
    }

    private void setViews() {
        shimmerLayout = findViewById(R.id.shimmer_layout);
        if (CommonUtils.isNetworkAvailable(PlaytimeOfferWallActivity.this)) {
            applicationName = getIntent().getStringExtra("applicationName");
            urlPage = getIntent().getStringExtra("url");
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
                                    if (CommonUtils.isNetworkAvailable(PlaytimeOfferWallActivity.this)) {
                                        webViewPage.loadUrl(fallbackUrl);
                                    } else {
                                        CommonUtils.setToast(PlaytimeOfferWallActivity.this, "No internet connection");
                                    }
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
                            if (CommonUtils.isNetworkAvailable(PlaytimeOfferWallActivity.this)) {
                                webViewPage.loadUrl(urlPage);
                            } else {
                                CommonUtils.setToast(PlaytimeOfferWallActivity.this, "No internet connection");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (CommonUtils.isNetworkAvailable(PlaytimeOfferWallActivity.this)) {
                            webViewPage.loadUrl(urlPage);
                        } else {
                            CommonUtils.setToast(PlaytimeOfferWallActivity.this, "No internet connection");
                        }
                    }
                    return true;
                }

                @Override
                public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                    try {
                        if (CommonUtils.isNetworkAvailable(PlaytimeOfferWallActivity.this)) {
                            if (webViewPage.canGoBack()) {
                                webViewPage.goBack();
                            }
                        } else {
                            CommonUtils.setToast(PlaytimeOfferWallActivity.this, "No internet connection");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    shimmerLayout.setVisibility(View.GONE);
                    if (isFirstTime) {
                        if (!SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getBoolean(SharePrefs.IS_CONSENT_GIVEN)) {
                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    CommonUtils.showConsentPopup(PlaytimeOfferWallActivity.this, SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getString(SharePrefs.CONSENT_TITLE), SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getString(SharePrefs.CONSENT_MESSAGE));
                                }
                            }, 1500);
                        } else {
                            askUsagePermissionAndResumePlaytimeUsage();
                        }
                    }
                    isFirstTime = false;
                }
            });
        } else {
            CommonUtils.setToast(PlaytimeOfferWallActivity.this, "No internet connection");
//          CommonUtils.Notify(PlaytimeOfferWallActivity.this, "No Internet Connection", "It seems you are not connected to internet. Please turn on internet connection and try again.", true);
        }
        registerPackageInstallBroadCast();
//        registerDeviceStatusBroadCast();
    }

    public void askUsagePermissionAndResumePlaytimeUsage() {
        if (SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getInt(SharePrefs.ONGOING_OFFER_COUNT) > 0 && !CommonUtils.isUsageStatsPermissionGranted(PlaytimeOfferWallActivity.this)) {
            CommonUtils.requestUsageStatsPermission(PlaytimeOfferWallActivity.this, getPackageName(), "To track playtime offers you need to give Usage Access Permission. Kindly go to settings screen and turn on toggle button to allow this permission.");
        }
        if (SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getInt(SharePrefs.ONGOING_OFFER_COUNT) > 0) {
            AppTrackingSetup.startAppTracking(PlaytimeOfferWallActivity.this);
            PlaytimeSDK.getInstance().setTimer();
        }
    }

    public class JSInterface {
        @JavascriptInterface
        public void onOfferClicked(String offerId, String screenNo, String url, String offer_type, String offerDetails, String packageName) {
            try {
                if (CommonUtils.isNetworkAvailable(PlaytimeOfferWallActivity.this)) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    try {
                        boolean shouldTrackUsage = offer_type.equals(Constants.OFFER_TYPE_PLAYTIME) || offer_type.equals(Constants.OFFER_TYPE_DAY);
                        if (!CommonUtils.isStringNullOrEmpty(offer_type) && shouldTrackUsage && !CommonUtils.isUsageStatsPermissionGranted(PlaytimeOfferWallActivity.this)) {
                            CommonUtils.requestUsageStatsPermission(PlaytimeOfferWallActivity.this, applicationName, "To track this offer you need to give Usage Access Permission. Kindly go to settings screen and turn on toggle button to allow this permission.");
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!CommonUtils.isStringNullOrEmpty(offerDetails)) {
                        PartnerApps objPartnerApp = getPartnerApps(offerDetails);
                        objPartnerApp.click_time = Calendar.getInstance().getTimeInMillis();
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
                                        if (CommonUtils.isNetworkAvailable(PlaytimeOfferWallActivity.this)) {
                                            CommonUtils.openUrl(PlaytimeOfferWallActivity.this, url);
                                        } else {
                                            CommonUtils.setToast(PlaytimeOfferWallActivity.this, "No internet connection");
                                        }
                                    }
                                });
                                break;
                            case "2":
                                // Open url in custom tab
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            if (CommonUtils.isNetworkAvailable(PlaytimeOfferWallActivity.this)) {
                                                Uri uri = Uri.parse(url);
                                                new CustomTabsIntent.Builder()
                                                        .build()
                                                        .launchUrl(PlaytimeOfferWallActivity.this, uri);
                                            } else {
                                                CommonUtils.setToast(PlaytimeOfferWallActivity.this, "No internet connection");
                                            }
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
                                        if (CommonUtils.isNetworkAvailable(PlaytimeOfferWallActivity.this)) {
                                            if (url != null) {
                                                CommonUtils.loadOffer(PlaytimeOfferWallActivity.this, url);
                                            }
                                        } else {
                                            CommonUtils.setToast(PlaytimeOfferWallActivity.this, "No internet connection");
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
                                            String url1 = url + "&is_offer_installed=" + (CommonUtils.isPackageInstalled(PlaytimeOfferWallActivity.this, packageName) ? 1 : 0);
//                                            Logger.getInstance().e("DETAILS URL:","===DETAILS URL===="+url1+" package : "+packageName);
                                            webViewPage.loadUrl(url1);
                                        } else {
                                            CommonUtils.setToast(PlaytimeOfferWallActivity.this, "No internet connection");
                                        }
                                    }
                                });
                                break;
                            case "5": // open app if its already installed
                                CommonUtils.openInstalledApp(PlaytimeOfferWallActivity.this, packageName);
                                break;
                            case "6": // trigger s2s click
                                CommonUtils.openPlayStore(PlaytimeOfferWallActivity.this, packageName);
                                String newUrl = url.replace("CLICK_TIME", String.valueOf(Calendar.getInstance().getTimeInMillis()));
                                new ClickOfferAsync(newUrl);
                                break;
                            case "7": // sync usage data
                                if (!SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getBoolean(SharePrefs.IS_SYNC_IN_PROGRESS)) {
                                    new SyncDataUtils().syncData(PlaytimeOfferWallActivity.this);
                                }
                                break;
                            case "8": // start timer and work manager
                                new PartnerAppsRepository(PlaytimeOfferWallActivity.this).startTracking(PlaytimeOfferWallActivity.this);
                                break;
                        }
                    }
                } else {
                    CommonUtils.setToast(PlaytimeOfferWallActivity.this, "No internet connection");
                }
            } catch (Exception e) {
                e.printStackTrace();
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

        @JavascriptInterface
        public int isAppInstalled(String packageName) {
            return CommonUtils.isPackageInstalled(PlaytimeOfferWallActivity.this, packageName) ? 1 : 0;
        }
    }

    @NonNull
    private static PartnerApps getPartnerApps(String offerDetails) throws JSONException {
        JSONObject json = new JSONObject(offerDetails);
        PartnerApps objPartnerApp = new PartnerApps(
                json.getInt("task_offer_id"),
                json.getString("task_offer_name"),
                json.getString("package_id"),
                json.getInt("is_installed"),
                json.getString("install_time"),
                json.getInt("conversion_id"),
                json.getString("last_completion_time"),
                json.getString("offer_type_id"),
                json.getInt("is_completed"));
        return objPartnerApp;
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
//            Logger.getInstance().e("PLAY TIME SDK: ", "onResume isTimer ON: ==>" + PlaytimeSDK.getInstance().getTimer() + " isTimeElapsed : " + ((Calendar.getInstance().getTimeInMillis() - SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getLong(SharePrefs.LAST_SYNC_TIME)) > (1.2 * 60 * 1000L)));
            if (SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getInt(SharePrefs.ONGOING_OFFER_COUNT) > 0 && (Calendar.getInstance().getTimeInMillis() - SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getLong(SharePrefs.LAST_SYNC_TIME)) > (1.2 * 60 * 1000L)) {
                PlaytimeSDK.getInstance().stopTimer();
                PlaytimeSDK.getInstance().setContext(PlaytimeOfferWallActivity.this);// set context because when process gets stop, it set null to context
                PlaytimeSDK.getInstance().setTimer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        try {
            if (webViewPage.canGoBack() && !CommonUtils.isNetworkAvailable(PlaytimeOfferWallActivity.this)) {
                CommonUtils.setToast(PlaytimeOfferWallActivity.this, "No internet connection");
            } else if (webViewPage.canGoBack()) {
                webViewPage.goBack();
            } else {
                if (doubleBackToExitPressedOnce) {
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.removeAllCookie();
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
                        if (!intent.getExtras().containsKey(Intent.EXTRA_REPLACING)) {
                            try {
                                // EDIT-check if it is a partner app
                                new PartnerAppsRepository(PlaytimeOfferWallActivity.this).checkIsPartnerApp(intent.getData().toString().replace("package:", ""),
                                        SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getString(SharePrefs.UDID),
                                        SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getString(SharePrefs.APP_ID),
                                        SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getString(SharePrefs.GAID),
                                        SharePrefs.getInstance(PlaytimeOfferWallActivity.this).getString(SharePrefs.USER_ID));

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
            }
        } catch (Exception e) {
            PlaytimeSDK.packageInstallBroadcast = null;
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
