package com.akouki.rssreader;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.akouki.rssreader.activities.FeedAddActivity;
import com.akouki.rssreader.activities.FeedViewActivity;
import com.akouki.rssreader.activities.SettingsActivity;
import com.akouki.rssreader.adapters.CustomDrawerAdapter;
import com.akouki.rssreader.adapters.RSSFeedAdapter;
import com.akouki.rssreader.data.Cache;
import com.akouki.rssreader.data.DatabaseHelper;
import com.akouki.rssreader.models.DrawerItem;
import com.akouki.rssreader.models.RSSArticle;
import com.akouki.rssreader.models.RSSFeed;
import com.akouki.rssreader.utils.PrefsManager;
import com.akouki.rssreader.utils.Utils;
import com.akouki.rssreader.utils.ViewUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String ACTION_DRAWER_UPDATE = "com.akouki.rssreader.DRAWER_UPDATE";
    public static final String ACTION_REFRESH = "com.akouki.rssreader.REFRESH";
    public static final String ACTION_ALARM_STOP = "com.akouki.rssreader.ALARM_STOP";
    public static final String ACTION_ALARM_UPDATE = "com.akouki.rssreader.ALARM_UPDATE";

    DatabaseHelper db;
    MyReceiver myReceiver;
    IntentFilter intentFilter;
    DividerItemDecoration divider;

    Boolean isListView = true;  // View Type: ListView or CardView
    Integer currFeedIndex = -1; // -2 Starred, -1: All, >= 0: Feed Id

    @Override
    protected void onStart() {
        super.onStart();
        myReceiver = new MyReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.ACTION_DRAWER_UPDATE);
        intentFilter.addAction(MainActivity.ACTION_REFRESH);
        intentFilter.addAction(MainActivity.ACTION_ALARM_STOP);
        intentFilter.addAction(MainActivity.ACTION_ALARM_UPDATE);
        registerReceiver(myReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        divider = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);

        setupUIComponents();
        loadSettings();
        loadData();
        updateNavigationDrawer();
        updateRecyclerView(-1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Update Navigation Drawer anyway
        updateNavigationDrawer();

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                // Get extras
                long feedId = data.getLongExtra("feed_id", -1);
                String feedUrl = data.getStringExtra("feed_url");

                // Download Articles
                startServiceNow(feedId);

                // Download Favicon
                Intent intent = new Intent(MainActivity.this, FaviconService.class);
                intent.putExtra("feed_id", feedId);
                intent.putExtra("feed_url", feedUrl);
                startService(intent);

                Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 2) {
            // Refresh RecyclerView only when we back from Starred
            if (currFeedIndex == -2)
                updateRecyclerView(currFeedIndex);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.action_view_change:
                isListView = !isListView;
                updateRecyclerView(currFeedIndex);
                break;
            case R.id.action_clear:
                db.deleteArticles();
                Cache.getInstance().clearArticles();
                updateRecyclerView(-1);
                updateNavigationDrawer();
                break;
            case R.id.action_sync:
                if (Utils.isNetworkAvailable(getApplicationContext())) {
                    startServiceNow(-1);
                    Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "There is no Internet connection!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.action_feed_add:
                startActivityForResult(new Intent(getApplicationContext(), FeedAddActivity.class), 1);
                break;
            case R.id.action_all:
                updateRecyclerView(-1);
                break;
            case R.id.action_starred:
                updateRecyclerView(-2);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupUIComponents() {
        // Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        // Setup DrawerLayout
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        // for Top menu
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation1);
        navigationView.setNavigationItemSelectedListener(this);
        // for Bottom menu
        NavigationView navigationView2 = (NavigationView) findViewById(R.id.navigation2);
        navigationView2.setNavigationItemSelectedListener(this);
    }

    private void loadSettings() {
        //===============================[Automatic Sync]===============================
        // 0 Disabled, 1 Enabled, 2 WiFi Only
        int syncAutomatic = PrefsManager.getSyncAutomatic(this);
        long syncInterval = PrefsManager.getSyncInterval(this);
        boolean syncStartup = PrefsManager.syncOnStartup(this);

        if (syncAutomatic != 0) {
            long lastRun = PrefsManager.getLastUpdate(this);
            long elapsedTime = System.currentTimeMillis() - lastRun;

            if (syncStartup || elapsedTime > syncInterval)
                startAlarm(syncInterval, 5000);
            else {
                long remainingTime = syncInterval - elapsedTime;
                startAlarm(syncInterval, remainingTime);
            }
        }

        //===============================[Clean Database]===============================
        long cleanInterval = PrefsManager.getCleanInterval(this);
        long lastClean = PrefsManager.getLastClean(this);
        long elapsedTime = System.currentTimeMillis() - lastClean;

        if (lastClean == 0) // First run
            PrefsManager.saveLastClean(this, System.currentTimeMillis());
        else if (elapsedTime > cleanInterval) {
            db.deleteArticles();
            PrefsManager.saveLastClean(this, System.currentTimeMillis());
        }
    }

    private void loadData() {

        if (Cache.getInstance().getFeeds().size() == 0) {
            Cache.getInstance().AddRange(db.getFeeds(true));
        }
    }

    private void updateNavigationDrawer() {
        final ArrayList<DrawerItem> drawerItems = new ArrayList<>();

        // Add feeds to Navigation Drawer List
        for (RSSFeed feed : Cache.getInstance().getFeeds()) {
            drawerItems.add(new DrawerItem(
                    feed.getFavicon(),
                    feed.getTitle(),
                    Cache.getInstance().getTotalArticles(feed)));
        }

        // Set Counter = How many feeds are starred
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation1);
        if (navigationView != null) {
            Menu menu = navigationView.getMenu();
            TextView tv = (TextView) menu.findItem(R.id.action_starred).getActionView().findViewById(R.id.txtStarredCounter);
            tv.setText("" + String.valueOf(Cache.getInstance().getTotalStarredArticles()));
        }

        // Setup Drawer Adapter
        CustomDrawerAdapter adapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item, drawerItems);
        ListView drawerList = (ListView) findViewById(R.id.drawer_list);
        drawerList.setAdapter(adapter);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateRecyclerView(position);
            }
        });
        drawerList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final RSSFeed feed = Cache.getInstance().getFeedByIndex(position);
                final CharSequence[] items = {"Delete feed", "Delete Articles"};

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(feed.getTitle());
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            if (db.deleteFeed(feed.getId())) {
                                Cache.getInstance().removeFeed(feed);
                                Toast.makeText(MainActivity.this, "Feed Deleted ", Toast.LENGTH_SHORT).show();
                            }
                        } else if (item == 1) {
                            if (db.deleteArticles(feed.getId())) {
                                Cache.getInstance().removeArticles(feed.getId());
                                Toast.makeText(MainActivity.this, "Articles Deleted", Toast.LENGTH_SHORT).show();
                            }
                        }
                        updateNavigationDrawer();
                        updateRecyclerView(-1);
                    }
                });
                builder.show();
                return true;
            }
        });

    }

    private void updateRecyclerView(final int index) {

        // Used when RecyclerView View Type changes
        currFeedIndex = index;

        // Close Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        // Load Data into RecyclerView
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycleView1);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                RSSFeed feed = null;
                switch (index) {
                    case -2:
                        feed = Cache.getInstance().getStarredFeeds();
                        break;
                    case -1:
                        feed = Cache.getInstance().getAllFeeds();
                        break;
                    default:
                        feed = Cache.getInstance().getFeedByIndex(index);
                        break;
                }

                RSSFeedAdapter adapter = new RSSFeedAdapter(feed, isListView);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setHasFixedSize(true);

                if (isListView) {
                    mRecyclerView.removeItemDecoration(divider);
                    mRecyclerView.addItemDecoration(divider);
                } else
                    mRecyclerView.removeItemDecoration(divider);

                mRecyclerView.setAdapter(adapter);
                adapter.setOnClickListener(new RSSFeedAdapter.IClickListener() {
                    @Override
                    public void OnItemClick(RSSArticle article, int feedId, int position) {
                        Intent intent = new Intent(MainActivity.this, FeedViewActivity.class);
                        intent.putExtra("feedId", feedId);
                        intent.putExtra("position", position);
                        startActivityForResult(intent, 2);
                    }
                });

                // Set Toolbar Title
                if (index == -2)
                    getSupportActionBar().setTitle("Starred");
                else if (index == -1)
                    getSupportActionBar().setTitle("All");
                else
                    getSupportActionBar().setTitle(Cache.getInstance().getFeedByIndex(index).getTitle());

            }
        }, 250);
    }

    private void startServiceNow(long feedId) {
        if (!Cache.getInstance().isEmpty()) {
            Intent intent = new Intent(this, RSSService.class);
            intent.putExtra("feed_id", feedId);
            intent.putExtra("forced_update", true);
            startService(intent);

            // Show notification?
            if (PrefsManager.showNotifications(this)) {
                ViewUtils.showNotification(MainActivity.this,
                        R.drawable.spin_animation,
                        "RSS Reader",
                        "Updating all feeds...");
            }
        }
    }

    private void startAlarm(long interval, long delay) {
        Intent intent = new Intent(this, RSSService.class);
        PendingIntent alarmIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + delay, interval, alarmIntent);
    }

    private void stopAlarm() {
        Intent intent = new Intent(this, RSSService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case ACTION_DRAWER_UPDATE:
                        updateNavigationDrawer();
                        break;
                    case ACTION_REFRESH:
                        Cache.getInstance().clearAll();
                        Cache.getInstance().AddRange(db.getFeeds(true));
                        updateNavigationDrawer();
                        updateRecyclerView(-1);
                        break;
                    case ACTION_ALARM_STOP:
                        stopAlarm();
                        break;
                    case ACTION_ALARM_UPDATE:
                        long syncInterval = PrefsManager.getSyncInterval(getBaseContext());
                        startAlarm(syncInterval, 30000);
                        break;
                }
            }
        }
    }// MyReceiver

}
