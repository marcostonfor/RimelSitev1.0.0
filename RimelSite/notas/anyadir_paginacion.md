# Para escalar el código de tu servidor de RSS y lograr que se generen nuevos documentos HTML dinámicos para cada bloque de noticias, se puede implementar una lógica de **paginación** en el servidor.

##  Esto permitirá que, en lugar de mostrar todas las noticias en una sola página, el servidor sirva noticias en bloques de 9 (usando el layout 3x3) y que se genere un nuevo documento HTML para cada bloque de noticias.

### Implementación de la solución

1. **Dividir el contenido en páginas**: Para implementar la paginación, vamos a dividir el contenido RSS en páginas de 9 elementos. Esto permitirá que se muestre un conjunto de noticias por página en un grid 3x3.
2. **Añadir un parámetro de página en la URL**: Se puede agregar un parámetro en la URL para que el cliente solicite diferentes páginas, como `/page/1`, `/page/2`, etc. Cada página mostrará el siguiente bloque de noticias.
3. **Mantener el layout 3x3**: El CSS no cambiará, y seguirá mostrando las noticias en el grid 3x3 por página.

### Modificación en el código actual:

#### 1. Añadir el soporte de paginación en el endpoint HTML

Vamos a modificar el endpoint que sirve el contenido HTML (`"/"`), para que reciba un parámetro de página, y en función de la página solicitada, devuelva el bloque de noticias correspondiente.

```java
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
            htmlResponse.append("<footer>");
            htmlResponse.append("<nav>");
            if (page > 1) {
                htmlResponse.append("<a href=\"/?page=").append(page - 1).append("\">Página anterior</a>");
            }
            if (end < rssFeedJson.length()) {
                if (page > 1) {
                    htmlResponse.append(" | ");
                }
                htmlResponse.append("<a href=\"/?page=").append(page + 1).append("\">Página siguiente</a>");
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
```

#### 2. Implementación del parámetro de página

- El servidor ahora recibirá una petición con un parámetro `page` en la URL (`/?page=1`, `/?page=2`, etc.).
- La variable `page` determinará qué bloque de noticias mostrar. Si no se especifica, mostrará la primera página por defecto.
- Se paginarán 9 noticias por página y se mostrarán en el layout 3x3.

#### 3. Modificaciones necesarias en los enlaces HTML

- Agregarás enlaces de "Página siguiente" y "Página anterior" para que el usuario pueda navegar entre los diferentes bloques de noticias.
- Estos enlaces estarán en el footer del HTML generado.

### Conclusión

Con esta modificación, el servidor escalará para manejar bloques de noticias de 9 en 9, generando un nuevo documento HTML por cada bloque de noticias. La paginación permitirá al usuario navegar entre las diferentes páginas de noticias manteniendo el mismo layout y estilos CSS.


