/*
 Copyright 2016 Robert Esler
 
 */


#import "PdPlugin.h"



@implementation PdPlugin

@synthesize floats;
@synthesize bangs;
@synthesize symbols;
@synthesize messages;
@synthesize lists;
@synthesize messageArgs;

//declare our externals if you have them ex:
//extern void my_external_setup(void);


// intialize libpd, change settings via the audioController

- (void)pluginInitialize
{
    
    self.audioController = [[PdAudioController alloc] init] ;
    // You don't have to use these, make sure the Background Audio is turned on for your project
    self.audioController.allowBluetoothA2DP = YES;
    self.audioController.allowAirPlay = YES;
    self.audioController.allowOverrideMutedMicrophoneInterruption = YES;
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
   
    //call the setup() function here
    //my_exteranl_setup();
    [PdBase openFile:@"cordova.pd" path:[[NSBundle mainBundle] bundlePath] ];
     // was [PdBase openFile:@"test.pd" path:[[NSBundle mainBundle] resourcePath]];
    
    [self.audioController setActive:YES];
    self.on = 1;
    // log actual settings
    [self.audioController print];
   
    //allocate NSMutableDictionary classes
    floats = [[NSMutableDictionary alloc] init];
    bangs = [[NSMutableDictionary alloc] init];
    symbols = [[NSMutableDictionary alloc] init];
    messages = [[NSMutableDictionary alloc] init];
    lists = [[NSMutableDictionary alloc] init];
    messageArgs = [[NSMutableDictionary alloc] init];
    NSLog(@"Pd Plugin Initialized!");
    
    //Here is our lock screen "Now Playing" code
    //Add or remove whatever you like.  See code below for how to implement
    MPRemoteCommandCenter *commandCenter = [MPRemoteCommandCenter sharedCommandCenter];

    MPRemoteCommand *pause = [commandCenter pauseCommand];
    [pause setEnabled:YES];
    [pause addTarget:self action:@selector(pauseAudio:)];
    
    MPRemoteCommand *play = [commandCenter playCommand];
    [play setEnabled:YES];
    [play addTarget:self action:@selector(pauseAudio:)];
    
    MPRemoteCommand *toggle = [commandCenter togglePlayPauseCommand];
    [toggle setEnabled:YES];
    [toggle addTarget:self action:@selector(pauseAudio:)];


    MPRemoteCommand *back = [commandCenter previousTrackCommand];
    [back setEnabled:YES];
    [back addTarget:self action:@selector(prevTrack:)];

    MPRemoteCommand *next = [commandCenter nextTrackCommand];
    [next setEnabled:YES];
    [next addTarget:self action:@selector(prevTrack:)];
    
    Class playingInfoCenter = NSClassFromString(@"MPNowPlayingInfoCenter");
    if(playingInfoCenter) {
        NSMutableDictionary *songInfo = [NSMutableDictionary dictionaryWithDictionary:[MPNowPlayingInfoCenter defaultCenter].nowPlayingInfo];
        [songInfo setObject:[NSString stringWithFormat:@"AppName"] forKey:MPMediaItemPropertyTitle];
        [songInfo setObject:[NSString stringWithFormat:@"by Your Name"] forKey:MPMediaItemPropertyArtist];
        [songInfo setObject:[NSNumber numberWithFloat:60000.0f] forKey:MPMediaItemPropertyPlaybackDuration];
        [songInfo setObject:[NSNumber numberWithFloat:0.0f] forKey:MPNowPlayingInfoPropertyElapsedPlaybackTime];
        [songInfo setObject:[NSNumber numberWithFloat:0.0f] forKey:MPNowPlayingInfoPropertyDefaultPlaybackRate];
        [MPNowPlayingInfoCenter defaultCenter].nowPlayingInfo = songInfo;
    }
}

- (BOOL)getAudioStatus {
    return self.audioController.active;
}

- (void)audioOn {
    self.audioController.active = YES;
    NSLog(@"Audio Status is Active");
}

- (void)audioOff {
    self.audioController.active = NO;
    NSLog(@"Audio Status is NOT Active");
}

-(void)pause {
    
    if(self.on)
    {
        //OFF
        // make sure you have a receive called 'on' in your pd patch
        [PdBase sendFloat:0 toReceiver:@"on"];
        
        Class playingInfoCenter = NSClassFromString(@"MPNowPlayingInfoCenter");
        if(playingInfoCenter) {
      
            NSMutableDictionary *songInfo = [NSMutableDictionary dictionaryWithDictionary:[MPNowPlayingInfoCenter defaultCenter].nowPlayingInfo];
            [songInfo setObject:[NSString stringWithFormat:@"MyApp: Pause"] forKey:MPMediaItemPropertyTitle];
            [songInfo setObject:[NSString stringWithFormat:@"by Your Name"] forKey:MPMediaItemPropertyArtist];
            [songInfo setObject:[NSNumber numberWithFloat:60000.0f] forKey:MPMediaItemPropertyPlaybackDuration];
            [songInfo setObject:[NSNumber numberWithFloat:1.0f] forKey:MPNowPlayingInfoPropertyElapsedPlaybackTime];
            [songInfo setObject:[NSNumber numberWithFloat:1.0f] forKey:MPNowPlayingInfoPropertyDefaultPlaybackRate];
            [songInfo setObject:[NSNumber numberWithFloat:0.0f] forKey:MPNowPlayingInfoPropertyPlaybackRate];
            [MPNowPlayingInfoCenter defaultCenter].nowPlayingInfo = songInfo;
        }
        
        //OFF
        self.on = 0;
    }
    else
    {
        Class playingInfoCenter = NSClassFromString(@"MPNowPlayingInfoCenter");
        if(playingInfoCenter) {
            
            NSMutableDictionary *songInfo = [NSMutableDictionary dictionaryWithDictionary:[MPNowPlayingInfoCenter defaultCenter].nowPlayingInfo];
            [songInfo setObject:[NSString stringWithFormat:@"MyApp: Playing"] forKey:MPMediaItemPropertyTitle];
            [songInfo setObject:[NSString stringWithFormat:@"by Your Name"] forKey:MPMediaItemPropertyArtist];
            [songInfo setObject:[NSNumber numberWithFloat:60000.0f] forKey:MPMediaItemPropertyPlaybackDuration];
            [songInfo setObject:[NSNumber numberWithFloat:1.0f] forKey:MPNowPlayingInfoPropertyElapsedPlaybackTime];
            [songInfo setObject:[NSNumber numberWithFloat:1.0f] forKey:MPNowPlayingInfoPropertyDefaultPlaybackRate];
            [songInfo setObject:[NSNumber numberWithFloat:1.0f] forKey:MPNowPlayingInfoPropertyPlaybackRate];
            [MPNowPlayingInfoCenter defaultCenter].nowPlayingInfo = songInfo;
         }
        self.on = 1;
       
        //ON
       // make sure you have a receive called 'on' in your pd patch
        [PdBase sendFloat:1 toReceiver:@"on"];

    }
   
}

-(MPRemoteCommandHandlerStatus) pauseAudio: (MPRemoteCommandHandlerStatus *)event
{
    [self pause];
    return MPRemoteCommandHandlerStatusSuccess;
}

-(MPRemoteCommandHandlerStatus) prevTrack: (MPRemoteCommandHandlerStatus *)event
{
    // make sure you have a receive called 'prev' in your pd patch
    [PdBase sendBangToReceiver: @"freq"];
    return MPRemoteCommandHandlerStatusSuccess;
}

- (void)sendFloat:(CDVInvokedUrlCommand*)command
{
    
    float sendToPd;
    
    NSString* receiveName = [command.arguments objectAtIndex:0];
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
        if([self isStringNumeric:tokens])
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

- (BOOL)isStringNumeric:(NSString *)string {
    NSScanner *scanner = [NSScanner scannerWithString:string];
    double result;
    return [scanner scanDouble:&result] && [scanner isAtEnd];
}

- (void)sendBang: (CDVInvokedUrlCommand *)command
{
    NSString* receiveName = [command.arguments objectAtIndex:0];
    //NSLog(@"sendBang!\n");
    
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
    //NSLog(@"listening to %@\n", theSend);
    CDVPluginResult* pluginResult = nil;

    
    if(bangs[theSend] != nil)
    {
        NSString *s = bangs[theSend];
        BOOL b;
        if([s isEqualToString:@"true"])
            b = YES;
        else
            b = NO;
    
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:b];

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    else
    {
        PdDispatcher* dispatcher = [[PdDispatcher alloc] init];
        [PdBase setDelegate:self];
        [PdBase subscribe:theSend];
        [dispatcher addListener:self forSource:theSend];
        [bangs setObject:@"false" forKey:theSend];
    }
    
    
    
}

- (void) cordovaReceiveFloat:(CDVInvokedUrlCommand *)command {
    
    NSString* theSend = [command.arguments objectAtIndex:0];
   // NSLog(@"listening to %@\n", theSend);
    CDVPluginResult* pluginResult = nil;
    
    if(floats[theSend] != nil)
    {
        NSNumber *n = floats[theSend];
        double d = [n doubleValue];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDouble:d];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    else
    {
        PdDispatcher *dispatcher = [[PdDispatcher alloc] init];
        [PdBase setDelegate:dispatcher];
        [PdBase setDelegate:self];
        [PdBase subscribe:theSend];
        [dispatcher addListener:self forSource:theSend];
        [floats setObject:@0 forKey:theSend];
    }
   
}

- (void)cordovaReceiveSymbol:(CDVInvokedUrlCommand *)command {
    
   
    NSString* theSend = [command.arguments objectAtIndex:0];
    //NSLog(@"listening to %@\n", theSend);
    CDVPluginResult* pluginResult = nil;
    
    if(symbols[theSend] != nil)
    {
        NSString *s = symbols[theSend];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:s];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        
    }
    else
    {
        PdDispatcher* dispatcher = [[PdDispatcher alloc] init];
        [PdBase setDelegate:self];
        [PdBase subscribe:theSend];
        [dispatcher addListener:self forSource:theSend];
        [symbols setObject:@"null" forKey:theSend];
    }
}

- (void)cordovaReceiveList:(CDVInvokedUrlCommand *)command {
    
    NSString* theSend = [command.arguments objectAtIndex:0];
    //NSLog(@"listening to %@\n", theSend);
    CDVPluginResult* pluginResult = nil;
    
    if(lists[theSend] != nil)
    {
        NSArray *s = lists[theSend];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:s];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        
    }
    else
    {
        PdDispatcher* dispatcher = [[PdDispatcher alloc] init];
        [PdBase setDelegate:self];
        [PdBase subscribe:theSend];
        [dispatcher addListener:self forSource:theSend];
        [lists setObject:@"null" forKey:theSend];
    }
    
}

- (void)cordovaReceiveMessage:(CDVInvokedUrlCommand *)command {
    
    NSString* theSend = [command.arguments objectAtIndex:0];
   // NSLog(@"listening to %@\n", theSend);
    CDVPluginResult* pluginResult = nil;
    
    
    if(messages[theSend] != nil)
    {
        NSArray *theMessageWithArguments = messageArgs[theSend];
        
        NSString *args = [theMessageWithArguments componentsJoinedByString:@","];
        
       
        NSString *fullMessage = [NSString stringWithFormat:@"%@ %@", messages[theSend], args];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:fullMessage];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    else
    {
        PdDispatcher* dispatcher = [[PdDispatcher alloc] init];
        [PdBase setDelegate:self];
        [PdBase subscribe:theSend];
        [dispatcher addListener:self forSource:theSend];
        [messages setObject:@"null" forKey:theSend];
        NSArray *ma =  @[@"arg1", @"arg2"];
        [messageArgs setObject:ma forKey:theSend];
    }
   

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
    
    NSNumber *f = [NSNumber numberWithFloat:received];
   
    if(floats[source] != nil)
    {
        [floats setObject:f forKey:source];
	    }
}

- (void)receiveBangFromSource:(NSString *)source {
    
       
        if(bangs[source] != nil)
        {
            [bangs setObject:@"true" forKey:source];
        }

}

- (void)receiveSymbol:(NSString *)symbol fromSource:(NSString *)source {
    
    if(symbols[source] != nil)
    {
        [symbols setObject:symbol forKey:source];
    }
    
   
}

- (void)receiveList:(NSArray *)list fromSource:(NSString *)source {
    
    if(lists[source] != nil)
    {
        [lists setObject:list forKey:source];
    }
}

- (void) receiveMessage:(NSString *)message withArguments:(NSArray *)arguments fromSource:(NSString *)source {
   
    if(messages[source] != nil)
    {
        [messages setObject:message forKey:source];
        [messageArgs setObject:arguments forKey:source];
    }
    
      
    
    
}

@end
