package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.*;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import static org.neo4j.driver.Values.parameters;

public class road implements HttpHandler {
   @Override
   public void handle(HttpExchange r) throws IOException {
      try {
         if (r.getRequestMethod().equals("PUT")) {
            putRoad(r);
         }
         else if (r.getRequestMethod().equals("GET")) {
            getNavigation(r);
         } else {
        	 JSONObject data = new JSONObject();
             data.put("status", "NOT FOUND");
             String response = data.toString();
             r.sendResponseHeaders(404, response.length());
             OutputStream os = r.getResponseBody();
             os.write(response.getBytes());
             os.close();
         }
      } catch (Exception e) {
         System.out.println("Error Occurred! Msg:   " + e);
      }
   }
   //TO DO
   private void getNavigation(HttpExchange r) throws IOException, JSONException {
	   int statusCode = 400;
	   JSONObject res = new JSONObject();
	   String requestURI = r.getRequestURI().toString();
	   String[] uriSplitter = requestURI.split("/");
	   // if there are extra url params send 400 and return
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
	   String parameter = uriSplitter[3];
	   String[] parameterSplitter = parameter.split("\\?");
	   // if there can not be split into driverUid and passengerUid, send 400 and return
	   if(parameterSplitter.length != 2){
		   statusCode = 400;
	   	   res.put("status", "BAD REQUEST");
	       String response = res.toString();
	       r.sendResponseHeaders(statusCode, response.length());
	       OutputStream os = r.getResponseBody();
	       os.write(response.getBytes());
	       os.close();
	       return;
	   }
	   String driverUid = parameterSplitter[0];
	   String passengerUid = parameterSplitter[1].substring(parameterSplitter[1].indexOf("=") + 1);
	   
	   
         try (Session session = Utils.driver.session()) {
        	String checkSameStreet = "MATCH (d:user{uid: $x}),(p:user{uid: $y}) " +
         			   "RETURN d.street_at, p.street_at";
        	Result checkSameStreetResult = session.run(checkSameStreet, parameters("x", driverUid, "y", passengerUid));
        	if (checkSameStreetResult.hasNext()) {
        		Record checkSameStreetRecord = checkSameStreetResult.next();
        		String driverStreet = checkSameStreetRecord.get("d.street_at").asString();
        		String passengerStreet = checkSameStreetRecord.get("p.street_at").asString();
        		if (driverStreet.equals(passengerStreet)) {
        			JSONObject data = new JSONObject();
        			data.put("total_time", 0);
                    res.put("status","OK");
                    res.put("data",data);
                    String response = res.toString();
                    r.sendResponseHeaders(200, response.length());
                    OutputStream os = r.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    return;
        		}
        	}
        	
        	
            String getStreets = "MATCH (d:user{uid: $x}),(p:user{uid: $y}) " +
                                "MATCH (start: road{name: d.street_at}), (end:road{name: p.street_at}) "+
                                 "MATCH pa=(start)-[:ROUTE_TO*]->(end) WITH pa,reduce(s=0, r IN relationships(pa) | s + r.travel_time) AS total_time " +
                                 "RETURN pa, total_time ORDER BY total_time ASC LIMIT 1";
            Result streets_result = session.run(getStreets, parameters("x", driverUid, "y", passengerUid));
            if (streets_result.hasNext()) {
               statusCode = 200;
               Record streets = streets_result.next();
               String total_time_string = streets.get("total_time").toString();
               System.out.println(total_time_string);
               // convert string into int
               int total_time = Integer.valueOf(total_time_string).intValue();
               Path path = streets.get("pa").asPath();
               // use iterator to go through the path and relationship
               Iterator<Relationship> relationships = path.relationships().iterator();
               Iterator<Node> nodes = path.nodes().iterator();
               // to deal with the start street first since it is special
               Node start = nodes.next();
               JSONObject startStreet = new JSONObject();
               startStreet.put("street",start.get("name").asString());
               startStreet.put("time",0);
               startStreet.put("has_traffic",start.get("is_traffic").asBoolean());
               // we create a list to store a JSONObject since the result is a list of JSON
               List<JSONObject> route = new ArrayList<>();
               route.add(startStreet);
               // go through the nodes and relationships
               while(relationships.hasNext()){
                  Relationship routeTo = relationships.next();
                  Node road = nodes.next();
                  JSONObject roadObject = new JSONObject();
                  roadObject.put("street", road.get("name").asString());
                  roadObject.put("time",routeTo.get("travel_time").asInt());
                  roadObject.put("has_traffic",road.get("is_traffic").asBoolean());
                  route.add(roadObject);
               }
               JSONObject data = new JSONObject();
               data.put("total_time",total_time);
               data.put("route",route);
               res.put("status","OK");
               res.put("data",data);
            } else {
               // Not Found
               statusCode = 404;
               JSONObject data = new JSONObject();
               res.put("data", data);
               res.put("status", "NOT FOUND");
            }
         } catch (Exception e) {
            // error happened
            statusCode = 500;
            res.put("status", "INTERNAL SERVER ERROR");
         }
         String response = res.toString();
         r.sendResponseHeaders(statusCode, response.length());
         OutputStream os = r.getResponseBody();
         os.write(response.getBytes());
         os.close();
   }

   private void putRoad(HttpExchange r) throws IOException, JSONException {
      String body = Utils.convert(r.getRequestBody());
      JSONObject res = new JSONObject();
      JSONObject req = new JSONObject(body);
      int statusCode = 400;
      if (req.has("roadName") && req.has("hasTraffic")) {
         String name = req.getString("roadName");
         Boolean traffic = req.getBoolean("hasTraffic");
         try (Session session = Utils.driver.session()) {
            String roadCheckQuery = "MATCH (n :road) where n.name=" + "'" + name + "' RETURN n";
            Result roadCheckResult = session.run(roadCheckQuery);
            if (roadCheckResult.hasNext()) {
               // Road found, update the info
               String update = "MATCH (n:road {name: '$x'}) SET n.is_traffic = $y RETURN n";
               Result updateRes = session.run(update, parameters("x", name, "y", traffic));
               if (updateRes.hasNext()) {
                  statusCode = 200;
                  res.put("status", "OK");
               }
            } else {
               // no user found, add the info as a new user
               String newRoadQuery = "CREATE (n: road {name:$x,is_traffic:$y}) RETURN n";
               Result newRoadRes = session.run(newRoadQuery, parameters("x", name, "y", traffic));
               if (newRoadRes.hasNext()) {
                  statusCode = 200;
                  res.put("status", "OK");
               }
            }
            String response = res.toString();
            r.sendResponseHeaders(statusCode, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
         } catch (Exception e) {
            // error happened
            res.put("status", "INTERNAL SERVER ERROR");
            statusCode = 500;
            String response = res.toString();
            r.sendResponseHeaders(statusCode, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
         }
      } else {
         res.put("status", "BAD REQUEST");
         String response = res.toString();
         r.sendResponseHeaders(statusCode, response.length());
         OutputStream os = r.getResponseBody();
         os.write(response.getBytes());
         os.close();
      }

   }
}
