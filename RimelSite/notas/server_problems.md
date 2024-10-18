# Por qué no sírve las páginas e interrumpe la conexión mi servidor

```java
// RssFeedServer.java

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
***

```java 
// RimelSiteScrapingWeb.java
/**
 * 
 */
package rimelSite.scraping;

/**
 * 
 */
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

public class RimelSiteScrapingWeb {
    
private String rssUrl;

    // private String rssUrl = "https://mascotasdezero.com/rss.php"; // reemplaza con la URL del feed RSS que deseas obtener
    RimelSiteScrapingWeb(String rssUrl) {
       this.setRssUrl(rssUrl);

    }

	/**
	 * @return the rssUrl
	 */
	public String getRssUrl() {
		return rssUrl;
	}

	/**
	 * @param rssUrl the rssUrl to set
	 */
	public void setRssUrl(String rssUrl) {
		this.rssUrl = rssUrl;
	}
	
    @SuppressWarnings("deprecation")
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

}







```
***

```java
// RimelSiteMain.java
/**
 * 
 */
package rimelSite.scraping;

import java.io.IOException;

/**
 * 
 */
public class RimelSiteMain {

	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
            RssFeedServer rssFeedServer = new RssFeedServer("src/main/webapp/url.txt", 8000);
            rssFeedServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}


```
***

## ¿Cómo podría evítar que se interrumpa la conexión antes de ser servída la página?

es por alguna política de cors, o es un problema de configuración en mi codigo fuente

gracias por ayudar 


***

### Aproxímacion a solución.


El problema de que la conexión se interrumpa antes de que la página sea servida puede deberse a varias causas. Vamos a repasar algunos posibles motivos que podrían estar afectando la conexión y las soluciones que puedes aplicar:

### 1. **Problemas de CORS (Cross-Origin Resource Sharing)**
Si tu servidor sirve contenido desde un origen diferente al que intenta acceder el cliente, podrías estar enfrentando problemas de CORS. Esto ocurre cuando el navegador bloquea las solicitudes debido a políticas de seguridad que impiden que los recursos se carguen desde diferentes dominios.

#### Solución:
Para habilitar CORS en tu servidor, puedes añadir los encabezados necesarios en las respuestas HTTP:

```java
exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
```

Coloca este encabezado justo antes de enviar la respuesta, ya sea en los contextos de `/rssfeed`, `/styles.css` o cualquier otro contexto que esté presentando problemas.

**Ejemplo:**
```java
exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
exchange.getResponseHeaders().set("Content-Type", "application/json");
exchange.sendResponseHeaders(200, response.getBytes().length);
```

Esto permitirá que cualquier origen (con `*`) acceda a los recursos. Si quieres restringir el acceso a ciertos dominios, puedes reemplazar el `*` con el dominio específico que debería tener acceso.

### 2. **Problemas en el manejo de excepciones (Errores silenciosos)**
En el código actual, las excepciones se están manejando con `e.printStackTrace()`, lo cual no es muy visible para el cliente y puede causar fallos silenciosos. Si ocurre una excepción en la parte de scraping o en la configuración de la conexión, el servidor podría terminar la conexión abruptamente sin respuesta clara.

#### Solución:
En lugar de solo imprimir la traza de la pila de errores, puedes devolver un mensaje de error detallado al cliente para que puedas detectar mejor dónde está fallando. Algo como esto:

```java
exchange.getResponseHeaders().set("Content-Type", "application/json");
String errorResponse = "{\"error\": \"Error while processing the RSS feed.\"}";
exchange.sendResponseHeaders(500, errorResponse.getBytes().length);
OutputStream os = exchange.getResponseBody();
os.write(errorResponse.getBytes());
os.close();
```

### 3. **Timeout en la conexión**
Si los tiempos de espera de conexión son demasiado cortos, especialmente durante la obtención de los feeds RSS, la conexión puede fallar antes de que se complete el procesamiento.

#### Solución:
Asegúrate de ajustar los tiempos de espera de la conexión en la clase `RimelSiteScrapingWeb`. Por ejemplo, puedes establecer tiempos de espera explícitos en la conexión:

```java
URLConnection connection = url.openConnection();
connection.setConnectTimeout(5000); // 5 segundos para establecer la conexión
connection.setReadTimeout(10000); // 10 segundos para leer los datos
```

### 4. **Sobreuso de recursos o ejecución sincrónica**
Si el servidor está procesando muchos feeds o si el scraping tarda demasiado tiempo, esto puede bloquear el hilo principal del servidor. En tu código, el servidor utiliza un solo hilo (`server.setExecutor(null)`), lo que significa que no hay concurrencia en las peticiones. Si el procesamiento de un feed toma demasiado tiempo, podría afectar otras solicitudes.

#### Solución:
Para mejorar el rendimiento, podrías usar un `Executor` con un `ThreadPoolExecutor` para manejar las solicitudes de manera concurrente:

```java
import java.util.concurrent.Executors;

server.setExecutor(Executors.newFixedThreadPool(10)); // Utiliza un pool de 10 hilos
```

Esto asegurará que las solicitudes no bloqueen el servidor y múltiples clientes puedan ser atendidos simultáneamente.

### 5. **Problemas con el archivo `index.html` o recursos estáticos**
Si los archivos `index.html` o `styles.css` no se están encontrando o no pueden ser leídos correctamente, podría causar que la conexión se cierre antes de que se envíe una respuesta. Asegúrate de que las rutas a estos archivos estén correctas:

```java
byte[] response = Files.readAllBytes(Paths.get("src/main/webapp/index.html"));
```

Verifica que la ruta sea accesible desde el contexto en que está ejecutándose el servidor.

### 6. **Permisos de red (Firewall) o configuraciones de servidor**
Si hay problemas en el servidor en cuanto a permisos o configuraciones de red (como firewalls bloqueando las peticiones), la conexión puede fallar antes de que se entregue la respuesta. Asegúrate de que el puerto (8000 en este caso) esté abierto y no esté bloqueado por alguna regla de firewall.

#### Solución:
- Verifica las configuraciones de red para asegurarte de que el puerto 8000 esté accesible desde donde intentas conectarte.

### 7. **Pruebas locales y logs**
Para identificar mejor dónde ocurre el fallo, agrega logs adicionales en las partes críticas del código, como cuando se realiza la conexión con el feed RSS o cuando el servidor comienza a servir las páginas.

---

### Resumen de pasos a seguir:
1. **Agregar los encabezados CORS** para permitir que los navegadores accedan a tu servidor.
2. **Manejo de excepciones más detallado** para devolver errores en formato JSON al cliente.
3. **Ajustar los tiempos de espera (timeouts)** para evitar que las conexiones se interrumpan prematuramente.
4. **Usar un `ThreadPoolExecutor`** para manejar múltiples solicitudes de manera concurrente.
5. **Verificar las rutas de los archivos estáticos** como `index.html` y `styles.css`.
6. **Comprobar la configuración de red** para garantizar que el servidor esté accesible externamente.

Implementando estas mejoras, deberías poder evitar las interrupciones en la conexión y diagnosticar mejor los problemas.



### Ajuste solución.

Si cuando usas una sola URL "a fuego" en tu código fuente funciona, pero al intentar usar varias URLs desde un archivo `.txt` falla, es posible que el problema esté relacionado con alguna de las siguientes causas:

### 1. **Problema con la lectura del archivo `url.txt`**
El problema podría estar en cómo se está leyendo el archivo que contiene las URLs (`url.txt`). Si el formato del archivo o las URLs no son correctos, el servidor no podría procesarlas adecuadamente. Asegúrate de que el archivo esté bien estructurado y que las URLs estén en líneas separadas sin espacios o caracteres adicionales.

#### Solución:
1. **Verifica el archivo `url.txt`**:
   Asegúrate de que el archivo `url.txt` tenga una URL por línea y que no haya líneas en blanco o caracteres extraños.
   Ejemplo correcto del archivo `url.txt`:
   ```
   https://example.com/rss1.xml
   https://example.com/rss2.xml
   ```

2. **Verifica si se leen todas las URLs**:
   Puedes imprimir en consola las URLs que se están leyendo desde el archivo para asegurarte de que todo funciona correctamente. Modifica tu constructor para agregar un log:
   ```java
   public RssFeedServer(String urlsFilePath, int port) throws IOException {
       rssUrls = Files.readAllLines(Paths.get(urlsFilePath));
       System.out.println("URLs cargadas: " + rssUrls); // Imprime las URLs leídas
       server = HttpServer.create(new InetSocketAddress(port), 0);
       setupEndpoints();
   }
   ```

   Si no se cargan las URLs correctamente, el problema puede estar en la ruta del archivo o en la forma en que el archivo se está leyendo.

### 2. **Problema con una de las URLs**
Si una de las URLs del archivo `.txt` tiene problemas (por ejemplo, es inaccesible, tiene un formato incorrecto, o devuelve un contenido inesperado), esto podría estar causando el fallo cuando intentas procesar la lista de URLs.

#### Solución:
1. **Manejo de errores para URLs específicas**:
   Modifica tu código para que maneje los errores de manera más robusta, ignorando las URLs que no funcionen y continuando con las otras. Puedes agregar un bloque `try-catch` en el bucle que procesa las URLs:

   ```java
   JSONArray rssFeedJson = new JSONArray();
   for (String rssUrl : rssUrls) {
       try {
           RimelSiteScrapingWeb rssReader = new RimelSiteScrapingWeb(rssUrl);
           JSONArray feed = rssReader.getRssFeedAsJson();
           for (int i = 0; i < feed.length(); i++) {
               rssFeedJson.put(feed.getJSONObject(i));
           }
       } catch (Exception e) {
           System.err.println("Error al procesar la URL: " + rssUrl);
           e.printStackTrace();
       }
   }
   ```

   Esto evitará que el servidor se caiga si una URL falla, y te permitirá ver en los logs si alguna URL específica está causando el problema.

2. **Prueba cada URL individualmente**:
   Antes de intentar usar varias URLs desde el archivo `.txt`, prueba cada URL manualmente para asegurarte de que todas funcionan correctamente. Si una URL no responde o es lenta, puede estar afectando el rendimiento general del servidor.

### 3. **Timeouts o problemas de rendimiento**
Si los feeds RSS de las URLs son grandes o responden lentamente, es posible que el tiempo de espera para recibir los datos se agote antes de completar la carga de todos los feeds. Esto puede causar que el servidor cierre la conexión antes de servir la respuesta completa.

#### Solución:
1. **Incrementa el timeout en `URLConnection`**:
   En tu clase `RimelSiteScrapingWeb`, puedes aumentar el tiempo de espera al conectarte a las URLs. Esto permitirá que las conexiones más lentas tengan más tiempo para completarse sin fallar.

   ```java
   URLConnection connection = url.openConnection();
   connection.setConnectTimeout(10000); // 10 segundos para conectar
   connection.setReadTimeout(20000); // 20 segundos para leer la respuesta
   ```

2. **Manejo de feeds RSS por partes**:
   Si los feeds son muy grandes, puede que quieras limitar la cantidad de entradas que procesas de cada feed para evitar que tu servidor se sobrecargue. Esto puede hacerse añadiendo una condición en tu bucle `for` que limite, por ejemplo, a 10 o 20 artículos por feed.

   ```java
   for (int i = 0; i < Math.min(10, items.getLength()); i++) {
       // Procesar solo los primeros 10 artículos
   }
   ```

### 4. **Sincronización y ejecución paralela de las solicitudes**
Si estás procesando varias URLs de manera secuencial en un solo hilo, el procesamiento podría tomar demasiado tiempo, lo que podría causar que las conexiones expiren antes de completarse.

#### Solución:
1. **Procesar las URLs de manera concurrente**:
   Considera ejecutar el procesamiento de cada URL en un hilo separado para que las conexiones no bloqueen todo el servidor. Puedes usar un `ExecutorService` para esto:

   ```java
   ExecutorService executor = Executors.newFixedThreadPool(5); // 5 hilos concurrentes
   for (String rssUrl : rssUrls) {
       executor.submit(() -> {
           try {
               RimelSiteScrapingWeb rssReader = new RimelSiteScrapingWeb(rssUrl);
               JSONArray feed = rssReader.getRssFeedAsJson();
               synchronized (rssFeedJson) {
                   for (int i = 0; i < feed.length(); i++) {
                       rssFeedJson.put(feed.getJSONObject(i));
                   }
               }
           } catch (Exception e) {
               e.printStackTrace();
           }
       });
   }
   executor.shutdown();
   executor.awaitTermination(30, TimeUnit.SECONDS); // Espera a que todos los hilos terminen
   ```

   Esto te permitirá procesar varias URLs al mismo tiempo sin que una URL lenta afecte las demás.

### 5. **Problemas con la ruta del archivo `url.txt`**
Si el archivo `url.txt` no se está leyendo correctamente por un problema de ruta, esto puede hacer que la lista de URLs esté vacía o que el programa falle al intentar acceder a las URLs.

#### Solución:
Verifica que la ruta del archivo sea correcta y accesible desde donde se está ejecutando tu aplicación. Si el archivo está en la carpeta `src/main/webapp/`, asegúrate de que sea accesible desde el contexto de ejecución del servidor.

**Prueba cargando el archivo con una ruta absoluta**:

```java
String filePath = Paths.get("src/main/webapp/url.txt").toAbsolutePath().toString();
rssUrls = Files.readAllLines(Paths.get(filePath));
```

Esto te permitirá verificar que el archivo esté en el lugar correcto.

### Conclusión:
Para evitar que se interrumpa la conexión al usar varias URLs desde un archivo `.txt`, prueba estos pasos:
1. Asegúrate de que el archivo `url.txt` esté correctamente formateado y las URLs se lean bien.
2. Maneja los errores individuales de cada URL para que una URL problemática no cause un fallo general.
3. Ajusta los tiempos de espera (timeouts) en las conexiones.
4. Considera procesar las URLs de manera concurrente para mejorar el rendimiento.
5. Verifica que la ruta del archivo `url.txt` sea correcta.

Prueba estas recomendaciones y verifica si el problema persiste. Si después de esto sigue fallando, me avisas para ayudarte a hacer un diagnóstico más profundo.