package com.epubreader.epubreader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.epubreader.library.EpubReaderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

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
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webview.addJavascriptInterface(new EpubReaderJavascriptInterface(), "AndroidApp");

        webview.setWebViewClient(new WebViewClient());
        webview.setWebChromeClient(new WebChromeClient());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

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

    @Override
    public void onResume() {
        super.onResume();
        actionStartServer();
    }

    @Override
    public void onPause() {
        super.onPause();
        actionStopServer();
    }

    public interface EpubReaderFragmentListener {
        void onEpubReaderReady();

        void onEpubReaderPageChanged(int page, double percentage);
    }

    /**
     * EPUB FRAGMENT ACTIONS
     */

    public void actionStartServer() {
        actionStopServer();

        server = EpubReaderServer.create(port, documentRoot, epubFile);

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

    public void actionGetCurrentPageCfi(final PageCfiClosure closure) {
        evaluateJavascript("getCurrentPageCfi();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (closure != null) {
                    closure.exec(value);
                }
            }
        });
    }

    public void actionGetCurrentChapter(final CurrentChapterClosure closure) {
        evaluateJavascript("getCurrentChapter();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (closure != null) {
                    String id = "";
                    String href = "";
                    int pages = 0;
                    int spinePos = 0;
                    String absoluteHref = "";
                    String cfi = "";
                    String title = "";

                    if (!TextUtils.isEmpty(value)) {
                        try {
                            value = value.replace("\"{\\\"", "{\\\"");
                            value = value.replace("\"}\"", "\"}");
                            JSONObject json = new JSONObject(value);
                            id = json.getString("id");
                            href = json.getString("href");
                            absoluteHref = json.getString("absoluteHref");
                            cfi = json.getString("cfi");
                            title = json.getString("title");

                            pages = json.getInt("pages");
                            spinePos = json.getInt("spinePos");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    closure.exec(id, href, pages, spinePos, absoluteHref, cfi, title);
                }
            }
        });
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

    public interface PageCfiClosure {

        void exec(String cfi);

    }

    public interface CurrentChapterClosure {

        void exec(String id, String href, int pages, int spinePos, String absoluteHref, String cfi, String title);

    }

    /**
     * JAVASCRIPT INTERFACE
     */

    private class EpubReaderJavascriptInterface {

        @JavascriptInterface
        public void onReady() {
            if (listener != null) {
                runOnMainThread(new Closure() {
                    @Override
                    public void exec() {
                        listener.onEpubReaderReady();
                    }
                });
            }
        }

        @JavascriptInterface
        public void onPageChanged(final int page, final double percentage) {
            if (listener != null) {
                runOnMainThread(new Closure() {
                    @Override
                    public void exec() {
                        listener.onEpubReaderPageChanged(page, percentage);
                    }
                });
            }
        }

    }

}
