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
	        RssFeedServer server = new RssFeedServer("src/main/webapp/url.txt", 8000);
	        server.start();
	        System.out.println("Server started on port 8000");
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}


}
