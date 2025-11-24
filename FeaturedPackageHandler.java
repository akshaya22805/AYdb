import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FeaturedPackageHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
        try {
            JSONObject responseJson = new JSONObject();
            JSONArray elementsArray = new JSONArray();

            try (Connection con = DB.getConnection()) {

                String sql = "SELECT * FROM featured_packages WHERE is_featured = 1";
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    JSONObject element = new JSONObject();

                    element.put("id", "pkg_" + rs.getInt("id"));
                    element.put("title",
                            rs.getString("package_name") != null ? rs.getString("package_name") : "");

                    element.put("subtitle",
                            "Duration: " + rs.getString("duration") +
                                    ", ₹" + rs.getInt("price"));

                    element.put("image",
                            rs.getString("image_url") != null ? rs.getString("image_url") : "");

                    // actions
                    JSONArray actions = new JSONArray();
                    JSONObject action = new JSONObject();
                    action.put("label", "Get Details");
                    action.put("name", "details_" + rs.getInt("id"));
                    action.put("type", "postback");
                    action.put("value", rs.getInt("id"));

                    actions.put(action);
                    element.put("actions", actions);

                    elementsArray.put(element);
                }

            } catch (Exception dbEx) {
                responseJson.put("error", "Database error: " + dbEx.getMessage());
                writeResponse(exchange, responseJson.toString());
                return;
            }

            responseJson.put("type", "multiple-product");
            responseJson.put("text", "Check out our featured packages");
            responseJson.put("elements", elementsArray);

            writeResponse(exchange, responseJson.toString());

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject err = new JSONObject();
            err.put("error", "Server error: " + e.getMessage());
            writeResponse(exchange, err.toString());
        }
    }

    private void writeResponse(HttpExchange exchange, String response) {
        try {
            exchange.sendResponseHeaders(200, response.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
