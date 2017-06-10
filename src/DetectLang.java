import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class DetectLang {


    public static JSONArray getLanguage(JSONArray jsonArray) {
        HttpClient httpclient = HttpClients.createDefault();
        try {
            URIBuilder builder = new URIBuilder("https://westus.api.cognitive.microsoft.com/text/analytics/v2.0/languages");

            builder.setParameter("numberOfLanguagesToDetect", "1");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", "3c59408816a24cafa733d03d47f54507");


            // Request body
            StringBuilder b = new StringBuilder();
            b.append("{\"documents\": [");
            for (int i = 0; i < jsonArray.size(); i++)
                b.append("{\"id\": \"" + ((JSONObject) jsonArray.get(i)).get("id") + "\", \"text\": \"" + extract((String) ((JSONObject) jsonArray.get(i)).get("article")) + "\"}, ");
            String detectJSON = b.substring(0, b.length()-2) + "]}";
            StringEntity reqEntity = new StringEntity(detectJSON);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            String json = EntityUtils.toString(entity);
            JSONParser languageParser = new JSONParser();
            JSONObject languageObj = (JSONObject) languageParser.parse(json);
            JSONArray languageArray = (JSONArray)languageObj.get("documents");
            languageArray.forEach(e -> {
                String id = (String)((JSONObject)e).get("id");
                JSONArray array = (JSONArray)((JSONObject)e).get("detectedLanguages");
                String language = (String) ((JSONObject)array.get(0)).get("name");
                HashMap<String, String> map = new HashMap<>();
                map.put(id, language);
                map.forEach((k, v) -> {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        if (((JSONObject)jsonArray.get(i)).get("id").equals(k)) {
                            ((JSONObject)jsonArray.get(i)).put("language", v);
                            break;
                        }
                    }
                });
            });
            return jsonArray;
        } catch (IOException | URISyntaxException | ParseException e) {
            e.printStackTrace();
            return null;
        }

    }

    private static String extract(String text) {
        return text.replaceAll("[^A-Za-z0-9\\s]", "");
    }
}
