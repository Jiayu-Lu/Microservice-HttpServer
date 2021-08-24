package ca.utoronto.utm.mcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Utils {
	public static MongoClientURI uri = new MongoClientURI("mongodb://root:123456@mongodb:27017");
	public static MongoClient mongoClient = new MongoClient(uri);
	public static MongoDatabase db = Utils.mongoClient.getDatabase("trip");
	public static MongoCollection<Document> collection = db.getCollection("trips");
	
   public static String convert(InputStream inputStream) throws IOException {

      try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
         return br.lines().collect(Collectors.joining(System.lineSeparator()));
      }
   }

   public static boolean isNumeric(String str) {
      try {
         Double.parseDouble(str);
         return true;
      } catch (NumberFormatException e) {
         return false;
      }
   }
   
   public static boolean checkPATCHTripBody(JSONObject body) throws JSONException {
	   if (!(body.has("distance") && body.has("endTime") && body.has("timeElapsed") && body.has("totalCost") && body.has("driverPayout"))) {
		   return false;
	   }
	   if (body.has("discount")) {
		   if (!isNumeric(body.get("discount").toString())) {
			   return false;
		   }
	   }
	   if (!isNumeric(body.get("distance").toString()) || !isNumeric(body.get("endTime").toString()) || !isNumeric(body.get("totalCost").toString()) || !isNumeric(body.get("driverPayout").toString())) {
		   return false;
	   }
	   return true;
   }
}
