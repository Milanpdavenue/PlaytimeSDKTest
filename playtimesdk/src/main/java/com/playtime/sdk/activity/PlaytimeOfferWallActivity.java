package com.playtime.sdk.activity;

import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.playtime.sdk.R;
import com.playtime.sdk.utils.CommonUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PlaytimeOfferWallActivity extends AppCompatActivity {
    private WebView webView;
    private WebView webViewPage;
    private String urlPage;
    private String appId;
    private String userId;
    private String deviceId;
    private String gaId;
    private String applicationName;
    private Dialog dialog;
    private Drawable appIconBitmap;
    private static final int REQUEST_USAGE_STATS = 1;
    private static final String TAG = "FileAvailabilityChecker";
    private boolean isDialogShown = false;
    private boolean isTaskPe = false;
    int redirectCount = -1;
    int red = 0;
    private String redirectedUrl = "";
    boolean isPLay = false;
    int notPlay = -1;
    boolean started = false;
    boolean isMmn = false;
    boolean isIn = false;
    boolean isLoaded = false;
    ArrayList<String> urlList = new ArrayList();

    public PlaytimeOfferWallActivity() {
    }

    @SuppressLint({"MissingInflatedId"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtils.setTheme(PlaytimeOfferWallActivity.this);
        setContentView(R.layout.webview);

        appId = getIntent().getStringExtra("appId");
        userId = getIntent().getStringExtra("userId");
        gaId = getIntent().getStringExtra("gaId");
        applicationName = getIntent().getStringExtra("applicationName");
        deviceId = Settings.Secure.getString(getContentResolver(), "android_id");
        urlPage = getIntent().getStringExtra("url");
        Log.e("URL===", urlPage);
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
        webViewPage.loadUrl(urlPage);
        if (!isUsageStatsPermissionGranted()) {
            requestUsageStatsPermission();
        } else {
            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long endTime = System.currentTimeMillis();
            long startTime = endTime - 86400000L;
            List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(0, startTime, endTime);
            Set<String> userInstalledAppPackages = new HashSet();
            Iterator var10 = usageStatsList.iterator();

            while (var10.hasNext()) {
                UsageStats usageStats = (UsageStats) var10.next();
                String packageName = usageStats.getPackageName();
                if (!isSystemApp(packageName) && !packageName.contains("com.google") && !packageName.contains("com.android") && !packageName.contains("com.gms")) {
                    userInstalledAppPackages.add(packageName);
                }
            }

            JSONArray jsonArray = new JSONArray(userInstalledAppPackages);
            sendPackages(jsonArray);
        }

        webView = findViewById(R.id.webView);
        webView.setVisibility(View.GONE);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadsImagesAutomatically(false);
        webSettings.setMixedContentMode(0);
        webView.setWebViewClient(new MyBrowserNew());
        webViewPage.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String urlPage) {
                if (CommonUtils.isNetworkAvailable(PlaytimeOfferWallActivity.this)) {
                    if (!isDialogShown) {
                        showLoadingDialog();
                    }

                    isTaskPe = true;
                    webView.loadUrl(urlPage);
                } else {
                    CommonUtils.setToast(PlaytimeOfferWallActivity.this, "No internet connection");
                }
                return true;
            }
        });
    }

    private boolean isUsageStatsPermissionGranted() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName());
        return mode == 0;
    }

    private void requestUsageStatsPermission() {
        PackageManager packageManager = getPackageManager();
        String packageName = getPackageName();

        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            applicationName = packageManager.getApplicationLabel(applicationInfo).toString();
            appIconBitmap = packageManager.getApplicationIcon(applicationInfo);
        } catch (PackageManager.NameNotFoundException var8) {
            var8.printStackTrace();
        }

        final Dialog dialog = new Dialog(this, android.R.style.Theme_Light);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        dialog.setCancelable(false);
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
                    Uri uri = Uri.fromParts("package", getPackageName(), (String) null);
                    intent.setData(uri);
                    startActivity(intent);
                    dialog.dismiss();
                } catch (ActivityNotFoundException var4) {
                    Intent intentx = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    startActivity(intentx);
                    dialog.dismiss();
                }

            }
        });
        dialogBtn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && isUsageStatsPermissionGranted()) {
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showLoadingDialog() {
        if (!isDialogShown) {
            dialog = new Dialog(this);
            dialog.requestWindowFeature(1);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.loadingbar);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            dialog.show();
            isDialogShown = true;
        }
    }

    private boolean isSystemApp(String packageName) {
        PackageManager packageManager = getPackageManager();

        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            if ((applicationInfo.flags & 1) == 0 && (applicationInfo.flags & 128) == 0) {
                int uid = applicationInfo.uid;
                return uid >= 1000 && uid <= 19999;
            } else {
                return true;
            }
        } catch (PackageManager.NameNotFoundException var5) {
            return false;
        }
    }

    protected void onResume() {
        super.onResume();
        redirectCount = -1;
        red = 0;
        redirectedUrl = "";
        isPLay = false;
        notPlay = -1;
        started = false;
        isMmn = false;
        isIn = false;
        isLoaded = false;
        urlList = new ArrayList();
        MyBrowserNew myBrowserNew = new MyBrowserNew();
        isDialogShown = false;
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        webView.setWebViewClient(myBrowserNew);
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    private void sendPackages(JSONArray jsonArray) {
    }

    private class MyBrowserNew extends WebViewClient {
        private Handler handler;

        private MyBrowserNew() {
        }

        @TargetApi(24)
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
            Uri uri = request.getUrl();
            return shouldOverrideUrlLoading(webView, uri.toString());
        }

        public boolean shouldOverrideUrlLoading(WebView webView, String str) {
            ++redirectCount;
            redirectedUrl = str;
            urlList.add(str);
            Log.d("TAG", "shouldOverrideUrlLoading:  red - " + redirectCount + " " + red + "");
            Log.d("TAG", "shouldOverrideUrlLoading:  s -" + str);
            if (!str.startsWith("market://") && !str.startsWith("https://play.google.com")) {
                if (!str.startsWith("https://") && !str.startsWith("http://") && !str.startsWith("market://")) {
                    isMmn = true;
                    Log.d("TAG", "shouldOverrideUrlLoading: not a url  s -" + str);
                    webView.goBack();
                    String url = webView.getUrl() != null ? webView.getUrl() : str;
                    startNewIntentActivity(url, 1);
                    webView.stopLoading();
                } else {
                    if (CommonUtils.isNetworkAvailable(PlaytimeOfferWallActivity.this)) {
                        webView.loadUrl(str);
                    } else {
                        CommonUtils.setToast(PlaytimeOfferWallActivity.this, "No internet connection");
                    }
                }
            } else {
                isPLay = true;
                startNewIntentActivity(str, 0);
                webView.stopLoading();
            }
            return true;
        }

        public void onPageCommitVisible(WebView view, String url) {
            super.onPageCommitVisible(view, url);
            Log.d("TAG", "onPageCommitVisible: " + url);
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            started = true;
            Log.d("TAG", "onPageStarted: " + redirectCount);
        }

        public void onPageFinished(WebView view, final String str) {
            Log.d("TAG", "WebViewClient: onPageFinished: url: " + str);
            Log.d("TAG", "isplay: o " + isPLay);
            Log.d("TAG", "notplay " + notPlay);
            Log.d("TAG", "redirectCount " + redirectCount);
            super.onPageFinished(view, str);
            urlList.add(str);
            if (started) {
                started = false;
            } else {
                Log.d("TAG", "onPageFinished: started " + redirectedUrl);
            }

            if (redirectedUrl.equals(str)) {
                Log.d("TAG", "checkForUrls: yes matched");
                if (!isPLay && !isMmn) {
                    startNewIntentActivity(str, 2);
                }
            }

            if (notPlay >= redirectCount && !isPLay) {
                startNewIntentActivity(redirectedUrl, 4);
                webView.stopLoading();
            }

            if (redirectCount <= 2) {
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        try {
                            if (!isPLay) {
                                Log.d("TAG12", "checkForUrls: " + str);
                                if (redirectCount <= 2) {
                                    webView.stopLoading();
                                    MyBrowserNew.this.startNewIntentActivity(redirectedUrl, 5);
                                } else {
                                    MyBrowserNew.this.startNewIntentActivity(redirectedUrl, 6);
                                    webView.stopLoading();
                                }
                            }
                        } catch (Exception var2) {
                            var2.printStackTrace();
                            Log.d("TAG", "run: err");
                        }

                    }
                }, 10000L);
            }

        }

        public void startNewIntentActivity(String url, int i) {
            try {
                isLoaded = true;
                Log.d("TAG", "startNewIntentActivity: " + i);
                webView.stopLoading();
                if (isPLay) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        intent.setPackage("com.android.vending");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (ActivityNotFoundException var5) {
                        openPlayStoreWebsite(url);
                    }

                    isLoaded = true;
                } else {
                    webView.stopLoading();

                    try {
                        if (isTaskPe) {
                            Log.d("TAG1", "onActivityResult: " + url);
                            isIn = true;
                            openExternalLink(url);
                            isTaskPe = false;
                        }
                    } catch (Exception var4) {
                        var4.printStackTrace();
                        Log.d("TAG", "startNewIntentActivity: err" + var4.getMessage());
                        openExternalLink(url);
                    }
                }
            } catch (Exception var6) {
                var6.printStackTrace();
                Log.d("TAG", "startNewIntentActivity: err" + var6.getMessage());
                Log.d("TAG", "startNewIntentActivity: " + urlList.get(urlList.size() - 1));
            }

        }

        private void openPlayStoreWebsite(String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        public void openExternalLink(String url) {
            Uri webpage = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            if (intent.resolveActivity(getPackageManager()) != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(urlList.get(urlList.size() - 1)));
                startActivity(i);
            }
        }
    }

}
