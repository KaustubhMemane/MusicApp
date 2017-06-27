package com.acadgild.musicapp.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;

import com.acadgild.musicapp.R;
import com.acadgild.musicapp.adapters.SongListAdapter;
import com.acadgild.musicapp.database.SonglistDBHelper;
import com.acadgild.musicapp.helper.Song;
import com.acadgild.musicapp.helper.SongView;
import com.acadgild.musicapp.services.MusicService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class ActivityDisplaySongs extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button mBtnImport;
    private ListView mListSongs;
  /*  private LinearLayout mLinearListImportedFiles;
    private RelativeLayout mRelativeBtnImport;*/
    private SongListAdapter mAdapterListFile;
    private String[] STAR = {"*"};
    private ArrayList<Song> mSongList;
    private MusicService serviceMusic;
    private Intent playIntent;


    //SQLite Database for storing songs information
    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_display_songs);
        SonglistDBHelper  songDBhelper = new SonglistDBHelper(this);
        mDb = songDBhelper.getWritableDatabase();
        init();
    }

    private void init() {
        getActionBar();
        mBtnImport = (Button) findViewById(R.id.btn_import_files);
        //mLinearListImportedFiles = (LinearLayout) findViewById(R.id.linear_list_imported_files);
        //mRelativeBtnImport = (RelativeLayout) findViewById(R.id.relative_btn_import);
        /*mListSongs = (ListView) findViewById(R.id.list_songs_actimport);
      mListSongs.setOnItemClickListener(this);*/
        mBtnImport.setOnClickListener(this);
/*
        mSongList = new ArrayList<Song>();
        mAdapterListFile = new SongListAdapter(ActivityDisplaySongs.this, mSongList);
*/
        /*mListSongs.setAdapter(mAdapterListFile);*/
    }

/*
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
*/

    @Override
    public void onClick(View v) {
        mSongList = listAllSongs();
//        mAdapterListFile.setSongsList(mSongList);

        Intent intent = new Intent(ActivityDisplaySongs.this,SongList.class);
       /* Bundle bundle = new Bundle();
        bundle.putSerializable("ARRAYLIST",(Serializable) mSongList);

        intent.putExtra("songBundleList", bundle);
       */

       intent.putParcelableArrayListExtra("extraSongs",mSongList);
       startActivity(intent);
//        mLinearListImportedFiles.setVisibility(View.VISIBLE);
//        mRelativeBtnImport.setVisibility(View.GONE);
//        serviceMusic.setSongList(mSongList);
    }

    //import list all songs from list



    private ArrayList<Song> listAllSongs() { //Fetch path to all the files from internal & external storage n store it in songList
        Cursor cursor;
        ArrayList<Song> songList = new ArrayList<Song>();
        Uri allSongsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        if (isSdPresent()) {
            cursor = managedQuery(allSongsUri, STAR, selection, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Song song = new Song();

                        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                        String[] res = data.split("\\.");
                        song.setSongName(res[0]);
                        //Log.d("test",res[0] );
                        song.setSongFullPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                        song.setSongId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                        song.setSongFullPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                        song.setSongAlbumName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                        song.setSongUri(String.valueOf(ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)))));
                        String duration = getDuration(Integer.parseInt(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))));
                        song.setSongDuration(duration);

                        songList.add(song);
                    } while (cursor.moveToNext());
                    return songList;
                }
                cursor.close();
            }
        }
        return null;
    }
    //Check whether sdcard is present or not
    private static boolean isSdPresent() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    //Method to convert the millisecs to min & sec
    private static String getDuration(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(6);
        sb.append(minutes < 10 ? "0" + minutes : minutes);
        sb.append(":");
        sb.append(seconds < 10 ? "0" + seconds : seconds);
        //sb.append(" Secs");
        return sb.toString();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        serviceMusic.setSelectedSong(position, MusicService.NOTIFICATION_ID);
    }


/*    @Override
    protected void onStart() {
        super.onStart();
        //Start service
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
    protected void onDestroy() {
        //Stop service
        unbindService(musicConnection);
        stopService(playIntent);
        serviceMusic = null;
        super.onDestroy();
    }*/
}
