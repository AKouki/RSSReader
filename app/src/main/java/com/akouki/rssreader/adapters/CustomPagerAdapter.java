package com.akouki.rssreader.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.akouki.rssreader.R;
import com.akouki.rssreader.models.RSSArticle;
import com.akouki.rssreader.utils.Utils;

import java.util.ArrayList;

public class CustomPagerAdapter extends PagerAdapter {
    private ArrayList<RSSArticle> articles;
    private Context mContext;

    public CustomPagerAdapter(Context context, ArrayList<RSSArticle> articles) {
        mContext = context;
        this.articles = articles;
    }


    @Override
    public int getCount() {
        return articles.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        RSSArticle article = articles.get(position);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.feed_article_view_item, container, false);

        TextView textView = (TextView) layout.findViewById(R.id.txtTitle);
        textView.setText(article.getTitle());

        TextView textView1 = (TextView) layout.findViewById(R.id.txtDate);
        textView1.setText(Utils.millisToDateTime(article.getPubDate()) + " - " + article.getChannel());

        TextView textView2 = (TextView) layout.findViewById(R.id.txtContent);
        textView2.setText(fromHtml(article.getDescription()));

        container.addView(layout);
        return layout;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return articles.get(position).getTitle();
    }
}
