 
#include "Timer.h"
#include "NetworkCoding.c"


module SYRENC @safe(){
  uses {
    interface Leds;
    interface Receive as ReceiveHelloMsg;
    interface AMSend as SendHelloMsg;
    interface AMPacket;
    
    interface Receive as ReceiveDataMsg;
    interface AMSend as SendDataMsg;

    interface Timer<TMilli> as HelloTimer;
    interface Timer<TMilli> as BackoffTimer;
    interface Timer<TMilli> as StatsTimer;
    interface Timer<TMilli> as NackTimer;

    interface CC2420Packet;

    //interface LocalTime<TMilli> as LocalTime;
  }
  provides{
    interface SYREN;
  }
}



implementation {

  uint32_t currentTime = 0;
  nxle_uint16_t nackTime;
  char shortTxRequired = 0;
  char shortTxCounter = 0;
  int txPower  = 2;
  char shortTransmissions = 0;
  char allowedToTx = 0;
  bool allCovered = FALSE;  
  unsigned char lastLoadedBatch = 255;
  unsigned char lastWrittenBatch = 255;
  unsigned short packetnumber;
  float threshold = 0.85;
  int rootNode = 1;	
  int dataPacketIdOffset = 0;
  message_t packet;
  unsigned char currentBatchId = 255;
  int lastNumOfRows = 0;
  unsigned short numOfTransmissions = 0;
  //radio_sense_msg_t lastPacket;
  bool locked = FALSE;
  unsigned char helloCounter = 0;
  unsigned short msgCounter = 0;
  unsigned int helloWindow = 0;
  bool isNack = FALSE;
  struct hello_struct helloStats[MAX_NUM_OF_NBRS];	
  struct cprp_struct cumCprpStats[MAX_NUM_OF_NBRS];
  struct nbr_struct cumLinkStats[MAX_NUM_OF_NBRS];
  struct message_expected_struct expectedMsgId[MAX_NUM_OF_NBRS];
  struct cprp_struct cprpStats[MAX_NUM_OF_NBRS];
  struct nbr_struct linkStats[MAX_NUM_OF_NBRS];   	     
  struct nbr_struct coverageProb[MAX_NUM_OF_NBRS];
  unsigned int uncoveredNbrs[MAX_NUM_OF_NBRS];
  unsigned short upstreamNbrs[MAX_NUM_OF_NBRS];
  struct stats_struct stats;
  
  unsigned char *rcvdPayloads[MAX_PACKETS_PER_BATCH];
  unsigned char *BPtr[MAX_PACKETS_PER_BATCH];
  unsigned short xid[MAX_PACKETS_PER_BATCH];
  int togenerate = 0;
  int upstreamNbrCounter = 0;
  unsigned short rcvdPayloadsCounter = 0;
  int lostPacket = 0;
  int findNbrPositionForNextMsg(unsigned int source);  
  int findSenderPositionForHelloStats(unsigned int source);
  int findNbrPositionForLinkStats(struct nbr_struct *lStats,unsigned int source);
  int findSenderPositionForCprpStats(struct cprp_struct *cStats, unsigned int source);
  int findNbrPositionForCprpStats(struct cprp_struct *cStats,unsigned int cprpSender, unsigned int source);
  int findNbrPositionInCoverage(int source);
  int getBackoffTimerValue();  
  int hasUncoveredNbrs();  
  void sendDataPacket(uint8_t type);
  void printCprpStats(struct nbr_struct *lStats, struct cprp_struct *cStats);
  void printUncoveredNbrs();
  void resetSYREN();
  

  command void SYREN.setTransmissionPower(int tPower){
    txPower = tPower;
  }
  command void SYREN.resetHello() {
    int i = 0;
    unsigned short myseed = 8090+TOS_NODE_ID;

    shortTxRequired = 0;
    shortTxCounter = 0;

    shortTransmissions = 0;
    allowedToTx = 0;
    msgCounter = 0;
    helloCounter = 0;
    helloWindow = 0;
    currentTime = 0;
    lastLoadedBatch = 255;
    lastWrittenBatch = 255;
    currentBatchId = 255;
    togenerate = 0;
    upstreamNbrCounter = 0;
    rcvdPayloadsCounter = 0;
    lostPacket = 0;
    locked = FALSE;
    dataPacketIdOffset = 0;
  
    lastNumOfRows = 0;
    numOfTransmissions = 0;
    isNack = FALSE;
    packetnumber = random_number(&myseed);
    memset(helloStats,0,sizeof(struct hello_struct)*MAX_NUM_OF_NBRS);
    
    memset(expectedMsgId,0,sizeof(struct message_expected_struct)*MAX_NUM_OF_NBRS);
    memset(cprpStats,0,sizeof(struct cprp_struct)*MAX_NUM_OF_NBRS);
    memset(linkStats,0,sizeof(struct nbr_struct)*MAX_NUM_OF_NBRS);

    memset(cumCprpStats,0,sizeof(struct cprp_struct)*MAX_NUM_OF_NBRS);
    memset(cumLinkStats,0,sizeof(struct nbr_struct)*MAX_NUM_OF_NBRS);

    memset(coverageProb,0,sizeof(struct nbr_struct)*MAX_NUM_OF_NBRS);
    memset(uncoveredNbrs,0,sizeof(int)*MAX_NUM_OF_NBRS);
    memset(upstreamNbrs,0,sizeof(unsigned short)*MAX_NUM_OF_NBRS);
    memset(&stats,0,sizeof(struct stats_struct));
    for (i = 0; i < MAX_PACKETS_PER_BATCH; i++){
      rcvdPayloads[i] = (unsigned char *)malloc(sizeof(unsigned char)*MAX_DATA_PAYLOAD_SIZE);
      BPtr[i] = (unsigned char *)malloc(sizeof(unsigned char)*MAX_PACKETS_PER_BATCH);
      memset(rcvdPayloads[i],0,sizeof(unsigned char)*MAX_DATA_PAYLOAD_SIZE);
      memset(BPtr[i],0,sizeof(unsigned char)*MAX_PACKETS_PER_BATCH);
    }
    memset(xid,0,sizeof(unsigned short)*MAX_PACKETS_PER_BATCH);
    initializeCoder(MAX_PACKETS_PER_BATCH,MAX_DATA_PAYLOAD_SIZE,29);
    
      
  }
  command void SYREN.startHello(){
    call SYREN.resetHello();
    call StatsTimer.startPeriodic(1);
    if(!(call HelloTimer.isRunning()))
      call HelloTimer.startOneShot(1000 + 30*TOS_NODE_ID);
  }
  command void SYREN.stopHello(){
    int i;
    stats.numOfTx = numOfTransmissions;
    stats.numOfRx = rcvdPayloadsCounter;

    for (i = 0; i < MAX_PACKETS_PER_BATCH; i++){
      free(rcvdPayloads[i]);
      free(BPtr[i]);
    }
    call StatsTimer.stop();
    if(call HelloTimer.isRunning())
      call HelloTimer.stop();
    call NackTimer.stop();
  }
  command void *SYREN.getLinkStats(){
    return cumLinkStats;
  }
  command void *SYREN.getCprpStats(){
    return cumCprpStats;
  }
  command void  *SYREN.getStats(){
    return &stats;
  }
  command void SYREN.setThreshold(uint8_t th){
    threshold = ((float)th/100);
  }
  void addToUpstreamNbr(unsigned short nbr){
    int i ;
    for(i = 0; i < MAX_NUM_OF_NBRS; i++){
      if (cumLinkStats[i].neighborId == nbr && cumLinkStats[i].cprpValue > 0.2)
	break;
    }
    if (i >= MAX_NUM_OF_NBRS)
      return;
    for(i = 0; i < upstreamNbrCounter; i++){
      if (upstreamNbrs[i] == nbr)
	return;
    }
    if (upstreamNbrCounter < MAX_NUM_OF_NBRS)
      upstreamNbrs[upstreamNbrCounter++] = nbr;
  }
  
  void load(unsigned char batchNum){
    unsigned char loaded = 0;
    if (lastLoadedBatch == batchNum)
      return;
    
    while (loaded < MAX_PACKETS_PER_BATCH){
      memset(rcvdPayloads[loaded],'a'+loaded,sizeof(nx_uint8_t)*MAX_DATA_PAYLOAD_SIZE);
      rcvdPayloadsCounter++;
      loaded++;
      
    }
    lastLoadedBatch = batchNum;
  }
  
    
  void writeData()
  {
    call Leds.led2On();
    lastWrittenBatch = currentBatchId;
    stats.timeOfRx = currentTime;
  }

  int getBackoffTimerValue(){
  	float value = 0;
	float x = 0;
	int w1=2,w2 = 1;
  	int i = 0, pos = 0;
  	float cprpPart = 0;

  	for (i = 0; i < MAX_NUM_OF_NBRS; i++){
  		if (uncoveredNbrs[i] == 0)
  			break;
		pos = findNbrPositionInCoverage(uncoveredNbrs[i]);
		if (coverageProb[pos].timesCovered >= rcvdPayloadsCounter)
		  continue;
		if(coverageProb[pos].timesCovered == MAX_PACKETS_PER_BATCH - 1)
		  cprpPart = 1 - coverageProb[pos].cprpValue;
		else cprpPart = 1;
  		value += cumLinkStats[findNbrPositionForLinkStats(cumLinkStats,uncoveredNbrs[i])].cprpValue*cprpPart;
  		
  	}
	x = rand()%MIN_BACKOFF_VALUE;
	if (value > 0)
	  value = w1*(MAX_NUM_OF_NBRS_TIMER - value)*(MAX_BACKOFF_VALUE - MIN_BACKOFF_VALUE)/MAX_NUM_OF_NBRS_TIMER + 
		  w2*(MAX_PACKETS_PER_BATCH - rcvdPayloadsCounter)*(MAX_BACKOFF_VALUE - MIN_BACKOFF_VALUE)/MAX_PACKETS_PER_BATCH +x;
  	else
	  value = -1;
	
  	return (int)value;
  }
  
  void updateCoverageProb(int v, int i, int flag){
  	int k = findNbrPositionInCoverage(i);
  	int cprpSender = findSenderPositionForCprpStats(cumCprpStats,v);
  	int cprpNbr = findNbrPositionForCprpStats(cumCprpStats,cprpSender,i);
  	int linkNbr = findNbrPositionForLinkStats(cumLinkStats,i);
  	
  	if (k == -1 || cprpSender == -1 || cprpNbr == -1 || linkNbr == -1){
		printf("******************Pointer corruption******************");
		return;
	}
	//printf("I am cov update sender %d nbr = %d\n",v,i);
  	if (flag == 1)
  		coverageProb[k].cprpValue = 1 - (1-coverageProb[k].cprpValue)*(1-cumCprpStats[cprpSender].cprp[cprpNbr].cprpValue);
  	else if (flag ==2)
  		coverageProb[k].cprpValue = 1 - (1-coverageProb[k].cprpValue)*(1-cumLinkStats[linkNbr].cprpValue);
  }
  int hasUncoveredNbrs(){
  	int i = 0;
  	for (i = 0; i < MAX_NUM_OF_NBRS; i++){
  		if (uncoveredNbrs[i] == 0)
  			break;
  	}
  	if (i == 0)
  		return 0;
  	else
  		return 1;
  		
  }
  
  void testUncoveredNbrs(){
  	int i = 0;
  	int timerVal;
  	for (i = 0; i < MAX_NUM_OF_NBRS; i++){
  		if (uncoveredNbrs[i] == 0)
  			break;
  	}
  	if (i == 0 && !shortTxRequired){
  		call BackoffTimer.stop();
  	}
	else if (shortTxRequired && (shortTxCounter > 0)){
	  if (call BackoffTimer.isRunning())
	    call BackoffTimer.stop();
	  call BackoffTimer.startOneShot(25 + rand()%MIN_BACKOFF_VALUE);
	}
  	else{
  		if (call BackoffTimer.isRunning())
  			call BackoffTimer.stop();
  		if ((timerVal = getBackoffTimerValue()) > 0)
  			call BackoffTimer.startOneShot(timerVal);
  	}	
  }
  void updateUncoveredNbrs(int i){
  	int k = findNbrPositionInCoverage(i);
  	int m = 0;
  	int p = 0;
  	if (k == -1){
		printf("******************Pointer corruption******************");
		return;
	}
	//dbg("Display","I am here to update uncovered neighbors %d has times covered = %d\n",i,coverageProb[k].timesCovered);
  	if (coverageProb[k].neighborId == i && coverageProb[k].cprpValue >= threshold){
		if(coverageProb[k].timesCovered < MAX_PACKETS_PER_BATCH)
		  coverageProb[k].timesCovered++;
		if(!allowedToTx && (coverageProb[k].timesCovered < rcvdPayloadsCounter))
		  allowedToTx = 1;
		
  		if (coverageProb[k].timesCovered >= MAX_PACKETS_PER_BATCH){
	  		for (p = 0; p < MAX_NUM_OF_NBRS; p++)
	  			if (uncoveredNbrs[p] == i)
	  				break;
	  		for (m = p; m < MAX_NUM_OF_NBRS-1; m++)
	  			uncoveredNbrs[m] = uncoveredNbrs[m+1];
	 
	  		uncoveredNbrs[MAX_NUM_OF_NBRS-1] = 0;
	  	}
	  	else
	  		coverageProb[k].cprpValue = coverageProb[k].cprpValue - threshold;
  	}
  }

  void sendDataPacket(uint8_t type){
	
    data_msg_t* rsm;
    int err;
    if (locked) {
      return;
    }
    else {
      rsm = (data_msg_t*)call SendDataMsg.getPayload(&packet, sizeof(data_msg_t));
      if (rsm == NULL) {
		return;
      }
      rsm->msgType = type;
      rsm->msgId = ++msgCounter;
      rsm->pktsReceived = rcvdPayloadsCounter;
      //rsm->timeStamp = sim_time();
      //rsm->idOffset = dataPacketIdOffset;
      //rsm->size = mixRcvdPayloads(rsm->payload);

      if (rcvdPayloadsCounter > 0 && rcvdPayloadsCounter < MAX_PACKETS_PER_BATCH){
	rsm->encodedPktNum = xid[numOfTransmissions];
	memcpy(rsm->payload,rcvdPayloads[numOfTransmissions],sizeof(unsigned char)*MAX_DATA_PAYLOAD_SIZE);
      }
      else{
	rsm->encodedPktNum = packetnumber;
	encode(rcvdPayloads,rcvdPayloadsCounter, rsm->payload,packetnumber);
	packetnumber++;
      }
      
      rsm->batchId = currentBatchId;

      call CC2420Packet.setPower(&packet, txPower);
      if ((err = call SendDataMsg.send(AM_BROADCAST_ADDR, &packet, sizeof(data_msg_t))) == SUCCESS) {
	stats.timeOfLastTx = currentTime;
	call Leds.led0On();
	locked = TRUE;
      }
    }
	
  }

  void sendPiggybackedHelloMessage(){
    
    hello_msg_t* rsm;
    if (locked) {
      return;
    }
    else {
      rsm = (hello_msg_t*)call SendHelloMsg.getPayload(&packet, sizeof(hello_msg_t));
      if (rsm == NULL) {
	return;
      }
      rsm->msgType = PIGGYBACKED_HELLO_PACKET;
      rsm->msgId = ++msgCounter;
      memcpy(rsm->payload,helloStats, sizeof(helloStats));
      
      call CC2420Packet.setPower(&packet, txPower);

      if (call SendHelloMsg.send(AM_BROADCAST_ADDR, &packet, sizeof(hello_msg_t)) == SUCCESS) {
	call Leds.led0On();	
	locked = TRUE;
      }
    }
	
  }
  
  event void StatsTimer.fired() {
    currentTime++;
  }

  event void BackoffTimer.fired() {
  	int i = 0;
	int pos = -1;
  	unsigned int tempUncoveredNbrs[MAX_NUM_OF_NBRS];  
	
	for (i = 0; i < MAX_NUM_OF_NBRS; i++)
	  tempUncoveredNbrs[i] = uncoveredNbrs[i];

	if (numOfTransmissions > MAX_TRANSMISSIONS_PER_BATCH){
	  for (i = 0; i < MAX_NUM_OF_NBRS; i++){
	    if (tempUncoveredNbrs[i] == 0)
	      break;
	    pos=findNbrPositionInCoverage(tempUncoveredNbrs[i]);
	    if(pos != -1){
	      coverageProb[pos].cprpValue = 1.0;
	      coverageProb[pos].timesCovered = MAX_PACKETS_PER_BATCH-1;
	      updateUncoveredNbrs(tempUncoveredNbrs[i]);	
	    }
	  }
	}
  	else if ((numOfTransmissions < rcvdPayloadsCounter) || rcvdPayloadsCounter >= MAX_PACKETS_PER_BATCH){
	  	
		printUncoveredNbrs();
	  	sendDataPacket(DATA_PACKET);
	  	call NackTimer.startOneShot(NACK_TIMEOUT_VALUE + rand()%30);
	  	for (i = 0; i < MAX_NUM_OF_NBRS; i++){
	  		if (tempUncoveredNbrs[i] == 0)
	  			break;
	  		updateCoverageProb(tempUncoveredNbrs[i],tempUncoveredNbrs[i],2);
	  		updateUncoveredNbrs(tempUncoveredNbrs[i]);	
	  	}
		allowedToTx = 0;
		
  	}
	
  	testUncoveredNbrs();
  }

  event void NackTimer.fired(){
    
    if (rcvdPayloadsCounter < MAX_PACKETS_PER_BATCH){
      if (rcvdPayloadsCounter > 0){
	  sendDataPacket(NACK_PACKET);
      }
      call NackTimer.startOneShot(NACK_TIMEOUT_VALUE+rand()%30);
    }
  }

  void resetSYREN(){
  	int i;
	int j = 0,k=0;
	float linkIndependence = 0.0, temp = 0;
	int cprpPos;
	int linkStatPos;

  	atomic{
	    memset(coverageProb,0,sizeof(struct nbr_struct)*MAX_NUM_OF_NBRS);
	    memset(uncoveredNbrs,0,sizeof(int)*MAX_NUM_OF_NBRS);
	    memset(upstreamNbrs,0,sizeof(unsigned short)*MAX_NUM_OF_NBRS);
	  	if(call BackoffTimer.isRunning())
	  		call BackoffTimer.stop();
	  	for (i = 0; i < MAX_NUM_OF_NBRS; i++){
			if (cumLinkStats[i].neighborId > 0 && cumLinkStats[i].cprpValue > 0.0){
				linkIndependence = 0.0;  
				for (j = 0; j < MAX_NUM_OF_NBRS; j++){
				  if (cumCprpStats[j].sender == cumLinkStats[i].neighborId || cumCprpStats[j].sender == 0)
				    continue;
				  cprpPos = findNbrPositionForCprpStats(cumCprpStats,j,cumLinkStats[i].neighborId);
				  linkStatPos = findNbrPositionForLinkStats(cumLinkStats,cumCprpStats[j].sender);
				  if(cprpPos >= 0 && linkStatPos >= 0){
				    if(cumLinkStats[linkStatPos].neighborId > 0 && cumCprpStats[j].cprp[cprpPos].neighborId > 0){
				      temp = cumLinkStats[linkStatPos].cprpValue*cumCprpStats[j].cprp[cprpPos].cprpValue;

				      if (linkIndependence < temp)
					  linkIndependence = temp;
				    }
				  }
				}
				
				if(cumLinkStats[i].cprpValue + linkIndependence > 0.5 && cumLinkStats[i].cprpValue > 0.3 ){
				  uncoveredNbrs[k] = cumLinkStats[i].neighborId;
				  coverageProb[k].neighborId = cumLinkStats[i].neighborId;
				  coverageProb[k].cprpValue = 0.0;
				  coverageProb[k].timesCovered = 0;
				  k++;
				}
			}
		}
	    rcvdPayloadsCounter = 0;
	}
      printCprpStats(cumLinkStats,cumCprpStats);

  }

  event void HelloTimer.fired() {
	int i,j;
	if (helloCounter == MAX_HELLO_MSG_PER_INTERVAL){
		helloCounter = 0;
		helloWindow++;
		
		//printCprpStats(linkStats,cprpStats);

		for (i= 0; i < MAX_NUM_OF_NBRS && helloStats[i].neighborId > 0; i++)
			helloStats[i].bitmap = 0;
		for (i= 0; i < MAX_NUM_OF_NBRS && linkStats[i].neighborId > 0; i++){
			cumLinkStats[i].neighborId = linkStats[i].neighborId;
			cumLinkStats[i].cprpValue += ((float)1/MAX_NUM_OF_HELLO_WINDOWS)*linkStats[i].cprpValue; 
			linkStats[i].cprpValue = 0.0;
		}
		for (i= 0; i < MAX_NUM_OF_NBRS && cprpStats[i].sender > 0; i++){
			cumCprpStats[i].sender = cprpStats[i].sender;
			for (j= 0; j < MAX_NUM_OF_NBRS && cprpStats[i].cprp[j].neighborId > 0;j++){
				cumCprpStats[i].cprp[j].neighborId = cprpStats[i].cprp[j].neighborId;
				cumCprpStats[i].cprp[j].cprpValue += ((float)1/MAX_NUM_OF_HELLO_WINDOWS)*cprpStats[i].cprp[j].cprpValue;
				cprpStats[i].cprp[j].cprpValue = 0.0;
			}
		}
		if (helloWindow < MAX_NUM_OF_HELLO_WINDOWS ){
		  call HelloTimer.startOneShot(1000 + 30*TOS_NODE_ID);  
		  return;
		}
	}
  	if (helloWindow >= MAX_NUM_OF_HELLO_WINDOWS ){
		dataPacketIdOffset = msgCounter;
		//printCprpStats(cumLinkStats,cumCprpStats);
		
		if (rootNode == TOS_NODE_ID){
			  
			resetSYREN();
			load(++currentBatchId);
			//dbg("TX_HANDLER","%f : Dissemination starts\n",((float)sim_time())/10000000000+5);
			stats.timeOfRx = currentTime + 2000;
			call BackoffTimer.startOneShot(2000);
		}
		call HelloTimer.stop();
		//call NackTimer.startOneShot(3000 + NACK_TIMEOUT_VALUE + rand()%30);
		
	}
	else{
  		
		sendPiggybackedHelloMessage();
		
	}
  }

  void printUncoveredNbrs(){
  	int i = 0;
  	printf("I %d cntr = %d. Uncovered nbrs are below *******************\nUncovered Nbrs: ",TOS_NODE_ID,rcvdPayloadsCounter);
  	for (i = 0; i < MAX_NUM_OF_NBRS; i++){
  		if (uncoveredNbrs[i] != 0)
  			printf("%d\t",uncoveredNbrs[i]);
  	}
  	for (i = 0; i < MAX_NUM_OF_NBRS; i++){
  		if (coverageProb[i].neighborId > 0)
  			printf("\nCoverage Probability: [%d] = %d \t timesCovered = %d",coverageProb[i].neighborId, (int)(100*coverageProb[i].cprpValue),coverageProb[i].timesCovered);
  	} 	
  	printf("\nBackofftimer value = %d\n",getBackoffTimerValue());
  	printf("\n***************************************\n");
	printfflush();
  }
  void printCprpStats(struct nbr_struct *lStats, struct cprp_struct *cStats){
  	int i = 0;
  	int j = 0;
  	printf("I am %d My Link quality and Cprp stats are below ===========\n",TOS_NODE_ID);
  	for (i= 0; i < MAX_NUM_OF_NBRS && lStats[i].neighborId > 0; i++){
  		printf("LQ: %d -> %d = %d\n",TOS_NODE_ID, lStats[i].neighborId,(int)(100*lStats[i].cprpValue));
  	}
  	for (i= 0; i < MAX_NUM_OF_NBRS && cStats[i].sender > 0; i++){
  		printf("S = %d: ",cStats[i].sender);
  		for (j= 0; j < MAX_NUM_OF_NBRS && cStats[i].cprp[j].neighborId > 0;j++){
  			printf("Nbr = %d Corr = %d\t",cStats[i].cprp[j].neighborId,(int)(100*cStats[i].cprp[j].cprpValue) );
  		}
  		printf("\n");
  	}
  	printf("=================================\n");
	printfflush();
  }
  int findNbrPositionForNextMsg(unsigned int source){
  	int i = 0;
  	for (; i < MAX_NUM_OF_NBRS; i++){
		////printf("%d\t",expectedMsgId[i].neighborId);
  		if ((expectedMsgId[i].neighborId == source) || (expectedMsgId[i].neighborId == 0))
  			return i;
  	}
  	return -1;
  }
  int countBits(unsigned short number){
  	unsigned short v = number;
  	unsigned int c; // c accumulates the total bits set in v
	for (c = 0; v; c++)
	  v &= v - 1; // clear the least significant bit set
	return c;
  }
  
  int findSenderPositionForHelloStats(unsigned int source){
  	int i = 0;
  	for (; i < MAX_NUM_OF_NBRS; i++){
	    if ((helloStats[i].neighborId == source) || (helloStats[i].neighborId == 0))
		    return i;
  		
  	}
  	return -1;
  }
  int findNbrPositionForLinkStats(struct nbr_struct *lStats, unsigned int source){
  	int i = 0;
  	for (; i < MAX_NUM_OF_NBRS; i++){
  		if ((lStats[i].neighborId == source) || (lStats[i].neighborId == 0))
  			return i;
  	}
  	return -1;
  }
  int findSenderPositionForCprpStats(struct cprp_struct *cStats,unsigned int source){
  	int i = 0;
  	for (; i < MAX_NUM_OF_NBRS; i++){
  		if ((cStats[i].sender == source) || (cStats[i].sender == 0))
  			return i;
  	}
  	return -1;
  }
  
  int findNbrPositionForCprpStats(struct cprp_struct *cStats,unsigned int cprpSender, unsigned int source){
  	int i = 0;
  	for (; i < MAX_NUM_OF_NBRS; i++){
  		if ((cStats[cprpSender].cprp[i].neighborId == source) || (cStats[cprpSender].cprp[i].neighborId == 0))
  			return i;
  	}
  	return -1;
  }

  int findNbrPositionInCoverage(int source){
	int i = 0;
  	for (; i < MAX_NUM_OF_NBRS; i++){
  		if ((coverageProb[i].neighborId == source) || (coverageProb[i].neighborId == 0))
  			return i;
  	}
  	return -1;
	
  }
  
void updateCprpStats(struct hello_struct *rcvdHelloStats, int sourceOfMsg, int msgId){
	
    int loopCounter = 0;
    int cprpSender;
    int cprpNbr;
    int helloStatPos;
    struct hello_struct *tempHelloStats;
    int nbrPos;
    int linkStatPos;
    int numOfSentMsgs;
    
    
    tempHelloStats = helloStats;
    numOfSentMsgs = helloCounter;
    

    nbrPos = findSenderPositionForHelloStats(sourceOfMsg);
    linkStatPos = findNbrPositionForLinkStats(linkStats,sourceOfMsg);

    if (nbrPos == -1 || linkStatPos == -1){
	    //printf("Me %d source = %d******************Pointer corruption nbrPos = %d linkstatpos = %d flag = %d******************",TOS_NODE_ID, sourceOfMsg, nbrPos, linkStatPos);
	    return;
    }
    for (loopCounter = 0; loopCounter < MAX_NUM_OF_NBRS; loopCounter++){
	      if (rcvdHelloStats[loopCounter].neighborId == 0)
		break;
	      if (rcvdHelloStats[loopCounter].neighborId == TOS_NODE_ID){
		      
		      linkStats[linkStatPos].neighborId = sourceOfMsg;
		      //printf("bitmap %u cntr = %u\n",rcvdHelloStats[loopCounter].bitmap,numOfSentMsgs);
		      linkStats[linkStatPos].cprpValue = ((float)countBits(rcvdHelloStats[loopCounter].bitmap))/(numOfSentMsgs);	
		      
	      }
	      else{
		      helloStatPos = findSenderPositionForHelloStats(rcvdHelloStats[loopCounter].neighborId);
		      
		      if (helloStatPos == -1 ){
			      continue;
			      //printf("%d ******************hello stat pos Pointer corruption flag = %d nbr = %d******************",TOS_NODE_ID, flag,rcvdHelloStats[loopCounter].neighborId);
			      return;
		      }		
		      if (tempHelloStats[helloStatPos].neighborId == rcvdHelloStats[loopCounter].neighborId){
			      cprpSender = findSenderPositionForCprpStats(cprpStats,rcvdHelloStats[loopCounter].neighborId);
			      if (cprpSender == -1 ){
				      //printf("******************cprp sender Pointer corruption******************");
				      return;
			      }	
			      cprpStats[cprpSender].sender = rcvdHelloStats[loopCounter].neighborId;
			      cprpNbr = findNbrPositionForCprpStats(cprpStats,cprpSender,sourceOfMsg);

			      if (cprpNbr == -1 ){
				      //printf("******************cprp nbr Pointer corruption******************");
				      return;
			      }	
			      
			      cprpStats[cprpSender].cprp[cprpNbr].neighborId = sourceOfMsg;
			      if (countBits(tempHelloStats[helloStatPos].bitmap) == 0)
				      cprpStats[cprpSender].cprp[cprpNbr].cprpValue = 0.0;
			      else
				      cprpStats[cprpSender].cprp[cprpNbr].cprpValue = ((float)countBits(rcvdHelloStats[loopCounter].bitmap & tempHelloStats[helloStatPos].bitmap))/countBits(tempHelloStats[helloStatPos].bitmap);
			      
			      //printf("Rcvd--> Nbr id: %d bitmap = %d\t", rcvdHelloStats[loopCounter].neighborId, rcvdHelloStats[loopCounter].bitmap);
			      //printf("CPRP sender: %d cprp nbr : %d cprp value : %f\n",cprpStats[cprpSender].sender,cprpStats[cprpSender].cprp[cprpNbr].neighborId,cprpStats[cprpSender].cprp[cprpNbr].cprpValue);
		      }
	      }
      }
      atomic{
	      tempHelloStats[nbrPos].neighborId = sourceOfMsg;
	      tempHelloStats[nbrPos].bitmap |= ((unsigned short)1 << ((msgId -1)%MAX_HELLO_MSG_PER_INTERVAL));
	      
      }
  }
    

  event message_t* ReceiveHelloMsg.receive(message_t* bufPtr, 
				   void* payload, uint8_t len) {
    int nbrPosition = -1;
    struct hello_struct *rcvdHelloStats;
    unsigned short source;
    hello_msg_t *rsm;
    

    if (len != sizeof(hello_msg_t)) {return bufPtr;}
    else {
    
      source = call AMPacket.source(bufPtr);
      rsm = (hello_msg_t*)payload;
      nbrPosition = findNbrPositionForNextMsg(source);
	  if (nbrPosition == -1 ){
			//printUncoveredNbrs();
			//printf("******************expectedMsgId[nbrPosition] Pointer corruption****************** %d\n",source);
			return bufPtr;
			//exit(0);
	  }	
      
      
      if(rsm->msgId < expectedMsgId[nbrPosition].id) //old message
      	return bufPtr;
   	  
   	  
   	  expectedMsgId[nbrPosition].neighborId = source;
   	  expectedMsgId[nbrPosition].id = rsm->msgId + 1; 
       
      if (rsm->msgType == PIGGYBACKED_HELLO_PACKET ){
	call Leds.led1Toggle();
	//dbg("Display","Received hello packet from = %d\n",source);
      	rcvdHelloStats = (struct hello_struct *)rsm->payload;
      	
	if (rsm->msgId > helloWindow*MAX_HELLO_MSG_PER_INTERVAL && rsm->msgId <= (helloWindow+1)*MAX_HELLO_MSG_PER_INTERVAL){
		updateCprpStats(rcvdHelloStats, source, rsm->msgId);
		
	}
      	
      }
      return bufPtr;
    }
  }
  event void SendHelloMsg.sendDone(message_t* bufPtr, error_t error) {
    if (&packet == bufPtr) {
      locked = FALSE;
      call Leds.led0Off();
      ++helloCounter;
      if (helloCounter == MAX_HELLO_MSG_PER_INTERVAL){
		//printCprpStats();
		call HelloTimer.startOneShot(2000 - 30*TOS_NODE_ID);
 		//printf("$$$$$$$$$$$$$$$$ 5th message sent from %d $$$$$$$$$$$$$$\n",TOS_NODE_ID);
	}
	else if (helloCounter < MAX_HELLO_MSG_PER_INTERVAL){
	  call HelloTimer.startOneShot(1000);
	}
    }
  }

  
  char isShortTxRequired(unsigned short source){
    int i;
    for (i = 0; i < upstreamNbrCounter; i++){
      if(upstreamNbrs[i] == source)
	break;
    }
    if(i < upstreamNbrCounter && rcvdPayloadsCounter >= MAX_PACKETS_PER_BATCH)
      return 1;
    return 0;
  }
  event message_t* ReceiveDataMsg.receive(message_t* bufPtr, 
				   void* payload, uint8_t len) {
    int i = 0;
    int nbrPosition = -1;
    unsigned short source;
    data_msg_t* rsm;
    unsigned char hasPacket = 0;
    int pos = 0;
    unsigned int tempUncoveredNbrs[MAX_NUM_OF_NBRS];

    if (len != sizeof(data_msg_t)) {return bufPtr;}
    else {
      source = call AMPacket.source(bufPtr);
      rsm = (data_msg_t*)payload;
      nbrPosition = findNbrPositionForNextMsg(source);
	  if (nbrPosition == -1 ){
			lostPacket++;
			//printf("me %d from %d******************expectedMsgId[nbrPosition] Pointer corruption******************",TOS_NODE_ID,source);
			return bufPtr;
			//exit(0);
	  }	
      
      
      if(rsm->msgId < expectedMsgId[nbrPosition].id) //old message
      	return bufPtr;
   	  
   	  
   	  expectedMsgId[nbrPosition].neighborId = source;
   	  expectedMsgId[nbrPosition].id = rsm->msgId + 1; 
       
      if (rsm->msgType == DATA_PACKET){
	call Leds.led1Toggle();
      	if (rsm->batchId != currentBatchId){
      		currentBatchId = rsm->batchId;
      		resetSYREN();
      	}
	//printf("Node = %d has received a data packet from %d with id %d counter = %d encoded pkt num = %d\n",TOS_NODE_ID, source, rsm->msgId,rcvdPayloadsCounter, rsm->encodedPktNum);
	
	if(rsm->batchId != lastWrittenBatch && rcvdPayloadsCounter < MAX_PACKETS_PER_BATCH)
	{
		  for(i=0; i<rcvdPayloadsCounter; i++){
			  if(xid[i]==rsm->encodedPktNum){		//already have packet
				  hasPacket = 1;
				  break;
			  }
		  }
		  if(!hasPacket){
		    xid[rcvdPayloadsCounter]=rsm->encodedPktNum;
		    
		    for( i=0; i<MAX_DATA_PAYLOAD_SIZE; i++)
			    rcvdPayloads[rcvdPayloadsCounter][i]=rsm->payload[i];
		    
		    rcvdPayloadsCounter++;

		    if(rcvdPayloadsCounter==MAX_PACKETS_PER_BATCH && TOS_NODE_ID != rootNode){
		    i = decode(rcvdPayloads,BPtr,xid,togenerate);  
		    if (i==MAX_PACKETS_PER_BATCH){
			togenerate = 0;
			writeData();
		    }
		    else {
		      rcvdPayloadsCounter = i;
		      togenerate = MAX_PACKETS_PER_BATCH - i;
		    }
		  }
		}
	    }
	else if (rcvdPayloadsCounter >= MAX_PACKETS_PER_BATCH)
	  shortTxRequired = isShortTxRequired(source);

      	for (i = 0; i < MAX_NUM_OF_NBRS; i++)
	    tempUncoveredNbrs[i] = uncoveredNbrs[i];

      	for (i = 0; i < MAX_NUM_OF_NBRS; i++){
      		
      		if (tempUncoveredNbrs[i] == 0)
      			break;
      		if (tempUncoveredNbrs[i] == source){
			pos = findNbrPositionInCoverage(tempUncoveredNbrs[i]);
      			if (pos == -1){
      				//printf("******************Pointer corruption******************");
					return;
      			}
      			coverageProb[pos].cprpValue = 1.0;
			if(rsm->pktsReceived >= MAX_PACKETS_PER_BATCH && rcvdPayloadsCounter < MAX_PACKETS_PER_BATCH)
			  addToUpstreamNbr(source);
			coverageProb[pos].timesCovered = rsm->pktsReceived - 1;
      		}
      		else updateCoverageProb(source,tempUncoveredNbrs[i],1);
		
      		updateUncoveredNbrs(tempUncoveredNbrs[i]);
		
      	}
	
      	testUncoveredNbrs();
	  
	if (call NackTimer.isRunning())
	  call NackTimer.startOneShot(NACK_TIMEOUT_VALUE + rand()%30);

	else if(rsm->pktsReceived >= MAX_PACKETS_PER_BATCH && rcvdPayloadsCounter < MAX_PACKETS_PER_BATCH)
	  call NackTimer.startOneShot(NACK_TIMEOUT_VALUE + rand()%30);
	
      	//printUncoveredNbrs();
      }

      else if (rsm->msgType == NACK_PACKET){
	
		
	if(rsm->batchId != lastWrittenBatch && rcvdPayloadsCounter < MAX_PACKETS_PER_BATCH)
	{
		  for(i=0; i<rcvdPayloadsCounter; i++){
			  if(xid[i]==rsm->encodedPktNum){		//already have packet
				  hasPacket = 1;
				  break;
			  }
		  }
		  if(!hasPacket){
		    xid[rcvdPayloadsCounter]=rsm->encodedPktNum;
		    
		    for( i=0; i<MAX_DATA_PAYLOAD_SIZE; i++)
			    rcvdPayloads[rcvdPayloadsCounter][i]=rsm->payload[i];
		    
		    rcvdPayloadsCounter++;

		    if(rcvdPayloadsCounter==MAX_PACKETS_PER_BATCH && TOS_NODE_ID != rootNode){
		    i = decode(rcvdPayloads,BPtr,xid,togenerate);  
		    if (i==MAX_PACKETS_PER_BATCH){
			togenerate = 0;
			writeData();
		    }
		    else {
		      rcvdPayloadsCounter = i;
		      togenerate = MAX_PACKETS_PER_BATCH - i;
		    }
		  }
		}
	    }
	
      	for (i = 0; i < MAX_NUM_OF_NBRS; i++){
      		
      		if (uncoveredNbrs[i] == 0){
			uncoveredNbrs[i] = source;
      			break;
		}
		else if (uncoveredNbrs[i] == source)
		  break;
      	}
	
	pos = findNbrPositionInCoverage(source);
	if (pos == -1)
	  return bufPtr;
	coverageProb[pos].cprpValue = 1.0;
	coverageProb[pos].timesCovered = rsm->pktsReceived - 1;
	updateUncoveredNbrs(source);
	testUncoveredNbrs();
      }
      return bufPtr;
    }
  }

  event void SendDataMsg.sendDone(message_t* bufPtr, error_t error) {
    if (&packet == bufPtr) {
      call Leds.led0Off();
      if(shortTxRequired){
	if(shortTxCounter > 0)
	  shortTxCounter --;
	if(shortTxCounter == 0)
	  shortTxRequired = 0;
      }
      numOfTransmissions++;
      locked = FALSE;
      
    }
  }

}
