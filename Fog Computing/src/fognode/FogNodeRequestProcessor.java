package fognode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;

public class FogNodeRequestProcessor extends Thread{

	String request;
	Socket neighbor;
	public FogNodeRequestProcessor(String request,Socket neighbor) {
		// TODO Auto-generated constructor stub
		this.request = request;
		this.neighbor = neighbor;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		//neighbor MRT Update
		try
		{
			int neighbor_response_time = Integer.parseInt(request);
			FogNodeMain.neighbor_mrt.put(neighbor, neighbor_response_time);
			/*
			 * Code to display Periodic Updates
			 * */
//			System.out.println("\nRT Update Received from: " + neighbor.getRemoteSocketAddress().toString());
//			Iterator<?> iterator = FogNodeMain.neighbor_mrt.entrySet().iterator();
//			while(iterator.hasNext())
//			{
//				Map.Entry pair = (Map.Entry)iterator.next();
//				System.out.println(pair.getKey()+":"+pair.getValue());
//			}
		}

		//IOTRequest forwared from Neighbor
		catch(NumberFormatException exception)
		{
			try
			{
				System.out.println("\nIOT Request Received from Neighbor FogNode: "+neighbor.getRemoteSocketAddress().toString());
				String[] requestValues = request.split(" ");
				String sequenceNumber = requestValues[0];
				String processingTime = requestValues[1];
				String forwardLimit = requestValues[2];
				String IOTHostName = requestValues[3];
				String IOTRequestPath = requestValues[4].trim();
				String IOTPort = IOTRequestPath.split(";")[0].split(":")[1];

				int processingTimeValue = 2*Integer.parseInt(processingTime.split(":")[1]);
				int forwardLimitOfRequest = Integer.parseInt(forwardLimit.split(":")[1]);
				int currentQueuingDelay = RequestQueue.getQueuingDelay();


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
				cookedRequest = cookedRequest.concat(IOTRequestPath);
				cookedRequest = cookedRequest.concat(";");
				cookedRequest = cookedRequest.concat(fogNodeDetails);
				
				//if Node Can process request
				//cook the request before adding to request queue
				if(currentQueuingDelay + processingTimeValue <= FogNodeMain.max_response_time && forwardLimitOfRequest>0)
				{
					System.out.println("Adding Request to Queue. Queue Size : "+RequestQueue.queue.size());
					RequestQueue.insertInQueue(cookedRequest, processingTimeValue);
				}

				//if node cant process request and forwardLimit > 0
				else if(forwardLimitOfRequest>0)
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
					
					System.out.println("Sending Request to Neighbor: "+socket+" as Queue is at Limit["+RequestQueue.getQueuingDelay()+"].");
					System.out.println(cookedRequest);
					try {
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
						writer.write(cookedRequest+"\n");
						writer.flush();
//						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//sent to cloud
				else if (forwardLimitOfRequest == 0)
				{
					System.out.println("As forwarding limit has been reached, packet was forwarded to the Cloud.");
					System.out.println("Packet: "+cookedRequest);
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}
