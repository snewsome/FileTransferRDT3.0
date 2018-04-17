import java.net.*;
import java.io.*;
import java.lang.*;

public class ClientWorkerReciever implements Runnable {
	private Socket client;
	private DataOutputStream outPipe;
	private DataInputStream inPipe;
	
	
	public ClientWorkerReciever(Socket client, OutputStream outToOther, InputStream inFromOther){
		this.client = client;
		inPipe = new DataInputStream(inFromOther);
		outPipe = new DataOutputStream(outToOther);
	}
	
	public void run() {
		String line;
		String pipeline;
		BufferedReader in = null;
		PrintWriter out = null;
		RandomJava random = new RandomJava();
		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);
		}
		catch(IOException e) {
			System.out.println("in or out failed");
			System.exit(-1);
		}
		
		int skipBack2here = 0;
		
		while(true) {
			
			try{	
				//read input from the pipe
				if(skipBack2here == 0) {
					pipeline = inPipe.readUTF();
					
					if(pipeline.charAt(pipeline.length()-2) == ('-')){
						//System.out.println("Closing 2");
						out.println("-1");
						client.close();
						System.exit(-1);
					}
					
					//send contents of inpipe to other client through out Socket
					if(pipeline!=null){
						//System.out.println("Debug at pipe Reciever: " + pipeline);
						//System.out.println("Reciever: Reading to other Thread contents and sending back to other client");
						out.println(pipeline);
						//if(pipeline.charAt(pipeline.length()-1) == ('.')){
						//	System.out.println("end of packets");
						//}
					}
				}
				
				//get contents from Reciever and send to pipe
				line = in.readLine();
				//decide on course of action
				//---------------------------------------------------------------
				String action = "";
				double newRandom = random.getRandomValue();
				if(newRandom < 0.5)
					action = "PASS";
				else if(.5 <= newRandom && newRandom < .75)
					action = "DROP";
				else
					action = "DROP";				
				//-------------------------------------------------------------
				//act on course of action
				//action = "PASS";
				if(line.equals("ACK2") ){
					outPipe.writeUTF(line);
					skipBack2here = 1;
				}
				else if(line.equals("ACK3") ){
					outPipe.writeUTF(line);
					skipBack2here = 0;
				}
				else if(action.equals("DROP") && line != null){ 
					out.println("ACK2");
					System.out.println("Received: " + line + ", " + action);
					skipBack2here = 1;
				}
				else if(action.equals("CORRUPT") && line != null){ 
					out.println("ACK2");
					System.out.println("Received: " + line + ", " + action);
					skipBack2here = 1;
				}
				else {
					skipBack2here = 0;
					//send input to other client through outpipe
					if(line!=null){
						System.out.println("Received: " + line + ", " + action);
						outPipe.writeUTF(line);
						//if(line.charAt(line.length()-1) == ('.')){
						//	System.out.println("end of packets");
						//}	
					}
				}
				
			}
			catch(IOException e){
				System.out.println("Read failed");
				System.exit(-1);
			}
		}
	}
}