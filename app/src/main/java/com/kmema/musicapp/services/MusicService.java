package com.kmema.musicapp.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.amitshekhar.DebugDB;
import com.kmema.musicapp.R;
import com.kmema.musicapp.activities.SongList;
import com.kmema.musicapp.database.FavoriteDatabase;
import com.kmema.musicapp.database.SonglistDBHelper;
import com.kmema.musicapp.helper.Song;

import java.util.ArrayList;
import java.util.logging.Handler;


public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener {

    String FolderName = null;
    String songNameToDisplay = null;
    Context songClassContext;
    private MediaPlayer mPlayer;
    private Uri mSongUri;
    SQLiteDatabase mDb;
    private ArrayList<Song> mListSongs;
    private int SONG_POS = 0;
    private final IBinder musicBind = new PlayerBinder();

    private final String ACTION_STOP = "com.acadgild.musicapp.STOP";
    private final String ACTION_NEXT = "com.acadgild.musicapp.NEXT";
    private final String ACTION_PREVIOUS = "com.acadgild.musicapp.PREVIOUS";
    private final String ACTION_PAUSE = "com.acadgild.musicapp.PAUSE";

    private static final int STATE_PAUSED = 1;
    private static final int STATE_PLAYING = 2;
    private int mState = 0;
    private static final int REQUEST_CODE_PAUSE = 101;
    private static final int REQUEST_CODE_PREVIOUS = 102;
    private static final int REQUEST_CODE_NEXT = 103;
    private static final int REQUEST_CODE_STOP = 104;
    public static int NOTIFICATION_ID = 11;
    private Notification.Builder notificationBuilder;
    private Notification mNotification;
    private String imageLink;
    TelephonyManager mgr;
    PhoneStateListener phoneStateListener;
    private AudioManager mAudioManager;


    @Override
    public void onCreate() {
        super.onCreate();

        SonglistDBHelper songlistDBHelper = new SonglistDBHelper(this);
        mDb = songlistDBHelper.getWritableDatabase();


        //Initializing the media player object
        mPlayer = new MediaPlayer();
        initPlayer();
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        notificationBuilder = new Notification.Builder(getApplicationContext());

        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    mPlayer.pause();
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    mPlayer.start();
                    //Not in call: Play music
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    mPlayer.start();
                    //A call is dialing, active or on hold
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }


    @Override
    public void onAudioFocusChange(int focusChange) {

        if (focusChange <= 0) {
            mPlayer.pause();
        } else {
            mPlayer.start();
            //GAIN -> PLAY
        }
    }

    public class PlayerBinder extends Binder {//Service connection to play in background

        public MusicService getService() {
            Log.d("test", "getService()");
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("test", "onBind Called ");
        return musicBind;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                if (action.equals(ACTION_PAUSE)) {
                    playPauseSong();
                } else if (action.equals(ACTION_NEXT)) {
                    nextSong();
                } else if (action.equals(ACTION_PREVIOUS)) {
                    previousSong();
                } else if (action.equals(ACTION_STOP)) {
                    stopSong();
                    stopSelf();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Stop the Mediaplayer
        stopSong();
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        mAudioManager.abandonAudioFocus(this);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
        mPlayer.stop();
        mPlayer.release();

/*        System.gc();*/
        System.exit(0);
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayer.reset();
        try {
            if (SONG_POS != mListSongs.size() - 1) {
                SONG_POS++;
            } else
                SONG_POS = 0;
            mPlayer.setDataSource(getApplicationContext(), Uri.parse(mListSongs.get(SONG_POS).getSongUri()));
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        //mPlayer.prepareAsync();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayer.start();
    }

    private void initPlayer() {
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void startSong(Uri songuri, String songName, int SONG_POS) {
        //Set data & start playing music
        mPlayer.reset();
        mState = STATE_PLAYING;
        mSongUri = songuri;
        try {
            mPlayer.setDataSource(getApplicationContext(), mSongUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mPlayer.prepareAsync();
        updateNotification(songName);
        songNameToDisplay = songName;
        imageLink = mListSongs.get(SONG_POS).getAlbumArt();

    }


    public void playPauseSong() {

        if (mState == STATE_PAUSED) {
            mState = STATE_PLAYING;
            mPlayer.start();

        } else {
            mState = STATE_PAUSED;
            setNoSongIsPlaying();
            mPlayer.pause();
        }
    }

    public void setNoSongIsPlaying() {
        mDb.delete(FavoriteDatabase.currentSongData.TABLE_NAME_CURRENT_SONG,
                "_id = ?", new String[]{String.valueOf(1)});
    }

    public void stopSong() {
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        mPlayer.stop();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
        System.exit(0);
    }


    public boolean checkSize() {
        Cursor c = mDb.query(FavoriteDatabase.currentSongData.TABLE_NAME_CURRENT_SONG,
                new String[]{FavoriteDatabase.currentSongData._ID,
                        FavoriteDatabase.currentSongData.COLUMN_CURRENT_SONG_NAME},
                null,
                null,
                null,
                null,
                FavoriteDatabase.currentSongData._ID);

        if (c != null && c.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void rememberCurrentSong() {
        ContentValues cv = new ContentValues();
        cv.put(FavoriteDatabase.currentSongData.COLUMN_CURRENT_SONG_NAME, mListSongs.get(SONG_POS).getSongName());
        cv.put(FavoriteDatabase.currentSongData.COLUMN_CURRENT_SONG_ALBUM_NAME, mListSongs.get(SONG_POS).getSongAlbumName());
        cv.put(FavoriteDatabase.currentSongData.COLUMN_CURRENT_FOLDER_NAME, FolderName);
        mDb.beginTransaction();
        if (!checkSize()) {
            mDb.insert(FavoriteDatabase.currentSongData.TABLE_NAME_CURRENT_SONG, null, cv);
        } else {
            String selection = "_id = ?";
            mDb.update(FavoriteDatabase.currentSongData.TABLE_NAME_CURRENT_SONG, cv, selection, new String[]
                    {
                            String.valueOf(1)
                    });

        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
        DebugDB.getAddressLog();
    }


    public void nextSong() {
        if (SONG_POS > mListSongs.size()) {
            SONG_POS = 0;
        }
        try {
            startSong(Uri.parse(mListSongs.get(SONG_POS + 1).getSongUri()), mListSongs.get(SONG_POS + 1).getSongName(), SONG_POS + 1);
            SONG_POS++;
            rememberCurrentSong();
        } catch (Exception e) {
            Toast.makeText(this, "No Next Song", Toast.LENGTH_SHORT).show();
            startSong(Uri.parse(mListSongs.get(SONG_POS).getSongUri()), mListSongs.get(SONG_POS).getSongName(), SONG_POS);
        }
    }

    public void previousSong() {
        try {
            startSong(Uri.parse(mListSongs.get(SONG_POS - 1).getSongUri()), mListSongs.get(SONG_POS - 1).getSongName(), SONG_POS - 1);
            SONG_POS--;
            rememberCurrentSong();
        } catch (Exception e) {
            Toast.makeText(this, "No previous Song", Toast.LENGTH_SHORT).show();
            startSong(Uri.parse(mListSongs.get(SONG_POS).getSongUri()), mListSongs.get(SONG_POS).getSongName(), SONG_POS);
        }
    }

    public void setSongURI(Uri uri) {
        this.mSongUri = uri;
    }


    /*public void setSongList(ArrayList<Song> listSong, int pos, int notification_id) {
        mListSongs = listSong;
        SONG_POS = pos;
        NOTIFICATION_ID = notification_id;
    }*/

    public void setSelectedSong(int pos, int notification_id, Context context) {
        SONG_POS = pos;
        NOTIFICATION_ID = notification_id;
        setSongURI(Uri.parse(mListSongs.get(SONG_POS).getSongUri()));
        showNotification();
        startSong(Uri.parse(mListSongs.get(SONG_POS).getSongUri()), mListSongs.get(SONG_POS).getSongName(), SONG_POS);
        rememberCurrentSong();
    }

    public void setSongList(ArrayList<Song> listSong) {
        mListSongs = listSong;
    }

    public void showNotification() {
        PendingIntent pendingIntent;
        Intent intent;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.notification_mediacontroller);

        notificationView.setTextViewText(R.id.notify_song_name, mListSongs.get(SONG_POS).getSongName());

        intent = new Intent(ACTION_STOP);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_STOP, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notify_btn_stop, pendingIntent);

        intent = new Intent(ACTION_PAUSE);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_PAUSE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notify_btn_pause, pendingIntent);

        intent = new Intent(ACTION_PREVIOUS);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_PREVIOUS, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notify_btn_previous, pendingIntent);

        intent = new Intent(ACTION_NEXT);
        pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_NEXT, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notify_btn_next, pendingIntent);

        mNotification = notificationBuilder
                .setSmallIcon(R.drawable.app_icon).setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setContent(notificationView)
                .setDefaults(Notification.FLAG_NO_CLEAR)
                .build();
        notificationManager.notify(NOTIFICATION_ID, mNotification);

    }

    private void updateNotification(String songName) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotification.contentView.setTextViewText(R.id.notify_song_name, songName);
        notificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    public String[] getsongName() {
        return new String[]{songNameToDisplay, imageLink};
    }

    public void setFolderName(String FolderName) {
        this.FolderName = FolderName;
    }
}
