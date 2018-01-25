package com.epubreader.epubreader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.epubreader.library.EpubReaderServer;

public class MainActivity extends AppCompatActivity {

    private EpubReaderServer server;
    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // server
        String documentRoot = ""; // place of static html/css
        String epubFile = ""; // place of ebook.epub

        server = EpubReaderServer.create(documentRoot, epubFile);

        // webview
        webview = findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewClient());
    }

    public void onBtStartClick(View view) {
        server.start();
        Toast.makeText(this, "Server started!", Toast.LENGTH_SHORT).show();
        webview.loadUrl("http://localhost:19090");
    }

    public void onBtStopClick(View view) {
        server.stop();
        Toast.makeText(this, "Server stopped!", Toast.LENGTH_SHORT).show();
    }
}
