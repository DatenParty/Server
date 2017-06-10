import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;

public class DetectLang {
    public DetectLang(String txt)
    {  HttpClient httpclient = HttpClients.createDefault();

        try {
            URIBuilder builder = new URIBuilder("https://westus.api.cognitive.microsoft.com/text/analytics/v2.0/languages");

            builder.setParameter("numberOfLanguagesToDetect", "1");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", "3c59408816a24cafa733d03d47f54507");


            // Request body
            StringEntity reqEntity = new StringEntity(txt);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null)
                System.out.println(EntityUtils.toString(entity));

        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        new DetectLang("{\"documents\":[{\"id\": \"sampleText\",\"text\": \"Auch gibt es niemanden, der den Schmerz an sich liebt, sucht oder wuenscht, nur\"}]}\n");
    }
}
