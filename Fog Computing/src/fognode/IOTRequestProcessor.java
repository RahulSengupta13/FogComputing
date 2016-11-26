package fognode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;

public class IOTRequestProcessor extends Thread{
	
	InetAddress IOTNodeAddress;
	int IOTRemotePort;
	String request;
	public IOTRequestProcessor(InetAddress IOTNodeAddress, int IOTRemotePort, String request) {
		// TODO Auto-generated constructor stub
		this.IOTNodeAddress = IOTNodeAddress;
		this.IOTRemotePort = IOTRemotePort;
		this.request = request;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		int currentQueuingDelay = RequestQueue.getQueuingDelay();
		
		//breakdown the packet
		String[] requestValues = request.split(" ");
		String sequenceNumber = requestValues[0];
		String processingTime = requestValues[1];
		String forwardLimit = requestValues[2];
		int processingTimeValue = 2*Integer.parseInt(processingTime.split(":")[1]);
		int forwardLimitOfRequest = Integer.parseInt(forwardLimit.split(":")[1]);
		String IOTHostName = requestValues[3];
		String IOTPort = requestValues[4].trim();
		

		String fogNodeDetails = "Visited_FogNode-"+FogNodeMain.my_address.toString();
		String cookedRequest = "";
		forwardLimitOfRequest--;
		cookedRequest = cookedRequest.concat(sequenceNumber);
		cookedRequest = cookedRequest.concat(" ");
		cookedRequest = cookedRequest.concat(processingTime);
		cookedRequest = cookedRequest.concat(" ");
		cookedRequest = cookedRequest.concat("FL:"+forwardLimitOfRequest);
		cookedRequest = cookedRequest.concat(" ");
		cookedRequest = cookedRequest.concat(IOTHostName);
		cookedRequest = cookedRequest.concat(" ");
		cookedRequest = cookedRequest.concat(IOTPort);
		cookedRequest = cookedRequest.concat(";");
		cookedRequest = cookedRequest.concat(fogNodeDetails);
		
		if(currentQueuingDelay + processingTimeValue <= FogNodeMain.max_response_time && forwardLimitOfRequest>0)
		{
			System.out.println("Adding to Request Queue. Size: "+RequestQueue.queue.size());
			RequestQueue.insertInQueue(cookedRequest, processingTimeValue);
		}
		
		//send to neighboring best fog node
		else if(forwardLimitOfRequest > 0)
		{
			int delay=9999;
			Socket socket = null;
			Iterator<?> iterator = FogNodeMain.neighbor_mrt.entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry pair = (Map.Entry)iterator.next();
				if((int)pair.getValue() < delay)
				{
					socket = (Socket)pair.getKey();
					delay = (int)pair.getValue();
				}
			}
			
			System.out.println("Sending Request to Neighbor: "+socket+" as Queue is at Limit["+RequestQueue.getQueuingDelay()+"]");
			System.out.println(cookedRequest);
			try {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				writer.write(cookedRequest+"\n");
				writer.flush();
//				writer.close();
			} catch (IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
		//send to cloud
		else if (forwardLimitOfRequest == 0)
		{
			System.out.println("\nAs forwarding limit has been reached, packet was forwarded to the Cloud.");
			System.out.println("Packet: "+cookedRequest);
		}
	}

}
