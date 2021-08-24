package ca.utoronto.utm.mcs;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;

/*
Please Write Your Tests For CI/CD In This Class.
You will see these tests pass/fail on github under github actions.
*/
public class AppTest {
	public static String tripIdString;
	public static String tripinfoHost = "tripinfomicroservice:8000";
	public static String locationHost = "locationmicroservice:8000";

	/**
	 * Before every testing, set up the server and start the server.
	 */
	@BeforeAll
	public static void setUp() {
		try {
			String requestBody;
			// add street1
			requestBody = "{\"roadName\": \"street1\", \"hasTraffic\": false}";
			HttpClient client4 = HttpClient.newHttpClient();
			HttpRequest request4 = HttpRequest.newBuilder()
					.method("PUT", HttpRequest.BodyPublishers.ofString(requestBody))
					.uri(URI.create("http://" + locationHost + "/location/road"))
					.build();
			try {
				HttpResponse<String> response = client4.send(request4, HttpResponse.BodyHandlers.ofString());
			} catch (Exception e) {
				e.printStackTrace();
			}

			// add street2
			requestBody = "{\"roadName\": \"street2\", \"hasTraffic\": false}";
			HttpClient client5 = HttpClient.newHttpClient();
			HttpRequest request5 = HttpRequest.newBuilder()
					.method("PUT", HttpRequest.BodyPublishers.ofString(requestBody))
					.uri(URI.create("http://" + locationHost + "/location/road"))
					.build();
			try {
				HttpResponse<String> response = client5.send(request5, HttpResponse.BodyHandlers.ofString());
			} catch (Exception e) {
				e.printStackTrace();
			}

			// connect stree1 and street2
			requestBody = "{\"roadName1\": \"street2\", \"roadName2\": \"street1\", \"hasTraffic\": false, \"time\": 10}";
			HttpClient client6 = HttpClient.newHttpClient();
			HttpRequest request6 = HttpRequest.newBuilder()
					.method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
					.uri(URI.create("http://" + locationHost + "/location/hasRoute"))
					.build();
			try {
				HttpResponse<String> response = client6.send(request6, HttpResponse.BodyHandlers.ofString());
			} catch (Exception e) {
				e.printStackTrace();
			}

			// add user
			requestBody = "{\"uid\": \"0\", \"is_driver\": false}";
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.method("PUT", HttpRequest.BodyPublishers.ofString(requestBody))
					.uri(URI.create("http://" + locationHost + "/location/user"))
					.build();
			try {
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			} catch (Exception e) {
				e.printStackTrace();
			}

			// patch user
			requestBody = "{\"longitude\": \"30\", \"latitude\": \"30\", \"street\": \"street1\"}";
			HttpClient client2 = HttpClient.newHttpClient();
			HttpRequest request2 = HttpRequest.newBuilder()
					.method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
					.uri(URI.create("http://" + locationHost + "/location/0"))
					.build();
			try {
				HttpResponse<String> response = client2.send(request2, HttpResponse.BodyHandlers.ofString());
			} catch (Exception e) {
				e.printStackTrace();
			}

			// add driver
			requestBody = "{\"uid\": \"10\", \"is_driver\": true}";
			HttpClient client3 = HttpClient.newHttpClient();
			HttpRequest request3 = HttpRequest.newBuilder()
					.method("PUT", HttpRequest.BodyPublishers.ofString(requestBody))
					.uri(URI.create("http://" + locationHost + "/location/user"))
					.build();
			try {
				HttpResponse<String> response = client3.send(request3, HttpResponse.BodyHandlers.ofString());
			} catch (Exception e) {
				e.printStackTrace();
			}

			// patch driver
			requestBody = "{\"longitude\": 30, \"latitude\": 30, \"street\": \"street2\"}";
			HttpClient client7 = HttpClient.newHttpClient();
			HttpRequest request7 = HttpRequest.newBuilder()
					.method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
					.uri(URI.create("http://" + locationHost + "/location/10"))
					.build();
			try {
				HttpResponse<String> response = client7.send(request7, HttpResponse.BodyHandlers.ofString());
			} catch (Exception e) {
				e.printStackTrace();
			}


			requestBody = "{\"driver\":\"10\",\"passenger\":\"0\",\"startTime\":\"1627797735\"}";
			HttpClient client8 = HttpClient.newHttpClient();
			HttpRequest request8 = HttpRequest.newBuilder()
					.method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
					.uri(URI.create("http://" + tripinfoHost + "/trip/confirm"))
					.build();
			try {
				HttpResponse<String> response = client8.send(request8, HttpResponse.BodyHandlers.ofString());
				JSONObject tripIdJSON = new JSONObject(response.body());
				tripIdString = tripIdJSON.getString("data");
			} catch (Exception e) {
				e.printStackTrace();
			}


		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Test POST /trip/request  with 200 response
	 */
	@Test
	public void testPostRequest200() {
		String requestBody = "{\"uid\":\"0\",\"radius\":\"1000\"}";
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.uri(URI.create("http://" + tripinfoHost + "/trip/request"))
				.build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertTrue(response.statusCode() == 200);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * Test POST /trip/request  with 400 response
	 */
	@Test
	public void testPostRequest400() {
		String requestBody = "{\"uid\":\"1\"}";
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.uri(URI.create("http://" + tripinfoHost + "/trip/request"))
				.build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertTrue(response.statusCode() == 400);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * Test POST /trip/confirm with 200 response
	 */
	@Test
	public void testPostConfirm200() {
		String requestBody = "{\"driver\":\"driver_info\",\"passenger\":\"passenger_info\",\"startTime\":\"1628223790\"}";
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.uri(URI.create("http://" + tripinfoHost + "/trip/confirm"))
				.build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertTrue(response.statusCode() == 200);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * Test POST /trip/confirm with 400 response
	 */
	@Test
	public void testPostConfirm400() {
		String requestBody = "{\"driver\":\"driver_info\",\"passenger\":\"passenger_info\"}";
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.uri(URI.create("http://" + tripinfoHost + "/trip/confirm"))
				.build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertTrue(response.statusCode() == 400);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * Test PATCH /trip/:_id with 200 response
	 */
	@Test
	public void testPatchTrip200() {

		String requestBody = "{\"distance\":\"100\",\"endTime\":\"1627798525\",\"timeElapsed\":\"10\",\"discount\":\"0\",\"totalCost\":\"100\",\"driverPayout\":\"65\"}";
		HttpClient client = HttpClient.newHttpClient();

		HttpRequest request = HttpRequest.newBuilder()
				.method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
				.uri(URI.create("http://" + tripinfoHost + "/trip/" + tripIdString))
				.build();

		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertTrue(response.statusCode() == 200);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * Test PATCH /trip/:_id with 400 response
	 */
	@Test
	public void testPatchTrip400() {
		String requestBody = "{\"distance\":\"100\",\"endTime\":\"1627798525\",\"timeElapsed\":\"10\",\"discount\":\"0\",\"totalCost\":\"100\",\"driverPayout\":\"65\"}";
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
				.uri(URI.create("http://" + tripinfoHost + "/trip"))   // bad request
				.build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertTrue(response.statusCode() == 400);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * Test GET /trip/passenger/:uid with 200 response
	 */
	@Test
	public void testGetPassenger200() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create("http://" + tripinfoHost + "/trip/passenger/0"))
				.build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertTrue(response.statusCode() == 200);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * Test GET /trip/passenger/:uid with 404 response
	 */
	@Test
	public void testGetPassenger404() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create("http://" + tripinfoHost + "/trip/passenger/20"))
				.build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertTrue(response.statusCode() == 404);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * Test GET /trip/driver/:uid with 200 response
	 */
	@Test
	public void testGetDriver200() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create("http://" + tripinfoHost + "/trip/driver/10"))
				.build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertTrue(response.statusCode() == 200);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * Test GET /trip/driver/:uid with 404 response
	 */
	@Test
	public void testGetDriver404() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create("http://" + tripinfoHost + "/trip/driver/30"))
				.build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertTrue(response.statusCode() == 404);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * Test GET /trip/driverTime/:_id with 200 response
	 */
	@Test
	public void testGetDriverTime200() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create("http://" + tripinfoHost + "/trip/driverTime/" + tripIdString))
				.build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertTrue(response.statusCode() == 200);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * Test GET /trip/driverTime/:_id with 400 response
	 */
	@Test
	public void testGetDriverTime400() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create("http://" + tripinfoHost + "/trip/driverTime"))  //bad request
				.build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertTrue(response.statusCode() == 400);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}