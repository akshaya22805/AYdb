import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.OutputStream;
import java.sql.*;

public class GetCustomerHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery(); // id=1
            int id = Integer.parseInt(query.split("=")[1]);

            Connection con = DB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM customers WHERE id = ?"
            );
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            JSONObject json = new JSONObject();
            if (rs.next()) {
                json.put("id", rs.getInt("id"));
                json.put("name", rs.getString("name"));
                json.put("email", rs.getString("email"));
            } else {
                json.put("message", "Not found");
            }

            String response = json.toString();
            exchange.sendResponseHeaders(200, response.length());

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

