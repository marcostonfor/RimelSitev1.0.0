# ¡Perfecto! Vamos a crear una hoja de estilos separada y asegurarnos de que tu servidor la sirva correctamente.

## Aquí tienes los pasos detallados:

### Paso 1: Crear el archivo CSS

Crea un archivo `styles.css` en el directorio `src/main/webapp/` con tus estilos CSS. Aquí tienes un ejemplo de contenido para `styles.css`:

```css
/* src/main/webapp/styles.css */
body { 
    font-family: Arial, sans-serif; 
    background-color: #f4f4f4; 
    padding: 20px; 
}
.feed-item { 
    background-color: #fff; 
    padding: 15px; 
    margin-bottom: 20px; 
    border-radius: 5px; 
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); 
}
.feed-item h2 { 
    margin: 0 0 10px; 
    font-size: 24px; 
}
.feed-item p { 
    margin: 5px 0; 
    font-size: 16px; 
}
.feed-item a { 
    color: #3498db; 
    text-decoration: none; 
}
.feed-item a:hover { 
    text-decoration: underline; 
}
```

### Paso 2: Modificar la clase `RssFeedServer` para servir el archivo CSS

Asegúrate de que la clase `RssFeedServer` esté configurada para servir el archivo CSS desde el directorio `src/main/webapp/`:

```java
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

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
                    htmlResponse.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/styles.css\">");
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

### Paso 3: Integración con tu clase `main`

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

Con estos pasos, tendrás una hoja de estilos separada que puedes modificar fácilmente, y tu servidor la servirá correctamente junto con el contenido HTML dinámico. Esto también te permitirá aplicar SASS en el futuro si lo deseas.

¿Hay algo más en lo que pueda ayudarte?

Origen: Conversación con Copilot 17/10/2024
(1) github.com. https://github.com/IvanIsProgrammer/InTouchServer/tree/25c73712b07054b41ebe293ee34dd81a7f3f4afa/src%2Fcom%2Fivan%2Fweb%2Fhandlers%2FRegistrationHandler.java.
(2) github.com. https://github.com/impresyjna1/TPSI/tree/c4f253dd130f65a3b0ee7b81f9de155e87d05ef3/LAB1%2Fsrc%2FTPSIServer.java.


