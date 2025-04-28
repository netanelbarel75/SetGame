package com.example.setcardgame.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.setcardgame.R;

/**
 * Service for playing background music in the Set Game app.
 * This service handles playback of background music, allowing it to continue
 * playing across different activities and fragments.
 */
public class BackgroundMusicService extends Service {
    private static final String TAG = "BackgroundMusicService";
    private static final String PREF_NAME = "MusicPreferences";
    private static final String KEY_MUSIC_ENABLED = "music_enabled";
    
    private MediaPlayer mediaPlayer;
    private boolean isPrepared = false;
    private boolean isMusicEnabled = true;
    private final IBinder binder = new MusicBinder();
    
    // Inner Binder class for client communication
    public class MusicBinder extends Binder {
        public BackgroundMusicService getService() {
            return BackgroundMusicService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        
        // Load user preference for music
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        isMusicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true);
        
        initializeMediaPlayer();
    }
    
    private void initializeMediaPlayer() {
        // Clean up any existing player
        releaseMediaPlayer();
        
        // Initialize the media player with a background music file
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build());
            
            // Set the audio file from resources (you'll need to add this file)
            try {
                // Use a method that's compatible with API level 23
                mediaPlayer.setDataSource(getResources().openRawResourceFd(R.raw.background_music).getFileDescriptor());
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(0.5f, 0.5f);
                
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mp -> {
                    isPrepared = true;
                    Log.d(TAG, "Media player prepared");
                    if (isMusicEnabled) {
                        mp.start();
                        Log.d(TAG, "Music playback started");
                    }
                });
                
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "Media player error: " + what + ", " + extra);
                    isPrepared = false;
                    return false;
                });
            } catch (Exception e) {
                Log.e(TAG, "Error setting data source: " + e.getMessage(), e);
                releaseMediaPlayer();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing media player: " + e.getMessage(), e);
            releaseMediaPlayer();
        }
    }
    
    /**
     * Start playing the background music
     */
    public void startMusic() {
        if (mediaPlayer != null && isPrepared && !mediaPlayer.isPlaying() && isMusicEnabled) {
            mediaPlayer.start();
            Log.d(TAG, "Music started");
        }
    }
    
    /**
     * Pause the background music
     */
    public void pauseMusic() {
        if (mediaPlayer != null && isPrepared && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.d(TAG, "Music paused");
        }
    }
    
    /**
     * Enable or disable background music
     * @param enabled True to enable music, false to disable
     */
    public void setMusicEnabled(boolean enabled) {
        isMusicEnabled = enabled;
        Log.d(TAG, "Music enabled set to: " + enabled);
        
        // Save the preference
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply();
        
        if (mediaPlayer != null && isPrepared) {
            if (enabled && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                Log.d(TAG, "Music started after enabling");
            } else if (!enabled && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                Log.d(TAG, "Music paused after disabling");
            }
        }
    }
    
    /**
     * Check if background music is currently enabled
     * @return True if music is enabled, false otherwise
     */
    public boolean isMusicEnabled() {
        return isMusicEnabled;
    }
    
    /**
     * Check if music is currently playing
     * @return True if music is playing, false otherwise
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
    
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            isPrepared = false;
            Log.d(TAG, "Media player released");
        }
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If the service gets killed, restart it
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        releaseMediaPlayer();
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }
    
    @Override
    public void onLowMemory() {
        // Pause music on low memory to free up resources
        pauseMusic();
        super.onLowMemory();
    }
}