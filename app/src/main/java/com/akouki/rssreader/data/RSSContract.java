package com.akouki.rssreader.data;

import android.provider.BaseColumns;

public final class RSSContract {
    public static final class RSSFeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "rssfeeds";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_FAVICON = "favicon";
    }

    public static final class RSSArticleEntry implements BaseColumns {
        public static final String TABLE_NAME = "rssarticles";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESC = "description";
        public static final String COLUMN_LINK = "link";
        public static final String COLUMN_PUBDATE = "pubDate";
        public static final String COLUMN_THUMBNAIL = "thumbnail";
        public static final String COLUMN_ISSTARRED = "starred";
        public static final String COLUMN_FEEDID = "feedid";
    }
}