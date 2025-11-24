import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class AddCustomerHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
        JSONObject responseJson = new JSONObject();
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                responseJson.put("error", "Only POST requests are allowed");
                sendResponse(exchange, 405, responseJson);
                return;
            }

            // Read request body
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            // Parse JSON
            JSONObject json;
            try {
                json = new JSONObject(body);
            } catch (JSONException e) {
                responseJson.put("error", "Invalid JSON input");
                sendResponse(exchange, 400, responseJson);
                return;
            }

            // Validate required fields
            if (!json.has("name") || !json.has("email")) {
                responseJson.put("error", "Missing required fields: name and email");
                sendResponse(exchange, 400, responseJson);
                return;
            }

            String name = json.getString("name");
            String email = json.getString("email");

            // Insert into database
            int customerId = 0;
            try (Connection con = DB.getConnection()) {
                String sql = "INSERT INTO customers (name, email) VALUES (?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            customerId = rs.getInt(1);
                        }
                    }
                }
            } catch (SQLException e) {
                responseJson.put("error", "Database error: " + e.getMessage());
                sendResponse(exchange, 500, responseJson);
                return;
            }

            // Return success response
            responseJson.put("customerId", customerId);
            sendResponse(exchange, 200, responseJson);

            System.out.println("Received name: " + name + ", email: " + email);
            System.out.println("Generated customerId: " + customerId);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                responseJson.put("error", "Internal server error");
                sendResponse(exchange, 500, responseJson);
            } catch (Exception ignored) {}
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, JSONObject responseJson) throws Exception {
        String response = responseJson.toString();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}

