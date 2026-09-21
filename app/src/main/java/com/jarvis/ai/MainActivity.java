package com.jarvis.ai;

import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private CameraManager cameraManager;
    private String cameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        webView = new WebView(this);
        setContentView(webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (cameraManager != null && cameraManager.getCameraIdList().length > 0) {
                cameraId = cameraManager.getCameraIdList()[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        webView.loadUrl("file:///android_asset/jarvis.html");
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public boolean setTorch(boolean turnOn) {
            try {
                if (cameraManager != null && cameraId != null) {
                    cameraManager.setTorchMode(cameraId, turnOn);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @JavascriptInterface
        public int getBatteryLevel() {
            try {
                BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
                return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            } catch (Exception e) {
                return -1;
            }
        }

        @JavascriptInterface
        public boolean openApp(String appName) {
            String packageName = "";
            String name = appName.toLowerCase();
            if (name.contains("youtube")) packageName = "com.google.android.youtube";
            else if (name.contains("whatsapp")) packageName = "com.whatsapp";
            else if (name.contains("chrome")) packageName = "com.android.chrome";
            else if (name.contains("camera")) packageName = "com.android.camera";
            else if (name.contains("settings")) packageName = "com.android.settings";

            if (!packageName.isEmpty()) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
