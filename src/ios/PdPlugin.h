/*
 Copyright 2016 Robert Esler
 
 */

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>
#import "PdBase.h"
#import "PdAudioController.h"
#import "PdDispatcher.h"
#import "PdFile.h"

@interface PdPlugin : CDVPlugin<PdListener, PdReceiverDelegate> {
PdDispatcher *dispatcher;
}

@property (nonatomic, retain) PdAudioController *audioController;

@property (nonatomic, copy) NSMutableDictionary *floats;
@property (nonatomic, copy) NSMutableDictionary *bangs;
@property (nonatomic, copy) NSMutableDictionary *symbols;
@property (nonatomic, copy) NSMutableDictionary *lists;
@property (nonatomic, copy) NSMutableDictionary *messages;
@property (nonatomic, copy) NSMutableDictionary *messageArgs;
- (void)sendFloat:(CDVInvokedUrlCommand*)command;
- (void)sendMessage: (CDVInvokedUrlCommand* )command;
- (void)sendBang: (CDVInvokedUrlCommand *)command;
- (void)sendSymbol: (CDVInvokedUrlCommand *)command;
- (void)sendList: (CDVInvokedUrlCommand *)command;
- (void)cordovaReceiveBang:(CDVInvokedUrlCommand*)command;
- (void)cordovaReceiveFloat:(CDVInvokedUrlCommand*)command;
- (void)cordovaReceiveSymbol:(CDVInvokedUrlCommand *)command;
- (void)cordovaReceiveList:(CDVInvokedUrlCommand *)command;
- (void)cordovaReceiveMessage:(CDVInvokedUrlCommand *)command;
- (void)test:(CDVInvokedUrlCommand*)command;
@end
