# Vamos a adaptar tu código para que lea las URLs desde un archivo `.txt` y procese cada una de ellas.

## Aquí tienes los pasos detallados:

### Paso 1: Crear el archivo `urls.txt`

Crea un archivo `urls.txt` en el directorio raíz de tu proyecto. Este archivo contendrá una URL por línea. Por ejemplo:

```
https://mascotasdezero.com/rss.php
https://example.com/rss
https://anotherexample.com/rss
```

### Paso 2: Modificar la clase `RssFeedServer` para leer las URLs desde el archivo

Vamos a modificar la clase `RssFeedServer` para que lea las URLs desde el archivo `urls.txt` y procese cada una de ellas. Aquí tienes el código actualizado:

```java
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class RssFeedServer {
    private HttpServer server;
    private List<String> rssUrls;

    public RssFeedServer(String urlsFilePath, int port) throws IOException {
        rssUrls = Files.readAllLines(Paths.get(urlsFilePath));
        server = HttpServer.create(new InetSocketAddress(port), 0);
        setupEndpoints();
    }

    private void setupEndpoints() {
        server.createContext("/rssfeed", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equals(exchange.getRequestMethod())) {
                    JSONArray rssFeedJson = new JSONArray();
                    for (String rssUrl : rssUrls) {
                        RimelSiteScrapingWeb rssReader = new RimelSiteScrapingWeb(rssUrl);
                        JSONArray feed = rssReader.getRssFeedAsJson();
                        for (int i = 0; i < feed.length(); i++) {
                            rssFeedJson.put(feed.getJSONObject(i));
                        }
                    }
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
                    byte[] response = Files.readAllBytes(Paths.get("src/main/webapp/index.html"));
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, response.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response);
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

### Paso 3: Modificar tu clase `main`

Modifica tu clase `main` para que pase la ruta del archivo `urls.txt` al constructor de `RssFeedServer`:

```java
public class Main {
    public static void main(String[] args) {
        try {
            RssFeedServer rssFeedServer = new RssFeedServer("urls.txt", 8000);
            rssFeedServer.start();

            // Puedes agregar lógica adicional aquí si es necesario

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

Con estos cambios, tu programa leerá las URLs desde el archivo `urls.txt`, procesará cada una de ellas y generará el contenido dinámico en función de los feeds RSS obtenidos.

¿Hay algo más en lo que pueda ayudarte?

Origen: Conversación con Copilot 17/10/2024
(1) github.com. https://github.com/IvanIsProgrammer/InTouchServer/tree/25c73712b07054b41ebe293ee34dd81a7f3f4afa/src%2Fcom%2Fivan%2Fweb%2Fhandlers%2FRegistrationHandler.java.
(2) github.com. https://github.com/kamilszmajdzinski/tpsi_headers/tree/7c94612d9864344aaff67f9f22277da2275368ea/src%2Fmain%2Fjava%2FTPSIServer.java.

