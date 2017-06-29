package com.acadgild.musicapp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.acadgild.musicapp.R;
import com.acadgild.musicapp.adapters.ListOfFavoriteListAdapter;
import com.acadgild.musicapp.adapters.SongListAdapter;
import com.acadgild.musicapp.database.FavoriteDatabase;
import com.acadgild.musicapp.database.SonglistDBHelper;
import com.acadgild.musicapp.helper.Album;
import com.acadgild.musicapp.helper.Song;
import com.acadgild.musicapp.helper.SongView;
import com.acadgild.musicapp.services.MusicService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class ActivityDisplaySongs extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {


    private ListOfFavoriteListAdapter mAdapterAlbum;
    private Cursor cursor = null;
    private ListView mListAlbum;
    private Button mBtnImport;
    private ListView mListSongs;
    /*  private LinearLayout mLinearListImportedFiles;
      private RelativeLayout mRelativeBtnImport;*/
    private SongListAdapter mAdapterListFile;
    private String[] STAR = {"*"};
    private ArrayList<Song> mSongList;
    private ArrayList<Album> mAlbumList;
    private MusicService serviceMusic;
    private Intent playIntent;
    private Context context;
    FloatingActionButton mBtnAddSongs;
    //SQLite Database for storing songs information
    private SQLiteDatabase mDb;
    String albumName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_display_songs);
        SonglistDBHelper songDBhelper = new SonglistDBHelper(this);
        mDb = songDBhelper.getWritableDatabase();

        cursor = getAllAlbum();
        if(cursor!=null)
            mAlbumList = cursorToArray(cursor);

        if(mAlbumList != null) {
            mListAlbum = (ListView) findViewById(R.id.list_album_actimport);

            mListAlbum.setOnItemClickListener(this);
            mAdapterAlbum = new ListOfFavoriteListAdapter(ActivityDisplaySongs.this, mAlbumList);
            mListAlbum.setAdapter(mAdapterAlbum);
        }
        checkPermission();
        init();
    }

    private void init() {
        getActionBar();
        mBtnAddSongs = (FloatingActionButton) findViewById(R.id.floatingActionButton2);
        mBtnImport = (Button) findViewById(R.id.btn_import_files);
        //mLinearListImportedFiles = (LinearLayout) findViewById(R.id.linear_list_imported_files);
        //mRelativeBtnImport = (RelativeLayout) findViewById(R.id.relative_btn_import);
        /*mListSongs = (ListView) findViewById(R.id.list_songs_actimport);
      mListSongs.setOnItemClickListener(this);*/
        mBtnAddSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityDisplaySongs.this);
                alertDialog.setTitle("New Album");
                final EditText input = new EditText(ActivityDisplaySongs.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("ENTER", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        albumName = input.getText().toString();
                        Toast.makeText(ActivityDisplaySongs.this, albumName + "YES", Toast.LENGTH_SHORT).show();
                        addNewAlbum(albumName);

                        cursor = null;
                        cursor = getAllAlbum();
                        mAlbumList = cursorToArray(cursor);

                        String sample = "";
                        if (cursor.moveToFirst()) {
                            while (!cursor.isAfterLast()) {
                                sample = sample + " " + cursor.getString(cursor.getColumnIndex(FavoriteDatabase.ListOfTable.COLUMN_LIST_NAME));
                                cursor.moveToNext();
                            }
                        }
                        Toast.makeText(ActivityDisplaySongs.this, sample + ": got it", Toast.LENGTH_SHORT).show();
 //                       mAdapterListFile.notifyDataSetChanged();
                        //mAdapterAlbum.setAlbum(mAlbumList);
                    }
                });
                alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ActivityDisplaySongs.this, "NO", Toast.LENGTH_SHORT).show();
                    }
                });


                AlertDialog alertDialog1 = alertDialog.create();
                alertDialog1.show();
            }
        });
        mBtnImport.setOnClickListener(this);

    }


    private void addNewAlbum(String albumName) {
        if (albumName != null && !albumName.isEmpty()) {
            ContentValues cv = new ContentValues();
            cv.put(FavoriteDatabase.ListOfTable.COLUMN_LIST_NAME, albumName);
            mDb.insert(FavoriteDatabase.ListOfTable.TABLE_NAME_FAV_LISTS, null, cv);
        } else {
            Toast.makeText(ActivityDisplaySongs.this, "ENTER ALBUM NAME", Toast.LENGTH_SHORT).show();
        }
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


    @Override
    public void onClick(View v) {

        mSongList = listAllSongs();
//        mAdapterListFile.setSongsList(mSongList);

        Intent intent = new Intent(ActivityDisplaySongs.this, SongList.class);
       /* Bundle bundle = new Bundle();
        bundle.putSerializable("ARRAYLIST",(Serializable) mSongList);

        intent.putExtra("songBundleList", bundle);
       */

        intent.putParcelableArrayListExtra("extraSongs", mSongList);
        startActivity(intent);
//        mLinearListImportedFiles.setVisibility(View.VISIBLE);
//        mRelativeBtnImport.setVisibility(View.GONE);
//        serviceMusic.setSongList(mSongList);

    }


    public void checkPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(this)) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 2909);
                } else {
                    // continue with your code
                }
            } else {
                // continue with your code
            }
        } catch (SecurityException se) {
            Log.d("FragmentCreate", "You don't have permissions");

         /*   errortext.setVisibility(View.VISIBLE);
            errortext.setText("Please provide Location permission to continue, Settings->Apps->RecommendedApp->Permissions");
         */
            Toast.makeText(this, "Please provide location permissions to continue", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2909: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("Permission", "Granted");
                } else {
                    Log.e("Permission", "Denied");
                }
                return;
            }
        }
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

    private ArrayList<Album> cursorToArray(Cursor cursor)
    {

        ArrayList<Album> sample = new ArrayList<Album>();
        if(!cursor.equals(null) && cursor!=null)
        {

            Album album = new Album();

            if(cursor.moveToFirst()) {
                do{
                    album.setAlbumName(cursor.getString(cursor.getColumnIndex(FavoriteDatabase.ListOfTable.COLUMN_LIST_NAME)));
                    sample.add(album);
                }while (cursor.moveToNext());
                return sample;
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
