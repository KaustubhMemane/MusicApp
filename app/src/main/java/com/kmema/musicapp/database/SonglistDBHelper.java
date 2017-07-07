package com.kmema.musicapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kmema on 6/24/2017.
 */

public class SonglistDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "songList.db";
    private static final int DATABASE_VERSION = 4;

    public SonglistDBHelper(Context context)
    {
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_TABLE_LIST = "CREATE TABLE " +
                FavoriteDatabase.ListOfTable.TABLE_NAME_FAV_LISTS + " (" +
                FavoriteDatabase.ListOfTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavoriteDatabase.ListOfTable.COLUMN_LIST_NAME + " TEXT NOT NULL"+
                ");";

        final String SQL_CREATE_SONGS_LIST = "CREATE TABLE " +
                FavoriteDatabase.ListofSongs.TABLE_NAME_SONG_LIST + "(" +
                FavoriteDatabase.ListofSongs._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavoriteDatabase.ListofSongs.COLUMN_SONG_NAME + " TEXT NOT NULL" +
                ");";

        final String SQL_CREATE_TAG_LIST = "CREATE TABLE " +
                FavoriteDatabase.connectionTableSong.TABLE_NAME_TAG + "(" +
                FavoriteDatabase.connectionTableSong._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavoriteDatabase.connectionTableSong.COLUMN_LIST_NAME + " TEXT NOT NULL," +
                FavoriteDatabase.connectionTableSong.COLUMN_SONG_NAME + " TEXT NOT NULL," +
                FavoriteDatabase.connectionTableSong.COLUMN_SONG_ALBUM_NAME + " TEXT NOT NULL," +
                FavoriteDatabase.connectionTableSong.COLUMN_SONG_FULL_PATH + " TEXT NOT NULL," +
                FavoriteDatabase.connectionTableSong.COLUMN_SONG_DURATION + " TEXT NOT NULL," +
                FavoriteDatabase.connectionTableSong.COLUMN_SONG_URI + " TEXT NOT NULL," +
                FavoriteDatabase.connectionTableSong.COLUMN_SONG_ARTWOTK + " TEXT," +
                FavoriteDatabase.connectionTableSong.COLUMN_SONG_ARTIST + " TEXT NOT NULL" +
                ");";

        db.execSQL(SQL_CREATE_TABLE_LIST);
        db.execSQL(SQL_CREATE_SONGS_LIST);
        db.execSQL(SQL_CREATE_TAG_LIST);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS "+ FavoriteDatabase.ListOfTable.TABLE_NAME_FAV_LISTS);
        db.execSQL("DROP TABLE IF EXISTS "+ FavoriteDatabase.ListofSongs.TABLE_NAME_SONG_LIST);
        db.execSQL("DROP TABLE IF EXISTS "+ FavoriteDatabase.connectionTableSong.TABLE_NAME_TAG);
        onCreate(db);
    }
}
