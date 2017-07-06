package com.kmema.musicapp.activities;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.kmema.musicapp.R;
import com.kmema.musicapp.adapters.ListAlbumAdapter;
import com.kmema.musicapp.adapters.ListOfFavoriteListAdapter;
import com.kmema.musicapp.adapters.SongListAdapter;
import com.kmema.musicapp.database.FavoriteDatabase;
import com.kmema.musicapp.database.SonglistDBHelper;
import com.kmema.musicapp.helper.Album;
import com.kmema.musicapp.helper.Song;
import com.kmema.musicapp.services.MusicService;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class ActivityDisplaySongs extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static SeekBar mSeekBar;
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
    long id;
    ListAlbumAdapter listAlbumAdapter;
    RecyclerView recyclerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_display_songs);


        recyclerView = (RecyclerView) findViewById(R.id.all_album_view_rc_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SonglistDBHelper songDBhelper = new SonglistDBHelper(this);
        mDb = songDBhelper.getWritableDatabase();
        cursor = getAllAlbum();
        listAlbumAdapter = new ListAlbumAdapter(ActivityDisplaySongs.this, cursor);
        recyclerView.setAdapter(listAlbumAdapter);


        /*if(cursor!=null)
            mAlbumList = cursorToArray(cursor);

        if(mAlbumList != null) {
            mListAlbum = (ListView) findViewById(R.id.list_album_actimport);

            mListAlbum.setOnItemClickListener(this);
            mAdapterAlbum = new ListOfFavoriteListAdapter(ActivityDisplaySongs.this, mAlbumList);
            mListAlbum.setAdapter(mAdapterAlbum);
        }*/

        checkPermission();
        init();
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                long id = (long) viewHolder.itemView.getTag(R.string.tag1_id);
                String albumNameToDelete = (String) viewHolder.itemView.getTag(R.string.tag2_name);
                removeAlbumList(id, albumNameToDelete);
                listAlbumAdapter.swapCursor(getAllAlbum());

            }

        }).attachToRecyclerView(recyclerView);


    }





    private void init() {
        getActionBar();
        mBtnAddSongs = (FloatingActionButton) findViewById(R.id.floatingActionButton2);
        mBtnImport = (Button) findViewById(R.id.btn_import_files);

        mBtnAddSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityDisplaySongs.this);
                alertDialog.setTitle("Enter New Album Name");
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
               //         Toast.makeText(ActivityDisplaySongs.this, albumName + "YES", Toast.LENGTH_SHORT).show();
                        addNewAlbum(albumName);

/*
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
*/
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
            if(checkPreviousAlbumName(albumName))
            {
                Toast.makeText(this, "No Duplicate Album", Toast.LENGTH_SHORT).show();
                return;
            }
            ContentValues cv = new ContentValues();
            cv.put(FavoriteDatabase.ListOfTable.COLUMN_LIST_NAME, albumName);
            mDb.insert(FavoriteDatabase.ListOfTable.TABLE_NAME_FAV_LISTS, null, cv);

            listAlbumAdapter.swapCursor(getAllAlbum());
        } else {
            Toast.makeText(ActivityDisplaySongs.this, "TRY Again", Toast.LENGTH_SHORT).show();
        }
    }


    public boolean checkPreviousAlbumName(String albumName)
    {
        Cursor c = mDb.query(FavoriteDatabase.ListOfTable.TABLE_NAME_FAV_LISTS,
                new String[]{FavoriteDatabase.ListOfTable._ID,
                        FavoriteDatabase.ListOfTable.COLUMN_LIST_NAME},
                FavoriteDatabase.ListOfTable.COLUMN_LIST_NAME + " = ?",
                new String[]{albumName},
                null,
                null,
                FavoriteDatabase.ListOfTable.COLUMN_LIST_NAME);

        if (c != null && c.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }


    private boolean removeAlbumList(long id, String albumNameToDelete) {

        try {
            mDb.delete(FavoriteDatabase.connectionTableSong.TABLE_NAME_TAG,
                    FavoriteDatabase.connectionTableSong.COLUMN_LIST_NAME +" = ?",new String[] {albumNameToDelete});
                 }catch (Exception e){
        }
        return mDb.delete(FavoriteDatabase.ListOfTable.TABLE_NAME_FAV_LISTS,
                FavoriteDatabase.ListOfTable._ID + "=" + id, null) > 0;
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

        Intent intent = new Intent(ActivityDisplaySongs.this, SongList.class);
        intent.putParcelableArrayListExtra("extraSongs", mSongList);
        startActivity(intent);
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
                        song.setSongAlbumName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));

                        song.setSongUri(String.valueOf(ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)))));

                        String duration = getDuration(Integer.parseInt(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))));

                        song.setSongDuration(duration);

                        Cursor cursor1 = managedQuery(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                                new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                                MediaStore.Audio.Albums._ID+ " = ?",
                                new String[] {String.valueOf(cursor.getColumnIndex(MediaStore.Audio.Media._ID))},
                                null);

                        if (cursor1.moveToFirst()) {
                            String path = cursor1.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                            Log.e("path::", path);
                        }


                        //Toast.makeText(this, MediaStore.Audio.Albums.ALBUM_ART"", Toast.LENGTH_SHORT).show();
                        songList.add(song);
                    } while (cursor.moveToNext());
                    return songList;
                }
                cursor.close();
            }
        }
        return null;
    }

    private ArrayList<Album> cursorToArray(Cursor cursor) {

        ArrayList<Album> sample = new ArrayList<Album>();
        if (!cursor.equals(null) && cursor != null) {

            Album album = new Album();

            if (cursor.moveToFirst()) {
                do {
                    album.setAlbumName(cursor.getString(cursor.getColumnIndex(FavoriteDatabase.ListOfTable.COLUMN_LIST_NAME)));
                    sample.add(album);
                } while (cursor.moveToNext());
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

//        Toast.makeText(this, , Toast.LENGTH_SHORT).show();
    }
}
