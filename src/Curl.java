import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.stream.Collectors;

public class Curl {

    public static void main(String[] args) throws Exception {
        //System.out.println(newCategory("Wo früher Güterzüge rangierten, leben heute mehrere Tausend Menschen. Jede freie Fläche wird genutzt, denn Bauland ist knapp in der wachsenden Stadt."));
    }

    static String newCategory(String artice) throws Exception {
        String serviceURL = "https://api.neofonie.de/rest/txt/analyzer";

        URL myURL = new URL(serviceURL);
        HttpURLConnection myURLConnection = (HttpURLConnection)myURL.openConnection();
        myURLConnection.setRequestMethod("POST");
        myURLConnection.setRequestProperty("X-Api-Key", "9a02d075-229b-8b4a-ec1a-74e35db9e901");
        myURLConnection.setUseCaches(false);
        myURLConnection.setDoInput(true);
        myURLConnection.setDoOutput(true);
        myURLConnection.connect();

        String data = URLEncoder.encode("text", "UTF-8") + "="
                + URLEncoder.encode(artice, "UTF-8");
        data += "&" + URLEncoder.encode("services", "UTF-8") + "="
                + URLEncoder.encode("entities", "UTF-8");

        OutputStreamWriter writer = new OutputStreamWriter(myURLConnection.getOutputStream());
        writer.write(data);
        writer.close();

        InputStream is = myURLConnection.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String result = br.lines().collect(Collectors.joining());

        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(result);
        JSONArray array = (JSONArray) obj.get("entities");
        if (array.size() == 0) return "Aktuell";
        array.sort(Comparator.comparing(e -> ((JSONObject)e).get("confidence").toString()));
        is.close();
        br.close();
        return ((JSONObject)array.get(array.size()-1)).get("surface").toString();
    }



}
