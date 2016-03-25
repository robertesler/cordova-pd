# Installing the library

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
PdPlugin.m, PdPlugin.h files and cordova-pd.js files that come with distribution.

## Using Custom Externals
To add any custom externals you need to follow the libpd protocol here:
[libpd iOS wiki](https://github.com/libpd/pd-for-ios/wiki/ios) 

## Android Version

Coming soon, in development.
