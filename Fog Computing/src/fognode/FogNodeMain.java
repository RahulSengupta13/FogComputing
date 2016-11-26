package fognode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class FogNodeMain {
	
	public static int max_response_time;
	public static int update_interval;
	public static InetAddress my_address;
	public static int my_udp_port;
	public static int my_tcp_port;
	public static ServerSocket my_tcp_socket;
	public static HashMap<Socket, Integer> neighbor_mrt = new HashMap<Socket, Integer>();
	public static RequestQueue delay_queue = new RequestQueue();
	public static void main(String[] args) 
	{
		if(args.length < 5)
		{
			System.out.println("Invalid number of arguments.");
			System.exit(0);
		}
		else if((args.length - 5) % 2 != 0)
		{
			System.out.println("IP/Port entries mismatched. Please enter an IP and a Port for every Fog Neighbor.");
			System.exit(0);
		}
		try {
			max_response_time = Integer.parseInt(args[0]);
			update_interval = Integer.parseInt(args[1]);
			my_address = InetAddress.getByName(args[2]);
			my_udp_port = Integer.parseInt(args[3]);
			my_tcp_port = Integer.parseInt(args[4]);
			my_tcp_socket = new ServerSocket(my_tcp_port);
			
			//start the iot message receiver thread
			IOTRequestAcceptor datagramAcceptor = new IOTRequestAcceptor(my_udp_port, my_address);
			datagramAcceptor.start();
			
			//start the request processor thread
			RequestQueueProcessor queueProcessor = new RequestQueueProcessor();
			queueProcessor.start();
			
			for(int i=5;i<args.length;i+=2)
			{
				InetAddress neighbor_fogNode = InetAddress.getByName(args[i]);
				int neighbor_fogNodeTcpPort = Integer.parseInt(args[i+1]);
				if(my_tcp_port < neighbor_fogNodeTcpPort)
				{
					System.out.println("TCP request initiated with: "+ args[i] +" at port: "+args[i+1]);
					Socket neighbor_socket = new Socket(neighbor_fogNode, neighbor_fogNodeTcpPort);
					neighbor_mrt.put(neighbor_socket, 9999);
					FogNode neighbor = new FogNode(neighbor_socket);
					neighbor.start();
				}
			}
			while(true)
			{
				Socket neighbor_socket = my_tcp_socket.accept();
				neighbor_mrt.put(neighbor_socket, 9999);
				System.out.println("TCP request received from: "+ neighbor_socket.getRemoteSocketAddress().toString());
				FogNode neighbor = new FogNode(neighbor_socket);
				neighbor.start();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
