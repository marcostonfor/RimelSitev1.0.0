# ¡Entendido! Vamos a crear una clase funcional que puedas utilizar desde tu clase `main` existente. 

## Esta clase se encargará de iniciar el servidor HTTP y servir los datos JSON.

### Clase `RssFeedServer`

Aquí tienes la clase `RssFeedServer` que puedes integrar en tu proyecto:

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
                    byte[] response = Files.readAllBytes(Paths.get("public/index.html"));
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

### Uso de la clase `RssFeedServer` desde tu clase `main`

Ahora, puedes usar esta clase desde tu clase `main` existente. Aquí tienes un ejemplo de cómo hacerlo:

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

Con esto, tu clase `RssFeedServer` se encargará de iniciar el servidor HTTP y servir los datos JSON, mientras que tu clase `main` simplemente iniciará el servidor.

¿Hay algo más en lo que pueda ayudarte?

Origen: Conversación con Copilot 17/10/2024
(1) github.com. https://github.com/IvanIsProgrammer/InTouchServer/tree/25c73712b07054b41ebe293ee34dd81a7f3f4afa/src%2Fcom%2Fivan%2Fweb%2Fhandlers%2FRegistrationHandler.java.
(2) github.com. https://github.com/kamilszmajdzinski/tpsi_headers/tree/7c94612d9864344aaff67f9f22277da2275368ea/src%2Fmain%2Fjava%2FTPSIServer.java.

