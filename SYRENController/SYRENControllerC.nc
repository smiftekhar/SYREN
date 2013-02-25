 
#include "Timer.h"
#include <UserButton.h>
#include "Messages.h"
#include "SYREN.h"

module SYRENControllerC {
  uses {
    interface Leds;
    interface Boot;
    
    
    interface SplitControl;
    

    interface AMSend as SerialAMSend;

    
    interface Get<button_state_t>;
    interface Notify<button_state_t>;
    
    interface DisseminationValue<Command>;
    interface StdControl as DisseminationStdControl;
    
    interface BlockRead;
    interface BlockWrite;
 
    interface SYREN;  

  }
  
  provides {
    command void stop();
    command void report();
  }

}
implementation {

  message_t packet;
  message_t serialPacket;
  unsigned char nodeState = 0;  
  bool locked=FALSE,locked1=FALSE;
  bool transmitter = FALSE;
  unsigned short msgId = 1;
  uint32_t numOfReports = 0;
  unsigned short nbrList[MAX_NUM_OF_NBRS];
  bool doReport = FALSE;
  int testCounter = 0;
  int numberOfPkts = 0;
  uint32_t lastWrittenaddr = 0;
  int MAX_NUM_OF_REPORTS = 0;
  int transmissionPower = 2;
  int indicator = 0;  
  uint8_t threshold;
  
  typedef struct AppData {
    nxle_uint16_t source;
    nxle_uint16_t id;
  } AppData;
  
  typedef struct LoggedData {
    nxle_uint16_t source;
    nxle_uint8_t data[MAX_SERIAL_PKT_SIZE];
  } LoggedData;

  unsigned char log[sizeof(struct nbr_struct)*MAX_NUM_OF_NBRS + sizeof(struct cprp_struct)*MAX_NUM_OF_NBRS + sizeof(struct stats_struct)];
  
  int offset = 0;
  
  void eraseLog(){
    
    if (call BlockWrite.erase() != SUCCESS) {
      // Handle error.
    }
    call Leds.led0On();
  }  
  
  int findNbrPosition(nx_uint16_t nbrId){
    int i = 0;
  	for (; i < MAX_NUM_OF_NBRS; i++){
  		if ((nbrList[i] == nbrId) || (nbrList[i] == 0))
  			return i;
  	}
  	return -1;
  }
  void reset(){
    indicator = 0;
    offset = 0;
    msgId = 1;
    numOfReports = 0;
    numberOfPkts = 0;
    locked = FALSE;
    locked1 = FALSE;
    nodeState = 0;
    doReport = FALSE;
    
  }
  void netprog_reboot() {
    WDTCTL = WDT_ARST_1_9; 
    while(1);
  }

  event void Boot.booted() {
    call SplitControl.start();
  }
  

  event void SplitControl.startDone(error_t err) {
    if (err == SUCCESS) {
      call DisseminationStdControl.start();
      call Notify.enable();
      reset();
      transmitter = TRUE;
      
      //call SerialAMControl.start();
    }
  }

  event void SplitControl.stopDone(error_t err) {
  }
  
  event void Notify.notify( button_state_t state ) {
    if ( state == BUTTON_PRESSED ) {
      call Leds.led2On();
    } else if ( state == BUTTON_RELEASED ) {
      nodeState = 1;
      call Leds.led2Off();
      call SYREN.setThreshold(threshold);
      
      call SYREN.setTransmissionPower(transmissionPower);
      call SYREN.startHello();
    }
  }
  
  
  event void DisseminationValue.changed()
  {
    const Command *cmd = call DisseminationValue.get();
//    printf("cmd: %d uidhash: 0x%lx imgNum: %d size: %u\n", cmd->type, cmd->uidhash, cmd->imgNum, cmd->size);
    switch (cmd->type) {
      case CMD_STOP:
	if ((cmd->nodeId == TOS_NODE_ID || cmd->nodeId == 0xFFFF) && nodeState == 1){
	  //call Leds.led2Toggle();
	  call stop();
	}
	break;
      case CMD_START_TRANSMISSION:
	
	if ((cmd->nodeId == TOS_NODE_ID || cmd->nodeId == 0xFFFF) && nodeState >= 0){
	  call Leds.led0Off();
	  call Leds.led1Off();
	  call Leds.led2Off();
	  nodeState = 1;
	  threshold = (0xFF & cmd->arg);
	  call SYREN.setThreshold(threshold);
	  call SYREN.setTransmissionPower(transmissionPower);
	  
	  call SYREN.startHello();
	}
	break;
      case CMD_REPORT:
	if ((cmd->nodeId == TOS_NODE_ID || cmd->nodeId == 0xFFFF) && (nodeState == 0)){
	  MAX_NUM_OF_REPORTS = cmd->arg;
	  call report();
	}
	break;
      case CMD_ERASE_LOG:
	if (cmd->nodeId == TOS_NODE_ID || cmd->nodeId == 0xFFFF){
	  transmissionPower = (0xFF & cmd->arg);
	  
	  eraseLog();
	}
	break;
    }
  }
  
  

  void sendReport(){
    LoggedData *out;
    int i = 0,j=0,len = 0;
    call Leds.led1Off();
    out = (LoggedData *)call SerialAMSend.getPayload(&serialPacket, sizeof(LoggedData));
    out->source = TOS_NODE_ID;
    len = (sizeof(struct nbr_struct)*MAX_NUM_OF_NBRS+ sizeof(struct cprp_struct)*MAX_NUM_OF_NBRS + sizeof(struct stats_struct) - offset) >= MAX_SERIAL_PKT_SIZE ? MAX_SERIAL_PKT_SIZE : (sizeof(struct nbr_struct)*MAX_NUM_OF_NBRS+ sizeof(struct cprp_struct)*MAX_NUM_OF_NBRS + sizeof(struct stats_struct) - offset);
    for (i = offset,j= 0; i < offset+len; i++)
      out->data[j++] = log[i];
    
    if (call SerialAMSend.send(AM_BROADCAST_ADDR, &serialPacket, sizeof(LoggedData)) == SUCCESS) {
      locked1 = TRUE;
    }
  }
  
  event void SerialAMSend.sendDone(message_t* bufPtr, error_t error) {
    if (&serialPacket == bufPtr) {
      locked1 = FALSE;
      if(doReport){
	offset = offset + MAX_SERIAL_PKT_SIZE;
	if (offset < sizeof(struct nbr_struct)*MAX_NUM_OF_NBRS+ sizeof(struct cprp_struct)*MAX_NUM_OF_NBRS + sizeof(struct stats_struct))
	  sendReport();
	else{
	  offset = 0;
	  numOfReports++;
	  if(numOfReports < MAX_NUM_OF_REPORTS)
	    call report();
	  else{
	    call BlockWrite.sync();
	  }
	}
      }
    }
  }
  
  command void stop(){
    call SYREN.stopHello();
    call Leds.led0Off();
    
    indicator = 1;
    call BlockWrite.write(lastWrittenaddr,(unsigned char*)(call SYREN.getLinkStats()),sizeof(struct nbr_struct)*MAX_NUM_OF_NBRS);
    
  }
  
  
  command void report(){
    call Leds.led1Off();
    doReport = TRUE;
    call BlockRead.read(numOfReports*(sizeof(struct nbr_struct)*MAX_NUM_OF_NBRS + sizeof(struct cprp_struct)*MAX_NUM_OF_NBRS + sizeof(struct stats_struct)),&log,sizeof(struct nbr_struct)*MAX_NUM_OF_NBRS + sizeof(struct cprp_struct)*MAX_NUM_OF_NBRS + sizeof(struct stats_struct));
  }

  event void BlockRead.readDone(storage_addr_t addr, void* buf, storage_len_t len,
		      error_t error){
    call Leds.led1On();	
    sendReport();
  }

  event void BlockRead.computeCrcDone(storage_addr_t addr, storage_len_t len,
			    uint16_t crc, error_t error){
			      
  }

  event void BlockWrite.writeDone(storage_addr_t addr, void* buf, storage_len_t len,
		      error_t error){
    
    if(error != SUCCESS){
      call Leds.led0On();
      return;
    }
    if(indicator == 1){
      indicator = 2;
      lastWrittenaddr += sizeof(struct nbr_struct)*MAX_NUM_OF_NBRS;
      call BlockWrite.write(lastWrittenaddr,(unsigned char*)(call SYREN.getCprpStats()),sizeof(struct cprp_struct)*MAX_NUM_OF_NBRS);
    }
    else if (indicator == 2){
      indicator = 3;
      lastWrittenaddr += sizeof(struct cprp_struct)*MAX_NUM_OF_NBRS;
      
      call BlockWrite.write(lastWrittenaddr,(struct stats_struct *)(call SYREN.getStats()),sizeof(struct stats_struct));
    }

    else if (indicator == 3){
      lastWrittenaddr += sizeof(struct stats_struct);
      call Leds.led1On();
      reset();
    }
  }

  event void BlockWrite.eraseDone(error_t error){
    call Leds.led0Off();
  }

  event void BlockWrite.syncDone(error_t error){
    call Leds.led2On();
  }
}
  




