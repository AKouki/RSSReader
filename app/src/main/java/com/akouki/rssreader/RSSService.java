package com.akouki.rssreader;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.akouki.rssreader.data.DatabaseHelper;
import com.akouki.rssreader.models.RSSArticle;
import com.akouki.rssreader.models.RSSFeed;
import com.akouki.rssreader.utils.PrefsManager;
import com.akouki.rssreader.utils.Utils;
import com.akouki.rssreader.utils.ViewUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RSSService extends IntentService {

    public RSSService(String name) {
        super(name);
    }

    public RSSService() {
        super("RSSService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        long feedId = intent.getLongExtra("feed_id", -1);
        boolean forcedUpdate = intent.getBooleanExtra("forced_update", false);

        if (forcedUpdate || canUpdate(feedId)) {

            if (feedId == -1) {
                ArrayList<RSSFeed> feeds = new DatabaseHelper(getApplicationContext()).getFeeds(false);
                for (RSSFeed feed : feeds) {
                    updateFeed(feed);
                }
                PrefsManager.saveLastRun(getApplicationContext(), System.currentTimeMillis());
            } else {
                RSSFeed feed = new DatabaseHelper(getApplicationContext()).getFeedById(feedId);
                updateFeed(feed);
            }

            // Show update notification?
            if (PrefsManager.showNotifications(getApplicationContext())) {
                ViewUtils.showNotification(getApplicationContext(),
                        R.drawable.ic_rss_feed_white_24dp,
                        "RSS Reader",
                        "Update Finished!");
            }

            // Update UI
            sendBroadcast(new Intent().setAction(MainActivity.ACTION_REFRESH));
        }
    }

    private void updateFeed(RSSFeed feed) {
        try {
            URL url = new URL(feed.getUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setDoInput(true);

            FeedParser feedParser = new FeedParser(getApplicationContext());
            ArrayList<RSSArticle> rssItems = feedParser.parse(conn.getInputStream());
            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            for (RSSArticle article : rssItems) {
                article.setChannel(feed.getTitle());
                helper.insert(article, feed.getId());
            }

            feed.addArticles(rssItems);

        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private boolean canUpdate(long feedId) {
        // Automatic Synchronization
        // 0 Disabled, 1 Enabled, 2 WiFi Only
        int syncAutomatic = PrefsManager.getSyncAutomatic(getApplicationContext());
        // Internet connection status
        // 0 No connection, 1 Mobile, 2 WiFi
        int statusCode = Utils.getConnectivityStatus(getApplicationContext());

        // No Internet connection
        if (statusCode == 0)
            return false;

        // There are no feeds in the database
        if (new DatabaseHelper(getApplicationContext()).getFeedsCount() == 0)
            return false;

        // Are we allowed to update under this connection?
        // syncAutomation == 1: Update from both 3G and WiFi
        return syncAutomatic == 1 || statusCode == syncAutomatic;
    }
}
