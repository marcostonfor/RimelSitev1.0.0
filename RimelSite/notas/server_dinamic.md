# Vamos a hacer que tu aplicación sea completamente dinámica,

## generando el contenido HTML en el servidor y enviándolo al cliente. Aquí tienes cómo hacerlo:

### Paso 1: Modificar la clase `RssFeedServer` para generar HTML dinámico

Vamos a modificar la clase `RssFeedServer` para que genere el contenido HTML dinámicamente y lo envíe al cliente:

```java
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class RssFeedServer {
    private HttpServer server;
    private RimelSiteScrapingWeb rssReader;

    public RssFeedServer(String rssUrl, int port) throws IOException {
        rssReader = new RimelSiteScrapingWeb(rssUrl);
        server = HttpServer.create(new InetSocketAddress(port), 0);
        setupEndpoints();
    }

    private void setupEndpoints() {
        server.createContext("/rssfeed", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equals(exchange.getRequestMethod())) {
                    JSONArray rssFeedJson = rssReader.getRssFeedAsJson();
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

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equals(exchange.getRequestMethod())) {
                    JSONArray rssFeedJson = rssReader.getRssFeedAsJson();
                    StringBuilder htmlResponse = new StringBuilder();
                    htmlResponse.append("<!DOCTYPE html>");
                    htmlResponse.append("<html lang=\"en\">");
                    htmlResponse.append("<head>");
                    htmlResponse.append("<meta charset=\"UTF-8\">");
                    htmlResponse.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
                    htmlResponse.append("<title>RSS Feed</title>");
                    htmlResponse.append("<style>");
                    htmlResponse.append("body { font-family: Arial, sans-serif; }");
                    htmlResponse.append(".feed-item { margin-bottom: 20px; }");
                    htmlResponse.append(".feed-item h2 { margin: 0; }");
                    htmlResponse.append(".feed-item p { margin: 5px 0; }");
                    htmlResponse.append("</style>");
                    htmlResponse.append("</head>");
                    htmlResponse.append("<body>");
                    htmlResponse.append("<h1>RSS Feed</h1>");
                    htmlResponse.append("<div id=\"feed\">");

                    for (int i = 0; i < rssFeedJson.length(); i++) {
                        htmlResponse.append("<div class=\"feed-item\">");
                        htmlResponse.append("<h2>").append(rssFeedJson.getJSONObject(i).getString("title")).append("</h2>");
                        htmlResponse.append("<p>").append(rssFeedJson.getJSONObject(i).getString("description")).append("</p>");
                        htmlResponse.append("<a href=\"").append(rssFeedJson.getJSONObject(i).getString("link")).append("\" target=\"_blank\">Read more</a>");
                        htmlResponse.append("<p><small>").append(rssFeedJson.getJSONObject(i).getString("pubDate")).append("</small></p>");
                        htmlResponse.append("</div>");
                    }

                    htmlResponse.append("</div>");
                    htmlResponse.append("</body>");
                    htmlResponse.append("</html>");

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
    }

    public void start() {
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port " + server.getAddress().getPort());
    }

    public void stop(int delay) {
        server.stop(delay);
        System.out.println("Server stopped");
    }
}
```

### Paso 2: Integración con tu clase `main`

Tu clase `main` debería seguir igual, iniciando el servidor:

```java
public class Main {
    public static void main(String[] args) {
        try {
            RssFeedServer rssFeedServer = new RssFeedServer("https://mascotasdezero.com/rss.php", 8000);
            rssFeedServer.start();

            // Puedes agregar lógica adicional aquí si es necesario

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

Con estos pasos, tu servidor generará el contenido HTML dinámicamente y lo enviará al cliente cuando se acceda a la raíz (`/`). Esto elimina la necesidad de un archivo `index.html` estático y hace que tu aplicación sea completamente dinámica.

¿Hay algo más en lo que pueda ayudarte?

Origen: Conversación con Copilot 17/10/2024
(1) github.com. https://github.com/IvanIsProgrammer/InTouchServer/tree/25c73712b07054b41ebe293ee34dd81a7f3f4afa/src%2Fcom%2Fivan%2Fweb%2Fhandlers%2FRegistrationHandler.java.
(2) github.com. https://github.com/impresyjna1/TPSI/tree/c4f253dd130f65a3b0ee7b81f9de155e87d05ef3/LAB1%2Fsrc%2FTPSIServer.java.

