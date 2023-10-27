import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;

public class Client{

    private Properties properties;
    private BufferedReader in;
    private Socket client;
    private PrintWriter out;
    private boolean done;

    Client(){
        properties = Constants.getInstance().getProperties();
    }
//    @Override
    public void run() {
        try {
            client = new Socket(properties.getProperty("server.host"),Integer.parseInt(properties.getProperty("server.port")));
            System.out.println("port="+client.getLocalPort());
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            //Because write is a blocking call, thus creating a separate thread for reading the from the input
            InputHandler inputHandler = new InputHandler();
            Thread t = new Thread(inputHandler);
            t.start();
            //reading the incoming messages in the main thread
            String inMessage;
            while((inMessage=in.readLine())!=null){
                System.out.println(inMessage);
            }
        }
        catch (IOException e){
            shutdown();
        }
    }

    public void shutdown(){
        try{
            done=true;// stops the while loop and terminates the inputHandler
            in.close();
            out.close();
            if(!client.isClosed()) client.close();
        }catch (IOException e){

        }
    }

    class InputHandler implements Runnable{

        @Override
        public void run() {
            try{
                BufferedReader inReader =  new BufferedReader(new BufferedReader(new InputStreamReader(System.in)));
                while(!done){
                    String message = inReader.readLine();
                    if(message.equals("/quit")){
                        out.println("/quit");
                        inReader.close();
                        shutdown();
                    }
                    else{
                        out.println(message);
                    }
                }
            }
            catch (IOException e){
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
