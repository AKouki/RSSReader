package com.akouki.rssreader.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;

import com.akouki.rssreader.R;
import com.akouki.rssreader.adapters.CustomPagerAdapter;
import com.akouki.rssreader.data.Cache;
import com.akouki.rssreader.data.DatabaseHelper;
import com.akouki.rssreader.models.RSSArticle;
import com.akouki.rssreader.models.RSSFeed;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.lang.reflect.Field;

public class FeedViewActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    RSSFeed feed;
    int currentPosition;
    MenuItem itemStarred;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_view);

        showActionOverflowMenu();

        int feedId = getIntent().getIntExtra("feedId", -1);
        switch (feedId) {
            case -2:
                feed = Cache.getInstance().getStarredFeeds();
                break;
            case -1:
                feed = Cache.getInstance().getAllFeeds();
                break;
            default:
                feed = Cache.getInstance().getFeedById(feedId);
                break;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(feed.getTitle());

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager1);
        viewPager.setAdapter(new CustomPagerAdapter(FeedViewActivity.this, feed.getArticles()));
        viewPager.addOnPageChangeListener(this);

        int position = getIntent().getIntExtra("position", 0);
        viewPager.setCurrentItem(position, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_feed, menu);

        // Get 'action_starred' menu item
        itemStarred = menu.findItem(R.id.action_starred);
        setStarredIcon(feed.getArticles().get(currentPosition).getIsStarred());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        RSSArticle currentArticle = feed.getArticles().get(currentPosition);
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_starred:
                int isStarred = currentArticle.getIsStarred() == 1 ? 0 : 1;
                DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                if (db.updateStarred(currentArticle.getId(), isStarred)) {
                    currentArticle.setIsStarred(isStarred);
                    setStarredIcon(currentArticle.getIsStarred());
                }
                return true;
            case R.id.action_share:
                shareArticle(currentArticle);
                return true;
            case R.id.action_open_browser:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(currentArticle.getLink())));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        getSupportActionBar().setSubtitle((position + 1) + "/" + feed.getArticles().size());
    }

    @Override
    public void onPageSelected(int position) {
        currentPosition = position;
        setStarredIcon(feed.getArticles().get(position).getIsStarred());
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private void showActionOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setStarredIcon(int isStarred) {
        if (itemStarred != null) {
            if (isStarred == 1)
                itemStarred.setIcon(R.drawable.ic_star_white_24dp);
            else
                itemStarred.setIcon(R.drawable.ic_star_border_white_24dp);
        }
    }

    private void shareArticle(RSSArticle article) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, article.getTitle());
        String text = Jsoup.clean(article.getDescription(), Whitelist.none());
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}
