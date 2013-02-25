 
#include "SYREN.h"

configuration SYRENAppC {

    provides{
      interface SYREN;
  }

}
implementation {
  components SYRENC as App, LedsC;
  components ActiveMessageC;
  components CC2420PacketC;
  components new AMSenderC(AM_HELLO_MSG) as SendHelloMsg;
  components new AMReceiverC(AM_HELLO_MSG) as ReceiveHelloMsg;
  components new AMSenderC(AM_DATA_MSG) as SendDataMsg;
  components new AMReceiverC(AM_DATA_MSG) as ReceiveDataMsg;
  components new TimerMilliC() as HelloTimerC;
  
  components new TimerMilliC() as BackoffTimerC;
  components new TimerMilliC() as StatsTimerC;
  components new TimerMilliC() as NackTimerC;
  

  SYREN = App;

  App.ReceiveHelloMsg -> ReceiveHelloMsg;  
  App.SendHelloMsg -> SendHelloMsg;
  App.AMPacket -> SendHelloMsg;

  App.SendDataMsg -> SendDataMsg;
  App.ReceiveDataMsg -> ReceiveDataMsg;

  App.Leds -> LedsC;
  App.HelloTimer -> HelloTimerC;
  
  App.BackoffTimer -> BackoffTimerC;
  App.StatsTimer -> StatsTimerC;
  App.NackTimer -> NackTimerC;
  App.CC2420Packet -> CC2420PacketC;

  


}
