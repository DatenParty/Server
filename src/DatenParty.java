import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

@SuppressWarnings("unchecked")
public class DatenParty {

    private final static String daten = "/var/datenparty/daten.json";
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy, HH:mm:ss");
    private final static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyy-MM-dd k:mm:ss");

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
        add("http://www.zeit.de/karriere");
        add("http://www.zeit.de/sport/nationalmannschaft-ticker");

        //Welt
        add("/mediathek");
        add("/debatte");
        add("/icon");
        add("/kmpkt");
        add("/vermischtes");
        add("/newsticker");
        add("/satire");
        add("/politik/ausland/video");
        add("/regionales");

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
        //Guardian
        add("https://www.theguardian.com/world");
        add("https://www.theguardian.com/travel");
        add("https://www.theguardian.com/artanddesign");
        add("https://www.theguardian.com/sport/live");
        /**/ add("https://www.theguardian.com/commentisfree"); /**/
        add("https://www.theguardian.com/fashion");
        add("https://www.theguardian.com/politics/blog/live");
    }};

     /** holt sich alle Artikel von der Website der Zeit*/
    private static ArrayList<JSONObject> getZeit() throws Exception {
        ArrayList<ArrayList<String>> values = new ArrayList<>();
        for (String e: getLinks("http://www.zeit.de/index", ".teaser-small__combined-link", false, "http://www.zeit.de"))
            try {
                Document d = Jsoup.connect(e).get();
                String text = d.select(".summary").text();
                String category = d.select(".article-heading__kicker").get(0).text();
                String dateText = d.select(".metadata__date").attr("datetime");
                String date = dateText.split("T")[0] + " " + dateText.split("T")[1].split("\\+")[0];
                LocalDateTime t = LocalDateTime.parse(date, format);
                String heading = d.select(".article-heading__title").text();
                String imglink = d.select(".article__media-item").attr("src");
                if (!text.equals("")) values.add(new ArrayList<>(Arrays.asList(generateID(e, 3), text, t.format(formatter), e, category, heading, imglink)));
            } catch (IOException | IndexOutOfBoundsException | DateTimeParseException e2) {
                System.out.println(e);
                e2.printStackTrace();
            }
        return toJSON(values, "Zeit");
    }
    /** holt sich alle Artikel von der Website der Welt */
    private static ArrayList<JSONObject> getWelt() throws Exception {
        ArrayList<ArrayList<String>> values = new ArrayList<>();
        for (String e: getLinks("https://www.welt.de", ".o-teaser__link", true, ""))
            try {
                Document d = Jsoup.connect(e).get();
                String text;
                try {
                    text = d.select(".c-summary__intro").text();
                } catch (IndexOutOfBoundsException e2) {
                    text = d.select(".c-summary__headline").text();
                    System.out.println(e);
                }
                String dateText = d.select(".c-publish-date").attr("datetime");
                String date = dateText.split("T")[0] + " " + dateText.split("T")[1].split("Z")[0];
                LocalDateTime t = LocalDateTime.parse(date, format);
                String category = d.select(".c-breadcrumb__element").get(1).text();
                String heading = d.select(".c-headline").text();
                String imglink;
                try {
                    imglink = d.select(".o-element__image").get(0).getElementsByTag("img").attr("src");
                } catch (IndexOutOfBoundsException e2) {
                    String style = d.select(".c-placeholder--is-preview").attr("style");
                    imglink = style.substring(22, style.length()-5);
                }
                if (!text.equals("")) values.add(new ArrayList<>(Arrays.asList(generateID(e, 2), text, t.format(formatter), e, category, heading, imglink)));
            } catch (IOException | IndexOutOfBoundsException | DateTimeParseException e2) {
                System.out.println(e);
                e2.printStackTrace();
            }
        return toJSON(values, "Welt");
    }
    /** holt sich alle Artikel von der Website Spiegel.de */
    private static ArrayList<JSONObject> getSpiegel() throws Exception {
        ArrayList<ArrayList<String>> values = new ArrayList<>();
        for (String e: getLinks("http://www.spiegel.de", ".article-title", true, ""))
            try {
                Document d = Jsoup.connect(e).get();
                String text = d.select(".article-intro").get(0).text();
                String category = d.select(".headline-intro").get(0).text();
                String date = d.select(".timeformat").attr("datetime");
                LocalDateTime t = LocalDateTime.parse(date, format);
                String heading = d.select(".headline").text();
                String imglink = d.select(".spPanoImageTeaserPic").attr("src");
                if (imglink.equals("")) imglink = d.select(".spPanoPlayerTeaserPic").attr("src");
                if (!text.equals("")) values.add(new ArrayList<>(Arrays.asList(generateID(e, 1), text, t.format(formatter), e, category, heading, imglink)));
            } catch (Exception e2) {
                System.out.println(e);
                e2.printStackTrace();
            }
        return toJSON(values, "Spiegel");
    }
    /** holt sich alle Artikel von der Website der FAZ */
    private static ArrayList<JSONObject> getFAZ() throws Exception {
        ArrayList<ArrayList<String>> values = new ArrayList<>();
        for (String e: getLinks("http://www.faz.net/", ".TeaserHeadLink", true, "")) {
            try {
                Document d = Jsoup.connect(e).get();
                String text = d.select(".Copy").get(0).text();
                String imglink = d.select(".MediaLink").get(0).getElementsByTag("a").get(0).select(".media").attr("src");
                try {
                    String dateText = d.select(".Datum").attr("content");
                    String date = dateText.split("T")[0] + " " + dateText.split("T")[1].split("\\+")[0];
                    LocalDateTime t = LocalDateTime.parse(date, format);
                    String category;
                    try {
                        category = d.select(".NavStep").get(1).text();
                    } catch (IndexOutOfBoundsException e3) {
                        category = d.select(".Stichwort").text();
                    }
                    String heading = d.getElementsByTag("h2").attr("itemprop", "headline").get(0).ownText();

                    if (!text.equals(""))
                        values.add(new ArrayList<>(Arrays.asList(generateID(e, 1), text, t.format(formatter), e, category, heading, imglink)));
                } catch (ArrayIndexOutOfBoundsException e2) {
                    try {
                        Document docu = Jsoup.connect("http://faz.net" + d.select(".mmNext").get(0).attr("href")).get();
                        String ti = docu.select(".date").get(0).attr("content");
                        String temporal = ti.split("T")[0] + " " + ti.split("T")[1].split("\\+")[0];
                        LocalDateTime time = LocalDateTime.parse(temporal, format);
                        String category2 = docu.select(".NavStep").get(1).text();
                        String heading2 = docu.getElementsByTag("h2").attr("itemprop", "headline").get(0).ownText();
                        if (!text.equals(""))
                            values.add(new ArrayList<>(Arrays.asList(generateID(e, 1), text, time.format(formatter), e, category2, heading2, imglink)));
                    } catch (IndexOutOfBoundsException | DateTimeParseException e3) {
                        System.out.println("Index: " + e);
                        e3.printStackTrace();
                    }
                }
            } catch (IndexOutOfBoundsException | DateTimeParseException e2) {
                System.out.println(e);
                e2.printStackTrace();
            }
        }
        return toJSON(values, "FAZ");
    }
    /** Informationen werden vom guardian geladen */
    private static ArrayList<JSONObject> getGuardian() {
        ArrayList<ArrayList<String>> values = new ArrayList<>();
        try {
            ArrayList<String> links = getLinks("https://www.theguardian.com/international", ".u-faux-block-link__overlay", false, "https://www.theguardian.com");
            for (String e: links) {
                try {
                    Document d = Jsoup.connect(e).get();
                    String text = d.select(".content__article-body").get(0).getElementsByTag("p").get(0).text();
                    String dateText = d.select(".content__dateline-wpd").attr("datetime");
                    String date = dateText.split("T")[0] + " " + dateText.split("T")[1].split("\\+")[0];
                    LocalDateTime t = LocalDateTime.parse(date, format);
                    String category = d.select(".content__section-label__link").text();
                    String heading = d.select(".content__headline").get(0).text();
                    String imglink = d.select(".responsive-img").attr("src");
                    if (!text.equals(""))
                        values.add(new ArrayList<>(Arrays.asList(generateID(e, 4), text, t.format(formatter), e, category, heading, imglink)));
                } catch (Exception e2) {
                    System.out.println(e);
                    e2.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toJSON(values, "The Guardian");
    }

    /** hier holt sich das Programm die Links der verschiedenen Artikel */
    private static ArrayList<String> getLinks(String site, String cssQuery, boolean rename, String prefix) throws Exception {
        Document doc = Jsoup.connect(site).get();
        Elements ele = doc.select(cssQuery);
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> newList = new ArrayList<>();
        for (Element x: ele) list.add(x.getElementsByAttribute("href").attr("href"));
        if (rename) {
            list.removeIf(e -> !e.startsWith("/") || isInList(e));
            list.replaceAll(e -> e = site + e);
        } else {
            for (String e: list)
                if ((e.startsWith(prefix)) && !isInList(e)) newList.add(e);
            list = newList;
        }
        Collections.shuffle(list);
        if (list.size() > 20) list.subList(20, list.size()).clear();
        return list;
    }

    /** Hier werden alle Daten gesammelt und in ein Json verpackt */
    public static void main(String[] args) {
        try {
            Log.write("start update");
            JSONArray array = new JSONArray();
            getZeit().forEach(array::add);
            getWelt().forEach(array::add);
            getSpiegel().forEach(array::add);
            getFAZ().forEach(array::add);
            getGuardian().forEach(array::add);
            Collections.shuffle(array);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(daten), Charset.forName("UTF-8").newEncoder()));
            writer.write(DetectLang.getLanguage(array).toJSONString());
            Log.write("daten.json wurde aktualisiert!");
            Log.write("Insgesamt " + array.size() + " Artikel");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /** Diese Funktion compeliert ein normales Array zu einem Json Array */
    private static ArrayList<JSONObject> toJSON(ArrayList<ArrayList<String>> values, String author) {
        ArrayList<JSONObject> array = new ArrayList<>();
        values.forEach(l ->
            array.add(new JSONObject() {{
                put("id", l.get(0));
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
        switch (mode) {
            //FAZ
            case 1: {
                String cut = text.split("-")[text.split("-").length-1];
                return cut.split("\\.")[0] + random;
            //Welt
            } case 2: {
                String cut = text.split("/")[text.split("/").length-2];
                return cut.split("e")[1] + random;
            //Zeit
            } case 3: {
                String[] array = text.split("/");
                String cut = array[array.length - 1];
                String[] date = array[array.length - 2].substring(2).split("-");
                return date[1] + date[0] +
                        (int)cut.charAt(0) +
                        (int)cut.charAt(1) +
                        (int)cut.charAt(cut.length()-2) +
                        (int)cut.charAt(cut.length()-1) + random;
            //Guardian
            } case 4: {
                String[] array = text.split("/");
                String year = array[array.length - 4];
                String monthText = array[array.length - 3];
                String month = Integer.toString((int) monthText.charAt(0)) + ((int) monthText.charAt(1)) + ((int) monthText.charAt(2));
                String day = array[array.length - 2];
                String keyText = array[array.length - 1].substring(array[array.length - 1].length()-3, array[array.length - 1].length()-1);
                String key = Integer.toString((int) keyText.charAt(0)) + ((int) keyText.charAt(1));
                return day+month+year+key+random;
            }
            default: return "error in id";
        }
    }
    /**Diese Funktion iteriert über die Listen und findet heraus ob der Link sich in der Blacklist befindet */
    private static boolean isInList(String e) {
        for (String x: blacklist) if(e.startsWith(x)) return true;
        return false;
    }
}