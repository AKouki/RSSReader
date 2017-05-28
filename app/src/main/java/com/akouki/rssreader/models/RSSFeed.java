package com.akouki.rssreader.models;

import java.util.ArrayList;

public class RSSFeed {
    private int id;
    private String title;
    private String url;
    private String favicon;
    private ArrayList<RSSArticle> articles;

    public RSSFeed(int id, String title, String url, String favicon) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.favicon = favicon;
        this.articles = new ArrayList<>();
    }

    public RSSFeed(String title, String url, String favicon) {
        this.title = title;
        this.url = url;
        this.favicon = favicon;
        this.articles = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    public ArrayList<RSSArticle> getArticles() {
        return articles;
    }

    public void setArticles(ArrayList<RSSArticle> articles) {
        this.articles = articles;
    }

    public void addArticles(ArrayList<RSSArticle> articles) {
        this.articles.addAll(articles);
    }

    public void addArticle(RSSArticle article) {
        this.articles.add(article);
    }

    public void clearArticles() {
        this.articles.clear();
    }
}