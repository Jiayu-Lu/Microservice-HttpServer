package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import com.sun.net.httpserver.HttpServer;

public class App {
	
	static int PORT = 8000;
	
   public static void main(String[] args) {
	  HttpServer server;
	try {
		server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
		server.createContext("/trip", new trip());//Patch
		server.createContext("/trip/request", new request());//Post
		server.createContext("/trip/confirm", new confirm());//Post
		server.createContext("/trip/passenger", new passenger());//Get
		server.createContext("/trip/driver", new driver());//Get
		server.createContext("/trip/driverTime", new driverTime());//Get
		server.start();
	    System.out.printf("Server started on port %d...\n", PORT);
	} catch (IOException e) {
		e.printStackTrace();
	}
}}
