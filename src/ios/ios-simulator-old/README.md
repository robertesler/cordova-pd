# Why is this here?
This is a libpd archive compiled for the x86_64 architecture.
This may be the architecture for the simulators. Link the libpd-ios-simulator.a in your Build Phases.
Make sure to not include this when building for devices or the App Store.
Though it is recommended to test on devices if you can. 
Use the main libpd-ios.a for devices, it is a FAT universal binary.  
