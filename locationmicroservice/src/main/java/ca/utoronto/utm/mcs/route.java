package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import netscape.javascript.JSObject;
import org.json.*;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import static org.neo4j.driver.Values.parameters;

public class route implements HttpHandler {
   @Override
   public void handle(HttpExchange r) throws IOException {
      try {
         if (r.getRequestMethod().equals("POST")) {
            routeAdd(r);
         } else if (r.getRequestMethod().equals("DELETE")) {
            routeDelete(r);
         }
         else if (r.getRequestMethod().equals("GET")) {
            getNearbyDriver(r);
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

   private void getNearbyDriver(HttpExchange r) throws IOException, JSONException {
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
      // if there can not be split into uid and radius, send 400 and return
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
      String uid = parameterSplitter[0];
      String radius_string = parameterSplitter[1].substring(parameterSplitter[1].indexOf("=") + 1);
      double radius = Double.valueOf(radius_string) * 1000;
      if(uid.isEmpty() || radius_string.isEmpty()) {
         // no uid provided or no radius provided
         statusCode = 400;
         res.put("status", "BAD REQUEST");
      }else{
         // have all required input
         String nearbyDriverQuery = "MATCH (u:user {uid : $x}),(n:user {is_driver: true}) " +
                                     "WHERE distance(point({longitude: u.longitude, latitude: u.latitude}), point({longitude: n.longitude, latitude: n.latitude})) <= $y " +
                                     "RETURN n.uid, n.longitude, n.latitude, n.street_at";
         try (Session session = Utils.driver.session()) {
            Result driver_result = session.run(nearbyDriverQuery, parameters("x", uid, "y", radius));
            JSONObject data = new JSONObject();
            if(driver_result.hasNext()){
               while(driver_result.hasNext()){
                  Record driver = driver_result.next();
                  String driverUid = driver.get("n.uid").asString();
                  double driver_longitude = driver.get("n.longitude").asDouble();
                  double driver_latitude = driver.get("n.latitude").asDouble();
                  String driver_street = driver.get("n.street_at").asString();
                  JSONObject validDriver = new JSONObject();
                  validDriver.put("longitude",driver_longitude);
                  validDriver.put("latitude",driver_latitude);
                  validDriver.put("street_at",driver_street);
                  data.put(driverUid,validDriver);
               }
               statusCode = 200;
               res.put("data", data);
               res.put("status", "OK");
         }else{
               //Not Found
               statusCode = 404;
               res.put("status", "NOT FOUND");
            }
      }catch (Exception e) {
         statusCode = 500;
         res.put("status", "INTERNAL SERVER ERROR");
      }
         String response = res.toString();
         r.sendResponseHeaders(statusCode, response.length());
         OutputStream os = r.getResponseBody();
         os.write(response.getBytes());
         os.close();
      }
   }


   private void routeAdd(HttpExchange r) throws IOException, JSONException {
      String body = Utils.convert(r.getRequestBody());
      JSONObject res = new JSONObject();
      JSONObject req = new JSONObject(body);
      int statusCode = 400;
      if (req.has("roadName1") && req.has("roadName2") && req.has("hasTraffic") && req.has("time")) {
         try (Session session = Utils.driver.session()) {
            String road1 = req.getString("roadName1");
            String road2 = req.getString("roadName2");
            Boolean isTraffic = req.getBoolean("hasTraffic");
            int time = req.getInt("time");
            String preparedStatement = "MATCH (r1:road {name: $x}), (r2:road {name: $y}) "
                  + "CREATE (r1) -[r:ROUTE_TO {travel_time: $z, is_traffic: $u}]->(r2) RETURN type(r)";
            Result result = session.run(preparedStatement,
                  parameters("x", road1, "y", road2, "z", time, "u", isTraffic));
            if (result.hasNext()) {
               // relationship created
               statusCode = 200;
               res.put("status", "OK");
            } else {
               statusCode = 500;
               res.put("status", "INTERNAL SERVER ERROR");
            }
         } catch (Exception e) {
            statusCode = 500;
            res.put("status", "INTERNAL SERVER ERROR");
         }
      } else {
         res.put("status", "BAD REQUEST");
      }
      String response = res.toString();
      r.sendResponseHeaders(statusCode, response.length());
      OutputStream os = r.getResponseBody();
      os.write(response.getBytes());
      os.close();
   }

   private void routeDelete(HttpExchange r) throws IOException, JSONException {
      String body = Utils.convert(r.getRequestBody());
      JSONObject res = new JSONObject();
      JSONObject req = new JSONObject(body);
      int statusCode = 400;
      if (req.has("roadName1") && req.has("roadName2")) {
         try (Session session = Utils.driver.session()) {
            String road1 = req.getString("roadName1");
            String road2 = req.getString("roadName2");
            String preparedStatement = "MATCH (r1:road {name: $x})-[r:ROUTE_TO]->(r2:road {name: $y}) "
                  + "DELETE r";
            // relationship deletion action
            session.run(preparedStatement, parameters("x", road1, "y", road2));
            statusCode = 200;
            res.put("status", "OK");
         } catch (Exception e) {
            statusCode = 500;
            res.put("status", "INTERNAL SERVER ERROR");
         }
      } else {
         res.put("status", "BAD REQUEST");
      }
      String response = res.toString();
      r.sendResponseHeaders(statusCode, response.length());
      OutputStream os = r.getResponseBody();
      os.write(response.getBytes());
      os.close();
   }
}
