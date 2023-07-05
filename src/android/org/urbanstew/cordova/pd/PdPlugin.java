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
import java.util.HashMap;
import java.util.Map;

//You may need to change this to your package name if it differs from this
//You can find it in AndroidManifest.xml or android.json most likely
import io.cordova.hellocordova.R;


import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.core.PdReceiver;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

public class PdPlugin extends CordovaPlugin {

    private Map<String, Boolean> bangs = new HashMap<>();
    private Map<String, Float> floats = new HashMap<>();
    private Map<String, String[]> lists = new HashMap<>();
    private Map<String, String> symbols = new HashMap<>();
    private Map<String, String> messages = new HashMap<>();
    private Map<String, String[]> messageArguments = new HashMap<>();
    private String[] theList;
    private String[] theMessageArguments;
	private CallbackContext callback;
	private boolean noBackgroundAudio = false;
	private float on = 0;


    //This should handle data from Pd
    private final PdReceiver receiver = new PdReceiver() {

@Override public void print(String s) {
			Log.e("From Pd: ", s);
}
		
@Override
    public void receiveBang(String source) {
         if (bangs.containsKey(source))
         {
            bangs.put(source, true);
         }
}

@Override
    public void receiveFloat(String source, float x) {

        if(floats.containsKey(source))
        {
            floats.put(source, x);
        }

    }

@Override
    public void receiveList(String source, Object... args) {

        if(lists.containsKey(source)) {
            theList = new String[args.length];
            int i = 0;
            for (Object arg : args) {
                theList[i++] = arg.toString();
            }
            lists.put(source, theList);
        }
    }

@Override
    public void receiveMessage(String source, String message, Object... args) {

        if(messages.containsKey(source)) {

            messages.put(source, message);
            theMessageArguments = new String[args.length];
            int i = 0;
            for (Object arg : args) {

                 theMessageArguments[i++] = arg.toString();
            }
            messageArguments.put(source, theMessageArguments);
        }
    }

@Override
    public void receiveSymbol(String source, String symbol) {
        if(symbols.containsKey(source))
        {
           symbols.put(source, symbol);
        }

    }

};//end receiver

@Override
public void onStop() {
    Log.d("PdPlugin", "OnStop()");
}

@Override
public void onPause(boolean multitasking) {
    Log.d("PdPlugin", "OnPause() " + multitasking);
}

    /* this is how we initialize Pd */
private void initPd() {
        Log.d("PdPlugin","initPd()");
        PdBase.setReceiver(receiver);

        //This will initialize the audio thread if you are not using the background service
        if(noBackgroundAudio) {
            AudioParameters.init(this.cordova.getActivity());
            int srate = Math.max(44100, AudioParameters.suggestSampleRate());
            try {
                PdAudio.initAudio(srate, 0, 2, 1, true);
                PdAudio.startAudio(this.cordova.getActivity());
            } catch (IOException e) {
                Log.w("Audio error: ", e);
            }
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
        initPd();

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
                 /*
                isDigitsOnly seems fail if there is '.' so floats seems to return false.
                So we just take the first character, it should be a number or a '.'
                Can't imagine many Pd messages that have a '.' at the beginning, but keep
                that in mind here.
                */

                char a = tokens[i].charAt(0);
                String s = Character.toString(a);

                if(TextUtils.isDigitsOnly(s) || s.equals("."))
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
        if(action.equals("echo"))
        {
            this.echo(args.getString(0));
            return true;
        }

        return false;
    }//end execute


@Override
public void onDestroy() {
        // make sure to release all resources
        if(noBackgroundAudio) {
            PdAudio.stopAudio();
            PdAudio.release();
        }
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
        if(bangs.containsKey(sendName))
        {
            this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                    bangs.get(sendName)));
        }
        else
        {
            PdBase.subscribe(sendName);
            bangs.put(sendName, false);
        }

    }

    private void cordovaReceiveFloat(String sendName) {

        if(floats.containsKey(sendName))
        {
            this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                    floats.get(sendName)));
        }
        else
        {
            PdBase.subscribe(sendName);
            floats.put(sendName, 0.f);
        }
}

    private void cordovaReceiveList(String sendName) {

		//JSONArray list;
        if(lists.containsKey(sendName)) {
            try {
                JSONArray list = new JSONArray(lists.get(sendName));
                this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                        list));
            } catch (JSONException j) {
                Log.w("cordovaRecieveList JSON Error: ", j);
            }
        }
        else
        {
            PdBase.subscribe(sendName);
            String [] dummy = new String[100];//if you have more than 100 lists in your Pd patch, God help you!
            lists.put(sendName, dummy);
        }
    }

    private void cordovaReceiveMessage(String sendName) {

        if(messages.containsKey(sendName)) {
            final StringBuffer buffer = new StringBuffer();
            buffer.append(messages.get(sendName));
            theMessageArguments = messageArguments.get(sendName);
            for (final String s : theMessageArguments) {
                buffer.append(" ");
                buffer.append(s);
            }
            this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                    buffer.toString()));
        }
        else
        {
            PdBase.subscribe(sendName);
            messages.put(sendName, "");
            String [] args = new String[100];
            messageArguments.put(sendName, args);
        }
    }

    private void cordovaReceiveSymbol(String sendName) {

            if(symbols.containsKey(sendName)) {
                this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                        symbols.get(sendName)));
            }
            else
            {
                PdBase.subscribe(sendName);
                symbols.put(sendName, "");
            }
    }

    private void echo(String e){
        this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                e));
       // Log.e("From PdPlugin", e);
    }
    

} //end PdPlugin class

