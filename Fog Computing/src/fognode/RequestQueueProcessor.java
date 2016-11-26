package fognode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class RequestQueueProcessor extends Thread{
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		while(true)
		{
			if(RequestQueue.getQueueLength() > 0)
			{
				QueueFormat entry = RequestQueue.getNextDelay();
				String request = entry.data;
				int processingTime = entry.delay;
				String[] requestValues = request.split(" ");
				String IOTHostName = requestValues[3].split(":")[1];
				try {
					InetAddress IOTAddress = InetAddress.getByName(IOTHostName);
					int IOTPort = Integer.parseInt(requestValues[4].trim().split(";")[0].split(":")[1]);
					byte[] byteRequest = request.getBytes();
					Thread.sleep(processingTime*1000);
					System.out.println("\nIOT Request Processed :"+request);
					DatagramPacket packet = new DatagramPacket(byteRequest, byteRequest.length, IOTAddress, IOTPort);
					IOTRequestAcceptor.dgSocket.send(packet);
					RequestQueue.removeFromQueue();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
