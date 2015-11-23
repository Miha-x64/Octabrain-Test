package net.aquadc.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;

/**
 * Created by miha on 11.11.15.
 *
 */
public class ImageCacher {
    private final Context context;
    private final ExecutorService executor;

    public ImageCacher(Context context, ExecutorService executor) {
        this.context = context;
        this.executor = executor;
    }

    /** check */
    public boolean inCache(Uri uri) {
        File dir = context.getCacheDir();
        String fName = hash(uri.toString()) + ".jpg";
        File img = new File(dir, fName);
        return img.exists();
    }

    /** get local */
    public String getPath(Uri uri) {
        File dir = context.getCacheDir();
        String fName = hash(uri.toString()) + ".jpg";
        File img = new File(dir, fName);
        return Uri.fromFile(img).getPath();
    }

    /** start async download with UI thread callback */
    public void download(Uri uri, OnDownloadListener listener) {
        new DownloadImageTask()
                .executeOnExecutor(executor, uri.toString(), listener);
    }

    private class DownloadImageTask extends AsyncTask<Object, Void, Bitmap> {

        private OnDownloadListener listener;

        protected Bitmap doInBackground(Object... in) {
            try {
                listener = (OnDownloadListener) in[1];
                File img = syncDownload((String) in[0]);
                return BitmapFactory.decodeFile(Uri.fromFile(img).getPath());
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Bitmap result) {
            if (listener != null)
                listener.onDownload(result);
        }
    }

    private static final String SHA512 = "SHA512";
    private static String hash(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance(SHA512);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @WorkerThread
    private File syncDownload(String url) throws IOException {
        File dir = context.getCacheDir();
        String fName = hash(url) + ".jpg";
        File img = new File(dir, fName);
        BufferedInputStream in      = new BufferedInputStream(new URL(url).openStream());
        BufferedOutputStream bout   = new BufferedOutputStream(new FileOutputStream(img), 1024);
        byte[] data = new byte[1024];
        int x;
        while((x=in.read(data, 0, 1024)) >= 0)
            bout.write(data,0,x);

        in.close();
        bout.close();
        return img;
    }

    /** get file synchroneously, regardless it in cache or not */
    @WorkerThread
    public File getFile(Uri uri) throws IOException {
        if (inCache(uri))
            return new File(getPath(uri));

        return syncDownload(uri.toString());
    }

    public interface OnDownloadListener {
        void onDownload(Bitmap bitmap);
    }
}
