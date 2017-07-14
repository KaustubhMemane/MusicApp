package com.kmema.musicapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amitshekhar.DebugDB;
import com.kmema.musicapp.R;
import com.kmema.musicapp.activities.ActivityDisplaySongs;
import com.kmema.musicapp.activities.SongList;
import com.kmema.musicapp.database.FavoriteDatabase;
import com.kmema.musicapp.database.SonglistDBHelper;
import com.kmema.musicapp.helper.Song;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by AcadGildMentor on 6/3/2015.
 */
public class SongListAdapter extends BaseAdapter {


    public Button youTubeBtn;
    private String currentSong;
    public String listNameTobeDelete;
    private Context mContext;
    private ArrayList<Song> songList;//Data Source for ListView
    public Button deleteBtn;
    private SQLiteDatabase mDb;

    public SongListAdapter(Context context, ArrayList<Song> list) {
        mContext = context;
        this.songList = list;
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Song getItem(int position) {
        return songList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            //Layout inflate for list item
               convertView = LayoutInflater.from(mContext).inflate(R.layout.song_list_item, null);
        }

        youTubeBtn = (Button) convertView.findViewById(R.id.buttonYouTube);
        deleteBtn=(Button)convertView.findViewById(R.id.buttonDelete);
        ImageView mImgSong = (ImageView) convertView.findViewById(R.id.img_listitem_file);
        TextView mtxtSongName = (TextView) convertView.findViewById(R.id.txt_listitem_filename);
        TextView mTxtSongAlbumName = (TextView) convertView.findViewById(R.id.txt_listitem_albumname);
        TextView mTxtSongDuration = (TextView) convertView.findViewById(R.id.txt_listitem_duration);
        TextView mTextSingerName = (TextView) convertView.findViewById(R.id.textViewSingerName);
        mTextSingerName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mTextSingerName.setMarqueeRepeatLimit(10000);
        mTextSingerName.setSelected(true);


        mtxtSongName.setText(songList.get(position).getSongName());
        mTxtSongAlbumName.setText(songList.get(position).getSongAlbumName());
        mTxtSongDuration.setText(songList.get(position).getSongDuration());
        mTextSingerName.setText(songList.get(position).getSongArtist());

        if((songList.get(position).getAlbumArt()) != null)
        {
            Drawable img = Drawable.createFromPath(songList.get(position).getAlbumArt());
            mImgSong.setImageDrawable(img);
        }
        else
        {
            mImgSong.setImageResource(R.drawable.no_clipart);
        }

       deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SonglistDBHelper songDBhelper = new SonglistDBHelper(mContext);
                mDb = songDBhelper.getWritableDatabase();

/*                if (songList.get(position).getSongName() != currentSong)
                {*/

                String songIsplaying = checkName();
                String songTodelete = songList.get(position).getSongName();
                if(!songIsplaying.equals(songTodelete)) {
                    boolean removed = false;
                    removed= removeAlbumList(listNameTobeDelete,songList.get(position).getSongName());
                    mDb.setTransactionSuccessful();
                    mDb.endTransaction();
                    mDb.close();
                    if(removed){
                        songList.remove(position);
                        notifyDataSetChanged();
                    }
                    else
                    {
                        Toast.makeText(mContext, "Not allowed", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(mContext, "Song is Playing! Can't Delete", Toast.LENGTH_SHORT).show();
                }
            }
        });


        youTubeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                alertDialog.setTitle("YouTube Search");
                final EditText input = new EditText(mContext);
                input.setText(songList.get(position).getSongName()+""+songList.get(position).getSongAlbumName());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Intent.ACTION_SEARCH);
                        intent.setPackage("com.google.android.youtube");
                        intent.putExtra("query", input.getText().toString());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                });
                alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(mContext, "NO", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog alertDialog1 = alertDialog.create();
                alertDialog1.show();
            }
        });




        return convertView;
    }

    public String checkName()
    {
        Cursor c = mDb.query(FavoriteDatabase.currentSongData.TABLE_NAME_CURRENT_SONG,
                new String[]{FavoriteDatabase.currentSongData._ID,
                        FavoriteDatabase.currentSongData.COLUMN_CURRENT_SONG_NAME},
                null,
                null,
                null,
                null,
                FavoriteDatabase.currentSongData._ID);

        String SongName = null;
        if (c != null && c.getCount() > 0) {
            if(c.moveToFirst())
            {
                SongName = (c.getString(c.getColumnIndex(FavoriteDatabase.currentSongData.COLUMN_CURRENT_SONG_NAME)));
            }
            c.close();
            return SongName;
        } else {
            return null;
        }
    }

    public void setSongsList(ArrayList<Song> list) {
        songList = list;
        this.notifyDataSetChanged();
    }


    public boolean removeAlbumList(String listNamefromToDeleted, String songNameTodelete)
    {
        mDb.beginTransaction();
        try {
            DebugDB.getAddressLog();
            return mDb.delete(FavoriteDatabase.connectionTableSong.TABLE_NAME_TAG,
                    FavoriteDatabase.connectionTableSong.COLUMN_LIST_NAME+" = ? and "+
                    FavoriteDatabase.connectionTableSong.COLUMN_SONG_NAME+" = ?",
                    new String[]{listNamefromToDeleted,songNameTodelete}) > 0;
        }
        catch (Exception e)
        {
            Toast.makeText(mContext, "Could Not Delete", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    public void SetListName(String listname)
    {
        this.listNameTobeDelete = listname;
    }

    public void setCurrentSongName(String currentSong)
    {
        this.currentSong = currentSong;
    }
}
