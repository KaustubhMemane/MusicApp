package com.kmema.musicapp.adapters;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kmema.musicapp.R;
import com.kmema.musicapp.activities.SongList;
import com.kmema.musicapp.database.FavoriteDatabase;
import com.kmema.musicapp.helper.Song;

import java.util.ArrayList;

/**
 * Created by kmema on 6/29/2017.
 */

public class ListAlbumAdapter extends RecyclerView.Adapter<ListAlbumAdapter.AlbumViewHolder>{


    private Context mContext;
    private Cursor mCursor;
    private ArrayList<Song> mSongList;

    public ListAlbumAdapter(Context mContext, Cursor mCursor)
    {
        this.mContext=mContext;
        this.mCursor = mCursor;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.album_one,parent,false);

        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AlbumViewHolder holder, int position) {
        if(!mCursor.moveToPosition(position))
            return;

        final String albumname = mCursor.getString(mCursor.getColumnIndex(FavoriteDatabase.ListOfTable.COLUMN_LIST_NAME));

        long id = mCursor.getLong(mCursor.getColumnIndex(FavoriteDatabase.ListOfTable._ID));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, SongList.class);
                intent.putExtra("albumnameExtra",albumname);
                mContext.startActivity(intent);
            }
        });

        holder.albumNameTextView.setText(albumname);

        holder.itemView.setTag(R.string.tag2_name,albumname);
        holder.itemView.setTag(R.string.tag1_id,id);
    }

    public void swapCursor(Cursor newCursor)
    {
        if(mCursor != null)
        {
            mCursor.close();
        }
        mCursor = newCursor;

        if(newCursor != null)
        {
            this.notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }


    class AlbumViewHolder extends RecyclerView.ViewHolder {

        TextView albumNameTextView;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            albumNameTextView = (TextView) itemView.findViewById(R.id.textViewAlbumName);
        }



    }
}
