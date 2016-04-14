/*
Does this shit get saved?
*/
package org.urbanstew.cordova.pd;

import java.io.File;
import java.io.IOException;
import android.util.Log;
import android.content.Context;
import android.app.Activity;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.core.PdReceiver;
import org.puredata.core.PdBase;

public class PdPlugin extends CordovaPlugin {

    private boolean theBang = false;
    private double theFloat = 0;
    private String[] theList;
    private String theSymbol;
    private String theMessage;
    private String[] theMessageArguments;
    private String[] theListArguments;
	private CallbackContext callback;
    //This should handle data from Pd
    private final PdReceiver receiver = new PdReceiver() {

@Override public void print(String s) {
			Log.i("From Pd: ", s);
}
		
@Override
    public void receiveBang(String source) {
        theBang = true;
    }

@Override
    public void receiveFloat(String source, float x) {
            theFloat = x;
    }

@Override
    public void receiveList(String source, Object... args) {
        theList = new String[args.length];
        int i = 0;
        for (Object arg: args) {
            theList[i++] = arg.toString();
        }
    }

@Override
    public void receiveMessage(String source, String message, Object... args) {
        theMessage = message;
        theMessageArguments = new String[args.length];
        int i = 0;
        for (Object arg: args) {
            theMessageArguments[i++] = arg.toString();
        }
    }

@Override
    public void receiveSymbol(String source, String symbol) {
        theSymbol = symbol;
    }

};//end receiver

	
/* this is how we initialize Pd */
private void initPd() {
        Log.i("initPd", " shit.");
        PdBase.setReceiver(receiver);
//        AudioParameters.init(this);
        int srate = Math.max(44100, AudioParameters.suggestSampleRate());
        try {
			PdAudio.initAudio(srate, 0, 2, 1, true);
		}
		catch(IOException e) {
			Log.w("Audio error: ", e);	
		}
		Activity activity = new Activity();
		Context context = activity.getApplicationContext();
        File dir = context.getFilesDir();
        File patchFile = new File(dir, "www/cordova.pd");
        try {
			PdBase.openPatch(patchFile.getAbsolutePath());
		}
		catch(IOException e) {
			Log.w("Patch file error: ", e);	
		}
}


@Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.initPd();
        PdBase.computeAudio(true);
    }

@Override
public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
    throws JSONException {
        callback = callbackContext;
        if(action.equals("sendBang"))
        {
            this.sendBang(args.getString(0));
            return true;
        }
        if(action.equals("sendFloat"))
        {
            this.sendFloat(args.getString(0), args.getDouble(0));
            return true;
        }
        if(action.equals("sendList"))
        {
            this.sendList(args.getString(0), args.getJSONObject(0));
            return true;
        }
        if(action.equals("sendSymbol"))
        {
            this.sendSymbol(args.getString(0), args.getString(1));
            return true;
        }
        if(action.equals("sendMessage"))
        {
            this.sendMessage(args.getString(0), args.getString(1), args.getJSONObject(0));
            return true;
        }

        if(action.equals("cordovaReceiveBang"))
        {
            this.cordovaReceiveBang(args.getString(0));
            return true;
        }

        if(action.equals("cordovaReceiveFloat"))
        {
            this.cordovaReceiveFloat(args.getString(0));
            return true;
        }

        if(action.equals("cordovaReceiveList"))
        {
            this.cordovaReceiveList(args.getString(0));
            return true;
        }

        if(action.equals("cordovaReceiveMessage"))
        {
            this.cordovaReceiveMessage(args.getString(0));
            return true;
        }

        if(action.equals("cordovaReceiveSymbol"))
        {
            this.cordovaReceiveSymbol(args.getString(0));
            return true;
        }

        return false;
    }//end execute


@Override
public void onDestroy() {
        // make sure to release all resources
        PdAudio.stopAudio();
        PdAudio.release();
        PdBase.release();
    }

	
    //Send to Pd
    
    private void sendBang(String receiveName) {
        PdBase.sendBang(receiveName);
    }
    
    private void sendFloat(String receiveName, double f) {
        PdBase.sendFloat(receiveName, (float)f);
    }
    
    private void sendList(String receiveName, Object ... args) {
        PdBase.sendList(receiveName, args);
    }
    
    private void sendSymbol(String receiveName, String sym) {
        PdBase.sendSymbol(receiveName, sym);
    }
    
    private void sendMessage(String receiveName, String msg, Object... args) {
        PdBase.sendMessage(receiveName, msg, args);
    }

    private void cordovaReceiveBang(String sendName) {
        PdBase.subscribe(sendName);
        this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
            theBang));
    }

    private void cordovaReceiveFloat(String sendName) {
        PdBase.subscribe(sendName);
        this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                                                                   (float)theFloat));
    }

    private void cordovaReceiveList(String sendName) {
        PdBase.subscribe(sendName);
		JSONArray list = null;
		try {
			list = new JSONArray(theList);
		}
		catch(JSONException j) {
			Log.w("Idiot, it don't work: ", j);
		}	
        this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
        list));
    }

    private void cordovaReceiveMessage(String sendName) {
        PdBase.subscribe(sendName);
        this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
        theMessage));
    }

    private void cordovaReceiveSymbol(String sendName) {
            PdBase.subscribe(sendName);
            this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
            theSymbol));
    }

} //end PdPlugin class

