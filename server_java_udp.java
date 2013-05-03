import java.io.*; 
import java.net.*; 
import java.util.Random;
  
/*Carina Rammelkamp CS176A Fall 2012

  material taken from: 
  source 1: http://www.cs.uic.edu/~troy/spring05/cs450/sockets/socket.html
  source 2: http://stackoverflow.com/questions/5267241/numberformatexception-error-parseint
  source 3: http://stackoverflow.com/questions/2978803/help-for-creating-a-random-string
 
  lines copied/modified from source 1 are labeled "1.1" through "1.14"
  lines copied/modified from source 2 are labeled "2.1" through "2.3"
  lines copied/modified from source 3 are labeled "3.1" through "3.6"
*/

class server_java_udp { 
    public static Boolean DEBUG = false;
    public static Boolean DEBUGGER = false;
    public static InetAddress ipAddress = null;
    public static int port = -1;

    public static void main(String args[]) throws Exception 
    { 


	String msgFromClient = null;
	int keyLength = -1;
	String sessionKey = null;
	String sendToClient=null;
	DatagramSocket serverSocket = null;
	int getACK=-22;

	try { 
	    if(args.length != 1)
		return;
	    try{
		port = Integer.parseInt(args[0]); //"1.1"
		serverSocket = new DatagramSocket(port); //"1.2"
	    }
	    catch (Exception e){ //"1.3"
		System.exit(1); //"1.4"
	    }
		
	    //server receiving messages from client
	    
	    msgFromClient = receive(serverSocket); //receive also sends 'ACK'
	    keyLength = parseClientMsg(msgFromClient);
	    sessionKey = generateSessionKey(keyLength);
	    sendToClient = "Session Key:  "+sessionKey;


	    if(DEBUG){
		System.out.println("message from client was: "+msgFromClient);
		System.out.println("key length from client was: "+keyLength);
		System.out.println("randomly generated key was: "+sessionKey);
	    }

	    getACK = ackHandshake(serverSocket, sendToClient); //sends new (2) messages to client, then waits for ACK
	    if(getACK == -1)
		System.exit(1);

	    while(true)
		{
		    if(sendToClient.equals(receive(serverSocket).trim())) 
			{
			    sessionKey = generateSessionKey(keyLength);
			    sendToClient = "Session Key:  "+sessionKey;
			    getACK = ackHandshake(serverSocket,sendToClient);
			    if(getACK == -1)
				System.exit(1);
			}
		    else{ 
			if(DEBUG)
			    System.out.println("content did not match");
			//send message
			String terminate = "Invalid session key.  Terminating.";
			getACK = ackHandshake(serverSocket, terminate);
			System.exit(1);
			//sendLengthAndMessage(serverSocket,terminate,ipAddress,port);
			//serverSocket.disconnect();	
			//System.exit(1);
				
		    }  
		}
	}
	catch (Exception ex) {
	    System.exit(1);
	}

    }

    /* ---receive--- 
       takes in:  the 'length' and 'msg' sent by the client
       and returns 'ACK' to the client
       returns:   the msg sent by the client
    */

    public static String receive(DatagramSocket serverSocket)
    {
	String length = null;
	String msg = null;
	
	int maxSizeOfData = 4096+28; // add 28 to cover "Session Key:  " intro 14chars = 28 bytes
	byte[] receiveLength = new byte[maxSizeOfData]; //"1.5"
	byte[] receiveMsg = new byte[maxSizeOfData]; //"1.6"
	byte[] sendData  = new byte[maxSizeOfData];  //"1.7"

	//get Packet Length (in bytes) + Packet Message from Client
	DatagramPacket receivePacketLength = 
	    new DatagramPacket(receiveLength, receiveLength.length); //"1.8" 

	DatagramPacket receivePacketMsg = 
	    new DatagramPacket(receiveMsg, receiveMsg.length); //"1.9" 
	    
	try{  //try to receive first message
	    serverSocket.setSoTimeout(0);
	    serverSocket.receive(receivePacketLength); //"1.10"  
	    serverSocket.setSoTimeout(2000);	    
	    length = new String(receivePacketLength.getData(), receivePacketLength.getOffset(), receivePacketLength.getLength(), "UTF-8"); //"2.1"
	}
	catch (Exception e){
	    if(DEBUG)
		System.out.println("did not recieve packets 1.");
	    System.exit(1);
		
	}

	try{ //try to receive second message
	    serverSocket.receive(receivePacketMsg);
	    msg = new String(receivePacketMsg.getData(), receivePacketMsg.getOffset(), receivePacketMsg.getLength(), "UTF-8"); //"2.2"
	}
	catch (Exception e){
	    if(DEBUG)
		System.out.println("did not recieve packets 2.");
	    System.exit(1); //"1.11" 
		
	}
	if(DEBUG)
	    {
		System.out.println("got packet info::::::::: "+length);
		System.out.println("got packet msg::::::::: "+msg);
	    }

	//send ACK message
	if(DEBUGGER)
	    {	    try{Thread.sleep(2000);
		} catch(Exception e){}
	    }


	if(msg==null || length == null)
	    {
		if(DEBUG)
		    System.out.println("msg and length are null");
	    }

	else if(Integer.parseInt(length) == msg.length())
	    {

		String ack = "ACK";
		sendData = ack.getBytes(); 
		    
		ipAddress = receivePacketLength.getAddress(); 
		port = receivePacketLength.getPort(); 
	
		DatagramPacket sendPacket = 
		    new DatagramPacket(sendData, sendData.length,ipAddress, port); //"1.12"
		try{
		    serverSocket.send(sendPacket); //"1.13"
		    return msg;
		    
		}
		catch (Exception e){
		    if(DEBUG)
			System.out.println("did not send ACK");
		}
	    }
		
	else
	    {
		if(DEBUG)
		    System.out.println("did not have correct number of bytes");
		System.exit(1);
	    }

	if(DEBUG)
	    System.out.println("past receive packet");

	return null;
    }


    /* ---receiveACK--- 
       takes in:  the socket
       returns:   1 if the server recieved 'ACK',
       0 else
    */

    public static int receiveACK(DatagramSocket clientSocket)
    {
	String ack=null;

	int maxSizeOfData = 4096+28; // add 28 to cover "Session Key:  " intro 14chars = 28 bytes
	byte[] getAckMsg = new byte[maxSizeOfData]; //"1.13"

	DatagramPacket ackPacket=
	    new DatagramPacket(getAckMsg, getAckMsg.length); //"1.14" 
	
	try{
	    clientSocket.receive(ackPacket);
	    ack = new String(ackPacket.getData(),ackPacket.getOffset(),ackPacket.getLength(),"UTF-8"); //"2.3"
	    if(DEBUG)
		System.out.println("message sent from client:" +ack);
		 
	}
	catch (Exception e){ return 0; }
	 
	

	if(ack.compareTo("ACK") == 0)
	    return 1;
	else
	    return 0;
	 
    }


    public static int parseClientMsg(String msg)
    {
	String getKeyLength = msg.substring(23);
	return Integer.parseInt(getKeyLength);
    }

    public static String generateSessionKey(int length){//"3.1"
	String alphabet = new String("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"); //"3.2"
	int n = alphabet.length(); //"3.3"

	String result = new String();
	Random r = new Random(); //"3.4"

	for (int i=0; i<length; i++) //"3.5"
	    result = result + alphabet.charAt(r.nextInt(n)); //"3.6"
	
	return result;
    }

    public static void sendLengthAndMessage(DatagramSocket serverSocket, String msg){


	byte[] sendMsg = msg.getBytes();
	byte[] msgLength = (Integer.toString(msg.length())).getBytes(); 

	
	DatagramPacket sendMsgLength = 
	    new DatagramPacket(msgLength, msgLength.length, ipAddress, port); 
	DatagramPacket sendPacketWithMsg = 
	    new DatagramPacket(sendMsg, sendMsg.length, ipAddress, port); 

	try{
	   
	    serverSocket.send(sendMsgLength); 

	    if(DEBUG)
		System.out.println("in sendLengthAndMessage, trying to send: "+msg);
	
	    serverSocket.send(sendPacketWithMsg); 

	    if(DEBUG)
		{
		    System.out.println("GOING TO SEND string length to server>>>" + sendMsg.length);
		    System.out.println("GOING TO SEND message to server>>>" + msg);
		    System.out.println("the length of the int representing the number of bytes has this many bytes: "+msgLength.length);
		}


	}
	catch (Exception e){}

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
	    sendLengthAndMessage(serverSocket , sendToClient);
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
		    return -1;
		}
	}
	catch(Exception e){}
	return 0;
    }

}  

