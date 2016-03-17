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

}

@property (nonatomic, retain) PdAudioController *audioController;
@property (nonatomic, assign) double theFloat;
@property (nonatomic, assign) BOOL theBang;
@property (nonatomic, assign) NSString *theString;
@property (nonatomic, assign) NSArray *theList;
@property (nonatomic, assign) NSString *theMessage;
@property (nonatomic, assign) NSArray *theArguments; //from theMessage
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
