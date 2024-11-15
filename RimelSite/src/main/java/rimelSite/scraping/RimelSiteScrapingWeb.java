package rimelSite.scraping;

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXParseException;

import org.json.JSONArray;
import org.json.JSONObject;

public class RimelSiteScrapingWeb {
    
    private String rssUrl;

    // Constructor
    public RimelSiteScrapingWeb(String rssUrl) {
       this.rssUrl = rssUrl;
    }

    // Método para obtener el feed RSS en formato JSON
    public JSONArray getRssFeedAsJson() {
        JSONArray jsonArray = new JSONArray();
        try {
            @SuppressWarnings("deprecation")
			URL url = new URL(rssUrl);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            NodeList items = doc.getElementsByTagName("item");

            // Procesar los elementos <item> del RSS feed
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("title", item.getElementsByTagName("title").item(0).getTextContent());
                jsonObject.put("link", item.getElementsByTagName("link").item(0).getTextContent());
                jsonObject.put("description", item.getElementsByTagName("description").item(0).getTextContent());
                jsonObject.put("pubDate", item.getElementsByTagName("pubDate").item(0).getTextContent());
                jsonArray.put(jsonObject);
            }
        } catch (SAXParseException e) {
            // Ignorar el feed malformado pero continuar con los otros feeds
            System.err.println("Error: XML malformado en " + rssUrl + ". Saltando feed.");
        } catch (Exception e) {
            System.err.println("Error al obtener el feed RSS de " + rssUrl);
            e.printStackTrace();
        }
        return jsonArray;
    }

}