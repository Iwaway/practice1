import java.security.Key;
import java.util.concurrent.ConcurrentHashMap;

public class Database {

    public static ConcurrentHashMap<Long, byte[]> processedMessages = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Byte, Key> clientKeys = new ConcurrentHashMap<>();

}
