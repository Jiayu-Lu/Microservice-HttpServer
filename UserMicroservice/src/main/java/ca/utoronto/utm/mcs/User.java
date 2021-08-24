package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.Iterator;

public class User implements HttpHandler {
   public Connection connection;

   public User() throws ClassNotFoundException, SQLException {
      String url = "jdbc:postgresql://postgres:5432/root";
      Class.forName("org.postgresql.Driver");
      this.connection = DriverManager.getConnection(url, "root", "123456");
   }

   public static boolean isNumeric(String str) {
      try {
         Double.parseDouble(str);
         return true;
      } catch (NumberFormatException e) {
         return false;
      }
   }

   @Override
   public void handle(HttpExchange r) throws IOException {
      try {
         if (r.getRequestMethod().equals("GET")) {
            handleGET(r);
         }
         if (r.getRequestMethod().equals("PATCH")) {
            handlePATCH(r);
         }
         if (r.getRequestMethod().equals("POST")) {
        	 String[] url = r.getRequestURI().getPath().split("/");
             if (url[url.length - 1].equals("register")) {
            	 handlePOSTRegister(r);
             }
             if (url[url.length - 1].equals("login")) {
            	 handlePOSTLogin(r);
             } else {
            	 JSONObject res = new JSONObject();
            	 res.put("status", "NOT FOUND");
                 String response = res.toString();
                 r.sendResponseHeaders(404, response.length());
                 OutputStream os = r.getResponseBody();
                 os.write(response.getBytes());
                 os.close();
             }
 
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

   public void handleGET(HttpExchange r) throws IOException, JSONException, SQLException {
      String[] url = r.getRequestURI().getPath().split("/");
      System.out.println(url[url.length - 1]);
      if (isNumeric(url[url.length - 1])) {
         try {
            ResultSet rs;
            int uid = Integer.parseInt(url[url.length - 1]);
            String prepare = "SELECT prefer_name as name, email, rides, isdriver,availableCoupons, redeemedCoupons FROM users WHERE uid = ?";
            PreparedStatement ps = this.connection.prepareStatement(prepare);
            ps.setInt(1, uid);
            rs = ps.executeQuery();
            if (rs.next()) {
               JSONObject var = new JSONObject();
               String name = rs.getString("name");
               String email = rs.getString("email");
               String rides = rs.getString("rides");
               Boolean isDriver = rs.getBoolean("isdriver");
               Array availableCoupons = rs.getArray("availableCoupons");
               Array redeemedCoupons = rs.getArray("redeemedCoupons");
               var.put("name", name);
               var.put("email", email);
               var.put("rides", rides);
               var.put("is_driver", isDriver);
               var.put("availableCoupons", availableCoupons.toString());
               var.put("redeemedCoupons", redeemedCoupons.toString());
               String response = var.toString();
               r.sendResponseHeaders(200, response.length());
               // Writing response body
               OutputStream os = r.getResponseBody();
               os.write(response.getBytes());
               os.close();
            } else {
               JSONObject res = new JSONObject();
               res.put("status", "NOT FOUND");
               String response = res.toString();
               r.sendResponseHeaders(404, response.length());
               // Writing response body
               OutputStream os = r.getResponseBody();
               os.write(response.getBytes());
               os.close();
            }

         } catch (SQLException se) {
            JSONObject res = new JSONObject();
            res.put("status", "INTERNAL SERVER ERROR");
            String response = res.toString();
            r.sendResponseHeaders(500, response.length());
            // Writing response body
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
         }
      }

   }

   public void handlePATCH(HttpExchange r) throws IOException, JSONException, SQLException {
      String[] url = r.getRequestURI().getPath().split("/");
      if (isNumeric(url[url.length - 1])) {
         ResultSet rs;
         int uid = Integer.parseInt(url[url.length - 1]);
         String preCheck = "SELECT count(*) as c FROM users WHERE uid = ?";
         PreparedStatement ps = this.connection.prepareStatement(preCheck);
         ps.setInt(1, uid);
         try {
            rs = ps.executeQuery();
            if (rs.next()) {
               int numOfUser = rs.getInt("c");
               String alters = "";
               if (numOfUser == 1) {
                  // Packing the alter info
                  String body = Utils.convert(r.getRequestBody());
                  JSONObject deserialized = new JSONObject(body);
                  Iterator<?> it = deserialized.keys();
                  String[] alternates = new String[deserialized.length()];
                  int order = 0;
                  while (it.hasNext()) {
                     String key = it.next().toString();
                     if (order != 0 && order < deserialized.length()) {
                        alters += ", ";
                     }
                     if (key.equals("is_driver")) {
                        alternates[order] = "is_driver";
                        alters += "isdriver = ? ";
                     } else if (key.equals("name")) {
                        alternates[order] = "name";
                        alters += "prefer_name = ? ";
                     } else {
                        alternates[order] = key;
                        alters += key + " = ? ";
                     }
                     order++;
                  }
                  String alternation = "UPDATE users SET " + alters + " WHERE uid = ?";
                  PreparedStatement ps1 = this.connection.prepareStatement(alternation);
                  for (int j = 0; j < deserialized.length(); j++) {
                     if (alternates[j].equals("is_driver")) {
                        ps1.setBoolean(j + 1, deserialized.getBoolean(alternates[j]));
                     } else if (alternates[j].equals("rides")) {
                        ps1.setInt(j + 1, deserialized.getInt(alternates[j]));
                     } else {
                        ps1.setString(j + 1, deserialized.getString(alternates[j]));
                     }
                  }
                  ps1.setInt(deserialized.length() + 1, uid);
                  try {
                     ps1.executeUpdate();
                     // success
                     JSONObject res = new JSONObject();
                     res.put("status", "OK");
                     String response = res.toString();
                     r.sendResponseHeaders(200, response.length());
                     // Writing response body
                     OutputStream os = r.getResponseBody();
                     os.write(response.getBytes());
                     os.close();
                  } catch (SQLException e) {
                     JSONObject res = new JSONObject();
                     res.put("status", "INTERNAL SERVER ERROR");
                     String response = res.toString();
                     r.sendResponseHeaders(500, response.length());
                     // Writing response body
                     OutputStream os = r.getResponseBody();
                     os.write(response.getBytes());
                     os.close();
                  }
               }
            } else {
               JSONObject res = new JSONObject();
               res.put("status", "NOT FOUND");
               String response = res.toString();
               r.sendResponseHeaders(404, response.length());
               // Writing response body
               OutputStream os = r.getResponseBody();
               os.write(response.getBytes());
               os.close();
            }
         } catch (SQLException e) {
            JSONObject res = new JSONObject();
            res.put("status", "INTERNAL SERVER ERROR");
            String response = res.toString();
            r.sendResponseHeaders(500, response.length());
            // Writing response body
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
         }
      } else {
         JSONObject res = new JSONObject();
         res.put("status", "BAD REQUEST");
         String response = res.toString();
         r.sendResponseHeaders(400, response.length());
         // Writing response body
         OutputStream os = r.getResponseBody();
         os.write(response.getBytes());
         os.close();
      }

   }

   public void handlePOSTRegister(HttpExchange r) throws IOException, JSONException, SQLException {
      try {
         String body = Utils.convert(r.getRequestBody());
         JSONObject deserialized = new JSONObject(body);
         if (!Utils.checkPOSTRegisterBody(deserialized)) {
        	 // Body parameters are invalid
        	 JSONObject res = new JSONObject();
             res.put("status", "BAD REQUEST");
             String response = res.toString();
             r.sendResponseHeaders(400, response.length());
             // Writing response body
             OutputStream os = r.getResponseBody();
             os.write(response.getBytes());
             os.close();
             return;
         }
         String name = deserialized.get("name").toString();
         String email = deserialized.get("email").toString();
         String password = deserialized.get("password").toString();
         
         // Check if email exists
         ResultSet rs;
         String checkEmail = "SELECT * FROM users WHERE email = ?;";
         PreparedStatement checkEmailPs = this.connection.prepareStatement(checkEmail);
         checkEmailPs.setString(1, email);
         rs = checkEmailPs.executeQuery();
         if (rs.next()) {
        	 JSONObject res = new JSONObject();
             res.put("status", "BAD REQUEST");
             String response = res.toString();
             r.sendResponseHeaders(400, response.length());
             // Writing response body
             OutputStream os = r.getResponseBody();
             os.write(response.getBytes());
             os.close();
             return;
         }
         
         // Insert the new user info
         String prepare = "INSERT INTO users(prefer_name, email, \"password\", rides, availableCoupons, redeemedCoupons) VALUES(?, ?, ?, ?, ?, ?); ";
         PreparedStatement ps = this.connection.prepareStatement(prepare);
         ps.setString(1, name);
         ps.setString(2, email);
         ps.setString(3, password);
         ps.setInt(4, 0);
         Integer[] ac = {};
         Array availableCoupons = connection.createArrayOf("int4", ac);
         ps.setArray(5, availableCoupons);
         Integer[] rc = {};
         Array redeemedCoupons = connection.createArrayOf("int4", rc);
         ps.setArray(5, availableCoupons);
         ps.setArray(6, redeemedCoupons);

         int count = ps.executeUpdate();
         if (count == 1) {
        	 JSONObject res = new JSONObject();
             res.put("status", "OK");
             String response = res.toString();
             r.sendResponseHeaders(200, response.length());
             // Writing response body
             OutputStream os = r.getResponseBody();
             os.write(response.getBytes());
             os.close();
         } else {
            JSONObject res = new JSONObject();
            res.put("status", "INTERNAL SERVER ERROR");
            String response = res.toString();
            r.sendResponseHeaders(500, response.length());
            // Writing response body
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
         }

      } catch (SQLException se) {
    	 se.printStackTrace();
         JSONObject res = new JSONObject();
         res.put("status", "INTERNAL SERVER ERROR");
         String response = res.toString();
         r.sendResponseHeaders(500, response.length());
         // Writing response body
         OutputStream os = r.getResponseBody();
         os.write(response.getBytes());
         os.close();
      }
   	}

   public void handlePOSTLogin(HttpExchange r) throws IOException, JSONException, SQLException {
      try {
         String body = Utils.convert(r.getRequestBody());
         JSONObject deserialized = new JSONObject(body);
          if (!Utils.checkPOSTLoginBody(deserialized)) {
        	 // Body parameters are invalid
        	 JSONObject res = new JSONObject();
             res.put("status", "BAD REQUEST");
             String response = res.toString();
             r.sendResponseHeaders(400, response.length());
             // Writing response body
             OutputStream os = r.getResponseBody();
             os.write(response.getBytes());
             os.close();
             return;
         }
         String email = deserialized.get("email").toString();
         String password = deserialized.get("password").toString();

         ResultSet rs;
         String checkEmail = "SELECT \"password\" AS password FROM users WHERE email = ?;";
         PreparedStatement checkEmailPs = this.connection.prepareStatement(checkEmail);
         checkEmailPs.setString(1, email);
         rs = checkEmailPs.executeQuery();
         if (rs.next()) {
            String correctPassword = rs.getString("password");
            if (correctPassword.equals(password)) {
               JSONObject res = new JSONObject();
               res.put("status", "OK");
               String response = res.toString();
               r.sendResponseHeaders(200, response.length());
               // Writing response body
               OutputStream os = r.getResponseBody();
               os.write(response.getBytes());
               os.close();
               return;
            } else {
               JSONObject res = new JSONObject();
               res.put("status", "BAD REQUEST");
               String response = res.toString();
               r.sendResponseHeaders(400, response.length());
               // Writing response body
               OutputStream os = r.getResponseBody();
               os.write(response.getBytes());
               os.close();
               return;
            }
         } else {
            JSONObject res = new JSONObject();
            res.put("status", "NOT FOUND");
            String response = res.toString();
            r.sendResponseHeaders(404, response.length());
            // Writing response body
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
            return;
         }
      } catch (SQLException se) {
         se.printStackTrace();
         JSONObject res = new JSONObject();
         res.put("status", "INTERNAL SERVER ERROR");
         String response = res.toString();
         r.sendResponseHeaders(500, response.length());
         // Writing response body
         OutputStream os = r.getResponseBody();
         os.write(response.getBytes());
         os.close();
      }
   }
}



	
