package com.acadgild.musicapp.helper;

/**
 * Created by kmema on 6/28/2017.
 */

public class Album {

    private String albumName;

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public Album(){}
    Album(String albumName)
    {
        this.albumName = albumName;
    }

}
