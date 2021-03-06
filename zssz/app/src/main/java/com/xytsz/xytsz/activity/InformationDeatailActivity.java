package com.xytsz.xytsz.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.xytsz.xytsz.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by admin on 2017/8/8.
 * <p>
 * <p>
 * 新闻展示页
 */
public class InformationDeatailActivity extends AppCompatActivity {

    @Bind(R.id.informationdetail_webview)
    WebView informationdetailWebview;
    @Bind(R.id.informationdetail_progressbar)
    ProgressBar informationdetailProgressbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informationdetail);
        ButterKnife.bind(this);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("新闻展示");

        }

        informationdetailWebview.loadUrl("http://www.baidu.com");
        WebSettings settings = informationdetailWebview.getSettings();
        settings.setUseWideViewPort(true);
        settings.setJavaScriptEnabled(true);


        informationdetailWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //加载开始
                informationdetailProgressbar.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //加载完成后

                informationdetailProgressbar.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }
        });



    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
