package com.akouki.rssreader.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.akouki.rssreader.models.RSSArticle;
import com.akouki.rssreader.models.RSSFeed;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "rssreader.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_RSS_FEEDS_CREATE =
            "CREATE TABLE " + RSSContract.RSSFeedEntry.TABLE_NAME +
                    " (" +
                    RSSContract.RSSFeedEntry._ID + " INTEGER PRIMARY KEY, " +
                    RSSContract.RSSFeedEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                    RSSContract.RSSFeedEntry.COLUMN_URL + " TEXT NOT NULL UNIQUE, " +
                    RSSContract.RSSFeedEntry.COLUMN_FAVICON + " TEXT" +
                    ")";

    private static final String TABLE_RSS_ARTICLES_CREATE =
            "CREATE TABLE " + RSSContract.RSSArticleEntry.TABLE_NAME +
                    "(" +
                    RSSContract.RSSArticleEntry._ID + " INTEGER PRIMARY KEY, " +
                    RSSContract.RSSArticleEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                    RSSContract.RSSArticleEntry.COLUMN_DESC + " TEXT NOT NULL, " +
                    RSSContract.RSSArticleEntry.COLUMN_LINK + " TEXT NOT NULL UNIQUE, " +
                    RSSContract.RSSArticleEntry.COLUMN_PUBDATE + " TEXT, " +
                    RSSContract.RSSArticleEntry.COLUMN_THUMBNAIL + " TEXT, " +
                    RSSContract.RSSArticleEntry.COLUMN_ISSTARRED + " INTEGER, " +
                    RSSContract.RSSArticleEntry.COLUMN_FEEDID + " INTEGER, " +
                    "FOREIGN KEY (" + RSSContract.RSSArticleEntry.COLUMN_FEEDID + ")" +
                    "REFERENCES " + RSSContract.RSSFeedEntry.TABLE_NAME + "(" + RSSContract.RSSFeedEntry._ID + ")" +
                    "ON DELETE CASCADE" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_RSS_FEEDS_CREATE);
        db.execSQL(TABLE_RSS_ARTICLES_CREATE);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RSSContract.RSSFeedEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RSSContract.RSSArticleEntry.TABLE_NAME);
        onCreate(db);
    }

    // ========================= SELECT =========================
    // Get specific feed
    public RSSFeed getFeedById(long feedId) {
        RSSFeed feed = null;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(RSSContract.RSSFeedEntry.TABLE_NAME,
                null,
                RSSContract.RSSFeedEntry._ID + " = ?",
                new String[]{String.valueOf(feedId)},
                null,
                null,
                null);

        if (c.moveToFirst()) {
            feed = new RSSFeed(c.getInt(0), c.getString(1), c.getString(2), c.getString(3));
        }
        c.close();
        db.close();

        return feed;
    }

    // Get all feeds - No need to load articles when updating...
    public ArrayList<RSSFeed> getFeeds(boolean loadArticles) {
        ArrayList<RSSFeed> feeds = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(RSSContract.RSSFeedEntry.TABLE_NAME, null, null, null, null, null, null);

        while (c.moveToNext()) {
            RSSFeed feed = new RSSFeed(c.getInt(0), c.getString(1), c.getString(2), c.getString(3));
            if (loadArticles)
                feed.setArticles(getArticles(feed));

            feeds.add(feed);
        }
        c.close();
        db.close();

        return feeds;
    }

    // Get articles for specific feed
    public ArrayList<RSSArticle> getArticles(RSSFeed feed) {
        ArrayList<RSSArticle> articles = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(
                RSSContract.RSSArticleEntry.TABLE_NAME,
                null,
                RSSContract.RSSArticleEntry.COLUMN_FEEDID + " = ?",
                new String[]{String.valueOf(feed.getId())},
                null,
                null,
                null);

        while (c.moveToNext()) {
            RSSArticle article = new RSSArticle();
            article.setId(c.getInt(0));
            article.setTitle(c.getString(1));
            article.setDescription(c.getString(2));
            article.setLink(c.getString(3));
            article.setPubDate(Long.valueOf(c.getString(4)));
            article.setThumbnail(c.getString(5));
            article.setIsStarred(c.getInt(6));
            article.setChannel(feed.getTitle());

            articles.add(article);
        }
        c.close();
        db.close();

        return articles;
    }

    // How many feeds do we have?
    public long getFeedsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db, RSSContract.RSSFeedEntry.TABLE_NAME);
    }

    // ========================= INSERT =========================
    // Insert new feed
    public long insert(RSSFeed feed) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RSSContract.RSSFeedEntry.COLUMN_TITLE, feed.getTitle());
        values.put(RSSContract.RSSFeedEntry.COLUMN_URL, feed.getUrl());
        if (!feed.getFavicon().isEmpty())
            values.put(RSSContract.RSSFeedEntry.COLUMN_FAVICON, feed.getFavicon());

        long result = db.insert(RSSContract.RSSFeedEntry.TABLE_NAME, null, values);
        return result;
    }

    // Insert new articles for specific feed
    public boolean insert(RSSArticle article, int feedId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RSSContract.RSSArticleEntry.COLUMN_TITLE, article.getTitle());
        values.put(RSSContract.RSSArticleEntry.COLUMN_DESC, article.getDescription());
        values.put(RSSContract.RSSArticleEntry.COLUMN_LINK, article.getLink());
        values.put(RSSContract.RSSArticleEntry.COLUMN_PUBDATE, article.getPubDate());
        values.put(RSSContract.RSSArticleEntry.COLUMN_THUMBNAIL, article.getThumbnail());
        values.put(RSSContract.RSSArticleEntry.COLUMN_ISSTARRED, article.getIsStarred());
        values.put(RSSContract.RSSArticleEntry.COLUMN_FEEDID, feedId);

        long result = db.insert(RSSContract.RSSArticleEntry.TABLE_NAME, null, values);
        db.close();

        return result < 0 ? false : true;
    }

    // ========================= UPDATE =========================
    // Update Favicon for specific feed
    public boolean updateFavicon(long feedId, String favicon) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RSSContract.RSSFeedEntry.COLUMN_FAVICON, favicon);

        long result = db.update(RSSContract.RSSFeedEntry.TABLE_NAME,
                values,
                "_id = ?",
                new String[]{String.valueOf(feedId)});
        db.close();

        return result >= 0 ? true : false;
    }

    // Set/Unset 'Starred' for article
    public boolean updateStarred(int articleId, int value) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RSSContract.RSSArticleEntry.COLUMN_ISSTARRED, value);

        long result = db.update(RSSContract.RSSArticleEntry.TABLE_NAME,
                values,
                "_id = ?",
                new String[]{String.valueOf(articleId)});
        db.close();

        return result >= 0 ? true : false;
    }

    // ========================= DELETE =========================
    // Delete feed by Id
    public boolean deleteFeed(int feedId) {
        SQLiteDatabase db = this.getWritableDatabase();

        long result = db.delete(
                RSSContract.RSSFeedEntry.TABLE_NAME,
                RSSContract.RSSFeedEntry._ID + "=?",
                new String[]{String.valueOf(feedId)});
        db.close();

        return result >= 0 ? true : false;
    }

    // Delete articles for specific feed
    public boolean deleteArticles(int feedId) {
        SQLiteDatabase db = this.getWritableDatabase();

        long result = db.delete(
                RSSContract.RSSArticleEntry.TABLE_NAME,
                RSSContract.RSSArticleEntry.COLUMN_FEEDID + "=?",
                new String[]{String.valueOf(feedId)});
        db.close();

        return result >= 0 ? true : false;
    }

    // Delete all articles from all feeds
    public void deleteArticles() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(RSSContract.RSSArticleEntry.TABLE_NAME, null, null);
        db.close();
    }

}