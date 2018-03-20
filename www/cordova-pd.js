/*
 
 Copyright 2018 Robert Esler
 
 These functions should send or recieve all data types from Pd (bang, float, message, symbol, and lists).
 There may be some latency, which may be improved at a later date.  If that is an issue then consider using
 the libpd native APIs.
 
 */
"use strict";

//var exec = require('cordova/exec');
//var cordova = require('cordova');
function Pd() {
    
}

//In plain JS, use this syntax window.plugins.pd.sendFloat("receiveName", float); 
//If using Typescript use this syntax (<any>window).plugins.pd.sendFloat("receiveName", float); 

Pd.prototype.sendFloat = function (receiveName, float, success, fail) {
    return cordova.exec(success, fail, "PdPlugin", "sendFloat", [receiveName, float]);
};
    
Pd.prototype.sendMessage = function (receiveName, message, list, success, fail) {
    return cordova.exec(success, fail, "PdPlugin", "sendMessage", [receiveName, message, list]);
};
    
Pd.prototype.sendBang = function (receiveName, success, fail) {
    return cordova.exec(success, fail, "PdPlugin", "sendBang", [receiveName]);
    
};
    
Pd.prototype.sendSymbol = function (receiveName, symbol, success, fail) {
    return cordova.exec(success, fail, "PdPlugin", "sendSymbol", [receiveName, symbol]);
};
    
Pd.prototype.sendList = function (receiveName, list, success, fail) {
    return cordova.exec(success, fail, "PdPlugin", "sendList", [receiveName, list]);
};
    
Pd.prototype.receiveBang = function (sendName, success, fail) {
    cordova.exec(success, fail, "PdPlugin", "cordovaReceiveBang", [sendName]);
    
};
    
Pd.prototype.receiveFloat = function (sendName, success, fail) {
    cordova.exec(success, fail, "PdPlugin", "cordovaReceiveFloat", [sendName]);
    
};
    
Pd.prototype.receiveSymbol = function (sendName, success, fail) {
    cordova.exec(success, fail, "PdPlugin", "cordovaReceiveSymbol", [sendName]);
    
};
    
Pd.prototype.receiveList = function (sendName, success, fail) {
    cordova.exec(success, fail, "PdPlugin", "cordovaReceiveList", [sendName]);
    
};
    
Pd.prototype.receiveMessage = function (sendName, success, fail) {
    cordova.exec(success, fail, "PdPlugin", "cordovaReceiveMessage", [sendName]);
    
};
    
Pd.prototype.echo = function(str, callback, fail) {
    cordova.exec(callback, fail, "PdPlugin", "test", [str]);
};

//module.exports = new Pd();


// This is old javascript, leaving it incase anything breaks.
Pd.install = function () {
    if (!window.plugins) {
        window.plugins = {};
    }
    
    window.plugins.pd = new Pd();
    return window.plugins.pd;
};

cordova.addConstructor(Pd.install);
    

