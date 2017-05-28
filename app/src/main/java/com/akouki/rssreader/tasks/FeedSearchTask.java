package com.akouki.rssreader.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.akouki.rssreader.R;
import com.akouki.rssreader.adapters.SearchResultAdapter;
import com.akouki.rssreader.models.RSSFeed;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class FeedSearchTask extends AsyncTask<Void, Void, ArrayList<RSSFeed>> {
    Context context;
    String feedUrl;
    ProgressDialog progressDialog;

    public FeedSearchTask(Context context, String feedUrl) {
        this.context = context;
        this.feedUrl = fixUrl(feedUrl);
    }

    private String fixUrl(String feedUrl) {
        if (!feedUrl.startsWith("http"))
            return "http://" + feedUrl;

        return feedUrl;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(context, "", "Searching...", true, true);
    }

    @Override
    protected ArrayList<RSSFeed> doInBackground(Void... params) {
        ArrayList<RSSFeed> arrayList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(feedUrl).timeout(10000).get();
            Elements links = doc.select("link[type=application/rss+xml]");

            for (Element link : links) {
                String title = link.attr("title");
                String url = link.attr("abs:href");
                arrayList.add(new RSSFeed(title, url, ""));
            }
        } catch (UnknownHostException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return arrayList;
    }

    @Override
    protected void onPostExecute(final ArrayList<RSSFeed> searchResult) {
        super.onPostExecute(searchResult);
        progressDialog.cancel();

        if (searchResult == null) {
            Toast.makeText(context, "This is invalid URL format!", Toast.LENGTH_SHORT).show();
        } else if (searchResult.size() == 0)
            Toast.makeText(context, "No RSS Feeds found!", Toast.LENGTH_SHORT).show();
        else {
            ListView lv = (ListView) ((Activity) context).findViewById(R.id.lvResults);
            SearchResultAdapter adapter = new SearchResultAdapter(context, searchResult);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    FeedAddTask feedAddTask = new FeedAddTask(context, searchResult.get(position).getUrl());
                    feedAddTask.execute();
                }
            });
        }
    }

}
