import java.io.*;
import java.net.*;

public class sender {
	private BufferedReader in;
	
    public static void main(String[] args) throws IOException {
        
        if (args.length != 3) {
            System.err.println(
                "Usage: java EchoClient <host name> <port number> <fileName>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
		String fileName = args[2];

        try (
            Socket echoSocket = new Socket(hostName, portNumber);
            PrintWriter out =
                new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
            BufferedReader stdIn =
                new BufferedReader(
                    new InputStreamReader(System.in))
        ) 
		{//try
			// FileReader reads text files in the default encoding.
			FileReader fileReader = 
            new FileReader(fileName);
	
			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = 
            new BufferedReader(fileReader);
			
			String line = bufferedReader.readLine();
			int sequenceNumber = 0;
			int IDnum = 1;
			int checksum = 0;
			int packetNumber = 0;
			int skip = 0;
			String lineArray[] = line.split(" ");
			int i = 0;

			while(i < lineArray.length) {
				String message = "";
				
				message += Integer.toString(sequenceNumber);
				message += " ";
				message += Integer.toString(IDnum);
				message += " ";
				checksum = 0;
				for (char ch : lineArray[i].toCharArray()) {
					checksum += ch;
				}
				message += Integer.toString(checksum);
				message += " ";
				message += lineArray[i];
				//System.out.println("Sent: " + message);
				//System.out.println("packet" + packetNumber);
				out.println(message);
				//out.println("Packet" + packetNumber);
				
			
				
				//set up for handling recieved packets
				String properAction;
				String inputFromReciever;
				//now to handle the ACK/DROP after sending the packet
				
				//wait for proper packet
				while((inputFromReciever = in.readLine()) == null) {};
				
				
				if(inputFromReciever.equals("ACK3")){ //if we immediately get a corrupt back
					
					properAction = "resend Packet" + packetNumber;
					IDnum++;
					System.out.println("Waiting ACK" + sequenceNumber + ", " + IDnum + ", " + "CORRUPT" + ", " + properAction);
					//System.out.println(inputFromReciever);
					
					
					//do not change packetNumber
				}
				else if(inputFromReciever.equals("ACK2")){ // if we immediately get a drop back
					properAction = "resend Packet" + packetNumber;
					IDnum++;
					System.out.println("Waiting ACK" + sequenceNumber + ", " + IDnum + ", " + "DROP" + ", " + properAction);
					
					
					//do not change packetNumber
				}
				else if(inputFromReciever.equals("ACK1") || inputFromReciever.equals("ACK0")){ // if not dropped and not corrupt then send the packet
					if(packetNumber==0)
						packetNumber++;
					else
						packetNumber--;
					
					properAction = "send Packet" + packetNumber;
					
					IDnum++;
					//handle last message
					if(lineArray[i].charAt(lineArray[i].length()-1) == ('.') && inputFromReciever.equals("ACK" + sequenceNumber)){
						//final acknowledge is correct
						System.out.println("Waiting ACK" + sequenceNumber + ", " + IDnum + ", " + inputFromReciever + ", " + "no more packets to send");
						out.println("-1");
						echoSocket.close();
						System.exit(1);
					}
					else if(lineArray[i].charAt(lineArray[i].length()-1) == ('.') && !(inputFromReciever.equals("ACK" + sequenceNumber))){
						//final packet got corrupted
						System.out.println("Waiting ACK" + sequenceNumber + ", " + IDnum + ", " + inputFromReciever + ", " + "no more packets to send");
					}
					else {//packet received well
						System.out.println("Waiting ACK" + sequenceNumber + ", " + IDnum + ", " + inputFromReciever + ", " + properAction);
					}
					if(sequenceNumber==0)
						sequenceNumber++;
					else
						sequenceNumber--;
					i++;
				}
				
				
				//System.out.println("RecievedACK: " + inputFromReciever);
				//System.out.println("Waiting ACK" + packetNumber + ", " + IDnum + ", " + inputFromReciever + ", " + properAction);
				
			}   

			// Always close files.
			bufferedReader.close();
		
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println("echo: " + in.readLine());
            }
			
			
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } 
    }
}