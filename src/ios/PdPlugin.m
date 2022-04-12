/*
 Copyright 2016 Robert Esler
 
 */


#import "PdPlugin.h"



@implementation PdPlugin

@synthesize theFloat;
@synthesize theBang;
@synthesize theString;
@synthesize theList;
@synthesize theMessage;
@synthesize theArguments;

// intialize libpd, change settings via the audioController

- (void)pluginInitialize
{
    
    self.audioController = [[PdAudioController alloc] init] ;
    PdAudioStatus status = [self.audioController configurePlaybackWithSampleRate:44100
                                                                  numberChannels:2
                                                                    inputEnabled:NO
                                                                   mixingEnabled:NO];
    if (status == PdAudioError) {
        NSLog(@"Error! Could not configure PdAudioController");
    } else if (status == PdAudioPropertyChanged) {
        NSLog(@"Warning: some of the audio parameters were not accceptable.");
    } else {
        NSLog(@"Audio Configuration successful.");
    }
    //This prints the Pd Log to the Xcode console view
    dispatcher = [[PdDispatcher alloc] init];
    [PdBase setDelegate:dispatcher];
    
    [PdBase openFile:@"cordova.pd" path:[[NSBundle mainBundle] bundlePath] ];
     // was [PdBase openFile:@"test.pd" path:[[NSBundle mainBundle] resourcePath]];
    
    [self.audioController setActive:YES];
    
    // log actual settings
    [self.audioController print];
    NSLog(@"Pd Plugin Initialized!");
}

- (void)sendFloat:(CDVInvokedUrlCommand*)command
{
    
   // CDVPluginResult* pdFloat;
    float sendToPd;
    
    NSString* receiveName = [command.arguments objectAtIndex:0];
    //NSNumber* value = [command.arguments objectAtIndex:1 withDefault:[NSNumber numberWithFloat:0.0]];
    
    NSNumber* value = [command.arguments objectAtIndex:1];
    sendToPd = value.floatValue;
    
    [PdBase sendFloat:sendToPd toReceiver:receiveName];
    
    
}

- (void)sendMessage: (CDVInvokedUrlCommand* )command
{
    NSString* receiveName = [command.arguments objectAtIndex:0];
    NSString* message = [command.arguments objectAtIndex:1];
    NSString* args = [command.arguments objectAtIndex:2];
    //separate the arguments with whitespace
    NSArray* argList = [args componentsSeparatedByString:@" "];
    NSMutableArray* list = [[NSMutableArray alloc] init];//list we send to libpd
    NSCharacterSet* notDigits = [[NSCharacterSet decimalDigitCharacterSet] invertedSet];
    
    //type check each argument
    for (NSString* tokens in argList)
    {
        if ([tokens rangeOfCharacterFromSet:notDigits].location == NSNotFound)
        {
            //String is probably a number
            float f = [tokens floatValue];
            NSNumber* n = @(f);
            //  NSLog(@"%@", n);
            [list addObject:n];
        }
        else //nope it's a string
            [list addObject:tokens];
    }
    
    [PdBase sendMessage:message withArguments:list toReceiver:receiveName];
    
}

- (void)sendBang: (CDVInvokedUrlCommand *)command
{
    NSString* receiveName = [command.arguments objectAtIndex:0];
    NSLog(@"sendBang!\n");
    
    [PdBase sendBangToReceiver:receiveName];
}

- (void)sendSymbol: (CDVInvokedUrlCommand *)command {
    
    NSString* receiveName = [command.arguments objectAtIndex:0];
    NSString* symbol = [command.arguments objectAtIndex:1];
    
    [PdBase sendSymbol:symbol toReceiver:receiveName];
    
    
}

- (void)sendList:(CDVInvokedUrlCommand *)command {
    
    NSString* receiveName = [command.arguments objectAtIndex:0];
    NSString* args = [command.arguments objectAtIndex:1];
    //separate the arguments with whitespace
    NSArray* argList = [args componentsSeparatedByString:@" "];
    NSMutableArray* list = [[NSMutableArray alloc] init];//list we send to libpd
    NSCharacterSet* notDigits = [[NSCharacterSet decimalDigitCharacterSet] invertedSet];
    
    //type check each argument
    for (NSString* tokens in argList)
    {
        if ([tokens rangeOfCharacterFromSet:notDigits].location == NSNotFound)
        {
            //String is probably a number
            float f = [tokens floatValue];
            NSNumber* n = @(f);
            //  NSLog(@"%@", n);
            [list addObject:n];
        }
        else //nope it's a string
            [list addObject:tokens];
    }
    
    [PdBase sendList:list toReceiver:receiveName];
    
}

//Receive Data from Pd...

- (void) cordovaReceiveBang:(CDVInvokedUrlCommand *)command {
    
    NSString* theSend = [command.arguments objectAtIndex:0];
    NSLog(@"listening to %@\n", theSend);
    CDVPluginResult* pluginResult = nil;

    PdDispatcher* dispatcher = [[PdDispatcher alloc] init];
    [PdBase setDelegate:self];
    [PdBase subscribe:theSend];
    [dispatcher addListener:self forSource:theSend];
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:theBang];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
   
    
}

- (void) cordovaReceiveFloat:(CDVInvokedUrlCommand *)command {
    
    NSString* theSend = [command.arguments objectAtIndex:0];
    NSLog(@"listening to %@\n", theSend);
    CDVPluginResult* pluginResult = nil;
    
     PdDispatcher* dispatcher = [[PdDispatcher alloc] init];
    [PdBase setDelegate:self];
    [PdBase subscribe:theSend];
    [dispatcher addListener:self forSource:theSend];
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDouble:theFloat];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}

- (void)cordovaReceiveSymbol:(CDVInvokedUrlCommand *)command {
    NSString* theSend = [command.arguments objectAtIndex:0];
    NSLog(@"listening to %@\n", theSend);
    CDVPluginResult* pluginResult = nil;
    
    PdDispatcher* dispatcher = [[PdDispatcher alloc] init];
    [PdBase setDelegate:self];
    [PdBase subscribe:theSend];
    [dispatcher addListener:self forSource:theSend];
   
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:theString];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)cordovaReceiveList:(CDVInvokedUrlCommand *)command {
    NSString* theSend = [command.arguments objectAtIndex:0];
    NSLog(@"listening to %@\n", theSend);
    CDVPluginResult* pluginResult = nil;
    
    PdDispatcher* dispatcher = [[PdDispatcher alloc] init];
    [PdBase setDelegate:self];
    [PdBase subscribe:theSend];
    [dispatcher addListener:self forSource:theSend];
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:theList];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

    
}

- (void)cordovaReceiveMessage:(CDVInvokedUrlCommand *)command {
    NSString* theSend = [command.arguments objectAtIndex:0];
    NSLog(@"listening to %@\n", theSend);
    CDVPluginResult* pluginResult = nil;
    
    PdDispatcher* dispatcher = [[PdDispatcher alloc] init];
    [PdBase setDelegate:self];
    [PdBase subscribe:theSend];
    [dispatcher addListener:self forSource:theSend];
    
    NSArray *theMessageWithArguments;
    theMessageWithArguments = [NSArray arrayWithObjects:theMessage, theArguments, nil];
    NSString *fullMessage = [[theMessageWithArguments valueForKey:@"description"] componentsJoinedByString:@" "];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:fullMessage];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}

- (void)echo:(CDVInvokedUrlCommand *)command {
    NSString* text = [command.arguments objectAtIndex:0];
    NSLog(@"print: %@\n", text);
    CDVPluginResult* pluginResult = nil;
    
   
    
    NSArray *theMessageWithArguments;
    theMessageWithArguments = [NSArray arrayWithObjects:theMessage, theArguments, nil];
    NSString *fullMessage = [[theMessageWithArguments valueForKey:@"description"] componentsJoinedByString:@" "];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:fullMessage];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}

// this was just a test using the original Cordova format
- (void)test:(CDVInvokedUrlCommand*)command
{
    NSLog(@"Yo!\n");
    CDVPluginResult* pluginResult = nil;
    NSString* myarg = [command.arguments objectAtIndex:0];
    
    if (myarg != nil) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"test is good"];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Arg was null"];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)receiveFloat:(float)received fromSource:(NSString *)source {
    
    theFloat = (double)received;
}

- (void)receiveBangFromSource:(NSString *)source {
    
    theBang = true;
}

- (void)receiveSymbol:(NSString *)symbol fromSource:(NSString *)source {
    
    theString = symbol;
}

- (void)receiveList:(NSArray *)list fromSource:(NSString *)source {
    
    theList = list;
}

- (void) receiveMessage:(NSString *)message withArguments:(NSArray *)arguments fromSource:(NSString *)source {
   
    theMessage = message;
    theArguments = arguments;
}

@end
