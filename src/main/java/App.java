public class App {
    public static void main(String[] args) {
        Receiver receiver = new Receiver();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Storage.products.get("Milk"));
    }
}
