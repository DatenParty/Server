import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Curl {

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
