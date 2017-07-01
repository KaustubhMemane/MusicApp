package com.acadgild.musicapp.helper;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {
    private String mSongName, mSongAlbumName , mSongFullPath , mSongDuration ;
    private String mSongUri, mAlbumArt;
    private int mSongId;

    public Song(){ }
    public Song(String name , int id ,  String album_name , String full_path , String duration , String songuri,String albumArt){
        this.mSongName = name;
        this.mSongId = id;
        this.mSongAlbumName = album_name;
        this.mSongFullPath = full_path;
        this.mSongDuration = duration;
        this.mSongUri = songuri;
        this.mAlbumArt = albumArt;
    }




    public String getAlbumArt() { return mAlbumArt;}

    public  void setAlbumArt(String albumArt) {  this.mAlbumArt = albumArt;}

    public String getSongName() {
        return mSongName;
    }

    public void setSongName(String mSongName) {
        this.mSongName = mSongName;
    }

    public String getSongFullPath() {
        return mSongFullPath;
    }

    public void setSongFullPath(String mSongFullPath) {
        this.mSongFullPath = mSongFullPath;
    }

    public String getSongAlbumName() {
        return mSongAlbumName;
    }

    public void setSongAlbumName(String mSongAlbumName) {
        this.mSongAlbumName = mSongAlbumName;
    }

    public String getSongDuration() {
        return mSongDuration;
    }

    public void setSongDuration(String mSongDuration) {
        this.mSongDuration = mSongDuration;
    }

    public int getSongId() {
        return mSongId;
    }

    public void setSongId(int mSongId) {
        this.mSongId = mSongId;
    }

    public void setSongUri(String uri){ this.mSongUri = uri; }

    public String getSongUri(){
        return this.mSongUri;
    }


    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;

    }

    protected Song(Parcel in) {
        mSongName = in.readString();
        mSongAlbumName = in.readString();
        mSongFullPath = in.readString();
        mSongDuration = in.readString();
        mSongUri = in.readString();
        mSongId = in.readInt();
        mAlbumArt = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSongName);
        dest.writeString(mSongAlbumName);
        dest.writeString(mSongFullPath);
        dest.writeString(mSongDuration);
        dest.writeString(String.valueOf(mSongUri));
        dest.writeInt(mSongId);
        dest.writeString(mAlbumArt);
    }
}
