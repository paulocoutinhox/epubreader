package com.epubreader.epubreader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.epubreader.library.EpubReaderServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.logging.Logger;

public class EpubReaderFragment extends Fragment {

    private static final String ARG_PORT = "port";
    private static final String ARG_DOCUMENT_ROOT = "document-root";
    private static final String ARG_EPUB_FILE = "epub-file";
    private static final String ARG_HOME = "home";

    private EpubReaderServer server;

    private String port;
    private String documentRoot;
    private String home;
    private String epubFile;
    private String logFile;

    private EpubReaderFragmentListener listener;
    private WebView webview;

    private final String TAG = "EPUBREADER";

    public EpubReaderFragment() {

    }

    public static EpubReaderFragment newInstance(String port, String home, String documentRoot, String epubFile) {
        EpubReaderFragment fragment = new EpubReaderFragment();
        Bundle args = new Bundle();

        args.putString(ARG_PORT, port);
        args.putString(ARG_HOME, home);
        args.putString(ARG_DOCUMENT_ROOT, documentRoot);
        args.putString(ARG_EPUB_FILE, epubFile);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            // arguments - required
            port = getArguments().getString(ARG_PORT);
            home = getArguments().getString(ARG_HOME);
            documentRoot = getArguments().getString(ARG_DOCUMENT_ROOT);
            epubFile = getArguments().getString(ARG_EPUB_FILE);
            logFile = home + "/epubreader.log";

            // copy assets
            AssetManager assetManager = getContext().getAssets();

            try {
                AssetsHelper.copyAssets(assetManager, new File(getContext().getFilesDir().getAbsolutePath()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i(TAG, "Document root: " + documentRoot);
            Log.i(TAG, "Epub file: " + epubFile);

            // server
            server = EpubReaderServer.create(port, documentRoot, epubFile);
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_epub_reader, container, false);

        // webview
        webview = view.findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webview.addJavascriptInterface(new EpubReaderJavascriptInterface(), "Android");

        webview.setWebViewClient(new WebViewClient() {

            private int running = 0;

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                running++;
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                running = Math.max(running, 1);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (--running == 0) {
                    //
                }
            }

        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EpubReaderFragmentListener) {
            listener = (EpubReaderFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement EpubReaderFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void setListener(EpubReaderFragmentListener listener) {
        this.listener = listener;
    }

    public interface EpubReaderFragmentListener {
        void onEpubLoaded();
    }

    /**
     * EPUB FRAGMENT ACTIONS
     */

    public void actionStartServer() {
        if (server != null) {
            server.start();
        }
    }

    public void actionStopServer() {
        if (server != null) {
            server.stop();
        }
    }

    public void actionGoToHome() {
        webview.loadUrl("http://localhost:" + port);
    }

    public String actionGetLogContent() {
        try {
            return getStringFromFile(logFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public void actionSetThemeDefault() {
        actionSetTheme("default");
    }

    public void actionSetThemeDark() {
        actionSetTheme("dark");
    }

    public void actionSetThemeSepia() {
        actionSetTheme("sepia");
    }

    public void actionSetTheme(String name) {
        evaluateJavascript("setTheme('" + name + "')", null);
    }

    public void actionNextPage() {
        evaluateJavascript("ebook.nextPage();", null);
    }

    public void actionPrevPage() {
        evaluateJavascript("ebook.prevPage();", null);
    }

    /**
     * EPUB FRAGMENT PRIVATE METHODS
     */

    private String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        reader.close();
        return sb.toString();
    }

    private String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        fin.close();
        return ret;
    }

    private void evaluateJavascript(final String script, final ValueCallback<String> resultCallback) {
        runOnMainThread(new Closure() {
            @Override
            public void exec() {
                if (Build.VERSION.SDK_INT > 18) {
                    webview.evaluateJavascript(script, resultCallback);
                } else {
                    webview.loadUrl("javascript:" + script);

                    if (resultCallback != null) {
                        resultCallback.onReceiveValue(null);
                    }
                }
            }
        });
    }

    private static void runOnMainThread(final Closure closure) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (closure != null) {
                    closure.exec();
                }
            }
        });
    }

    public interface Closure {

        void exec();

    }

    /**
     * JAVASCRIPT INTERFACE
     */

    private class EpubReaderJavascriptInterface {

        @JavascriptInterface
        public void getPageData(final int currentPageIndex, final int totalPages, final float percentage) {

        }

        @JavascriptInterface
        public void pageLoaded() {
            if (listener != null) {
                runOnMainThread(new Closure() {
                    @Override
                    public void exec() {
                        listener.onEpubLoaded();
                    }
                });
            }
        }

    }

}
