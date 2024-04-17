/**
 *
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 */

package org.urbanstew.cordova.pd;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;

//You will need to change this to match your package name, check AndroidManifest.xml or android.json
import io.cordova.hellocordova.R;

/**
 *
 * PdPluginService allows applications to run Pure Data as a (local) service, with foreground priority if desired.
 * This is the generally the same code as PdService, but updated for Android S+ (31 or higher).
 *
 * original author Peter Brinkmann (peter.brinkmann@gmail.com)
 * updates by Robert Esler
 *
 */
public class PdPluginService extends Service {

    public class PdBinder extends Binder {
        public PdPluginService getService() {
            return PdPluginService.this;
        }
    }

    private static final String TAG = "CordovaPdPluginService";
    private static final int NOTIFICATION_ID = 1;
    private static boolean abstractionsInstalled = false;
    private final PdBinder binder = new PdBinder();
    private boolean hasForeground = false;

    private volatile int sampleRate = 0;
    private volatile int inputChannels = 0;
    private volatile int outputChannels = 0;
    private volatile float bufferSizeMillis = 0.0f;
    private MediaSessionCompat mediaSessionCompat;


    /**
     * @return the current audio buffer size in milliseconds (approximate value;
     * the exact value is a multiple of the Pure Data tick size (64 samples))
     */
    public float getBufferSizeMillis() {
        return bufferSizeMillis;
    }

    /**
     * @return number of input channels
     */
    public int getInputChannels() {
        return inputChannels;
    }

    /**
     * @return number of output channels
     */
    public int getOutputChannels() {
        return outputChannels;
    }

    /**
     * @return current sample rate
     */
    public int getSampleRate() {
        return sampleRate;
    }


    /**
     * Initialize Pure Data and audio thread
     *
     * @param srate   sample rate
     * @param nic     number of input channels
     * @param noc     number of output channels
     * @param millis  audio buffer size in milliseconds; for Java audio only (Android 2.2 or earlier),
     *                will be ignored by OpenSL components
     * @throws IOException  if the audio parameters are not supported by the device
     */
    public synchronized void initAudio(int srate, int nic, int noc, float millis) throws IOException {
        stopForeground();
        Resources res = getResources();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (srate < 0) {
            String s = prefs.getString(res.getString(R.string.pref_key_srate), null);
            if (s != null) {
                srate = Integer.parseInt(s);
            } else {
                srate = PdBase.suggestSampleRate();
                if (srate < 0) {
                    srate = AudioParameters.suggestSampleRate();
                }
            }
        }
        if (nic < 0) {
            String s = prefs.getString(res.getString(R.string.pref_key_inchannels), null);
            if (s != null) {
                nic = Integer.parseInt(s);
            } else {
                nic = PdBase.suggestInputChannels();
                if (nic < 0) {
                    nic = AudioParameters.suggestInputChannels();
                }
            }
        }
        if (noc < 0) {
            String s = prefs.getString(res.getString(R.string.pref_key_outchannels), null);
            if (s != null) {
                noc = Integer.parseInt(s);
            } else {
                noc = PdBase.suggestOutputChannels();
                if (noc < 0) {
                    noc = AudioParameters.suggestOutputChannels();
                }
            }
        }
        if (millis < 0) {
            millis = 50.0f;  // conservative choice
        }
        int tpb = (int) (0.001f * millis * srate / PdBase.blockSize()) + 1;
        PdAudio.initAudio(srate, nic, noc, tpb, true);
        sampleRate = srate;
        inputChannels = nic;
        outputChannels = noc;
        bufferSizeMillis = millis;

    }

    /*
    * Bluetooth Media Buttons Callback
    * */
    private MediaSessionCompat.Callback mediaSessionCompatCallBack = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            super.onPlay();
            //Toast.makeText(getApplication(), "Play Button is pressed!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPause() {
            super.onPause();
           // Toast.makeText(getApplication(), "Pause Button is pressed!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
           // Toast.makeText(getApplication(), "Next Button is pressed!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
           // Toast.makeText(getApplication(), "Previous Button is pressed!", Toast.LENGTH_SHORT).show();
        }

        public void onFastForward() {
            super.onFastForward();
            //Toast.makeText(getApplication(), "Fast Forward Button is pressed!", Toast.LENGTH_SHORT).show();
        }

        public void onRewind() {
            super.onRewind();
            //Toast.makeText(getApplication(), "Rewind Button is pressed!", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onStop() {
            super.onStop();
           // Toast.makeText(getApplication(), "Stop Button is pressed!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            String intentAction = mediaButtonEvent.getAction();

            if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction))
            {
                KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

                if (event != null)
                {
                    int action = event.getAction();
                    if (action == KeyEvent.ACTION_DOWN) {
                        switch (event.getKeyCode()) {
                            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                                // code for fast forward
                                //Toast.makeText(getApplication(),"Fast Forward is pressed!",Toast.LENGTH_SHORT).show();
                                return true;
                            case KeyEvent.KEYCODE_MEDIA_NEXT:
                                // code for next
                                //Toast.makeText(getApplication(),"Next is pressed!",Toast.LENGTH_SHORT).show();
                                return true;
                            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                                // code for play/pause
                                //Toast.makeText(getApplication(),"Play Button is pressed!",Toast.LENGTH_SHORT).show();
                                return true;
                            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                                // code for previous
                               // Toast.makeText(getApplication(),"Previous is pressed!",Toast.LENGTH_SHORT).show();
                                return true;
                            case KeyEvent.KEYCODE_MEDIA_REWIND:
                                // code for rewind
                                // Toast.makeText(getApplication(),"Rewind is pressed!",Toast.LENGTH_SHORT).show();
                                return true;
                            case KeyEvent.KEYCODE_MEDIA_STOP:
                                // code for stop
                                //Toast.makeText(getApplication(),"----Stop Button is pressed!---",Toast.LENGTH_SHORT).show();
                                return true;

                        }
                        return false;

                    }
                    if (action == KeyEvent.ACTION_UP) {

                    }
                }
            }

            return super.onMediaButtonEvent(mediaButtonEvent);
        }

    };

    /**
     * Start the audio thread without foreground privileges
     */
    public synchronized void startAudio() {
        PdAudio.startAudio(this);
    }

    /**
     * Start the audio thread with foreground privileges
     *
     * @param intent       intent to be triggered when the user selects the notification of the service
     * @param icon         icon representing the notification
     * @param title        title of the notification
     * @param description  description of the notification
     */
    public synchronized void startAudio(Intent intent, int icon, String title, String description) {
        startAudio(makeNotification(intent, icon, title, description));
    }

    /**
     * Start the audio thread with foreground privileges
     *
     * @param notification notification to display
     */
    public synchronized void startAudio(Notification notification) {
        startForeground(notification);
        PdAudio.startAudio(this);
    }

    /**
     * Stop the audio thread
     */
    public synchronized void stopAudio() {
        PdAudio.stopAudio();
        stopForeground();
    }

    /**
     * @return true if and only if the audio thread is running
     */
    public synchronized boolean isRunning() {
        return PdAudio.isRunning();
    }

    /**
     * Releases all resources
     */
    public synchronized void release() {
        stopAudio();
        PdAudio.release();
        PdBase.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        release();
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AudioParameters.init(this);

        if (!abstractionsInstalled) {
            try {
                File dir = getFilesDir();
                IoUtils.extractZipResource(getResources().openRawResource(R.raw.extra_abs), dir, true);
                abstractionsInstalled = true;
                PdBase.addToSearchPath(dir.getAbsolutePath());
                PdBase.addToSearchPath(getApplicationInfo().nativeLibraryDir);  // Location of standard externals.
            } catch (IOException e) {
                Log.e(TAG, "unable to unpack abstractions:" + e.toString());
            }
        }

        mediaSessionCompat = new MediaSessionCompat(this, "MEDIA");
        mediaSessionCompat.setCallback(mediaSessionCompatCallBack);
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        PlaybackStateCompat.Builder mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                PlaybackStateCompat.ACTION_FAST_FORWARD |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mediaSessionCompat.setPlaybackState(mStateBuilder.build());
        mediaSessionCompat.setActive(true);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
        mediaSessionCompat.release();
    }

    private Notification makeNotification(Intent intent, int icon, String title, String description) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel =
                    new NotificationChannel(TAG, TAG, NotificationManager.IMPORTANCE_LOW);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        //PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        //This is now necessary for Android 31 or higher.
        PendingIntent pi = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return new NotificationCompat.Builder(PdPluginService.this, TAG)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(description)
                .setContentIntent(mediaSessionCompat.getController().getSessionActivity())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                /* These are possible actions you could add to your notification GUI
                   Feel free to change or remove these.  Your app may crash if you
                   don't have corresponding strings in the strings.xml or icons in
                   the res/drawable* folders
                */
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_action_name, "Something",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(),
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)))

                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_action_stop, "Stop",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(),
                                PlaybackStateCompat.ACTION_STOP)))

                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_action_play, "Play",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(),
                                PlaybackStateCompat.ACTION_FAST_FORWARD)))

                // Take advantage of MediaStyle features
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken())
                        .setShowActionsInCompactView(0,1,2))//the 0,1,2 refer to the above .addAction() calls


                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .build();
    }

    private void startForeground(Notification notification) {
        stopForeground();
        startForeground(NOTIFICATION_ID, notification);
        hasForeground = true;
    }

    private void stopForeground() {
        if (hasForeground) {
            stopForeground(true);
            hasForeground = false;
        }
    }
}
