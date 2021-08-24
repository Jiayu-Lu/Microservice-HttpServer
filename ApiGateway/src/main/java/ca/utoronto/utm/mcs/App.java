package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class App {
   static int PORT = 8000;

   public static void main(String[] args) throws IOException {
	   HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
			server.createContext("/user", new user());
			server.createContext("/location", new location());
			server.createContext("/trip", new trip());
		    
		    server.start();

		    System.out.printf("Server started on port %d...\n", PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
   }
}
