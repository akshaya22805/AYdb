import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckEmailHandler implements HttpHandler {

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

            JSONObject json;
            try {
                json = new JSONObject(body);
            } catch (Exception e) {
                responseJson.put("error", "Invalid JSON input");
                sendResponse(exchange, 400, responseJson);
                return;
            }

            // Validate required field
            if (!json.has("email")) {
                responseJson.put("error", "Missing required field: email");
                sendResponse(exchange, 400, responseJson);
                return;
            }

            String email = json.getString("email");
            int exists = 0;

            // Check if email exists in database
            try (Connection con = DB.getConnection()) {
                String sql = "SELECT COUNT(*) FROM customers WHERE email = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            exists = 1;
                        }
                    }
                }
            } catch (Exception e) {
                responseJson.put("error", "Database error: " + e.getMessage());
                sendResponse(exchange, 500, responseJson);
                return;
            }

            responseJson.put("exists", exists);
            sendResponse(exchange, 200, responseJson);

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

