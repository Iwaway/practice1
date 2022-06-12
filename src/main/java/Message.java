import java.lang.reflect.Array;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Message {
    private int cType;
    private int bUserId;
    private byte[] message;
    private byte[] bytes;

    public Message(ByteBuffer byteBuffer, int wLen){
        this.cType = byteBuffer.getInt();
        this.bUserId = byteBuffer.getInt();
        message = new byte[wLen - Integer.BYTES * 2];
        byteBuffer.get(this.message, Integer.BYTES * 2, wLen);
        bytes = AES.encryptData(getBytesTool());
        this.cType = toInt(sliceOf(bytes, 0,4));
        this.bUserId = toInt(sliceOf(bytes, 4, 8));
        this.message = sliceOf(bytes, 8, bytes.length);
    }

    int toInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    static byte[] intToBytes(final int i) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.putInt(i);
            return byteBuffer.array();
        } catch (BufferOverflowException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getBytesTool() {
        try {
            ByteBuffer bb = ByteBuffer.allocate(Array.getLength(intToBytes(cType)) + Array.getLength(intToBytes(bUserId)) + Array.getLength(message));
            bb.put(intToBytes(cType));
            bb.put(intToBytes(bUserId));
            bb.put(message);
            return bb.array();
        } catch (BufferOverflowException e) {
            e.printStackTrace();
        }
        return null;
    }

    public  byte[] getBytes(){
        return bytes;
    }

    byte[] sliceOf(byte[] bytes, int from, int to) {
        byte[] res = new byte[to - from];
        System.arraycopy(bytes, from, res, 0, to - from);
        return res;
    }
}
