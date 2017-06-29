package com.acadgild.musicapp.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.acadgild.musicapp.R;
import com.acadgild.musicapp.adapters.SongListAdapter;
import com.acadgild.musicapp.helper.Song;
import com.acadgild.musicapp.services.MusicService;

import java.util.ArrayList;

public class SongList extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private SongListAdapter mAdapterListFile;
    private ArrayList<Song> mSongList;
    private ListView mListSongs;
    private MusicService serviceMusic;
    private Intent playIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        mListSongs = (ListView) findViewById(R.id.list_songs_actimport);
        mListSongs.setOnItemClickListener(this);


        mSongList=getIntent().getParcelableArrayListExtra("extraSongs");

/*
        Bundle extras = getIntent().getBundleExtra("ARRAYLIST");
        if(extras != null)
        {
            mSongList = (ArrayList<Song>) extras.getSerializable("songList");
        }
*/
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

/*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
*/

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
