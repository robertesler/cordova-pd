/*
 
 Copyright 2016 Robert Esler
 
 These functions should send or recieve all data types from Pd (bang, float, message, symbol, and lists).
 There may be some latency, which may be improved at a later date.  If that is an issue then consider using 
 the libpd native APIs.
 
 */


var pd = {
    
 
sendFloat: function (receiveName, float, success, fail) {
    return cordova.exec(success, fail, "PdPlugin", "sendFloat", [receiveName, float]);
},
   
sendMessage: function (receiveName, message, list, success, fail) {
    return cordova.exec(success, fail, "PdPlugin", "sendMessage", [receiveName, message, list]);
},

sendBang: function (receiveName, success) {
    return cordova.exec(success, null, "PdPlugin", "sendBang", [receiveName]);

},

sendSymbol: function (receiveName, symbol, success, fail) {
    return cordova.exec(success, fail, "PdPlugin", "sendSymbol", [receiveName, symbol]);
},
    
sendList: function (receiveName, list, success, fail) {
    return cordova.exec(success, fail, "PdPlugin", "sendList", [receiveName, list]);
},
    
receiveBang: function (sendName, success) {
     cordova.exec(success, function(err) {success('Nothing to echo.');}, "PdPlugin", "cordovaReceiveBang", [sendName]);
    
},
    
receiveFloat: function (sendName, success) {
     cordova.exec(success, function(err) {success('Nothing to echo.');}, "PdPlugin", "cordovaReceiveFloat", [sendName]);
    
},
    
receiveSymbol: function (sendName, success) {
    cordova.exec(success, function(err) {success('Nothing to echo.');}, "PdPlugin", "cordovaReceiveSymbol", [sendName]);
    
},
    
receiveList: function (sendName, success) {
    cordova.exec(success, function(err) {success('Nothing to echo.');}, "PdPlugin", "cordovaReceiveList", [sendName]);
    
},
    
receiveMessage: function (sendName, success) {
    cordova.exec(success, function(err) {success('Nothing to echo.');}, "PdPlugin", "cordovaReceiveMessage", [sendName]);
    
},
    
echo: function(str, callback) {
       cordova.exec(callback, function(err) {callback('Nothing to echo.');}, "PdPlugin", "test", [str]);
}

    
};
