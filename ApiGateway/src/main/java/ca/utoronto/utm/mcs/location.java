package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class location implements HttpHandler{
	
	@Override
	   public void handle(HttpExchange r) throws IOException {
	      try {
	    	  String targetContainer = "locationmicroservice";
	    	  
	    	  ApigatewayThread thread = new ApigatewayThread();
	    	  thread.setAttributes(r, targetContainer);
	    	  thread.start();	    	
	      } catch (Exception e) {
		         e.printStackTrace();
	            JSONObject data = new JSONObject();
	            try {
					data.put("status", "INTERNAL SERVER ERROR");
					String response = data.toString();
		            r.sendResponseHeaders(500, response.length());
		            OutputStream os = r.getResponseBody();
		            os.write(response.getBytes());
		            os.close();
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
	      }

	   }
	
}
