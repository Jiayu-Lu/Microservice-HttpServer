package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONException;
import org.json.JSONObject;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.and;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class confirm implements HttpHandler{

	@Override
	public void handle(HttpExchange r) throws IOException {
		try {
	         if (r.getRequestMethod().equals("POST")) {
	        	 handlePost(r);
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
	
	public void handlePost(HttpExchange r) throws IOException, JSONException{
		String body;
		try {
			body = Utils.convert(r.getRequestBody());
			JSONObject deserialized = new JSONObject(body);
	        if (!(deserialized.has("driver") && deserialized.has("passenger") && deserialized.has("startTime") 
	        		&& Utils.isNumeric(deserialized.get("startTime").toString()))) {
	       	 	// Body parameters are invalid
	       	 	JSONObject res = new JSONObject();
	            res.put("status", "BAD REQUEST");
	            String response = res.toString();
	            r.sendResponseHeaders(400, response.length());
	            OutputStream os = r.getResponseBody();
	            os.write(response.getBytes());
	            os.close();
	            return;
	        }
	        String driver = deserialized.get("driver").toString();
	        String passenger = deserialized.get("passenger").toString();
	        int startTime = deserialized.getInt("startTime");
	        
	        MongoCollection<Document> collection = Utils.collection;
	        Document doc = new Document();
			doc.put("driver", driver);
			doc.put("passenger", passenger);
			doc.put("startTime", startTime);
			collection.insertOne(doc);
			
			Document myDoc = collection.find(and(eq("driver", driver), eq("passenger", passenger), eq("startTime", startTime))).first();
			
			if (myDoc.isEmpty()) {
				JSONObject res = new JSONObject();
		        res.put("status", "NOT FOUND");
		        String response = res.toString();
		        r.sendResponseHeaders(404, response.length());
		        OutputStream os = r.getResponseBody();
		        os.write(response.getBytes());
		        os.close();
			} else {
				JSONObject res = new JSONObject();
		        res.put("status", "OK");
		        res.put("data", myDoc.get("_id"));
		        String response = res.toString();
		        r.sendResponseHeaders(200, response.length());
		        OutputStream os = r.getResponseBody();
		        os.write(response.getBytes());
		        os.close();
			}
		} catch (Exception e) {
			JSONObject res = new JSONObject();
	        res.put("status", "INTERNAL SERVER ERROR");
	        String response = res.toString();
	        r.sendResponseHeaders(500, response.length());
	        OutputStream os = r.getResponseBody();
	        os.write(response.getBytes());
	        os.close();
		}
	}


	
}
