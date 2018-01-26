package com.epubreader.epubreader;

import android.annotation.TargetApi;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetsHelper {

    private static final String LOG_TAG = "EPUBREADER";

    /**
     * Info: prior to Android 2.3, any compressed asset file with an
     * uncompressed size of over 1 MB cannot be read from the APK. So this
     * should only be used if the device has android 2.3 or later running!
     *
     * @param assetManager
     * @param targetFolder
     * @throws Exception
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean copyAssets(AssetManager assetManager, File targetFolder) throws Exception {
        Log.i(LOG_TAG, "Copying files from assets to folder " + targetFolder);
        return copyAssets(assetManager, "", targetFolder);
    }

    /**
     * The files will be copied at the location targetFolder+path so if you
     * enter path="abc" and targetfolder="sdcard" the files will be located in
     * "sdcard/abc"
     *
     * @param assetManager
     * @param path
     * @param targetFolder
     * @return
     * @throws Exception
     */
    public static boolean copyAssets(AssetManager assetManager, String path, File targetFolder) throws Exception {
        Log.i(LOG_TAG, "Copying " + path + " to " + targetFolder);
        String sources[] = assetManager.list(path);

        if (sources.length == 0) { // its not a folder, so its a file:
            String[] allowed = {".html", ".css", ".js", ".epub", ".ico"};
            boolean has = false;

            for (String item : allowed) {
                if (path.contains(item)) {
                    has = true;
                }
            }

            if (has) {
                copyAssetFileToFolder(assetManager, path, targetFolder);
            } else {
                Log.i(LOG_TAG, "  > Skipping " + path);
                return false;
            }
        } else {
            String[] allowed = {"www", "js", "css", "libs", "data"};
            boolean has = false;

            for (String item : allowed) {
                if (path.equals("") || path.startsWith(item)) {
                    has = true;
                }
            }

            if (has) {
                File targetDir = new File(targetFolder, path);
                targetDir.mkdirs();

                for (String source : sources) {
                    String fullSourcePath = path.equals("") ? source : (path + File.separator + source);
                    copyAssets(assetManager, fullSourcePath, targetFolder);
                }
            } else {
                Log.i(LOG_TAG, "  > Skipping " + path);
                return false;
            }
        }

        return true;
    }

    private static void copyAssetFileToFolder(AssetManager assetManager, String fullAssetPath, File targetBasePath) throws IOException {
        InputStream in = assetManager.open(fullAssetPath);
        OutputStream out = new FileOutputStream(new File(targetBasePath, fullAssetPath));

        byte[] buffer = new byte[16 * 1024];
        int read;

        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }

        in.close();
        out.flush();
        out.close();
    }

}
