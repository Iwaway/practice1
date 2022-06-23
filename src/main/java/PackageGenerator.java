import java.io.IOException;

// Фейкова реалізація інтерфейсу, що генерує довільні повідомлення
public class PackageGenerator {

    public PackageGenerator() {
    }

    public static byte[] generatePackage() {
        Message msg = generateMsg();
        try {
            return Encriptor.encrypt(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 1. Взнати кількість товару на складі
    // 2. Списати певну кількість товару
    // 3. Зарахувати певну кількість товару
    // 4. Додати групу товарів
    // 5. Додати назву товару до групи
    // 6. Встановити ціну на конкретний товар
    private static Message generateMsg() {
        int command = (int) (Math.random() * 6) + 1;
        int user = (int) (Math.random() * 10) + 1;
        int length = (int) (Math.random() * 26) + 5;
        return new Message(getAlphaNumericString(length), command, user);
    }


    // random string of length N generator
    private static String getAlphaNumericString(int n) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }
}
