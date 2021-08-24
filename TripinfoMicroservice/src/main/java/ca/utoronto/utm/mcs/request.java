package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class request implements HttpHandler{

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
	
	public void handlePost(HttpExchange r) throws Exception {
		try {
			String body = Utils.convert(r.getRequestBody());
	        JSONObject deserialized = new JSONObject(body);
	        if (!(deserialized.has("uid") && deserialized.has("radius"))) {
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
	        String uid = deserialized.get("uid").toString();
	        String radius = deserialized.get("radius").toString();
	        String URL = "http://locationmicroservice:8000/location/nearbyDriver/" + uid + "?radius=" + radius;
	        HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.GET()
					.uri(URI.create(URL))
					.build();
			HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
			ArrayList<String> responseArray = new ArrayList<String>();
			if (httpResponse.statusCode() == 404) {
				JSONObject res = new JSONObject();
		        res.put("status", "NOT FOUND");
		        res.put("data", responseArray.toString());
		        String response = res.toString();
		        r.sendResponseHeaders(404, response.length());
		        OutputStream os = r.getResponseBody();
		        os.write(response.getBytes());
		        os.close();
			}
			if (httpResponse.statusCode() == 400) {
				JSONObject res = new JSONObject();
		        res.put("status", "BAD REQUEST");
		        res.put("data", responseArray.toString());
		        String response = res.toString();
		        r.sendResponseHeaders(400, response.length());
		        OutputStream os = r.getResponseBody();
		        os.write(response.getBytes());
		        os.close();
			}
			JSONObject responseJSON = new JSONObject(httpResponse.body());
			Iterator<String> keys = ((JSONObject) responseJSON.get("data")).keys();
			while(keys.hasNext()) {
			    String key = keys.next();
			    responseArray.add(key);
			}
			if (responseArray.size() == 0) {
				JSONObject res = new JSONObject();
		        res.put("status", "NOT FOUND");
		        res.put("data", responseArray.toString());
		        String response = res.toString();
		        r.sendResponseHeaders(404, response.length());
		        OutputStream os = r.getResponseBody();
		        os.write(response.getBytes());
		        os.close();
		        return;
			}
			JSONObject res = new JSONObject();
	        res.put("status", "OK");
	        res.put("data", responseArray.toString());
	        String response = res.toString();
	        r.sendResponseHeaders(200, response.length());
	        OutputStream os = r.getResponseBody();
	        os.write(response.getBytes());
	        os.close();
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

