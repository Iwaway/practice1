import java.util.StringTokenizer;

public class ProcessingWorker implements Runnable {

    private Message message;

    public ProcessingWorker(Message message) {
        this.message = message;
    }

    public void addToStorage() {
        StringTokenizer st = new StringTokenizer(message.getMessage());
        String name = st.nextToken();
        int amount = Integer.parseInt(st.nextToken());
        if (Storage.products.containsKey(name)) {
            int cur = Storage.products.get(name);
            if (amount + cur >= 0)
                Storage.products.replace(name, cur + amount);
        } else if (amount > 0)
            Storage.products.put(name, amount);
    }

    @Override
    public void run() {
        MessageGenerator mc = new MessageGenerator(message.getClientAppId(),
                Database.clientKeys.get(message.getClientAppId()));
        byte[] response = mc.createFromMessage(message);
        Encriptor.encryptResponse(response, message.getClientAppId());
    }
}
