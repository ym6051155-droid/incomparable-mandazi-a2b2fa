package site.sust.library;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private static final String HOME_URL = "https://sust-library.site/";
    private static final String STATE_URL = "current_url";

    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(20, 66, 112));
        getWindow().setNavigationBarColor(Color.WHITE);

        FrameLayout root = new FrameLayout(this);

        webView = new WebView(this);
        webView.setLayoutParams(new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        progressBar = new ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        );
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(3)
        );
        progressParams.gravity = Gravity.TOP;
        progressBar.setLayoutParams(progressParams);
        progressBar.setMax(100);

        root.addView(webView);
        root.addView(progressBar);
        setContentView(root);

        configureWebView();

        String initialUrl = HOME_URL;
        if (savedInstanceState != null) {
            String restored = savedInstanceState.getString(STATE_URL);
            if (restored != null && !restored.isBlank()) {
                initialUrl = restored;
            }
        } else {
            Uri incoming = getIntent().getData();
            if (incoming != null && "sust-library.site".equalsIgnoreCase(incoming.getHost())) {
                initialUrl = incoming.toString();
            }
        }

        webView.loadUrl(initialUrl);
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setUserAgentString(
            settings.getUserAgentString() + " SUSTLibraryAndroid/1.0.0"
        );

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(
                    newProgress >= 100 ? View.GONE : View.VISIBLE
                );
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(
                WebView view,
                WebResourceRequest request
            ) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                String host = uri.getHost();

                if (
                    ("http".equalsIgnoreCase(scheme) ||
                     "https".equalsIgnoreCase(scheme)) &&
                    (
                        "sust-library.site".equalsIgnoreCase(host) ||
                        host == null
                    )
                ) {
                    return false;
                }

                openExternal(uri);
                return true;
            }

            @Override
            public void onReceivedError(
                WebView view,
                WebResourceRequest request,
                WebResourceError error
            ) {
                if (request.isForMainFrame() && !isOnline()) {
                    showOfflinePage();
                }
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(
                String url,
                String userAgent,
                String contentDisposition,
                String mimeType,
                long contentLength
            ) {
                startDownload(
                    url,
                    userAgent,
                    contentDisposition,
                    mimeType
                );
            }
        });
    }

    private void startDownload(
        String url,
        String userAgent,
        String contentDisposition,
        String mimeType
    ) {
        try {
            String fileName = URLUtil.guessFileName(
                url,
                contentDisposition,
                mimeType
            );

            DownloadManager.Request request =
                new DownloadManager.Request(Uri.parse(url));

            if (userAgent != null && !userAgent.isBlank()) {
                request.addRequestHeader("User-Agent", userAgent);
            }

            String cookie = CookieManager.getInstance().getCookie(url);
            if (cookie != null && !cookie.isBlank()) {
                request.addRequestHeader("Cookie", cookie);
            }

            if (mimeType != null && !mimeType.isBlank()) {
                request.setMimeType(mimeType);
            }

            request.setTitle(fileName);
            request.setDescription("تنزيل ملف من مكتبة SUST");
            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            );
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            );
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);

            DownloadManager manager =
                (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

            if (manager == null) {
                throw new IllegalStateException("DownloadManager unavailable");
            }

            manager.enqueue(request);
            Toast.makeText(
                this,
                "بدأ تنزيل " + fileName,
                Toast.LENGTH_LONG
            ).show();
        } catch (Exception error) {
            openExternal(Uri.parse(url));
            Toast.makeText(
                this,
                "تم فتح رابط الملف في المتصفح",
                Toast.LENGTH_LONG
            ).show();
        }
    }

    private void openExternal(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (Exception error) {
            Toast.makeText(
                this,
                "لا يوجد تطبيق لفتح هذا الرابط",
                Toast.LENGTH_LONG
            ).show();
        }
    }

    private boolean isOnline() {
        ConnectivityManager manager =
            (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE
            );

        if (manager == null) return false;

        Network network = manager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities =
            manager.getNetworkCapabilities(network);

        return capabilities != null &&
            (
                capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                ) ||
                capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
                ) ||
                capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_ETHERNET
                )
            );
    }

    private void showOfflinePage() {
        String html =
            "<!doctype html><html dir='rtl' lang='ar'>" +
            "<head><meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<style>" +
            "body{margin:0;min-height:100vh;display:grid;place-items:center;" +
            "padding:24px;background:#f3f6fb;color:#172033;" +
            "font-family:Tahoma,Arial,sans-serif}" +
            ".card{max-width:440px;background:white;border:1px solid #d7e0e9;" +
            "border-radius:20px;padding:28px 20px;text-align:center;" +
            "box-shadow:0 16px 45px rgba(15,23,42,.10)}" +
            "h1{font-size:24px;margin:0 0 10px}" +
            "p{line-height:1.8;color:#667085}" +
            "button{border:0;border-radius:10px;background:#1f5fae;color:white;" +
            "padding:12px 20px;font:inherit;font-weight:bold}" +
            "</style></head><body><main class='card'>" +
            "<h1>لا يوجد اتصال بالإنترنت</h1>" +
            "<p>تحقق من الشبكة ثم اضغط إعادة المحاولة.</p>" +
            "<button onclick=\"location.href='" + HOME_URL + "'\">إعادة المحاولة</button>" +
            "</main></body></html>";

        webView.loadDataWithBaseURL(
            HOME_URL,
            html,
            "text/html",
            "UTF-8",
            null
        );
    }

    private int dp(int value) {
        return Math.round(
            value * getResources().getDisplayMetrics().density
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String current = webView != null ? webView.getUrl() : HOME_URL;
        outState.putString(STATE_URL, current);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
