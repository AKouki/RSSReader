package com.akouki.rssreader;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.akouki.rssreader.data.Cache;
import com.akouki.rssreader.data.DatabaseHelper;
import com.akouki.rssreader.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FaviconService extends IntentService {

    public FaviconService() {
        super("FaviconService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Get extras
        String url = intent.getStringExtra("feed_url");
        long id = intent.getLongExtra("feed_id", -1);

        // Download Favicon
        String favicon = downloadFavicon(url).replace("\n", "");
        if (favicon != null) {
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            if (dbHelper.updateFavicon(id, favicon)) {
                // Update Navigation Drawer
                Cache.getInstance().updateFavicon(id, favicon);
                sendBroadcast(new Intent().setAction(MainActivity.ACTION_DRAWER_UPDATE));
            }
        }
    }

    private String downloadFavicon(String urlFeed) {
        try {
            String newUrl = "https://www.google.com/s2/favicons?domain=" + new URL(urlFeed).getHost();
            URL url = new URL(newUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept-Encoding", "gzip");
            conn.setDoInput(true);
            conn.connect();

            InputStream is = conn.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return Utils.BitmapToBase64(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
