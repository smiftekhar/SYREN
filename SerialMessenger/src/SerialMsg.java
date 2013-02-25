public class SerialMsg extends net.tinyos.message.Message {

    /** The Active Message type associated with this message. */
    public static final int AM_TYPE = 137;
    

    public SerialMsg(byte[] data) {
        super(data);
        amTypeSet(AM_TYPE);
    }
}
