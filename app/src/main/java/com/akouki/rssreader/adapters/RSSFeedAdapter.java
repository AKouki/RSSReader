package com.akouki.rssreader.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akouki.rssreader.R;
import com.akouki.rssreader.models.RSSArticle;
import com.akouki.rssreader.models.RSSFeed;
import com.akouki.rssreader.utils.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class RSSFeedAdapter extends RecyclerView.Adapter<RSSFeedAdapter.MyViewHolder> {
    Context context;
    RSSFeed feed;
    IClickListener listener;
    boolean isListView;

    public RSSFeedAdapter(RSSFeed feed, Boolean isListView) {
        this.feed = feed;
        this.isListView = isListView;
    }

    public void setOnClickListener(IClickListener listener) {
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (isListView)
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.feed_listview_item, parent, false);
        else
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.feed_cardview_item, parent, false);

        context = parent.getContext();
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        RSSArticle article = feed.getArticles().get(position);
        holder.bind(article, listener);

        holder.imageView.setVisibility(View.GONE);
        Picasso.with(context).cancelRequest(holder.imageView);
        Picasso.with(context)
                .load(article.getThumbnail())
                .resize(400, 280)
                .centerCrop()
                .onlyScaleDown()
                .into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.imageView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                        holder.imageView.setVisibility(View.GONE);
                    }
                });

        holder.txt1.setText(Utils.millisToDateTime(article.getPubDate()));
        holder.txt2.setText(article.getChannel());
        holder.txt3.setText(article.getTitle());
        holder.txt4.setText(stripHtml(article.getDescription()));
    }

    public CharSequence stripHtml(String s) {
//        return Html.fromHtml(s).toString()
//                .replace('\n', (char) 32)
//                .replace((char) 160, (char) 32)
//                .replace((char) 65532, (char) 32).trim();
        return Jsoup.clean(s, Whitelist.none()).trim();
    }

    @Override
    public int getItemCount() {
        return this.feed.getArticles().size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView txt1, txt2, txt3, txt4;
        public ImageView imageView;

        public MyViewHolder(View v) {
            super(v);
            txt1 = (TextView) v.findViewById(R.id.txtDate);
            txt2 = (TextView) v.findViewById(R.id.txtFeedName);
            txt3 = (TextView) v.findViewById(R.id.txtTitle);
            txt4 = (TextView) v.findViewById(R.id.txtDescription);
            imageView = (ImageView) v.findViewById(R.id.ivThumbnail);
        }

        public void bind(final RSSArticle item, final IClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.OnItemClick(item, feed.getId(), getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(v.getContext(), "Clicked at: " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
        }
    }

    public interface IClickListener {
        void OnItemClick(RSSArticle article, int feedId, int position);
    }


}