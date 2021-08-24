package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;

public class ApigatewayThread extends Thread{
	public HttpExchange r;
	public String requestMethod;
	public String requestURI;
	public String requestBody;
	public String targetContainer;
	  
	public void run(){       
       String targetURI = requestURI.replace("8004", "8000").replace("localhost", targetContainer);
       System.out.println(targetURI + "\n" + requestMethod + "\n" + requestBody);
	   HttpClient client = HttpClient.newHttpClient();
	   HttpRequest request = HttpRequest.newBuilder()
										.method(requestMethod, HttpRequest.BodyPublishers.ofString(requestBody))
										.uri(URI.create(targetURI))
										.build();
	   try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			
            String res = response.body();
            System.out.println(res);
            r.sendResponseHeaders(response.statusCode(), res.length());
            OutputStream os = r.getResponseBody();
            os.write(res.getBytes());
            os.close();
		} catch (Exception e) {
			e.printStackTrace();
			String res = "{status: INTERNAL SREVRE ERROR}";
			try {
				r.sendResponseHeaders(500, res.length());
				OutputStream os = r.getResponseBody();
	            os.write(res.getBytes());
	            os.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public void setAttributes(HttpExchange r, String targetContainer) throws IOException {
		this.r = r;	  
		this.requestMethod = r.getRequestMethod();
		this.requestURI = "http://" + r.getRequestHeaders().getFirst("Host") + r.getRequestURI().toString();
		this.requestBody = Utils.convert(r.getRequestBody());
		this.targetContainer = targetContainer;
	}
}
