import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Log {
    static void write(String text) {
        try {
            FileWriter f = new FileWriter("log.txt");
            String timeStamp = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] ";
            f.write(timeStamp + text + "\n");
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
