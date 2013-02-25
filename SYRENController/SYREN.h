#ifndef SYREN_H
#define SYREN_H

#include "printf.h"

#define MAX_PACKETS_TRANSMITTED 1
#define MAX_PACKETS_PER_BATCH 24
#define MAX_NUM_OF_NBRS_TIMER 50
#define MAX_HELLO_MSG_PER_INTERVAL 16
#define MAX_NUM_OF_HELLO_WINDOWS 3
#define MAX_DATA_PAYLOAD_SIZE 22
#define MAX_HELLO_PAYLOAD_SIZE 4*MAX_NUM_OF_NBRS
#define HELLO_PACKET 1
#define PIGGYBACKED_HELLO_PACKET 2
#define DATA_PACKET 3
#define NACK_PACKET 4
#define MAX_QUEUE_SIZE 2

#define MIN_BACKOFF_VALUE 16
#define MAX_BACKOFF_VALUE 80
#define MAX_TRANSMISSIONS_PER_BATCH 3*MAX_PACKETS_PER_BATCH
#define MAX_DATA_PER_BATCH MAX_PACKETS_PER_BATCH*MAX_DATA_PAYLOAD_SIZE

#define TRANSMISSION_POWER 2
#define NACK_TIMEOUT_VALUE 500

struct stats_struct{
	uint16_t numOfTx;
	uint16_t numOfRx;
	uint32_t timeOfRx;
	uint32_t timeOfLastTx;
};

struct message_expected_struct{
	unsigned short neighborId;
	unsigned int id;
	unsigned int piggybackedId;
};

struct hello_struct{
	nxle_uint16_t neighborId;
	nxle_uint16_t bitmap;
};

struct nbr_struct{
	unsigned short neighborId;
	float cprpValue;
	short int timesCovered;
};

struct cprp_struct{
	unsigned short sender;
	struct nbr_struct cprp[MAX_NUM_OF_NBRS];
};

typedef struct hello_msg {
  nxle_uint8_t msgType;
  nxle_uint16_t msgId;
  //unsigned short source;
  //unsigned short size;
  //long long timeStamp;
  //unsigned int idOffset;
  //nxle_uint8_t batchId;
  nxle_uint8_t payload[MAX_HELLO_PAYLOAD_SIZE];
} hello_msg_t;


typedef struct data_msg {
  nxle_uint8_t msgType;
  nxle_uint16_t msgId;
  nxle_uint16_t encodedPktNum;
  //unsigned short size;
  //long long timeStamp;
  //unsigned int idOffset;
  nxle_uint8_t pktsReceived;
  nxle_uint8_t batchId;
  nxle_uint8_t payload[MAX_DATA_PAYLOAD_SIZE];
} data_msg_t;


enum {
  AM_HELLO_MSG = 7,
  AM_DATA_MSG = 8,
};

#endif
