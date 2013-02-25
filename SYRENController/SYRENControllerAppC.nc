#include "StorageVolumes.h"

configuration SYRENControllerAppC {}
implementation {
  components MainC, SYRENControllerC as App, LedsC;
  components ActiveMessageC;
  //components SerialActiveMessageC;
  components SerialStarterC;
  components new SerialAMSenderC(REPORTER);
  
  
  
  components UserButtonC;
  
  components new DisseminatorC(Command, KEY);
  components DisseminationC;
  components new BlockStorageC(VOLUME_BLOCKTEST);
  
  components SYRENAppC;
  
  components PrintfC;
  
  App.Boot -> MainC.Boot;

  App.SplitControl -> ActiveMessageC;
  App.Leds -> LedsC;

  
  
  App.SerialAMSend -> SerialAMSenderC;
  //App.SerialPacket -> SerialActiveMessageC;
 

  
  App.Get -> UserButtonC;
  App.Notify -> UserButtonC;
  
  
  App.DisseminationStdControl -> DisseminationC;
  
  
  
  #ifndef BASESTATION
    App.DisseminationValue -> DisseminatorC;
  #endif
  
  #ifdef BASESTATION
    components new CommandInterpreterC(COMMAND_INTERPRETER);
    CommandInterpreterC.DisseminationUpdate -> DisseminatorC;
  #endif
    
    //App.BlockRead -> BlockStorageC.BlockRead;
    //App.BlockWrite -> BlockStorageC.BlockWrite;
    App.BlockRead -> BlockStorageC;
    App.BlockWrite -> BlockStorageC;

    App.SYREN -> SYRENAppC;
}


