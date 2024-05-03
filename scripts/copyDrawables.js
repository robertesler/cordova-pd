const fs = require('fs');
var path = require('path');
var process = require('process');



//This script will attempt to copy the temp drawables to the android resource folder
//If it doesn't work, submit a ticket to https://github.com/robertesler/cordova-pd/issues
module.exports = function(ctx) {
    // Make sure android platform is part of build
   	 if (!ctx.opts.plugin.platform.includes('android')) return;

   	 var files = [
         "src/android/res/drawable-anydpi/ic_action_mic.xml",
         "src/android/res/drawable-anydpi/ic_action_name.xml",
         "src/android/res/drawable-anydpi/ic_action_stop.xml",
         "src/android/res/drawable-anydpi/ic_action_play.xml",

         "src/android/res/drawable-hdpi/ic_action_mic.png",
         "src/android/res/drawable-hdpi/ic_action_name.png",
         "src/android/res/drawable-hdpi/ic_action_stop.png",
         "src/android/res/drawable-hdpi/ic_action_play.png",

         "src/android/res/drawable-mdpi/ic_action_mic.png",
         "src/android/res/drawable-mdpi/ic_action_name.png",
         "src/android/res/drawable-mdpi/ic_action_stop.png",
         "src/android/res/drawable-mdpi/ic_action_play.png",

         "src/android/res/drawable-xhdpi/ic_action_mic.png",
         "src/android/res/drawable-xhdpi/ic_action_name.png",
         "src/android/res/drawable-xhdpi/ic_action_stop.png",
         "src/android/res/drawable-xhdpi/ic_action_play.png",

         "src/android/res/drawable-xxhdpi/ic_action_mic.png",
         "src/android/res/drawable-xxhdpi/ic_action_name.png",
         "src/android/res/drawable-xxhdpi/ic_action_stop.png",
         "src/android/res/drawable-xxhdpi/ic_action_play.png"
     ];
    console.log("Creating drawable directories.");
    var anydpi = path.join(process.cwd(), 'platforms/android/app/src/main/res/drawable-anydpi/');
    var hdpi = path.join(process.cwd(), 'platforms/android/app/src/main/res/drawable-hdpi/');
    var mdpi = path.join(process.cwd(), 'platforms/android/app/src/main/res/drawable-mdpi/');
    var xhdpi = path.join(process.cwd(), 'platforms/android/app/src/main/res/drawable-xhdpi/');
    var xxhdpi = path.join(process.cwd(), 'platforms/android/app/src/main/res/drawable-xxhdpi/');
    var res = [anydpi, hdpi, mdpi, xhdpi, xxhdpi];

    //Make our directories
    for(var i = 0; i < res.length; i++)
    {
        fs.mkdir(res[i], { recursive: false }, (err) => {
            if (err) throw err;
        });
    }
    //Copy our files...the path keeps getting mangled, so I'll put this in the plugin.xml
    /*
    var j = 0;
    for(var i = 0; i < files.length; i++)
    {
        if(j = res.length)  j = 0;
        var current = path.join(process.cwd(), files[i]);
        console.log(current);
        fs.copyFile(current, res[j], fs.constants.COPYFILE_FICLONE, (err) => {
            if (err) throw err;
            console.log(`${file[i]} was copied to ${res[j]}`);
        });
        j++;
    }
    */
};