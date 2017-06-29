package com.acadgild.musicapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.acadgild.musicapp.R;
import com.acadgild.musicapp.database.FavoriteDatabase;
import com.acadgild.musicapp.helper.Album;

import java.util.ArrayList;

/**
 * Created by kmema on 6/25/2017.
 */

public class ListOfFavoriteListAdapter extends BaseAdapter {

    private Context mContext;
    private Cursor cursor;
    private ArrayList<Album> mAlbum;
    public  ListOfFavoriteListAdapter(Context context, ArrayList<Album> mAlbum)
    {
        mContext = context;
        //this.cursor = cursor;
        this.mAlbum = mAlbum;
    }


    @Override
    public int getCount() {
        return mAlbum.size();
    }

    @Override
    public Object getItem(int position) {
        return mAlbum.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.album_one, null);
        }

        TextView mtextViewAlbumName = (TextView) convertView.findViewById(R.id.textViewAlbumName);
        TextView mtextAlbumDuration = (TextView) convertView.findViewById(R.id.textViewDuration);

        mtextViewAlbumName.setText(mAlbum.get(position).getAlbumName());

        return convertView;
    }

    public void setAlbum(ArrayList<Album> mAlbum)
    {
        this.mAlbum = mAlbum;
        this.notifyDataSetChanged();
    }
}
