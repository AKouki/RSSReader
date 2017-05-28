package com.akouki.rssreader.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.akouki.rssreader.R;
import com.akouki.rssreader.models.RSSFeed;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends BaseAdapter {
    Context context;
    List<RSSFeed> resultItems = new ArrayList<>();

    public SearchResultAdapter(Context context, List<RSSFeed> items) {
        this.context = context;
        this.resultItems = items;
    }

    @Override
    public int getCount() {
        return resultItems.size();
    }

    @Override
    public Object getItem(int position) {
        return resultItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;
        final RSSFeed item = resultItems.get(position);
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.feed_search_item, parent, false);
            holder = new ViewHolder(rowView);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        holder.title.setText(item.getTitle());
        holder.url.setText(item.getUrl());

        return rowView;
    }

    private class ViewHolder {
        TextView title, url;

        ViewHolder(View v) {
            title = (TextView) v.findViewById(R.id.txtSearchItemTitle);
            url = (TextView) v.findViewById(R.id.txtSearchItemUrl);
        }
    }
}
