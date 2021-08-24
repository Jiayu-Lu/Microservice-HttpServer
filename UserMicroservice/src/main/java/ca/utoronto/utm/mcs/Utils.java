package ca.utoronto.utm.mcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.json.JSONObject;

public class Utils {
   public static String convert(InputStream inputStream) throws IOException {

      try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
         return br.lines().collect(Collectors.joining(System.lineSeparator()));
      }
   }
   
   public static boolean checkPOSTRegisterBody(JSONObject body) {
	   return body.has("name") && body.has("email") && body.has("password");
   }
   
   public static boolean checkPOSTLoginBody(JSONObject body) {
	   return body.has("email") && body.has("password");
   }
}
