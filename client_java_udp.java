import java.io.*; 
import java.net.*; 
import java.lang.Object;
  
/*Carina Rammelkamp CS176A Fall 2012
  material taken from: 
  source 1: http://www.cs.uic.edu/~troy/spring05/cs450/sockets/socket.html
  source 2: http://stackoverflow.com/questions/5267241/numberformatexception-error-parseint
  lines copied from source 1 are labeled "1.1" through "1.10"
  lines copied from source 2 are labeled "2.1" through "2.3"
*/

class client_java_udp { 
    
    public static Boolean DEBUG = false;
    public static InetAddress ipAddress = null;
    public static int port=-1;

    public static void main(String args[]) throws Exception 
    { 
	DatagramSocket clientSocket = null;
	String msg = new String();
	int keyLength = -1;
	int getACK = -22;

	try {
	    if(args.length != 3)
		return;
	    else if(Integer.parseInt(args[2]) < 1 || Integer.parseInt(args[2]) > 4096) //check key length 
		return;
	    else if(Integer.parseInt(args[1]) < 1024 || Integer.parseInt(args[1]) > 65535) //check port
		{
		    System.err.println("Could not connect to server.  Terminating."); 
		    System.exit(1);
		}

	    try {
		ipAddress = InetAddress.getByName(args[0]); //"1.1"
		port = Integer.parseInt(args[1]); 
		clientSocket = new DatagramSocket(); //"1.2"
		clientSocket.connect(ipAddress, port);
		keyLength = Integer.parseInt(args[2]);
	   
		//msg to be sent to server, server recieving bytes needs space of at least 26+4096
		msg = "Connect.  Key length:  "+keyLength; 
	    }
	    catch (Exception e){
		System.err.println("Could not connect to server.  Terminating."); 
		System.exit(1);//"1.3"
	    }


	    //sending message from client to server: "Connect. Key Length: ...." 
	    getACK = ackHandshake(clientSocket, msg); 
	    if(getACK == -1)
		System.exit(1);

	    while(true){
		//getting the message with key from server, replying with 'ACK'
		msg = receive(clientSocket); 

		if(msg.equals("Invalid session key.  Terminating."))
		    {
			System.out.println(msg);
			System.exit(1);
		    }

		else if(msg == null)
		    System.exit(1);

		//to print session keys
		System.out.println(msg);
		Thread.sleep(1000);

		//sending message from server back to server
		getACK = ackHandshake(clientSocket, msg);
		if(getACK == -1)
		    System.exit(1);
	
	    }
	}
       	catch (Exception ex) { 
	    clientSocket.close(); 
	    System.exit(1);

       	}
       
    }

    public static void sendLengthAndMessage(DatagramSocket clientSocket, String msg){

	byte[] sendMsg = msg.getBytes();
	byte[] msgLength = (Integer.toString(msg.length())).getBytes(); 
	
	DatagramPacket sendMsgLength = 
	    new DatagramPacket(msgLength, msgLength.length); //"1.4"
	DatagramPacket sendPacketWithMsg = 
	    new DatagramPacket(sendMsg, sendMsg.length); //"1.5"

	try{
	    
	    clientSocket.send(sendMsgLength); //"1.6"
	    clientSocket.send(sendPacketWithMsg); //"1.7"

	    if(DEBUG)
		{
		    System.out.println("GOING TO SEND string length to server>>>" + sendMsg.length);
		    System.out.println("GOING TO SEND message to server>>>" + msg);
		    System.out.println("the length of the int representing the number of bytes has this many bytes: "+msgLength.length);
		}
	}
	catch (Exception e){}
    }

    public static int receiveACK(DatagramSocket clientSocket)
    {
	String ack=null;
	int maxSizeOfData = 4096+28; //28 to cover "Session Key:  " intro 14chars = 28 bytes
	byte[] getAckMsg = new byte[maxSizeOfData];

	DatagramPacket ackPacket=
	    new DatagramPacket(getAckMsg, getAckMsg.length); //"1.8" 
	
	try{
	    clientSocket.receive(ackPacket);
	    ack = 
		new String(ackPacket.getData(),ackPacket.getOffset(),ackPacket.getLength(),"UTF-8"); //"2.1"
	    if(DEBUG)
		System.out.println("message sent from server:" +ack);		 
	}
	catch (Exception e){ return 0; }
	
	if(ack.compareTo("ACK") == 0)
	    return 1;
	else
	    return 0;
	 
    }


    public static String receive(DatagramSocket serverSocket)
    {
	String length = null;
	String msg = null;
	
	int maxSizeOfData = 4096+28; // add 28 to cover "Session Key:  " intro 14chars = 28 bytes
	byte[] receiveLength = new byte[maxSizeOfData];
	byte[] receiveMsg = new byte[maxSizeOfData]; 
	byte[] sendData  = new byte[maxSizeOfData]; 


	DatagramPacket receivePacketLength = 
	    new DatagramPacket(receiveLength, receiveLength.length); //"1.9"

	DatagramPacket receivePacketMsg = 
	    new DatagramPacket(receiveMsg, receiveMsg.length); //"1.10"

	try{ //try to receive first message
	    serverSocket.receive(receivePacketLength); 
	    serverSocket.setSoTimeout(2000);  
	    
	    length = new String(receivePacketLength.getData(), receivePacketLength.getOffset(), receivePacketLength.getLength(), "UTF-8"); 
	}
	catch (Exception e){
	    System.out.println("Could not fetch result.  Terminating.");
	    System.exit(1);
	}
	try{ //try to receive second message
	    serverSocket.receive(receivePacketMsg);
	    msg = new String(receivePacketMsg.getData(), receivePacketMsg.getOffset(), receivePacketMsg.getLength(), "UTF-8"); //"2.2"
	}
	catch (Exception e){
	    System.out.println("Could not fetch result.  Terminating.");
	    System.exit(1);
	}

	//send ACK message
	
	if(msg.equals("Invalid session key.  Terminating.") || Integer.parseInt(length) == msg.length())
	    {
		String ack = "ACK";
		sendData = ack.getBytes(); 
		
		DatagramPacket sendPacket = 
		    new DatagramPacket(sendData, sendData.length, receivePacketLength.getAddress(),
				       receivePacketLength.getPort()); //"2.3"
		try{
		    serverSocket.send(sendPacket); 
		    if(DEBUG)
			System.out.println("sending msg packet now");
		    return msg;
		    
		}
		catch (Exception e){
		    if(DEBUG)
			System.out.println("did not send ACK");
		}
	
	    }
       
	if(DEBUG)
	    System.out.println("past receive packet");

	return null;
	
    }

  
    /* ---ackHandshake--- 
       takes in:  the socket, a msg to the client
       calls sendLengthAndMessage
       checks to make sure it has received 'ACK' response from client
    */

    public static int ackHandshake(DatagramSocket serverSocket, String sendToClient)
    {
	try{
	    //ack handshake
	    sendLengthAndMessage(serverSocket, sendToClient);
	    serverSocket.setSoTimeout(500);


	    int maxTimes = 2; //can only send packet 2 more times
	    int receivedACK = -1;

	    for(int i =0 ; i<maxTimes; i++)
		{
		    if((receivedACK = receiveACK(serverSocket)) == 0)
			{
			    sendLengthAndMessage(serverSocket,sendToClient);
			    serverSocket.setSoTimeout(500);
			}
		    else { break;}
		}

	    if(receivedACK == 0)
	    	{
		    if(DEBUG)
			System.out.println("did not get ACK");
		    //ACTUAL ERROR MESSAGE IS:
		    System.out.println("Failed to send message.  Terminating.");
		    return -1;
		   
		}
	    
	}
	catch(Exception e){
	 
	}
	return 0;
    }

} 

