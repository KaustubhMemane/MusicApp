package com.kmema.musicapp.database;

import android.provider.BaseColumns;

/**
 * Created by kmema on 6/23/2017.
 */

public class FavoriteDatabase {


    public static final class ListOfTable implements BaseColumns {
        public static final String TABLE_NAME_FAV_LISTS = "FavoriteList";
        public static final String COLUMN_LIST_NAME = "NameofList";
    }

    public static final class ListofSongs implements BaseColumns {
        public static final String TABLE_NAME_SONG_LIST = "songsList";
        public static final String COLUMN_SONG_NAME = "nameofSong";
    }

    public static  final class connectionTableSong  implements BaseColumns{
        public static final String TABLE_NAME_TAG = "Tag_table";
        public static final String COLUMN_LIST_NAME = "listNameTag";
        public static final String COLUMN_SONG_NAME = "songNameTag";
        public static final String COLUMN_SONG_ALBUM_NAME = "songAlbumName";
        public static final String COLUMN_SONG_FULL_PATH = "songFullPath";
        public static final String COLUMN_SONG_DURATION = "songDuration";
        public static final String COLUMN_SONG_URI = "songURI";
    }
}
