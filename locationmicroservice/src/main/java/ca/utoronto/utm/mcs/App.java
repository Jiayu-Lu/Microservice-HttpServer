package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class App {
   static int PORT = 8000;

   public static void main(String[] args) {
    HttpServer server;
	try {
		server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
		//PUT /location/user and DELETE /location/user
		server.createContext("/location/user", new user());

		// PUT /location/road
	    server.createContext("/location/road", new road());
	    // PUT /location/navigation/:driverUid?passengerUid=
	    server.createContext("/location/navigation/", new road());
	    
	    //GET /location/:uid and PATCH /location/:uid
	    server.createContext("/location", new location());

	    //POST /location/hasRoute
	    server.createContext("/location/hasRoute", new route());
	    //DELETE /location/route
	    server.createContext("/location/route", new route());
	    //GET /location/nearbyDriver/:uid?radius=
	    server.createContext("/location/nearbyDriver/", new route());
	    
	    server.start();

	      System.out.printf("Server started on port %d...\n", PORT);
	} catch (IOException e) {
		e.printStackTrace();
	}
   }
}