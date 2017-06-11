import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class API {
    public API() {
        try {
            final String url = "http://datenparty.org:5001/api";
            URLConnection connection = new URL(url).openConnection();
            String encoding = connection.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            BufferedReader inReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));
            String json = inReader.lines().collect(Collectors.toCollection(ArrayList::new)).get(0);
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new API();
    }
}
