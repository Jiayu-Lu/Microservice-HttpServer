package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.client.MongoCollection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class driverTime implements HttpHandler{

	@Override
	public void handle(HttpExchange r) throws IOException {
		try {
	         if (r.getRequestMethod().equals("GET")) {
	        	 handleGET(r);
	         } else {
	        	 JSONObject res = new JSONObject();
	             res.put("status", "NOT FOUND");
	             String response = res.toString();
	             r.sendResponseHeaders(404, response.length());
	             OutputStream os = r.getResponseBody();
	             os.write(response.getBytes());
	             os.close();
	         }
	     } catch (Exception e) {
	        e.printStackTrace();
	     }
		
	}
	
	public void handleGET(HttpExchange r) throws IOException, JSONException, InterruptedException {
		int statusCode = 400;
		JSONObject res = new JSONObject();
		try {
			String requestURI = r.getRequestURI().toString();
	        String[] uriSplitter = requestURI.split("/");
	        if (uriSplitter.length != 4) {
	            statusCode = 400;
	            res.put("status", "BAD REQUEST");
	            String response = res.toString();
	            r.sendResponseHeaders(statusCode, response.length());
	            OutputStream os = r.getResponseBody();
	            os.write(response.getBytes());
	            os.close();
	            return;
	        }
	        String _id = uriSplitter[3];
	        
	        Document doc = new Document();
	        
	        try {
	        	doc.put("_id", new ObjectId(_id));
	        } catch (IllegalArgumentException e) {
	        	statusCode = 400;
	            res.put("status", "BAD REQUEST");
	            String response = res.toString();
	            r.sendResponseHeaders(statusCode, response.length());
	            OutputStream os = r.getResponseBody();
	            os.write(response.getBytes());
	            os.close();
	            return;
	        }
			Document tripDoc = Utils.collection.find(doc).first();
			if (!tripDoc.isEmpty()) {
				String driverUid = tripDoc.get("driver").toString();
				String passengerUid = tripDoc.get("passenger").toString();
				
				String requestURL = "http://locationmicroservice:8000/location/navigation/" + driverUid + "?passengerUid=" + passengerUid;
				HttpClient client = HttpClient.newHttpClient();
				HttpRequest request = HttpRequest.newBuilder()
						.GET()
						.uri(URI.create(requestURL))
						.build();
				HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
				
				JSONObject requestJSON = new JSONObject(httpResponse.body());
				JSONObject data = new JSONObject();
				if (httpResponse.statusCode() == 200) {
					JSONObject responseData = (JSONObject) requestJSON.get("data");
					int total_time = responseData.getInt("total_time");
					data.put("total_time", total_time);
					statusCode = 200;
			        res.put("status", "OK");
			        res.put("data", data);
			        String response = res.toString();
			        r.sendResponseHeaders(statusCode, response.length());
			        OutputStream os = r.getResponseBody();
			        os.write(response.getBytes());
			        os.close();
				} else {
					statusCode = 404;
			        res.put("status", "NOT FOUND");
			        res.put("data", data);
			        String response = res.toString();
			        r.sendResponseHeaders(statusCode, response.length());
			        OutputStream os = r.getResponseBody();
			        os.write(response.getBytes());
			        os.close();
				}
				
				
			} else {
				statusCode = 404;
		        res.put("status", "NOT DOUND");
		        String response = res.toString();
		        r.sendResponseHeaders(statusCode, response.length());
		        OutputStream os = r.getResponseBody();
		        os.write(response.getBytes());
		        os.close();
			}
		} catch (Exception e) {
			statusCode = 500;
	        res.put("status", "INTERNAL SERVER ERROR");
	        String response = res.toString();
	        r.sendResponseHeaders(statusCode, response.length());
	        OutputStream os = r.getResponseBody();
	        os.write(response.getBytes());
	        os.close();
		}
	}

}
