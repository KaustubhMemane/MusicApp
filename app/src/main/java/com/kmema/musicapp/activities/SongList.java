package com.kmema.musicapp.activities;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.kmema.musicapp.R;
import com.kmema.musicapp.adapters.SongListAdapter;
import com.kmema.musicapp.database.FavoriteDatabase;
import com.kmema.musicapp.database.SonglistDBHelper;
import com.kmema.musicapp.helper.Song;
import com.kmema.musicapp.services.MusicService;

import java.util.ArrayList;

public class SongList extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private SongListAdapter mAdapterListFile;
    private ArrayList<Song> mSongList;
    private ListView mListSongs;
    private MusicService serviceMusic;
    private Intent playIntent;
    ImageView albumArtImage;
    Button addButton;
    private SQLiteDatabase mDb;
    private String mAlbumName = null;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);


        SonglistDBHelper songDBhelper = new SonglistDBHelper(this);

        mDb = songDBhelper.getWritableDatabase();

        mListSongs = (ListView) findViewById(R.id.list_songs_actimport);
        mListSongs.setOnItemClickListener(this);

        mListSongs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

//                Toast.makeText(SongList.this, "YES ITS A LONG CLICK", Toast.LENGTH_SHORT).show();
                addSongToList(position);
                return true;
            }
        });


        albumArtImage=(ImageView)findViewById(R.id.songImageView);

        mAlbumName = getIntent().getExtras().getString("albumnameExtra");

        if(mAlbumName != null)
        {
            Cursor cursor = getDataForAlbum(mAlbumName);
            if(cursor.getCount() <= 0)
            {
                return;
            }
            else
            {
                mSongList = converIntoList(cursor);
            }
        }
        else
        {
            mSongList = getIntent().getParcelableArrayListExtra("extraSongs");
        }


        mAdapterListFile = new SongListAdapter(SongList.this, mSongList);
        mListSongs.setAdapter(mAdapterListFile);


        mAdapterListFile.setSongsList(mSongList);

            if (mSongList == null || mSongList.isEmpty()) {
                Toast.makeText(SongList.this, "NULL", Toast.LENGTH_SHORT).show();
            }
            else
            {
                //serviceMusic.setSongList(mSongList);
            }
        }

    private ArrayList<Song> converIntoList(Cursor cursor) {
        ArrayList<Song> sample = new ArrayList<Song>();

        if (!cursor.equals(null) && cursor != null) {
            if (cursor.moveToFirst()) {
/*                this.mSongName = name;
                this.mSongId = id;
                this.mSongAlbumName = album_name;
                this.mSongFullPath = full_path;
                this.mSongDuration = duration;
                this.mSongUri = songuri;
                this.mAlbumArt = albumArt;*/
                do {
                    Song song = new Song();
                    song.setSongName(cursor.getString(cursor.getColumnIndex(FavoriteDatabase.connectionTableSong.COLUMN_SONG_NAME)));
                    song.setSongAlbumName(cursor.getString(cursor.getColumnIndex(FavoriteDatabase.connectionTableSong.COLUMN_SONG_ALBUM_NAME)));
                    song.setSongFullPath(cursor.getString(cursor.getColumnIndex(FavoriteDatabase.connectionTableSong.COLUMN_SONG_FULL_PATH)));
                    song.setSongDuration(cursor.getString(cursor.getColumnIndex(FavoriteDatabase.connectionTableSong.COLUMN_SONG_DURATION)));
                    song.setSongUri(cursor.getString(cursor.getColumnIndex(FavoriteDatabase.connectionTableSong.COLUMN_SONG_URI)));
                    sample.add(song);
                } while (cursor.moveToNext());
                return sample;
            }
        }
        return null;

    }


    Cursor getDataForAlbum(String mAlbumName)
        {
            Cursor c = mDb.query(FavoriteDatabase.connectionTableSong.TABLE_NAME_TAG,
                    new String[]{FavoriteDatabase.connectionTableSong._ID,
                            FavoriteDatabase.connectionTableSong.COLUMN_SONG_DURATION,
                            FavoriteDatabase.connectionTableSong.COLUMN_SONG_URI,
                            FavoriteDatabase.connectionTableSong.COLUMN_SONG_FULL_PATH,
                            FavoriteDatabase.connectionTableSong.COLUMN_SONG_ALBUM_NAME,
                            FavoriteDatabase.connectionTableSong.COLUMN_LIST_NAME,
                            FavoriteDatabase.connectionTableSong.COLUMN_SONG_NAME},
                    FavoriteDatabase.connectionTableSong.COLUMN_LIST_NAME + " = ?",
                    new String[]{mAlbumName},
                    null,
                    null,
                    FavoriteDatabase.connectionTableSong.COLUMN_SONG_ALBUM_NAME);

            if (c != null && c.getColumnCount() > 0) {
                return c;
            } else {
                return null;
            }
        }

    /*public void setSongAlbumArt(String path){
        Bitmap bm= BitmapFactory.decodeFile(path);
        albumArtImage.setImageBitmap(bm);
    }
    */

    public void addSongToList(final int position) {
        final Cursor cursor = getAllAlbum();
        final ArrayList<String> mylist = new ArrayList<String>();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    mylist.add(cursor.getString(cursor.getColumnIndex(FavoriteDatabase.ListOfTable.COLUMN_LIST_NAME)));
                }while(cursor.moveToNext());
            }
        }
        else
        {
            return;
        }
        if(mylist.size()<=0)
        {
            Toast.makeText(this, "No List to Add", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(SongList.this);
        builder.setTitle("Select Album");

        builder.setSingleChoiceItems(mylist.toArray(new String[mylist.size()]), -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(SongList.this, mylist.get(which), Toast.LENGTH_SHORT).show();
                addSongToAlbum(mylist.get(which),position);
            }
        });

        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Toast.makeText(SongList.this, "YES ACCEPTTED", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }

    private Cursor getAllAlbum() {
        Cursor c = mDb.query(FavoriteDatabase.ListOfTable.TABLE_NAME_FAV_LISTS,
                new String[]{FavoriteDatabase.ListOfTable._ID,
                        FavoriteDatabase.ListOfTable.COLUMN_LIST_NAME},
                null,
                null,
                null,
                null,
                FavoriteDatabase.ListOfTable.COLUMN_LIST_NAME);

        if (c != null && c.getColumnCount() > 0) {
            return c;
        } else {
            return null;
        }
    }

    private void addSongToAlbum(String albumName, int position) {

        String songName = mSongList.get(position).getSongName();

        if (albumName != null && !albumName.isEmpty()) {
            if(!checkDuplicateSong(songName, albumName))
            {
                ContentValues cv = new ContentValues();
                cv.put(FavoriteDatabase.connectionTableSong.COLUMN_LIST_NAME, albumName);
                cv.put(FavoriteDatabase.connectionTableSong.COLUMN_SONG_ALBUM_NAME,mSongList.get(position).getSongAlbumName());
                cv.put(FavoriteDatabase.connectionTableSong.COLUMN_SONG_DURATION,mSongList.get(position).getSongDuration());
                cv.put(FavoriteDatabase.connectionTableSong.COLUMN_SONG_FULL_PATH, mSongList.get(position).getSongFullPath());
                cv.put(FavoriteDatabase.connectionTableSong.COLUMN_SONG_NAME, mSongList.get(position).getSongName());
                cv.put(FavoriteDatabase.connectionTableSong.COLUMN_SONG_URI, mSongList.get(position).getSongUri());
                mDb.insert(FavoriteDatabase.connectionTableSong.TABLE_NAME_TAG, null, cv);
            }
            else
            {
                Toast.makeText(this, "Song Already Present", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(SongList.this, "could not insert", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkDuplicateSong(String songName, String albumName)
    {
        Cursor c = mDb.query(FavoriteDatabase.connectionTableSong.TABLE_NAME_TAG,
                new String[]{FavoriteDatabase.connectionTableSong._ID,
                        FavoriteDatabase.connectionTableSong.COLUMN_SONG_NAME},
                FavoriteDatabase.connectionTableSong.COLUMN_SONG_NAME + " = ? AND "+
                FavoriteDatabase.connectionTableSong.COLUMN_LIST_NAME + " = ?",
                new String[]{songName, albumName},
                null,
                null,
                FavoriteDatabase.connectionTableSong.COLUMN_SONG_NAME);

        if (c != null && c.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        serviceMusic.setSelectedSong(position, MusicService.NOTIFICATION_ID);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
         else
        {
            playIntent = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(serviceMusic != null)
        {
            Toast.makeText(this, "ITS NOT NULL ON DESTROY", Toast.LENGTH_SHORT).show();
            Intent in = new Intent(this,ActivityDisplaySongs.class);
            startActivity(in);
        }
    }

    @Override
    public void onBackPressed() {
//              onKeyDown(KeyEvent.KEYCODE_HOME);
        Intent intent = new Intent(this, ActivityDisplaySongs.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        startActivity(intent);
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.PlayerBinder binder = (MusicService.PlayerBinder) service;
            //get service
            serviceMusic = binder.getService();
            serviceMusic.setSongList(mSongList);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceMusic = null;
        }
    };
}