package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class passenger implements HttpHandler{
	
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
	
	public void handleGET(HttpExchange r) throws Exception {
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
	        String uid = uriSplitter[3];
	        
	        MongoCollection<Document> collection = Utils.collection;
	        Document doc = new Document();
			doc.put("passenger", uid);
			FindIterable<Document> responseDoc = collection.find(doc);
			MongoCursor<Document> cursor = responseDoc.cursor();
			
			JSONObject data = new JSONObject();
			ArrayList<JSONObject> tripsArr = new ArrayList<JSONObject>();
			if (cursor.hasNext()) {
				while (cursor.hasNext()) {
					Document tripDoc = cursor.next();
					JSONObject trip = new JSONObject();
					trip.put("_id", tripDoc.get("_id"));
					trip.put("distance", tripDoc.get("distance"));
					trip.put("totalCost", tripDoc.get("totalCost"));
					trip.put("discount", tripDoc.get("discount"));
					trip.put("startTime", tripDoc.get("startTime"));
					trip.put("endTime", tripDoc.get("endTime"));
					trip.put("timeElapsed", tripDoc.get("timeElapsed"));
					trip.put("driver", tripDoc.get("driver"));
					tripsArr.add(trip);
				}
				data.put("trips", tripsArr);
				statusCode = 200;
		        res.put("status", "OK");
		        res.put("data", data);
		        String response = res.toString();
		        r.sendResponseHeaders(statusCode, response.length());
		        OutputStream os = r.getResponseBody();
		        os.write(response.getBytes());
		        os.close();
			} else {
				data.put("trips", tripsArr);
				statusCode = 404;
		        res.put("status", "NOT DOUND");
		        res.put("data", data);
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
