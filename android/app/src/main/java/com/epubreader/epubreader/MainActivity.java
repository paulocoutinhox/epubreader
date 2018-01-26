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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements EpubReaderFragment.EpubReaderFragmentListener {

    private EpubReaderFragment fragment;
    private ProgressDialog progressDialog;

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

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onEpubLoaded() {
        progressDialog.dismiss();
    }
}
