package com.akouki.rssreader.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.akouki.rssreader.FeedParser;
import com.akouki.rssreader.data.Cache;
import com.akouki.rssreader.data.DatabaseHelper;
import com.akouki.rssreader.models.RSSFeed;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class FeedAddTask extends AsyncTask<Void, Void, String> {
    Context context;
    String feedUrl;
    ProgressDialog progressDialog;

    public FeedAddTask(Context context, String feedUrl) {
        this.context = context;
        this.feedUrl = feedUrl;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(context, "", "Please wait...", true, false);
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            URL url = new URL(feedUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setDoInput(true);

            return new FeedParser(context).parseFeedTitle(conn.getInputStream());
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String title) {
        super.onPostExecute(title);
        progressDialog.cancel();

        if (title != null && !title.isEmpty()) {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            long id = dbHelper.insert(new RSSFeed(title, feedUrl, ""));

            if (id >= 0) {
                // Update Cache
                Cache.getInstance().Add(new RSSFeed((int) id, title, feedUrl, ""));

                // Return to Main Activity
                Intent intent = new Intent();
                intent.putExtra("feed_id", id);
                intent.putExtra("feed_url", feedUrl);
                ((Activity) context).setResult(Activity.RESULT_OK, intent);
                ((Activity) context).finish();
            } else {
                Toast.makeText(context, "This feed already exists!", Toast.LENGTH_SHORT).show();
            }

        } else
            Toast.makeText(context, "This is not valid rss/xml file!", Toast.LENGTH_SHORT).show();
    }
}
