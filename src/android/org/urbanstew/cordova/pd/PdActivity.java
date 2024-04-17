/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package org.urbanstew.cordova.pd;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdPreferences;

import java.io.IOException;

//You will need to change this to match your package name, Check AndroidManifest.xml or android.json
import io.cordova.hellocordova.MainActivity;
import io.cordova.hellocordova.R;


public class PdActivity extends MainActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final String TAG = "MyApp";
    private PdPluginService pdService = null;
    private Toast toast = null;

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast == null) {
                    toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
                }
                toast.setText(TAG + ": " + msg);
                toast.show();
            }
        });
    }

    private final ServiceConnection pdConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            pdService = ((PdPluginService.PdBinder)service).getService();
            // initPd();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // this method will never be called
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        AudioParameters.init(this);
        PdPreferences.initPreferences(getApplicationContext());
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
        bindService(new Intent(this, PdPluginService.class), pdConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        requestAudioPermission();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanup();
        stopAudio();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (pdService.isRunning()) {
            startAudio();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
            }
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startAudio();
        } else {
            toast("Can't start audio - microphone permission required!");
        }
    }
    private void startAudio() {
        String name = getResources().getString(R.string.app_name);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        try {
            int srate = Math.max(44100, AudioParameters.suggestSampleRate());
            pdService.initAudio(srate, -1, -1, -1);   // negative values will be replaced with defaults/preferences
            pdService.startAudio(intent, R.drawable.icon, "Title of App", "This is what I do.");
        } catch (IOException e) {
            toast(e.toString());
        }
    }

    private void stopAudio() {
        pdService.stopAudio();
    }

    private void cleanup() {
        try {
            unbindService(pdConnection);
        } catch (IllegalArgumentException e) {
            // already unbound
            pdService = null;
        }
    }

    private boolean recordAudioPermissionGranted() {
        int permissionResult =
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return permissionResult == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
    }

}
