package fognode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class IOTRequestAcceptor extends Thread{
	
	int myUdpPort;
	public static DatagramSocket dgSocket;
	public IOTRequestAcceptor(int myUdpPort, InetAddress myAddress) {
		// TODO Auto-generated constructor stub
		this.myUdpPort = myUdpPort;
		try 
		{
			dgSocket = new DatagramSocket(myUdpPort, myAddress);
			System.out.println("IOT Datagram Receiver Thread Started at port "+myUdpPort);
		} catch (SocketException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		while(true)
		{
			try 
			{
				byte[] requestReceived = new byte[1024];
				DatagramPacket packetFromIot = new DatagramPacket(requestReceived, requestReceived.length);
				dgSocket.receive(packetFromIot);
				InetAddress IOTSender = packetFromIot.getAddress();
				int remoteIOTPort = packetFromIot.getPort();
				String request = new String(packetFromIot.getData());
				System.out.println("\nIOT Request received from: "+packetFromIot.getAddress().toString());
				IOTRequestProcessor processor = new IOTRequestProcessor(IOTSender,remoteIOTPort,request);
				processor.start();
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
