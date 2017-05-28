package com.akouki.rssreader;

import android.content.Context;
import android.util.Xml;

import com.akouki.rssreader.models.RSSArticle;
import com.akouki.rssreader.utils.PrefsManager;
import com.akouki.rssreader.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

// https://developer.android.com/samples/BasicSyncAdapter/src/com.example.android.basicsyncadapter/net/FeedParser.html
public class FeedParser {

    // We don't use XML namespaces
    private static final String ns = null;

    // We need this for PrefsManager
    Context context;

    public FeedParser(Context context) {
        this.context = context;
    }

    public ArrayList<RSSArticle> parse(InputStream inputStream) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();
            ArrayList<RSSArticle> articles = readFeed(parser);
            return articles;
        } finally {
            inputStream.close();
        }
    }

    public String parseFeedTitle(InputStream inputStream) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag(); //<rss>
            parser.nextTag(); //<channel>
            parser.require(XmlPullParser.START_TAG, ns, "channel");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equals("title")) {
                    return readTag(parser, "title");
                } else {
                    skip(parser);
                }
            }

        } finally {
            inputStream.close();
        }

        return null;
    }

    private ArrayList<RSSArticle> readFeed(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "rss");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("channel")) {
                return readChannel(parser);
            } else {
                skip(parser);
            }
        }
        return null;
    }

    private ArrayList<RSSArticle> readChannel(XmlPullParser parser) throws IOException, XmlPullParserException {
        ArrayList<RSSArticle> items = new ArrayList<RSSArticle>();

        parser.require(XmlPullParser.START_TAG, ns, "channel");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("item")) {
                items.add(readItem(parser));
            } else {
                skip(parser);
            }
        }
        return items;
    }

    private RSSArticle readItem(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "item");

        // Article fields
        String title = null;
        String description = null;
        String link = null;
        long pubDate = 0;
        String thumbnail = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("title")) {
                title = readTag(parser, "title");
            } else if (name.equals("description") || name.equals("atom:summary")) {
                description = readTag(parser, name);
                // Download thumbnail if enabled
                if (PrefsManager.downloadThumbnails(context)) {
                    thumbnail = getFirstImageFromContent(description);
                }
                // Clean non-safe html tags
                description = Jsoup.clean(description, Whitelist.basic());
            } else if (name.equals("link")) {
                link = readTag(parser, "link");
            } else if (name.equals("pubDate")) {
                pubDate = Utils.pubDateToMillis(readTag(parser, "pubDate"));
            } else {
                skip(parser);
            }
        }

        return new RSSArticle(title, description, link, pubDate, thumbnail);
    }

    private String getFirstImageFromContent(String description) {
        try {
            Document doc = Jsoup.parse(description);
            Element image = doc.getElementsByTag("img").first();
            String url = image.absUrl("src");

            if (url.isEmpty() || url.contains("feeds.feedburner.com"))
                return null;

            return url;
        } catch (Exception e) {
            return null;
        }
    }

    private String readTag(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String result = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return result;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = null;
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}