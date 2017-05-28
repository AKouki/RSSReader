package com.akouki.rssreader.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.akouki.rssreader.R;
import com.akouki.rssreader.models.DrawerItem;
import com.akouki.rssreader.utils.Utils;

import java.util.ArrayList;

public class CustomDrawerAdapter extends ArrayAdapter<DrawerItem> {
    Context context;
    ArrayList<DrawerItem> drawerItemList;
    int resourceId;

    public CustomDrawerAdapter(Context context, int resource, ArrayList<DrawerItem> itemArrayList) {
        super(context, resource, itemArrayList);
        this.context = context;
        this.drawerItemList = itemArrayList;
        this.resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder = null;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.custom_drawer_item, parent, false);
            holder = new ViewHolder(rowView);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }


        DrawerItem drawerItem = drawerItemList.get(position);
        if (drawerItem.getFavicon() != null && !drawerItem.getFavicon().isEmpty()) {
            try {
                holder.favicon.setImageBitmap(Utils.Base64ToBitmap(drawerItem.getFavicon()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        holder.name.setText(drawerItem.getName());

        String counter;
        if (drawerItem.getCounter() > 99)
            counter = "99+";
        else
            counter = String.valueOf(drawerItem.getCounter());
        holder.counter.setText(counter);

        return rowView;
    }

    static class ViewHolder {
        ImageView favicon;
        TextView name, counter;

        ViewHolder(View v) {
            favicon = (ImageView) v.findViewById(R.id.ivFavicon);
            name = (TextView) v.findViewById(R.id.txtName);
            counter = (TextView) v.findViewById(R.id.txtCounter);
        }
    }
}