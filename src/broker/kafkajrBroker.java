package broker;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class kafkajrBroker {
    private final int port;
    private final String dataDir;
    private HttpServer httpServer;

    public kafkajrBroker(int port, String dataDir) {
        this.port = port;
        this.dataDir = dataDir;
    }

    public void start() throws IOException{
        System.out.println("-------kafkajr broker startup-------");
        System.out.println("port = " + port);
        System.out.println("dataDir = " + dataDir);

        // Initializing an http server
        httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        // Using ThreadPool for concurrent request handling
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * 2
        );

        httpServer.setExecutor(executor);

        httpServer.createContext("/health", exchange -> {
            String response = "{\"status\":\"healthy\"}";
            byte[] bytes = response.getBytes(); // Converting into bytes for serialization

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        httpServer.start();
        System.out.println("---Request ready for broker---");
        System.out.println("GET /health");
    }

    public void stop(){
        System.out.println("\n=== Kafka-JR Broker Shutdown ===");
        if (httpServer != null) {
            httpServer.stop(5);
            System.out.println("✓ HTTP Server stopped");
        }

        System.out.print("Broker Shutdown Complete.");
    }

    public static void main(String args[]) throws IOException{
        int port = 8080;
        String dataDir = "data/kafkajr";

        for(int i = 0; i < args.length; i++){
            if(args[i].equals("--port") && i + 1 < args.length){
                port = Integer.parseInt(args[i + 1]);
            } else if(args[i].equals("--data-dir") && i + 1 < args.length){
                dataDir = args[i+1];
            }
        }
        kafkajrBroker broker = new kafkajrBroker(port, dataDir);

        try {
            broker.start();
            System.out.println("\nPress Ctrl+C to stop the broker");
            Thread.currentThread().join();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }
}
