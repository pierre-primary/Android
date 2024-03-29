/**
 @author oopp1990
 */
package com.android.bitmap.download;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @title 根据 图片url地址下载图片 可以是本地和网络
 */
public class SimpleDownloader implements Downloader {

    private static final String TAG = SimpleDownloader.class.getSimpleName();
    private static final int IO_BUFFER_SIZE = 8 * 1024; // 8k

    public byte[] download(String urlString) {
        if (urlString == null)
            return null;

        if (urlString.trim().toLowerCase().startsWith("http")) {
            return getFromHttp(urlString);
        } else if (urlString.trim().toLowerCase().startsWith("file:")) {
            try {
                File f = new File(new URI(urlString));
                if (f.exists() && f.canRead()) {
                    return getFromFile(f);
                }
            } catch (URISyntaxException e) {
                Log.e(TAG, "Error in read from file - " + urlString + " : " + e);
            }
        } else {
            File f = new File(urlString);
            if (f.exists() && f.canRead()) {
                return getFromFile(f);
            }
        }

        return null;
    }

    private byte[] getFromFile(File file) {
        if (file == null) return null;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "Error in read from file - " + file + " : " + e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                    fis = null;
                } catch (IOException e) {
                    // do nothing
                }
            }
        }

        return null;
    }

    private byte[] getFromHttp(String urlString) {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        FlushedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new FlushedInputStream(new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b;
            while ((b = in.read()) != -1) {
                baos.write(b);
            }
            return baos.toByteArray();
        } catch (final IOException e) {
            Log.e(TAG, "Error in downloadBitmap - " + urlString + " : " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
            }
        }
        return null;
    }


    public class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int by_te = read();
                    if (by_te < 0) {
                        break; // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
}
