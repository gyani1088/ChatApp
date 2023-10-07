import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{
    Properties properties;
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;

    private ExecutorService pool;

    public Server() {
        this.properties = Constants.getInstance().getProperties();
        this.connections = new ArrayList<>();
        this.done = false;
    }

    @Override
    public void run() {
        try{
            server = new ServerSocket(Integer.parseInt(properties.getProperty("server.port")));
            pool = Executors.newCachedThreadPool();
            while(!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            shutdown();
        }
    }

    public void broadcast(String message){
        for(ConnectionHandler ch: connections){
            if(ch!=null) ch.sendMessage(message);
        }
    }

    public void shutdown(){
        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) server.close();
        }
        catch (IOException e){

        }
    }

    class ConnectionHandler implements Runnable{
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String name;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try{
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter a nickname:");
                name = in.readLine();
                System.out.println(name+" connected");
                broadcast(name+ " joined the chat");
                String message;
                while((message=in.readLine())!=null){
                    if(message.startsWith("/nick ")){
                        //TODO: handle nickname
                        String[] messageSplit = message.split(" ",2);
                        if(messageSplit.length == 2){
                            broadcast("System: "+name+" renamed themselves to "+messageSplit[1]);
                            System.out.println(name+" renamed themselves to "+messageSplit[1]);
                            name = messageSplit[1];
                            out.println("Successfully changed nickname to "+name);
                        }
                        else{
                            out.println("Nickname not provided");
                        }
                    }
                    else if(message.startsWith("/quit")){
                        broadcast(name +" left the chat");
                        shutdown();
                    }
                    else{
                        broadcast(name+": "+ message);
                    }
                }
            }
            catch (IOException e){
                shutdown();
                System.out.println(e.getMessage());
            }
        }

        public void sendMessage(String message){
            out.println(message);
        }

        public void shutdown(){
            try {
                in.close();
                out.close();
                if (!client.isClosed()) client.close();
            }
            catch(IOException e){

            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
