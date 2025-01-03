# Introduction
This plugin is meant to unite the libpd and cordova frameworks so app developers can 
also have access to Pure Data, via libpd, for audio and music synthesis.  

I assume the following:
1) You already know how to use Pure Data.
2) You are familiar with the Cordova framework and how to use and install plugins.
3) You have at least some basic knowledge of programming in different languages like Obj-C, Java and web languages.

## Copyright
cordova-pd is copyrighted by Robert Esler and part of the non-profit urbanSTEW, 2024.

## Example
Here is an example of an app that uses this library:
1) iOS, https://apps.apple.com/vn/app/troooba/id6480323332
2) Android, https://play.google.com/store/apps/details?id=org.urbanstew.troooba
   
## Installing the library

To clone the repository to the root plugins folder try:
```
cordova plugin add org.urbanstew.cordova.pd 
OR
cordova plugin add https://github.com/robertesler/cordova-pd.git
```
## Instructions for iOS
Right now you can only use the library on an iOS device.  The iOS simulator seems to have changed how it handles static libraries.  I'm looking into it, but will not get to it anytime soon.  I've left the old static lib (libpd-ios-simulator.a) in the distribution in case it is useful. 

1) Check the config.xml to make sure org.urbanstew.cordova.pd has been added for example (cordova should do this automatically:
```
<plugin name="org.urbanstew.cordova.pd" spec="0.0.12" />
```
2) Then either add the iOS platform or try:
```
cordova prepare ios
```

3) Open the Xcode project in Xcode. Under the "Build Phases" add a new "Headers Phase"
 You can do this by pressing the '+' icon.

4) Add all the .h files in the plugin root folder ../org.urbanstew.cordova.pd/src/ios/headers/
  to the "Project" headers phase.

5) Build your application using Xcode.  The cordova build option does not currently work when
  linking the static library.

6) Add your pd patch(es) to the project's Resource folder in Xcode. (right-click on the Resources folder and "Add Files...").  The patch should be in the root /www folder.

7) If you want to test in the simulator see the REAME.md file in the x86_64 folder.
   
8) If you want your app to run audio in the background make sure to edit your Signing and Capabilities, then Background Modes.  This library includes a basic lock screen notification.

##NOTE:

 This project uses dependencies that are maintained by other developers.  These include:

libpd 
```
git clone https://github.com/libpd/libpd.git
```
pure data (source)
```
git clone git://git.code.sf.net/p/pure-data/pure-data
```

You can of course build your project manually cloning libpd yourself.  You would just need the 
PdPlugin.m, PdPlugin.h files and cordova-pd.js file that comes with this distribution.

## Using Custom Externals
To add any custom externals you need to follow the libpd protocol here:
[libpd iOS wiki](https://github.com/libpd/pd-for-ios/wiki/ios) 

## Using your patches in iOS
Your main patch needs to be called cordova.pd and located in the root /www folder.  You can use
other abstractions or folders, look at the [declare] object.  Make sure you added the patch to your Xcode project, see above.

## Passing and Receiving Data 
In general you should try to only pass data to Pd and not rely too much on receiving data from Pd.  
This is in part because it can be slow, and unreliable.  But if you do need to get data from Pd then
make sure it is not data that needs to be handled quickly. 

Messages and Lists:  Right now the API for these is still not fully tested. Since libpd type checks every argument or every element of a message or list it is very slow.

## Instructions for Android 

The Android version is now stable.  It currently comes with an Android Service that allows the application to run audio in the background with a customizable lock screen notification.

After adding the platform and plugin:

1) Check the config.xml to make sure org.urbanstew.cordova.pd has been added for example (cordova should do this automatically:
```
<plugin name="org.urbanstew.cordova.pd" spec="0.0.12" />
```
2) Copy the drawables in the /src/android/res/* to your platform/android/app/src/main/res folder.  I've tried to automate this, but it never seems to work as expected.
3) Then either add the android platform or try:
```
cordova prepare android
```
The plugin looks for a patch named cordova.pd in the res/raw/ directory.  When the plugin installs
it will copy the demo cordova.pd patch to this directory.  Just make sure to keep track of this if 
you plan to release both iOS and Android.  

If you want cordova to copy your .pd file from /www you can try uncommenting the line #116 in plugin.xml:
```
<hook type="before_prepare" src="scripts/copyPdFile.js" />
```
This should automatically update your patch for the Android build when you execute:
```
cordova prepare android
```
If you need to use abstractions or externals look at the commented out code in the PdPlugin.java and alter it accordingly.  It seems to work if the patches are .zip then extracted by Android.

You will need to edit the three Java files, line: 
```
import io.cordova.hellocordova.R
```
to reflect the package name of your app.  I found the package name in the /platforms/android/android.json.  Change the name respectively and add the .R at the end. 

## Using ng-cordova-pd (deprecated)

The Angular JS wrapper for this plugin allows you to use cordova-pd in Ionic
projects and any other projects that use Angular JS.  Currently, the plugin only supports Angular JS version 1, not the newer version 2 or Angular.io.

To use the plugin add ng-cordova-pd.js to your root /www/js and  to your index.html:
```
<script src="js/ng-cordova-pd.js"></script>
```
Add ```'ngPd'``` to your app.js modules, and inject ```$puredata``` into your 
controller. 

Then in your controllers follow this style:
```
$scope.test = function () {
            
                $ionicPlatform.ready(function() {
                        $puredata.sendBang("start").then(function (result) {
                            console.log("bang: " + result);
                        }, function (err) {
                        // error
                            console.log("Error: " + err);
                        });
                });//end platform ready
            };//end $scope.test
```
Make sure to wrap your plugin function calls in ```$ionicPlatform.ready()``` and 
to use the ```.then()``` method.  This is especially if you are trying to receive
data from Pd.  

## Typescript and Ionic 2 (deprecated)
  It appears that Ionic 2 is relying heavily on typescript.  To interface with this plugin directly in typescript use this syntax:
  ```
(<any>window).plugins.pd.sendBang("receiveName")
```
Just replace the 'sendBang' portion with the appropriate method.

## API

## Sending Data to Pd

Send a bang to a receiver
```
window.plugins.pd.sendBang("receiveName")
```
Send a float to a receiver
```
window.plugins.pd.sendFloat("receiveName", float)
```
Send a symbol to a receiver
```
window.plugins.pd.sendSymbol("receiveName", symbol)
```
Send a message to a receiver, make sure your arguments are a single string '1 2 3'
```
window.plugins.pd.sendMessage("receiveName", message, argumentList)
```
Send a list to a receiver, make sure your list is a single string '1 2 3'
```
window.plugins.pd.sendList("receiveName", list)
```

## Receiving Data from Pd

Receive a bang from a sender (returns a bool set as true)
```
window.plugins.pd.receiveBang("sendName", success, fail)

//example code for most receives from libpd:
window.plugins.pd.receiveBang("sendName",
function success(e) {
 console.log(e);//this is the return value from Cordova
},
function fail() {
 console.log("It failed.");
});
```
Receive a float from a sender (returns a float)
```
window.plugins.pd.receiveFloat("sendName", success, fail)
```
Receive a symbol from a sender (returns a String)
```
window.plugins.pd.receiveSymbol("sendName", success, fail)
```
Receive a message from a sender (returns a String)
```
window.plugins.pd.receiveMessage("sendName", success, fail)
```
Receive a List from a sender (returns a JSON)
```
window.plugins.pd.receiveList("sendName", success, fail)
```
