# Introduction
This plugin is meant to unite the libpd and cordova frameworks so app developers can 
also have the power of Pure Data for audio and music synthesis.  

I assume the following:
1) You already know how to use Pure Data.
2) You are familiar with the Cordova framework and how to use and install plugins.
3) You have at least some basic knowledge of programming in different languages like Obj-C, Java and web languages.
 
## Installing the library

To clone the repository to the root plugins folder try:
```
cordova plugin add org.urbanstew.cordova.pd 
OR
cordova plugin add https://github.com/robertesler/cordova-pd.git
```
## Instructions for iOS
1) Check the config.xml to make sure org.urbanstew.cordova.pd has been added for example (cordova should do this automatically:
```
<plugin name="org.urbanstew.cordova.pd" spec="0.0.2-dev" />
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

#NOTE:

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
other abstractions or folders, look at the [declare] object.

## Passing and Receiving Data 
In general you should try to only pass data to Pd and not rely too much on receiving data from Pd.  
This is in part because it can be slow, and unreliable.  But if you do need to get data from Pd then
make sure it is not data that needs to be handled quickly. 

Lists: I have been using Pd for the better part of 15 years and I have never used a formal list.  
That being said, libpd has a sendList and receiveList API.  It’s weird at best.  I can’t get iOS to 
“understand” my lists.  Android sends them fine but in JSON format.  I don’t know what the 
difference would be to send a message like “list my stuff, and more stuff”.  So, my point is
don’t use lists, use messages.

## Instructions for Android 

The Android version is still in development.  It will run as of now but still in a testing phase. 
Check in regularly for any updates.  

1) Check the config.xml to make sure org.urbanstew.cordova.pd has been added for example (cordova should do this automatically:
```
<plugin name="org.urbanstew.cordova.pd" spec="0.0.2-dev" />
```
2) Then either add the android platform or try:
```
cordova prepare android
```
The plugin looks for a patch named cordova.pd in the res/raw/ directory.  When the plugin installs
it will copy the demo cordova.pd patch to this directory.  Just make sure to keep track of this if 
you plan to release both iOS and Android.  

You may need to edit the PdPlugin.java file line: 
```
import com.ionicframework.starter.R
```
to reflect the package name of your app.  I found the package name in the /platforms/android/AndroidManifest.xml.  Change the name respectively and add the .R at the end. 

## Using ng-cordova-pd

The Angular JS wrapper for this plugin allows you to use cordova-pd in Ionic
projects and any other projects that use Angular JS.  Currently, the plugin only supports Angular JS version 1, not the newer version 2 or Angular.io.
However, if you have a good example of using this plugin with Angular.io I will accept pull requests.  

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

## API

###Sending Data to Pd

Send a bang to a receiver
```
$puredata.sendBang("receiveName")
```
Send a float to a receiver
```
$puredata.sendFloat("receiveName", float)
```
Send a symbol to a receiver
```
$puredata.sendSymbol("receiveName", symbol)
```
Send a message to a receiver
```
$puredata.sendMessage("receiveName", message, argumentList)
```
Send a list to a receiver (not currently working in iOS, see note above.)
```
$puredata.sendList("receiveName", list)
```

###Receiving Data from Pd

Receive a bang from a sender (returns a bool set as true)
```
$puredata.receiveBang("sendName")
```
Receive a float from a sender (returns a float)
```
$puredata.receiveFloat("sendName")
```
Receive a symbol from a sender (returns a String)
```
$puredata.receiveSymbol("sendName")
```
Receive a message from a sender (returns a String)
```
$puredata.receiveMessage("sendName")
```
Receive a List to a sender (returns a JSON)
```
$puredata.receiveList("sendName")
```
