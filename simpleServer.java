import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class simpleServer {
    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/checkEmail", new CheckEmailHandler());
        server.createContext("/addCustomer", new AddCustomerHandler());
        server.createContext("/getCustomer", new GetCustomerHandler());
        server.createContext("/getPackageDetails",new FeaturedPackageHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on http://localhost:8080");
    }
}

