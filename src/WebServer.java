import java.io.* ;
import java.net.* ;
import java.util.* ;

public final class WebServer {
    public static void main(String argv[]) throws Exception {
    // Get the port number from the command line.
        int port = (new Integer(argv[0])).intValue();
        
        // Establish the listen socket.
        ServerSocket socket = new ServerSocket(port);
        
        // Process HTTP service requests in an infinite loop.
        while (true) {
            // Listen for a TCP connection request.
            Socket connection = socket.accept();
            System.out.println("connection: "+connection);
            // Construct an object to process the HTTP request message.
            HttpRequest request = new HttpRequest(connection);
            System.out.println("request: "+request);    
            // Create a new thread to process the request.
            Thread thread = new Thread(request);
            System.out.println("thread: "+thread);    
            // Start the thread.
            thread.start();
        }
        
    }


}
