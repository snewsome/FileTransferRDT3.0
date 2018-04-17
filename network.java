import java.net.*;
import java.io.*;

public class network {
    public static void main(String[] args) throws IOException {
        
        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }
        
        int portNumber = Integer.parseInt(args[0]);
		ServerSocket serverSocket = null;
        
        try  
		{
            serverSocket =  new ServerSocket(Integer.parseInt(args[0]));
        } 
		catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
		
		ClientWorkerSender sender;
		ClientWorkerReciever receiver;
		/* set up pipes */
		PipedOutputStream pout1 = new PipedOutputStream();
		PipedInputStream pin1 = new PipedInputStream(pout1);
		
		PipedOutputStream pout2 = new PipedOutputStream();
		PipedInputStream pin2 = new PipedInputStream(pout2);
		try{
			sender = new ClientWorkerSender(serverSocket.accept(), pout1, pin2);
			Thread t = new Thread(sender);
			t.start();
			receiver = new ClientWorkerReciever(serverSocket.accept(), pout2, pin1);
			Thread t2 = new Thread(receiver);
			t2.start();
		}
		catch(IOException e){
			System.out.println("Accept Failed: 4444");
			System.exit(-1);
		}
	}
}