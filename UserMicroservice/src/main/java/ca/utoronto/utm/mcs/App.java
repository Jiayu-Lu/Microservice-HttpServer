package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;

public class App {
   static int PORT = 8000;

   public static void main(String[] args) {
	   try {
		   HttpServer server;
		   server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
		   server.createContext("/user", new User());
		   server.start();
		   System.out.printf("Server started on port %d...\n", PORT);
	   } catch (Exception e) {
			e.printStackTrace();
	   }
		
   }
}
