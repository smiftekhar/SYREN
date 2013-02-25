
#ifndef NETWORK_CODING_H_
#define NETWORK_CODING_H_


unsigned char moddiv[256];
unsigned char NC_PKTS_PER_BATCH;
unsigned char NC_BYTES_PER_PKT;
unsigned char primitive;

void initializeCoder(unsigned char numOfPackets, unsigned char numOfBytes, unsigned char prim);
void encode (unsigned char **XPtr, unsigned char numOfPackets, unsigned char payload[], unsigned short packetnumber);
int decode(unsigned char **XPtr, unsigned char **BPtr, unsigned short xid[], int togenerate);

#endif
