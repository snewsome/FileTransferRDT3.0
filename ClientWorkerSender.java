import java.net.*;
import java.io.*;
import java.lang.*;

public class ClientWorkerSender implements Runnable {
	private Socket client;
	private DataOutputStream outPipe;
	private DataInputStream inPipe;
	
	
	public ClientWorkerSender(Socket client, OutputStream outToOther, InputStream inFromOther){
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
		
		//set up variables
		
		int IDnum = 1;
		
		while(true) {
			try{
				line = in.readLine();
				if(line.charAt(line.length()-2) == ('-')){
					//System.out.println("Closing");
					outPipe.writeUTF("-1");
					break;
				}
					
				String action = "";
				double newRandom = random.getRandomValue();
				if(newRandom < 0.5)
					action = "PASS";
				else if(.5 <= newRandom && newRandom < .75)
					action = "CORRUPT";
				else
					action = "DROP";
				//act on action
				//action = "PASS";
				//send input to other client through outpipe
				if(line!=null){
					//System.out.println("Sending to other Thread");
					//System.out.println("Received: " + line);
					if(action.equals("PASS")){
						String[] message = line.split(" ");
						outPipe.writeUTF(line);
						//line = in.readLine();
						line = "Packet"+ message[0];
						System.out.println("Received: " + line + ", " + IDnum + ", " + action);
						IDnum ++;
					}
					else if(action.equals("CORRUPT")) {
						String[] message = line.split(" ");
						
						//checksum + 1
						message[message.length - 2] = Integer.toString( Integer.parseInt(message[message.length - 2]) + 1); 
						String recompiled = "";
						recompiled += (message[0] + " ");
						recompiled += (message[1] + " ");
						recompiled += (message[2] + " ");
						recompiled += message[3];
						//System.out.println("Debug" + " " + recompiled);
						
						//line = in.readLine();
						line = "Packet"+ message[0];
						System.out.println("Received: " + line + ", " + IDnum + ", " + action);
						outPipe.writeUTF(recompiled);
					}
					else {
						String[] message = line.split(" ");
						line = "Packet"+ message[0];
						System.out.println("Received: " + line + ", " + IDnum + ", " + action);
						out.println("ACK2");
						continue;
					}
					//if(line.charAt(line.length()-1) == ('.')){
					//	System.out.println("end of packets");
					//}
					
				}							
				
				pipeline = inPipe.readUTF();
				//send contents of inpipe to other client depending on action
				if(pipeline!=null){
					//System.out.println("Debug at pipe Sender: " + pipeline);
					//System.out.println("Sender: Reading to other Thread contents and sending back to other client");
					out.println(pipeline);
					//if(pipeline.charAt(pipeline.length()-1) == ('.')){
					//	System.out.println("end of packets");
					//}
				}
			}
			catch(IOException e){
				System.out.println("Read failed");
				System.exit(-1);
			}
		}
	}
}