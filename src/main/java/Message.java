public class Message {
    private byte clientAppId;
    private long messageId;
    private int cType;
    private int bUserId;
    private String message;

    public Message(byte clientAppId, long messageId, int cType, int bUserId, String message) {
        super();
        this.clientAppId = clientAppId;
        this.messageId = messageId;
        this.cType = cType;
        this.bUserId = bUserId;
        this.message = message;
    }

    public byte getClientAppId() {
        return clientAppId;
    }

    public void setClientAppId(byte clientAppId) {
        this.clientAppId = clientAppId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public int getcType() {
        return cType;
    }

    public void setcType(int cType) {
        this.cType = cType;
    }

    public int getbUserId() {
        return bUserId;
    }

    public void setbUserId(int bUserId) {
        this.bUserId = bUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
