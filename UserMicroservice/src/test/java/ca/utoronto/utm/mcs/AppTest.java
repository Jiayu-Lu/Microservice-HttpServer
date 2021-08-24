package ca.utoronto.utm.mcs;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.assertTrue;

/*
Please Write Your Tests For CI/CD In This Class.
You will see these tests pass/fail on github under github actions.
*/
public class AppTest {

	public static String host = "usermicroservice:8000";

	@BeforeAll
	public static void setUp() {
		try {
			String url = "jdbc:postgresql://postgres:5432/root";
			Class.forName("org.postgresql.Driver");
			Connection connection = DriverManager.getConnection(url, "root", "123456");
			String prepare = "INSERT INTO users(prefer_name,email,password,rides,availableCoupons,redeemedCoupons)"
					+ "VALUES(?, ?, ?, '0', '{}', '{}')";
			PreparedStatement ps = connection.prepareStatement(prepare);
			ps.setString(1, "fake");
			ps.setString(2, "fake@exam.com");
			ps.setString(3, "123456");
			ps.executeUpdate();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Test post /user/register 200 response
	 */
	@Test
	public void testPostRegister200() {
		String requestBody = "{\"name\": \"username1\", \"email\": \"user1@exam.com\", \"password\": \"123456\"}";
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.uri(URI.create("http://" + host + "/user/register"))
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
	 * Test post /user/register 400 response
	 */
	@Test
	public void testPostRegister400() {
		String requestBody = "{\"name\": \"username1\", \"email\": \"user1@exam.com\"}";
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.uri(URI.create("http://" + host + "/user/register"))
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
	 * Test  POST /user/login 200 response
	 */
	@Test
	public void testPostLogin200() {
		String requestBody = "{\"password\": \"123456\", \"email\": \"fake@exam.com\"}";
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.uri(URI.create("http://" + host + "/user/login"))
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
	 * * Test  POST /user/login 404 response
	 */
	@Test
	public void testPostLogin404() {
		String requestBody = "{\"password\": \"123456\", \"email\": \"nonexist@exam.com\"}";
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.uri(URI.create("http://" + host + "/user/login"))
				.build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println(response.statusCode() + "; " + response.body());
			assertTrue(response.statusCode() == 404);

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

}