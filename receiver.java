import java.io.*;
import java.net.*;

public class receiver {
	private BufferedReader in;
	
    public static void main(String[] args) throws IOException {
        
        if (args.length != 2) {
            System.err.println(
                "Usage: java EchoClient <host name> <port number> <fileName>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
		
		int IDnum = 0;
		int packetNumber = 1;
		String totalMessage = "";

        try (
            Socket echoSocket = new Socket(hostName, portNumber);
            PrintWriter out =
                new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
        ) 
		{//try
			String input;
			String message = "";
			String ackToBeSent = "ACK" + packetNumber;
			String nextMessagePiece;
			String lineArray[];
            while (true) {
				while((input = in.readLine()) == null) {}; //wait until real input to react
                //System.out.println("echo: " + input);
				
				
				if(input.charAt(input.length()-2) == ('-')){
					echoSocket.close();
					System.exit(-1);
				}
				if(input.equals("ACK2")){ //if we recieve an ack2 then we should send the ack again
					//System.out.println("Debug");
					out.println(ackToBeSent);
					IDnum++;
					System.out.println("Waiting " + packetNumber + ", " + IDnum + ", " + input + ", " + ackToBeSent);
					continue;
				}		
				//do the checksum
				lineArray = input.split(" ");
				int checksum = 0;
				for (char ch : lineArray[3].toCharArray()) {
					checksum += ch;
				}
				//System.out.println("Checksum: " + checksum + " Checked against " + Integer.parseInt(lineArray[2]));
				if(checksum != Integer.parseInt(lineArray[2])){ //if we recieve a corrupt packet send ack3
					ackToBeSent = "ACK" + "3";
					out.println(ackToBeSent);
					//System.out.println("ack3 sent");
					IDnum++;
					System.out.println("Waiting " + packetNumber + ", " + IDnum + ", " + input + ", " + ackToBeSent);
					continue;
				}			
				else if (checksum == Integer.parseInt(lineArray[2])){ //if successful send confirmed 
					IDnum++;
					if(packetNumber==0)
						packetNumber++;
					else
						packetNumber--;
					ackToBeSent = "ACK" + packetNumber;
					System.out.println("Waiting " + packetNumber + ", " + IDnum + ", " + input + ", " + ackToBeSent); 
					//System.out.println("Packet number changed");
					
					nextMessagePiece = input.split(" ")[input.split(" ").length -1]; //this is to parse later
				
					totalMessage += (nextMessagePiece + " ");
					
					//System.out.println(checksum + " = " +  Integer.parseInt(lineArray[2]));
					if((nextMessagePiece.charAt(nextMessagePiece.length()-1) == ('.')) 
						&& (checksum == Integer.parseInt(lineArray[2])))
					{
						System.out.println("Message: " + totalMessage);
						out.println(ackToBeSent);
						continue;
					}
					
				}	
				
				//System.out.println("ack sent: " + ackToBeSent);
				out.println(ackToBeSent);
				
			}
        } 
		catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } 
		catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } 
    }
}