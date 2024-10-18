# Voy a explicarte claramente dónde debe ir cada parte del código y cómo se integra en tu servidor `HttpServer` de Java.

## En tu clase `RssFeedServer`, el problema surgía porque intentabas usar una variable `rssReader` que no estaba definida en el contexto de tu método `handle`. La solución que te propuse es correcta, pero entiendo que pueda haber sido un poco confusa en cuanto a su integración en tu código.

Voy a explicarte cómo queda el código completo y cómo organizarlo dentro de tu proyecto.

### Estructura del Proyecto
Tu proyecto tiene varias clases importantes:
1. `RssFeedServer`: Esta clase configura el servidor HTTP y define los endpoints (`/rssfeed`, `/` y `/styles.css`).
2. `RimelSiteScrapingWeb`: Esta clase se encarga de obtener y convertir los feeds RSS de las URLs en formato JSON.
3. `RimelSiteMain`: Esta es la clase principal donde inicias el servidor.

### Cómo Organizar el Código

#### 1. Clase `RssFeedServer`
Esta clase configura tu servidor HTTP, define los contextos (rutas) y gestiona las peticiones.

Aquí está el código actualizado para la clase `RssFeedServer`, con las correcciones necesarias para que funcione con la lista de URLs del archivo `.txt`:

```java
package rimelSite.scraping;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class RssFeedServer {
    private HttpServer server;
    private List<String> rssUrls; // Lista de URLs leídas desde el archivo

    public RssFeedServer(String urlsFilePath, int port) throws IOException {
        rssUrls = Files.readAllLines(Paths.get(urlsFilePath)); // Cargar las URLs desde el archivo .txt
        server = HttpServer.create(new InetSocketAddress(port), 0); // Crear servidor en el puerto especificado
        setupEndpoints(); // Configurar los endpoints
    }

    private void setupEndpoints() {
        // Endpoint que devuelve el feed RSS como JSON
        server.createContext("/rssfeed", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equals(exchange.getRequestMethod())) {
                    JSONArray rssFeedJson = new JSONArray();  // JSON final con todos los feeds

                    // Iterar sobre las URLs almacenadas en rssUrls y obtener sus feeds
                    for (String rssUrl : rssUrls) {
                        RimelSiteScrapingWeb rssReader = new RimelSiteScrapingWeb(rssUrl);  // Crear un nuevo lector para cada URL
                        JSONArray feed = rssReader.getRssFeedAsJson();  // Obtener el feed RSS en formato JSON
                        for (int i = 0; i < feed.length(); i++) {
                            rssFeedJson.put(feed.getJSONObject(i));  // Añadir cada entrada del feed al JSON general
                        }
                    }

                    // Enviar la respuesta JSON
                    String response = rssFeedJson.toString(2);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
                }
            }
        });

        // Endpoint que devuelve el contenido RSS en HTML
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equals(exchange.getRequestMethod())) {
                    JSONArray rssFeedJson = new JSONArray();  // JSON final con todos los feeds

                    // Iterar sobre las URLs almacenadas en rssUrls y obtener sus feeds
                    for (String rssUrl : rssUrls) {
                        RimelSiteScrapingWeb rssReader = new RimelSiteScrapingWeb(rssUrl);  // Crear un nuevo lector para cada URL
                        JSONArray feed = rssReader.getRssFeedAsJson();  // Obtener el feed RSS en formato JSON
                        for (int i = 0; i < feed.length(); i++) {
                            rssFeedJson.put(feed.getJSONObject(i));  // Añadir cada entrada del feed al JSON general
                        }
                    }

                    // Crear la respuesta HTML a partir del JSON
                    StringBuilder htmlResponse = new StringBuilder();
                    htmlResponse.append("<!DOCTYPE html>");
                    htmlResponse.append("<html lang=\"en\">");
                    htmlResponse.append("<head>");
                    htmlResponse.append("<meta charset=\"UTF-8\">");
                    htmlResponse.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
                    htmlResponse.append("<title>RSS Feed</title>");
                    htmlResponse.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/styles.css\">");
                    htmlResponse.append("</head>");
                    htmlResponse.append("<body>");
                    htmlResponse.append("<h1>RSS Feed</h1>");
                    htmlResponse.append("<div id=\"container\">");
                    htmlResponse.append("<div id=\"feed\">");

                    // Añadir el contenido del feed al HTML
                    for (int i = 0; i < rssFeedJson.length(); i++) {
                        htmlResponse.append("<div class=\"feed-item\">");
                        htmlResponse.append("<h2>").append(rssFeedJson.getJSONObject(i).getString("title")).append("</h2>");
                        htmlResponse.append("<p>").append(rssFeedJson.getJSONObject(i).getString("description")).append("</p>");
                        htmlResponse.append("<a href=\"").append(rssFeedJson.getJSONObject(i).getString("link"))
                                .append("\" target=\"_blank\">Read more</a>");
                        htmlResponse.append("<p><small>").append(rssFeedJson.getJSONObject(i).getString("pubDate"))
                                .append("</small></p>");
                        htmlResponse.append("</div>");
                    }

                    htmlResponse.append("</div>");
                    htmlResponse.append("</div>");
                    htmlResponse.append("</body>");
                    htmlResponse.append("</html>");

                    // Enviar la respuesta HTML
                    String response = htmlResponse.toString();
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
                }
            }
        });

        // Endpoint para el archivo CSS
        server.createContext("/styles.css", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equals(exchange.getRequestMethod())) {
                    byte[] response = Files.readAllBytes(Paths.get("src/main/webapp/styles.css"));
                    exchange.getResponseHeaders().set("Content-Type", "text/css");
                    exchange.sendResponseHeaders(200, response.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response);
                    os.close();
                } else {
                    exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
                }
            }
        });
    }

    public void start() {
        server.setExecutor(null); // Crear un ejecutor por defecto
        server.start();  // Iniciar el servidor
        System.out.println("Server started on port " + server.getAddress().getPort());
    }

    public void stop(int delay) {
        server.stop(delay);  // Detener el servidor con un retraso
        System.out.println("Server stopped");
    }
}
```

#### 2. Clase `RimelSiteScrapingWeb`
Esta clase no necesita cambios. Aquí está nuevamente para referencia:

```java
package rimelSite.scraping;

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RimelSiteScrapingWeb {
    private String rssUrl;

    public RimelSiteScrapingWeb(String rssUrl) {
        this.rssUrl = rssUrl;
    }

    public JSONArray getRssFeedAsJson() {
        JSONArray jsonArray = new JSONArray();
        try {
            URL url = new URL(rssUrl);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            NodeList items = doc.getElementsByTagName("item");

            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("title", item.getElementsByTagName("title").item(0).getTextContent());
                jsonObject.put("link", item.getElementsByTagName("link").item(0).getTextContent());
                jsonObject.put("description", item.getElementsByTagName("description").item(0).getTextContent());
                jsonObject.put("pubDate", item.getElementsByTagName("pubDate").item(0).getTextContent());
                jsonArray.put(jsonObject);
            }
       
       
       
       