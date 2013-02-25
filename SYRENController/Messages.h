#ifndef __MESSAGES_H__
#define __MESSAGES_H__

#include <message.h>

#define MAX_NUM_OF_MSGS 10
#define MAX_NUM_OF_BYTES 12
#define MAX_NUM_OF_NBRS 12
#define MAX_SERIAL_PKT_SIZE 12


enum {
  CMD_STOP = 49,
  CMD_LOCAL_STOP = 50,
  CMD_START_TRANSMISSION = 51,
  CMD_REPORT = 52,
  CMD_REPORT_LOCAL = 53,
  CMD_ERASE_LOG = 54,
};

enum {
  KEY = 0xDE00,
  COMMAND_INTERPRETER = 0x89,
  REPORTER = 6,
};

typedef nx_struct Command {
  nxle_uint8_t type;
  nxle_uint16_t nodeId;
  nxle_uint8_t arg;  
} Command;
 

  typedef struct ReceptionRecord{
      unsigned short nbrMap[MAX_NUM_OF_NBRS];
      unsigned char reception[MAX_NUM_OF_NBRS][MAX_NUM_OF_BYTES];
  }ReceptionRecord;

#endif