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

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equals(exchange.getRequestMethod())) {
                    // Obtener el parámetro de la página desde la URL
                    String query = exchange.getRequestURI().getQuery();
                    int page = 1; // Valor predeterminado para la página
                    if (query != null && query.startsWith("page=")) {
                        page = Integer.parseInt(query.split("=")[1]);
                    }

                    // Cargar todas las noticias de las URLs
                    JSONArray rssFeedJson = new JSONArray();
                    for (String rssUrl : rssUrls) {
                        RimelSiteScrapingWeb rssReader = new RimelSiteScrapingWeb(rssUrl);
                        JSONArray feed = rssReader.getRssFeedAsJson();
                        for (int i = 0; i < feed.length(); i++) {
                            rssFeedJson.put(feed.getJSONObject(i));
                        }
                    }
                    
                    
                    

                    // Configurar el tamaño de la paginación (9 noticias por página)
                    int pageSize = 9;
                    int start = (page - 1) * pageSize;
                    int end = Math.min(start + pageSize, rssFeedJson.length());

                    // Crear el HTML con el bloque de noticias para la página solicitada
                    StringBuilder htmlResponse = new StringBuilder();
                    htmlResponse.append("<!DOCTYPE html>");
                    htmlResponse.append("<html lang=\"en\">");
                    htmlResponse.append("<head>");
                    htmlResponse.append("<meta charset=\"UTF-8\">");
                    htmlResponse.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
                    htmlResponse.append("<title>RSS Feed - Página ").append(page).append("</title>");
                    htmlResponse.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/styles.css\">");
                    htmlResponse.append("</head>");
                    htmlResponse.append("<body>");
                    htmlResponse.append("<header>");
                    htmlResponse.append("<h1>Difusor de Animales domésticos Abandonados - Página ").append(page).append("</h1>");
                    htmlResponse.append("<h2>Web de Rimel</h2>");
                    htmlResponse.append("</header>");
                    htmlResponse.append("<main>");
                    htmlResponse.append("<div id=\"container\">");
                    htmlResponse.append("<div id=\"feed\">");

                    // Añadir las noticias del bloque correspondiente a esta página
                    for (int i = start; i < end; i++) {
                        htmlResponse.append("<div class=\"feed-item\">");
                        htmlResponse.append("<h2>").append(rssFeedJson.getJSONObject(i).getString("title")).append("</h2>");
                        htmlResponse.append("<p>").append(rssFeedJson.getJSONObject(i).getString("description")).append("</p>");
                        htmlResponse.append("<a href=\"").append(rssFeedJson.getJSONObject(i).getString("link"))
                                .append("\" target=\"_blank\">Leer más</a>");
                        htmlResponse.append("<p><small>").append(rssFeedJson.getJSONObject(i).getString("pubDate"))
                                .append("</small></p>");
                        htmlResponse.append("</div>");
                    }

                    htmlResponse.append("</div>");
                    htmlResponse.append("</div>");
                    htmlResponse.append("</main>");

                    // Agregar enlaces de paginación
                 // Agregar enlaces de paginación con secuencia de 5 números
                    htmlResponse.append("<footer>");
                    htmlResponse.append("<nav>");

                    // Calcular el total de páginas
                    int totalPages = (int) Math.ceil((double) rssFeedJson.length() / pageSize);

                    // Enlace "Primero"
                    if (page > 1) {
                        htmlResponse.append("<a href=\"/?page=1\">Primero</a> | ");
                    }

                    // Enlace "Página anterior"
                    if (page > 1) {
                        htmlResponse.append("<a href=\"/?page=").append(page - 1).append("\">Página anterior</a> | ");
                    }

                    // Mostrar secuencia de 5 páginas
                    int startPage = Math.max(1, page - 2);  // Inicia la secuencia hasta 2 páginas antes de la actual
                    int endPage = Math.min(totalPages, page + 2);  // Termina la secuencia hasta 2 páginas después de la actual

                    for (int i = startPage; i <= endPage; i++) {
                        if (i == page) {
                            // Página actual, no es un enlace
                            htmlResponse.append("<strong>").append(i).append("</strong> ");
                        } else {
                            // Otras páginas como enlaces
                            htmlResponse.append("<a href=\"/?page=").append(i).append("\">").append(i).append("</a> ");
                        }
                    }

                    // Enlace "Página siguiente"
                    if (page < totalPages) {
                        htmlResponse.append("| <a href=\"/?page=").append(page + 1).append("\">Página siguiente</a>");
                    }

                    // Enlace "Último"
                    if (page < totalPages) {
                        htmlResponse.append(" | <a href=\"/?page=").append(totalPages).append("\">Último</a>");
                    }

                    htmlResponse.append("</nav>");
                    htmlResponse.append("<cite>&trade; &copy; <strong>Rimel Site Web.</strong> <span>AUTOR:</span><em>marcos Tonfor</em>");
                    htmlResponse.append("</footer>");

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

