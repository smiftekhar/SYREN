 
interface SYREN{
  command void setTransmissionPower(int txPower);
  command void setThreshold(uint8_t th);
  command void resetHello();
  command void *getLinkStats();
  command void *getCprpStats();
  command void startHello();
  command void stopHello();
  command void *getStats();
}