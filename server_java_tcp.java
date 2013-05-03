import java.net.*;
import java.io.*;
import java.util.Random;

/* Carina Rammelkamp CS176A Fall 2012 
   class server_java_tcp
   tutorial and learning material taken from: 
   http://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
   The commented numbers, numbers 1 through 10, (ie. '//1') indicate 
   lines copied from the client code used in the oracle tutorial.
   
   http://stackoverflow.com/questions/2978803/help-for-creating-a-random-string
   The commented numbers, numbers 11 through 16, (ie. '//1') indicate 
   lines copied from the client code used in the stackoverflow tutorial.
*/

public class server_java_tcp {
    public static void main(String[] args) throws IOException {

	Boolean TAKEOUT = false;

        ServerSocket serverSocket = null; //1

	if(Integer.parseInt(args[0]) < 1024 || Integer.parseInt(args[0]) > 65535)
	     return;
        try {
            serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        } catch (IOException e) { //2
	    if(TAKEOUT)
		System.err.println("Could not listen on port.");
            System.exit(1);
        }

        Socket clientSocket = null;

        try { //3
            clientSocket = serverSocket.accept(); //4
        } catch (IOException e) { //5
	    if(TAKEOUT)
		System.err.println("Accept failed.");
            System.exit(1); //6
        }

	int length = -1;
	OutputStream out = clientSocket.getOutputStream(); //7
        InputStream in = clientSocket.getInputStream();	//8						
	String inputLine = null;//9
	String sessionKey = new String();
	//byte[] seshKey = new byte[10000];
	byte[] fromClient = new byte[10000];
	String msgFromClient = new String();
	Integer keyLength = -1;
	
	try{
	    length = in.read(fromClient);
	    msgFromClient = new String(fromClient, 0, length);
	    keyLength = Integer.valueOf(msgFromClient.substring(23));

	if(TAKEOUT)
	    System.out.println("msg from client is: "+msgFromClient);
	}

	catch (Exception e){
	    if(TAKEOUT)
		System.out.println("could not read first message from client");
	    System.exit(1);}


	if(TAKEOUT)
	    {
		System.out.println(msgFromClient);
		System.out.println(keyLength);
	    }

	/* sending sessionKey to client */
	if(msgFromClient.substring(0,21).compareTo("Connect.  Key length:")==0)
	    {
	
		sessionKey = "Session Key:  "+generateSessionKey(keyLength.intValue());
		out.write(sessionKey.getBytes());
		out.flush();

		if(TAKEOUT)
		    System.out.println("got message from client, sending client session key:"+sessionKey);
    
		try{
		    
		while (true) {//10
		    try{

		    fromClient = new byte[10000];
		    length = in.read(fromClient);
		    inputLine = new String(fromClient, 0, length);

		    if(TAKEOUT)
			System.out.println("*******"+inputLine);

		    }catch(Exception e){ System.exit(1);}

		    if((inputLine.trim()).equals(sessionKey)){
			sessionKey = "Session Key:  "+generateSessionKey(keyLength.intValue());
			out.write(sessionKey.getBytes());
			out.flush();
		    }
		    else{

			if(TAKEOUT)
			    {
				System.out.println("sessionKey>>>>>>>>>"+sessionKey);
				System.out.println("input>>>>>>>>>>>>>>"+ inputLine);
			    }

			out.write((new String("Invalid session key.  Terminating.")).getBytes());
			out.flush();
			break;
		    }
		    if(TAKEOUT)
			System.out.println("got message from client, sending client session key:"+sessionKey);
		}
		}
		catch (Exception e){ System.exit(1);}
	    }


        out.close();
        in.close();
        clientSocket.close();
        serverSocket.close();
    }
    
    public static String generateSessionKey(int length){//11
	String alphabet = new String("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"); //12
	int n = alphabet.length(); //13

	String result = new String();
	Random r = new Random(); //14

	for (int i=0; i<length; i++) //15
	    result = result + alphabet.charAt(r.nextInt(n)); //16
	
	return result;
    }
}