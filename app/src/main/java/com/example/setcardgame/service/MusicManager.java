package com.example.setcardgame.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * Singleton manager class for controlling background music.
 * This class simplifies interacting with the BackgroundMusicService from
 * different parts of the application.
 */
public class MusicManager {
    private static final String TAG = "MusicManager";
    private static MusicManager instance;
    
    private BackgroundMusicService musicService;
    private boolean isServiceBound = false;
    private Context applicationContext;
    private boolean pendingMusicEnabled = true; // Default music to be on
    
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroundMusicService.MusicBinder binder = (BackgroundMusicService.MusicBinder) service;
            musicService = binder.getService();
            isServiceBound = true;
            Log.d(TAG, "Service connected");
            
            // Apply any pending music state once connected
            musicService.setMusicEnabled(pendingMusicEnabled);
            if (pendingMusicEnabled) {
                musicService.startMusic();
            }
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            isServiceBound = false;
            Log.d(TAG, "Service disconnected");
        }
    };
    
    // Private constructor for singleton
    private MusicManager() {}
    
    /**
     * Get the singleton instance
     * @return MusicManager instance
     */
    public static synchronized MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }
    
    /**
     * Initialize the MusicManager with the application context
     * @param context Application context
     */
    public void init(Context context) {
        if (applicationContext == null) {
            applicationContext = context.getApplicationContext();
            connectToService();
        }
    }
    
    /**
     * Bind to the background music service
     */
    public void connectToService() {
        if (applicationContext != null && !isServiceBound) {
            Intent intent = new Intent(applicationContext, BackgroundMusicService.class);
            applicationContext.startService(intent);
            applicationContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "Connecting to music service");
        }
    }
    
    /**
     * Unbind from the background music service
     */
    public void disconnectFromService() {
        if (applicationContext != null && isServiceBound) {
            applicationContext.unbindService(serviceConnection);
            isServiceBound = false;
            Log.d(TAG, "Disconnected from music service");
        }
    }
    
    /**
     * Start playing background music
     */
    public void startMusic() {
        if (isServiceBound && musicService != null) {
            musicService.startMusic();
        } else {
            Log.d(TAG, "Cannot start music - service not bound");
        }
    }
    
    /**
     * Pause background music
     */
    public void pauseMusic() {
        if (isServiceBound && musicService != null) {
            musicService.pauseMusic();
        } else {
            Log.d(TAG, "Cannot pause music - service not bound");
        }
    }
    
    /**
     * Enable or disable background music
     * @param enabled True to enable music, false to disable
     */
    public void setMusicEnabled(boolean enabled) {
        // Always update pending state
        pendingMusicEnabled = enabled;
        Log.d(TAG, "Music enabled set to: " + enabled + " (pending: " + !isServiceBound + ")");
        
        if (isServiceBound && musicService != null) {
            musicService.setMusicEnabled(enabled);
        } else {
            Log.d(TAG, "Cannot set music enabled - service not bound, will apply when connected");
            // Will be applied when service connects
        }
    }
    
    /**
     * Check if background music is currently enabled
     * @return True if music is enabled, false otherwise
     */
    public boolean isMusicEnabled() {
        if (isServiceBound && musicService != null) {
            return musicService.isMusicEnabled();
        }
        // Use the pending state if service not yet bound
        return pendingMusicEnabled;
    }
    
    /**
     * Check if music is currently playing
     * @return True if music is playing, false otherwise or if service is not bound
     */
    public boolean isPlaying() {
        if (isServiceBound && musicService != null) {
            return musicService.isPlaying();
        }
        return false;
    }
    
    /**
     * Toggle music state between enabled and disabled
     * @return The new state (true if enabled, false if disabled)
     */
    public boolean toggleMusic() {
        boolean newState = !isMusicEnabled();
        setMusicEnabled(newState);
        return newState;
    }
}