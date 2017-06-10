import org.json.simple.*;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@SuppressWarnings("unchecked")
public class DatenParty {

    private final static String daten = "/var/datenparty/daten.json";

    /** Welche Nachrichten wollen wir nicht herunterladen */

    private static ArrayList<String> blacklist = new ArrayList<String>() {{
        //Zeit
        add("http://www.zeit.de/zeit-magazin");
        add("http://www.zeit.de/campus");
        add("http://www.zeit.de/2");
        add("http://www.zeit.de/zeit-geschichte");
        add("http://www.zeit.de/zeit-wissen");
        add("http://www.zeit.de/freitext");
        add("http://www.zeit.de/video");
        add("http://www.zeit.de/vorabmeldungen");
        add("http://www.zeit.de/sport");
        //Welt
        add("/mediathek");
        add("/debatte");
        add("/icon");
        add("/kmpkt");
        add("/vermischtes");
        add("/newsticker");
        add("/satire");
        add("/politik/ausland/video");
        //Spiegel
        add("/spiegel");
        add("/stil");
        add("/video");
        add("/fotostrecke");
        add("/sptv");
        //FAZ
        add("/aktuell/reise");
        add("/aktuell/frankfurter");
        add("/aktuell/stil");
        add("/aktuell/beruf-chance");
    }};

     /** holt sich alle Artikel von der Website der Zeit*/
    private static ArrayList<JSONObject> getZeit() throws Exception {
        ArrayList<ArrayList<String>> values = new ArrayList<>();
        for (String e: getLinks("http://www.zeit.de/index", ".teaser-small__combined-link", false))
            try {
                Document d = Jsoup.connect(e).get();
                String text = d.select(".summary").text();
                String category = d.select(".article-heading__kicker").get(0).text();
                String time = d.select(".metadata__date").attr("datetime");
                LocalDateTime t = LocalDateTime.parse(time.substring(0, time.indexOf("+")));
                String heading = d.select(".article-heading__title").text();
                String imglink = d.select(".article__media-item").attr("src");
                if (!text.equals("")) values.add(new ArrayList<>(Arrays.asList(generateID(e, 3), text,
                        (t.getHour() < 10 ? "0" + t.getHour() : t.getHour()) + ":" + (t.getMinute() < 10 ? "0" + t.getMinute() : t.getMinute()), e, category, heading, imglink)));
            } catch (IOException | ArrayIndexOutOfBoundsException e2) {
                System.out.println(e);
                e2.printStackTrace();
            }
        return toJSON(values, "Zeit");
    }
    /** holt sich alle Artikel von der Website der Welt */
    private static ArrayList<JSONObject> getWelt() throws Exception {
        ArrayList<ArrayList<String>> values = new ArrayList<>();
        for (String e: getLinks("https://www.welt.de", ".o-teaser__link", true))
            try {
                Document d = Jsoup.connect(e).get();
                String text;
                try {
                    text = d.select(".c-summary__intro").text();
                } catch (IndexOutOfBoundsException e2) {
                    text = d.select(".c-summary__headline").text();
                    System.out.println(e);
                }
                String time = d.select(".c-publish-date").text();
                String category = d.select(".c-breadcrumb__element").get(1).text();
                String heading = d.select(".c-headline").text();
                if (!text.equals("")) values.add(new ArrayList<>(Arrays.asList(generateID(e, 2), text, time.split(" ")[1], e, category, heading, null)));
            } catch (IOException e2) {
                System.out.println(e);
                e2.printStackTrace();
            }
        return toJSON(values, "Welt");
    }
    /** holt sich alle Artikel von der Website Spiegel.de */
    private static ArrayList<JSONObject> getSpiegel() throws Exception {
        ArrayList<ArrayList<String>> values = new ArrayList<>();
        for (String e: getLinks("http://www.spiegel.de", ".article-title", true))
            try {
                Document d = Jsoup.connect(e).get();
                String text = d.select(".article-intro").get(0).text();
                String category = d.select(".headline-intro").get(0).text();
                LocalTime t = LocalTime.parse(d.select(".timeformat").attr("datetime").split(" ")[1]);
                String heading = d.select(".headline").text();
                String imglink = d.select(".spPanoGalleryTeaserPic").attr("src");
                if (!text.equals("")) values.add(new ArrayList<>(Arrays.asList(generateID(e, 1), text,
                        (t.getHour() < 10 ? "0" + t.getHour() : t.getHour()) + ":" + (t.getMinute() < 10 ? "0" + t.getMinute() : t.getMinute()), e, category, heading, imglink)));
            } catch (Exception e2) {
                System.out.println(e);
                e2.printStackTrace();
            }
        return toJSON(values, "Spiegel");
    }
    /** holt sich alle Artikel von der Website der FAZ */
    private static ArrayList<JSONObject> getFAZ() throws Exception {
        ArrayList<ArrayList<String>> values = new ArrayList<>();
        for (String e: getLinks("http://www.faz.net/", ".TeaserHeadLink", true)) {
            Document d = Jsoup.connect(e).get();
            String text = d.select(".Copy").get(0).text();
            String time = d.select(".lastUpdated").text();
            String[] t = time.split(" ");
            String category = d.select(".NavStep").get(1).text();
            String heading = d.getElementsByTag("h2").attr("itemprop", "headline").get(0).ownText();
            String imglink = d.select(".media").attr("src");
            try {
                if (!text.equals("")) values.add(new ArrayList<>(Arrays.asList(generateID(e, 1), text, t[2], e, category, heading, imglink)));
            } catch (ArrayIndexOutOfBoundsException e2) {
                try {
                    Document docu = Jsoup.connect("http://faz.net" + d.select(".mmNext").get(0).attr("href")).get();
                    String ti = docu.select(".date").get(0).text().split("")[1];
                    String category2 = docu.select(".NavStep").get(1).text();
                    String heading2 = docu.getElementsByTag("h2").attr("itemprop", "headline").get(0).ownText();
                    if (!text.equals("")) values.add(new ArrayList<>(Arrays.asList(generateID(e, 1), text, ti, e, category2, heading2, imglink)));
                } catch (IndexOutOfBoundsException e3) {
                    System.out.println("Index: " + e);
                    e3.printStackTrace();
                }
            }
        }
        return toJSON(values, "FAZ");
    }
    /** hier holt sich das Programm die Links der verschiedenen Artikel */
    private static ArrayList<String> getLinks(String site, String cssQuery, boolean rename) throws Exception {
        Document doc = Jsoup.connect(site).get();
        Elements ele = doc.select(cssQuery);
        ArrayList<String> list = new ArrayList<>();
        ele.forEach(e -> list.add(e.getElementsByAttribute("href").attr("href")));
        if (rename) {
            list.removeIf(e -> !e.startsWith("/") || isInList(e));
            list.replaceAll(e -> e = site + e);
        } else list.removeIf(e -> !e.startsWith("http://www.zeit.de") || isInList(e));
        Collections.shuffle(list);
        if (list.size() > 20) list.subList(20, list.size()).clear();
        return list;
    }
    /** Hier werden alle Daten gesammelt und in ein Json verpackt */
    public static void main(String[] args) throws Exception {
        Log.write("start update");
        ArrayList<JSONObject> zeit = getZeit();
        ArrayList<JSONObject> welt = getWelt();
        ArrayList<JSONObject> spiegel = getSpiegel();
        ArrayList<JSONObject> faz = getFAZ();
        JSONArray array = new JSONArray();
        zeit.forEach(array::add);
        welt.forEach(array::add);
        spiegel.forEach(array::add);
        faz.forEach(array::add);
        Collections.shuffle(array);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(daten), Charset.forName("UTF-8").newEncoder()));
        writer.write(DetectLang.getLanguage(array).toJSONString());
        Log.write("daten.json wurde aktualisiert!");
        Log.write("Insgesamt " + array.size() + " Artikel");
        writer.close();
    }
    /** Diese Funktion compeliert ein normales Array zu einem Json Array */
    private static ArrayList<JSONObject> toJSON(ArrayList<ArrayList<String>> values, String author) throws Exception {
        ArrayList<JSONObject> array = new ArrayList<>();
        values.forEach(l ->
            array.add(new JSONObject() {{
                put("id", l.get(0));
                put("yes", 0);
                put("no", 0);
                put("author", author);
                put("date", l.get(2));
                put("link", l.get(3));
                put("article", l.get(1));
                put("category", l.get(4));
                put("heading", l.get(5));
                put("imglink", l.get(6));
            }}));
        return array;
    }
    /** diese Funktion nimmt sich die Liks und erstellt eine ID */
    private static String generateID(String text, int mode) {
        int random = new Random().nextInt(1000);
        if (mode == 1) { //Spiegel, FAZ
            String cut = text.split("-")[text.split("-").length-1];
            return cut.split("\\.")[0] + random;
        } else if (mode == 2) { //Welt
            String cut = text.split("/")[text.split("/").length-2];
            return cut.split("e")[1] + random;
        } else { //Zeit
            String[] array = text.split("/");
            String cut = array[array.length - 1];
            String[] date = array[array.length - 2].substring(2).split("-");
            return date[1] + date[0] +
                    (int)cut.charAt(0) +
                    (int)cut.charAt(1) +
                    (int)cut.charAt(cut.length()-2) +
                    (int)cut.charAt(cut.length()-1) + random;
        }
    }
    /**Diese Funktion iteriert über die Listen und findet heraus ob der Link sich in der Blacklist befindet */
    private static boolean isInList(String e) {
        for (String x: blacklist) if(e.startsWith(x)) return true;
        return false;
    }
}