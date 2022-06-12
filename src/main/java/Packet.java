import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Packet {
    private byte bMagic = 0x13;
    private byte bSrc;
    private long bPktId;
    private int wLen;
    private short wCrc16;
    private Message bMsg;
    private short wCrc16_end;
    private byte[] packet;

    public Packet(byte[] bytes){
        if (bytes[0]!=0x13){
            throw new Error("Invalid packet!");
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        this.bSrc = byteBuffer.get(1);
        this.bPktId = byteBuffer.getLong(2);
        this.wLen = byteBuffer.getInt();
        this.wCrc16 = byteBuffer.getShort();
        this.bMsg = new Message(byteBuffer, this.wLen);
        this.wCrc16_end = byteBuffer.getShort();
        this.packet = bytes;
    }

    public boolean isCorrect(){
        return new Validation(this).isCorrect();
    }

    public byte[] getPacket() {
        return packet;
    }

    public int getLen(){
        return wLen;
    }

    public String getMessage() {
        byte[] decryptbMsq = null;
        try {
            decryptbMsq = AES.decryptData(bMsg.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(sliceOf(decryptbMsq, 8, Array.getLength(decryptbMsq)), StandardCharsets.UTF_16BE);
    }

    byte[] sliceOf(byte[] bytes, int from, int to) {
        byte[] res = new byte[to - from];
        System.arraycopy(bytes, from, res, 0, to - from);
        return res;
    }
}
