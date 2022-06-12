import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class Validation {
    private Packet packet;

    public Validation(byte[] packet) {
        this.packet = new Packet(packet);
    }

    public Validation(Packet packet) {
        this.packet = packet;
    }

    public boolean isCorrect() {
        try {
            int crc16 = new CRC16(sliceOf(packet.getPacket(), 0, 14)).getCRC();
            int crc16Code = ByteBuffer.wrap(sliceOf(packet.getPacket(), 14, 18)).getInt();

            int msgLen = packet.getLen();
            int crc16Msg = new CRC16(sliceOf(packet.getPacket(), 18, 18 + msgLen)).getCRC();
            int crc16MsgCode = ByteBuffer.wrap(sliceOf(packet.getPacket(), 18 + msgLen, 22 + msgLen)).getInt();

            return (crc16 == crc16Code) && (crc16Msg == crc16MsgCode);
        } catch (BufferOverflowException e) {
            e.printStackTrace();
        }
        return false;
    }

    byte[] sliceOf(byte[] bytes, int from, int to) {
        byte[] res = new byte[to - from];
        System.arraycopy(bytes, from, res, 0, to - from);
        return res;
    }
}
