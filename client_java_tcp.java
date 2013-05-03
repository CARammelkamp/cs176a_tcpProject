
import java.io.*;
import java.net.*;

/* Carina Rammelkamp CS176A Fall 2012 
   class client_java_tcp
   tutorial and learning material taken from: 
   http://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
   The commented numbers, numbers "1" through "12" indicate 
   lines copied from the client code used in this tutorial.
*/

public class client_java_tcp {
    public static void main(String[] args) throws IOException{

	Boolean TAKEOUT = false;
	Socket tcpSocket = null;
        OutputStream out = null;
        InputStream in = null;
	int keylength = -1;

	if(TAKEOUT)
	    {
		System.out.println("server ip: "+args[0].toString());
		System.out.println("port: "+args[1].toString());
		System.out.println("key length:" + Integer.valueOf(args[2]).toString());
		System.out.println("number of elements in input array: "+args.length);
	    }
   
	if(args.length != 3)
	    return;
	else if(Integer.parseInt(args[2]) < 1 || Integer.parseInt(args[2]) > 4096) //chars are 2 bits in java, and can only have a max 4096 bit key
	    return;
	else if(Integer.parseInt(args[1]) < 1024 || Integer.parseInt(args[1]) > 65535)
	    {
		System.err.println("Could not connect to server.  Terminating."); 
		System.exit(1);
	    }

	try {
	    tcpSocket = new Socket(args[0], Integer.parseInt(args[1])); //"1"
            out = tcpSocket.getOutputStream(); //"2"
            in = tcpSocket.getInputStream(); //"3"
            keylength = Integer.parseInt(args[2]);
	} catch (Exception e) { //"4"
	    System.err.println("Could not connect to server.  Terminating."); 
	    System.exit(1); //"5"
	} 





	String fromServer = new String(); //"6"
	byte[] getFromServer = new byte[10000];
	String toServer = "Connect.  Key length:  "+keylength; //"7"

	if(TAKEOUT)
	    {
		System.out.println("THE STRING:" +toServer);
		System.out.println("THE NUMBER OF BYTES: " + (toServer.getBytes()).length);
	    }

	int length;
	
	//trying to send info to server, use printWriter to print data to server
	
	try{
	    out.write(toServer.getBytes());

	    if(TAKEOUT)
		System.out.println(toServer);
	    
	    while (true) { //"8"
		try{
		    getFromServer = new byte[10000];
		    length = in.read(getFromServer); 
		    fromServer = new String(getFromServer,0,length);

		    if(TAKEOUT)
			{
			    System.out.println("THE STRING:" +fromServer);
			    System.out.println("THE NUMBER OF BYTES: " + (fromServer.getBytes()).length);
			}

		}catch (Exception e){
		    System.err.println("Could not fetch result.  Terminating.");
		    System.exit(1);}

	
			if(fromServer.compareTo("Invalid session key.  Terminating.")==0){
			    System.err.println(fromServer);
			    System.exit(1);
			}
			else{

			    try{
			    System.out.println(fromServer);
			    //toServer = fromServer.substring(fromServer.lastIndexOf(" ", fromServer.length()-1)+1);
			    // out.write(toServer.getBytes());  
			    out.write(fromServer.getBytes());  
			    out.flush();
			    Thread.sleep(1000);
			    }
			    catch (Exception ie){ //"9"
				//System.exit(1);
				System.err.println("Could not fetch result.  Terminating.");
				System.exit(1);
			    }			   
			}
	
	    }
	}
	    catch (IOException e){ 
		if(TAKEOUT)
		    System.out.println("could not write to server");
		
	
	      
	    out.close(); //"10"
	    in.close(); //"11"
	    tcpSocket.close(); //"12"
	    }
    }
}