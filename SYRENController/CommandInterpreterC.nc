
generic configuration CommandInterpreterC(am_id_t AMId)
{
   uses interface DisseminationUpdate<Command>;
}

implementation
{
  //components new SerialAMSenderC(AMId);
  components new SerialAMReceiverC(AMId);  
  //components new TimerMilliC() as Timer;
  components LedsC;
  components new CommandInterpreterP();
  
  //CommandInterpreterP.SerialAMSender -> SerialAMSenderC;
  CommandInterpreterP.SerialAMReceiver -> SerialAMReceiverC;
  CommandInterpreterP.Leds -> LedsC;
  CommandInterpreterP.DisseminationUpdate = DisseminationUpdate;
  
  components TestAMC;
  CommandInterpreterP.stop -> TestAMC.stop;
  CommandInterpreterP.report -> TestAMC.report;
}
