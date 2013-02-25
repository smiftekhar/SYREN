#include "Messages.h"

generic module CommandInterpreterP()
{
  uses {
    interface DisseminationUpdate<Command>;
    //interface AMSend as SerialAMSender;
    interface Receive as SerialAMReceiver;
    //interface Timer<TMilli> as DelayTimer;
    interface Leds;
 
    command void stop();
    command void report();
  }
}

implementation
{
  typedef struct SerialReqPacket {
    nxle_uint8_t cmd;
    nxle_uint16_t  nodeId;
    nxle_uint8_t arg;
    nxle_uint16_t nack_time;
  } SerialReqPacket;
  
  typedef nx_struct SerialReplyPacket {
    nxle_uint8_t error;
  } SerialReplyPacket;

  message_t serialMsg;
  Command cmd;
/*
  void sendReply(error_t error)
  {
    uint8_t len = sizeof(SerialReplyPacket);
    SerialReplyPacket *reply = (SerialReplyPacket *)call SerialAMSender.getPayload(&serialMsg, len);
    if (reply == NULL) {
      return;
    }
    reply->error = error;
    call SerialAMSender.send(AM_BROADCAST_ADDR, &serialMsg, len);
  }
*/
  event message_t* SerialAMReceiver.receive(message_t* msg, void* payload, uint8_t len)
  {
    
    SerialReqPacket *request = (SerialReqPacket *)payload;
   
   
    memset(&cmd, 0, sizeof(Command));
    
    cmd.type = request->cmd;
    cmd.nodeId = request->nodeId;
    cmd.arg = request->arg;
    cmd.nack_time = request->nack_time;  
    switch (request->cmd) {
    case CMD_STOP:
      call DisseminationUpdate.change(&cmd);
      break;
    case CMD_START_TRANSMISSION:
      call DisseminationUpdate.change(&cmd);
      break;
    case CMD_REPORT:
      call DisseminationUpdate.change(&cmd);
      break;
    case CMD_REPORT_LOCAL:
      call report();
      break;
    case CMD_ERASE_LOG:
      call DisseminationUpdate.change(&cmd);
      break;
    }
    return msg;
  }

  //event void SerialAMSender.sendDone(message_t* msg, error_t error) {}
}
 
