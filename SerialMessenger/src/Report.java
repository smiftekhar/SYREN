public class Report extends net.tinyos.message.Message {

    /** The Active Message type associated with this message. */
    public static final int AM_TYPE = 6;
    private int reportingNodeId;
    
    private byte []payload;
    public Report(byte[] data) {
        super(data);
        amTypeSet(AM_TYPE);
    }
    void setParameters(){
    	byte []data = this.dataGet();
    	
    	reportingNodeId = (0xFF & data[9]);
        reportingNodeId = (reportingNodeId << 8 ) | (0xFF & data[8]);
            
        payload = new byte[data.length - 10];
        for (int i = 10; i < data.length; i++)
        	payload[i-10] = data[i];
    }
    int getNameOfReportingNode(){
    	return reportingNodeId;
    }
    
    byte []getData(){
    	return payload;
    }
}
