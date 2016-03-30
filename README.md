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
cordova plugin add https://github.com/robertesler/cordova-pd.git
```
## Instructions for iOS
1) Make sure to add the plugin to your config.xml in your root folder by adding:
```
<plugin name="PdPlugin" value="PdPlugin" />
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
I have updated the libpd Objective-C wrapper to include Automatic Reference Counting (ARC) with 
this plugin. 

You can of course build your project manually cloning libpd yourself.  You would just need the 
PdPlugin.m, PdPlugin.h files and cordova-pd.js file that comes with this distribution.

## Using Custom Externals
To add any custom externals you need to follow the libpd protocol here:
[libpd iOS wiki](https://github.com/libpd/pd-for-ios/wiki/ios) 

## Using your patches
Your main patch needs to be called cordova.pd and located in the root /www folder.  You can use
other abstractions or folders, look at the [declare] object.

## Android Version

Coming soon, in development.

## Using ng-cordova-pd

The Angular JS wrapper for this plugin allows you to use cordova-pd in Ionic
projects and any other projects that use Angular JS.  

To use the plugin add ng-cordova-pd.js to your root /www/jsand  to your index.html:
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
