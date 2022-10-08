package com.scsa.andr.selfmanagementapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class GameShop extends AppCompatActivity {

    WebView wView; // 웹뷰
    ProgressBar pBar; // 로딩바

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_shop);

        wView = findViewById(R.id.wView);
        wView.setWebViewClient(new WebViewClient()); // 웹뷰클라이언트
        pBar = findViewById(R.id.pBar);
        pBar.setVisibility(View.GONE); // 로딩바 가려두기

        initWebView();
    }

    public void initWebView() {
        wView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                pBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        WebSettings ws = wView.getSettings();
        ws.setJavaScriptEnabled(true);
        wView.loadUrl("https://store.kakao.com/category/9/104113101100?level=3");

    }

    @Override
    public void onBackPressed() {
        if (wView.canGoBack()) {
            wView.goBack();
        } else {
            super.onBackPressed();
        }
    }

}