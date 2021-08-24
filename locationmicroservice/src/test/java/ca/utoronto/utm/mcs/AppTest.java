package ca.utoronto.utm.mcs;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Session;

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

	public static String host = "locationmicroservice:8000";

	/**
	 * Before testing, set up the server and start the server.
	 */
	@BeforeAll
	public static void setUp() {
		try (Session session = Utils.driver.session()) {
			String setupQuery =
					"CREATE (:user {uid:'0', is_driver:false, longitude: 30, latitude: 30, street_at: 'street2'}) " +
							"CREATE (:user {uid:'10', is_driver:true, longitude: 31, latitude: 31, street_at: 'street1'}) " +
							"CREATE (s1:road {name: 'street1', is_traffic: false}) " +
							"CREATE (s2:road {name: 'street2', is_traffic: false}) " +
							"CREATE (s1)-[:ROUTE_TO {travel_time: 10, is_traffic: false}]->(s2) ";
			session.run(setupQuery);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Test GET /location/nearbyDriver/:uid?radius= 200 response
	 */
	@Test
	public void testGetNearbyDriver200() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create("http://" + host + "/location/nearbyDriver/0?radius=10000"))
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
	 * Test GET /location/nearbyDriver/:uid?radius= 404 response
	 */
	@Test
	public void testGetNearbyDriver404() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create("http://" + host + "/location/nearbyDriver/30?radius=1000"))
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
	 * Test GET /location/navigation/:driverUid?passengerUid= 200 response
	 */
	@Test
	public void testNavigation200() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create("http://" + host + "/location/navigation/10?passengerUid=0"))
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
	 * Test GET /location/navigation/:driverUid?passengerUid= 404 response
	 */
	@Test
	public void testNavigation404() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create("http://" + host + "/location/navigation/30?passengerUid=0"))
				.build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertTrue(response.statusCode() == 404);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}