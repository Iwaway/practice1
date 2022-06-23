public class Message {

    byte[] msg;
    String messageSTR;

    public Message(String message, int command, int user){
        this.messageSTR=message;
        int messageL = message.length();
        int length = messageL +8;
        this.msg = new byte[length];

        this.msg[0] = (byte) ((command >>> 24) & 0xFF);
        this.msg[1] = (byte) ((command >>> 16) & 0xFF);
        this.msg[2] = (byte) ((command >>> 8) & 0xFF);
        this.msg[3] = (byte) (command & 0xFF);

        this.msg[4] = (byte) ((user >>> 24) & 0xFF);
        this.msg[5] = (byte) ((user >>> 16) & 0xFF);
        this.msg[6] = (byte) ((user >>> 8) & 0xFF);
        this.msg[7] = (byte) (user & 0xFF);

        System.arraycopy(message.getBytes(),0,this.msg,8, messageL);
    }

}
