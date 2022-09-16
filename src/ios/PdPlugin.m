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
   
    
    [PdBase openFile:@"cordova.pd" path:[[NSBundle mainBundle] bundlePath] ];
     // was [PdBase openFile:@"test.pd" path:[[NSBundle mainBundle] resourcePath]];
    
    [self.audioController setActive:YES];
    
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
