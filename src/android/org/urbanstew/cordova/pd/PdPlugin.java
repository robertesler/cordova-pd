/*
Android plugin for libpd and cordova.
*/
package org.urbanstew.cordova.pd;

import java.io.File;
import java.io.IOException;
import android.util.Log;
import java.io.InputStream;
import android.content.res.Resources;
import android.text.TextUtils;

//You may need to change this to your package name if it differs from this
//You can find it in AndroidManifest.xml most likely
import io.ionic.starter.R;

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
import org.puredata.core.utils.IoUtils;

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
        PdBase.setReceiver(receiver);
        AudioParameters.init(this.cordova.getActivity());
        int srate = Math.max(44100, AudioParameters.suggestSampleRate());
        try {
			PdAudio.initAudio(srate, 0, 2, 1, true);
		}
		catch(IOException e) {
			Log.w("Audio error: ", e);
		}
        Resources res = this.cordova.getActivity().getResources();
        File patchFile = null;
        try {
                /*the following bit is experimental.  If you need to link abstractions it seems like using
                 the IoUtils.extractZipResources() properly links the paths to your externals/abstractions etc.*/
/*
                File dir = this.cordova.getActivity().getFilesDir();
                patchFile = new File(dir, "cordova.pd");//your main patch, you can change the name
                IoUtils.extractZipResource(res.openRawResource(R.raw.cordova), dir, true);//make sure your zip is called cordova.zip
                PdBase.openPatch(patchFile.getAbsolutePath());
 */
            /*if you are using more than one patch or external comment the following lines out 
             and uncomment the above.*/
            InputStream in = res.openRawResource(R.raw.cordova);
            patchFile = IoUtils.extractResource(in, "cordova.pd", this.cordova.getActivity().getCacheDir());
            PdBase.openPatch(patchFile);
             
        } catch (IOException e) {
            Log.e("File error: ", e.toString());
        }

}


@Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {

        super.initialize(cordova, webView);
        this.initPd();
        PdAudio.startAudio(this.cordova.getActivity());
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
            this.sendFloat(args.getString(0), args.getDouble(1));
            return true;
        }
        if(action.equals("sendList"))
        {
             //send message args as a single string
            String messageArgs = args.getString(1);
            String delims = "[ ]+";
            //split string into tokens separated by " "
            String[] tokens = messageArgs.split(delims);
            Object[] toLibpd = new Object[tokens.length];
            //type check then convert if necessary
            for(int i = 0; i < tokens.length; i++)
            {
                if(TextUtils.isDigitsOnly(tokens[i]))
                {
                    toLibpd[i] = Float.parseFloat(tokens[i]);
                }
                else
                {
                    toLibpd[i] = tokens[i];
                }
            }
            
            this.sendList(args.getString(0), toLibpd);
            return true;
        }
        if(action.equals("sendSymbol"))
        {
            this.sendSymbol(args.getString(0), args.getString(1));
            return true;
        }
        if(action.equals("sendMessage"))
        {
            //send message args as a single string
            String messageArgs = args.getString(2);
            String delims = "[ ]+";
            //split string into tokens separated by " "
            String[] tokens = messageArgs.split(delims);
            Object[] toLibpd = new Object[tokens.length];
            //type check then convert if necessary
            for(int i = 0; i < tokens.length; i++)
            {
                if(TextUtils.isDigitsOnly(tokens[i]))
                {
                    toLibpd[i] = Float.parseFloat(tokens[i]);
                }
                else
                {
                    toLibpd[i] = tokens[i];
                }
            }
        
            this.sendMessage(args.getString(0), args.getString(1), toLibpd);
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
        PdBase.sendFloat(receiveName, (float) f);
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
		//JSONArray list;
        try {
            JSONArray list = new JSONArray(theList);
            this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                    list));
		}
		catch(JSONException j) {
			Log.w("Idiot, it don't work: ", j);
		}	

    }

    private void cordovaReceiveMessage(String sendName) {
        PdBase.subscribe(sendName);
        final StringBuffer buffer = new StringBuffer();
        buffer.append(theMessage);
        for(final String s : theMessageArguments) {
            buffer.append(" ");
            buffer.append(s);
        }
        this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
        buffer.toString()));
    }

    private void cordovaReceiveSymbol(String sendName) {
            PdBase.subscribe(sendName);
            this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
            theSymbol));
    }

} //end PdPlugin class

