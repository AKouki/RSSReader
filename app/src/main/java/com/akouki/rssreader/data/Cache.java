package com.akouki.rssreader.data;

import com.akouki.rssreader.models.RSSArticle;
import com.akouki.rssreader.models.RSSFeed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// This is not exactly memory caching system - It is mostly a data holder
public class Cache {
    private ArrayList<RSSFeed> feeds = new ArrayList<>();

    public void Add(RSSFeed feed) {
        feeds.add(feed);
    }

    public void AddRange(ArrayList<RSSFeed> feeds) {
        for (RSSFeed feed : feeds) {
            Add(feed);
        }
    }

    public void removeFeed(RSSFeed feed) {
        feeds.remove(feed);
    }

    public void removeArticles(int feedId) {
        for (RSSFeed feed : feeds) {
            if (feed.getId() == feedId) {
                feed.clearArticles();
            }
        }
    }

    public ArrayList<RSSFeed> getFeeds() {
        return feeds;
    }

    public RSSFeed getFeedByIndex(int index) {
        return feeds.get(index);
    }

    public RSSFeed getFeedById(long feedId) {
        for (RSSFeed feed : feeds) {
            if (feed.getId() == feedId)
                return feed;
        }
        return null;
    }

    private ArrayList<RSSArticle> getArticles(boolean starredOnly) {
        ArrayList<RSSArticle> arrayList = new ArrayList<>();
        for (RSSFeed feed : feeds) {
            for (RSSArticle article : feed.getArticles()) {
                if (starredOnly) {
                    if (article.getIsStarred() == 1)
                        arrayList.add(article);
                } else {
                    arrayList.add(article);
                }
            }
        }

        return arrayList;
    }

    public RSSFeed getAllFeeds() {
        // Get all articles
        ArrayList<RSSArticle> articles = getArticles(false);

        // Sort by pubDate
        Collections.sort(articles, new Comparator<RSSArticle>() {
            @Override
            public int compare(RSSArticle o1, RSSArticle o2) {
                return Long.valueOf(o2.getPubDate()).compareTo(o1.getPubDate());
            }
        });

        RSSFeed feed = new RSSFeed(-1, "All", "", "");
        feed.addArticles(articles);

        return feed;
    }

    public RSSFeed getStarredFeeds() {
        // Get Starred only
        ArrayList<RSSArticle> articles = getArticles(true);

        // Sort by pubDate
        Collections.sort(articles, new Comparator<RSSArticle>() {
            @Override
            public int compare(RSSArticle o1, RSSArticle o2) {
                return Long.valueOf(o2.getPubDate()).compareTo(o1.getPubDate());
            }
        });

        RSSFeed feed = new RSSFeed(-2, "Starred", "", "");
        feed.addArticles(articles);

        return feed;
    }

    public void updateFavicon(long feedId, String favicon) {
        for (RSSFeed feed : feeds) {
            if (feed.getId() == feedId) {
                feed.setFavicon(favicon);
                break;
            }
        }
    }

    public void clearAll() {
        feeds.clear();
    }

    public void clearArticles() {
        for (RSSFeed feed : feeds) {
            feed.getArticles().clear();
        }
    }

    public int getTotalArticles(RSSFeed feed) {
        return feed.getArticles().size();
    }

    public int getTotalStarredArticles() {
        int cnt = 0;
        for (RSSFeed feed : feeds) {
            for (RSSArticle article : feed.getArticles()) {
                if (article.getIsStarred() == 1)
                    cnt++;
            }
        }
        return cnt;
    }

    public boolean isEmpty() {
        return feeds.size() == 0;
    }

    // Singleton Pattern
    private static Cache instance;

    private Cache() {
    }

    public static Cache getInstance() {
        if (instance == null) {
            instance = new Cache();
        }
        return instance;
    }
}
