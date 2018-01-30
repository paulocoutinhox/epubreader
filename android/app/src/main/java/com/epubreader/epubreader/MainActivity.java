package com.epubreader.epubreader;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements EpubReaderFragment.EpubReaderFragmentListener {

    private EpubReaderFragment fragment;
    private ProgressDialog progressDialog;
    private TextView tvPage;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading. Please wait...");

        // configuration
        String port = "9090";
        String home = getFilesDir().getAbsolutePath();
        String documentRoot = home + "/www";
        String epubFile = home + "/data/ebook.epub";

        // page textview
        tvPage = findViewById(R.id.tv_page);


        // main fragment
        fragment = EpubReaderFragment.newInstance(port, home, documentRoot, epubFile);
        fragment.setListener(this);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment);
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (fragment == null) {
            return super.onOptionsItemSelected(item);
        }

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_item_start:
                progressDialog.show();
                fragment.actionStartServer();
                fragment.actionGoToHome();
                Toast.makeText(this, "Server started!", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menu_item_stop:
                fragment.actionStopServer();
                Toast.makeText(this, "Server stopped!", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menu_item_home:
                fragment.actionGoToHome();
                return true;

            case R.id.menu_item_server_log:
                try {
                    Log.i("EPUBREADER", fragment.actionGetLogContent());
                    Toast.makeText(this, "Log was sent to logcat!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;

            case R.id.menu_item_theme_default:
                fragment.actionSetThemeDefault();
                return true;

            case R.id.menu_item_theme_dark:
                fragment.actionSetThemeDark();
                return true;

            case R.id.menu_item_next_page:
                fragment.actionNextPage();
                return true;

            case R.id.menu_item_page_cfi:
                fragment.actionGetCurrentPageCfi(new EpubReaderFragment.PageCfiClosure() {
                    @Override
                    public void exec(String cfi) {
                        Toast.makeText(MainActivity.this, String.format(Locale.getDefault(), "Cfi: %s", cfi), Toast.LENGTH_SHORT).show();
                    }
                });
                return true;

            case R.id.menu_item_current_chapter:
                fragment.actionGetCurrentChapter(new EpubReaderFragment.CurrentChapterClosure() {
                    @Override
                    public void exec(String id, String href, int pages, int spinePos, String absoluteHref, String cfi, String title) {
                        Toast.makeText(MainActivity.this, String.format(Locale.getDefault(), "ID: %s\nHREF: %s\nPages: %d\nSpinePos: %d\nAbs HREF: %s\nCFI: %s\nTitle: %s", id, href, pages, spinePos, absoluteHref, cfi, title), Toast.LENGTH_SHORT).show();
                    }
                });
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onEpubReaderReady() {
        progressDialog.dismiss();
    }

    @Override
    public void onEpubReaderPageChanged(int page, double percentage) {
        tvPage.setText(String.format(Locale.getDefault(), "Page: %d - %.0f%%", page, Math.ceil(percentage * 100)));
    }

}
