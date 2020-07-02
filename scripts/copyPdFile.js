const fs = require('fs');
var path = require('path');
var process = require('process')

//This script will attempt to copy your global .pd file to the android resource folder
//If it doesn't work, submit a ticket to https://github.com/robertesler/cordova-pd/issues
module.exports = function(ctx) {
    // Make sure android platform is part of build
   	 if (!ctx.opts.platforms.includes('android')) return;

    const pdFile = path.join(process.cwd(), '/www/cordova.pd');
    const resPath = path.join(process.cwd(), '/platforms/android/app/src/main/res/raw/cordova.pd');
    //  console.log(`Copied ${pdFile} to ${resPath}`);
 	 


// cordova.pd will be created or overwritten by default.
fs.copyFile(pdFile, resPath, fs.constants.COPYFILE_FICLONE, (err) => {
    if (err) throw err;
    console.log(`${pdFile} was copied to ${resPath}`);
});

console.log(`Copied ${pdFile} to ${resPath}`);
};

