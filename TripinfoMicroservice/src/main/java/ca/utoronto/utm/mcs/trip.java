package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.result.UpdateResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class trip implements HttpHandler{

	@Override
	public void handle(HttpExchange r) throws IOException {
		try {
	         if (r.getRequestMethod().equals("PATCH")) {
	        	 handlePATCH(r);
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
	
	public void handlePATCH(HttpExchange r) throws IOException, JSONException {
		int statusCode = 400;
	    JSONObject res = new JSONObject();
		try {
			String body = Utils.convert(r.getRequestBody());
	        JSONObject deserialized = new JSONObject(body);
	        if (!Utils.checkPATCHTripBody(deserialized)) {
	       	 	// Body parameters are invalid
	            res.put("status", "BAD REQUEST");
	            String response = res.toString();
	            r.sendResponseHeaders(400, response.length());
	            // Writing response body
	            OutputStream os = r.getResponseBody();
	            os.write(response.getBytes());
	            os.close();
	            return;
	        }
	        
	        int distance = Integer.parseInt(deserialized.get("distance").toString());
	        int endTime = Integer.parseInt(deserialized.get("endTime").toString());
	        String timeElapsed = deserialized.get("timeElapsed").toString();
	        double totalCost = Double.parseDouble(deserialized.get("totalCost").toString());
	        double driverPayout = Double.parseDouble(deserialized.get("driverPayout").toString());
	        double discount;
	        if (deserialized.has("discount")) {
	        	discount = Double.parseDouble(deserialized.get("discount").toString());
	        } else {
	        	discount = 0;
	        }
	        
	        String requestURI = r.getRequestURI().toString();
	        String[] uriSplitter = requestURI.split("/");
	        if (uriSplitter.length != 3) {
	            statusCode = 400;
	            res.put("status", "BAD REQUEST");
	            String response = res.toString();
	            r.sendResponseHeaders(statusCode, response.length());
	            OutputStream os = r.getResponseBody();
	            os.write(response.getBytes());
	            os.close();
	            return;
	        }
	        String _id = uriSplitter[2];
	        
	        Document doc = new Document();
			doc.put("distance", distance);
			doc.put("endTime", endTime);
			doc.put("timeElapsed", timeElapsed);
			doc.put("discount", discount);
			doc.put("totalCost", totalCost);
			doc.put("driverPayout", driverPayout);
			try {
				ObjectId objectId = new ObjectId(_id);
				UpdateResult ur = Utils.collection.updateOne(eq("_id", objectId), new Document("$set", doc));
				if (ur.getModifiedCount() != 0) {
					statusCode = 200;
			        res.put("status", "OK");
			        String response = res.toString();
			        r.sendResponseHeaders(statusCode, response.length());
			        OutputStream os = r.getResponseBody();
			        os.write(response.getBytes());
			        os.close();
				} else {
					statusCode = 404;
			        res.put("status", "NOT FOUND");
			        String response = res.toString();
			        r.sendResponseHeaders(statusCode, response.length());
			        OutputStream os = r.getResponseBody();
			        os.write(response.getBytes());
			        os.close();
				}
			} catch(Exception e) {
				statusCode = 400;
		        res.put("status", "BAD REQUEST");
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
