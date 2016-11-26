package fognode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;

public class FogNode extends Thread{

	Socket neighbor;
	public FogNode(Socket neighbor) {
		// TODO Auto-generated constructor stub
		this.neighbor = neighbor;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		try 
		{
			new receiver(neighbor.getInputStream()).start();
			new update_sender(neighbor.getOutputStream()).start();
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	class iot_requestSender extends Thread
//	{	
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			super.run();
//			
//		}
//	}
	
	class update_sender extends Thread
	{
		OutputStreamWriter client;
		update_sender(OutputStream client)
		{
			this.client = new OutputStreamWriter(client);
		}
		@Override
		public void run() {
			BufferedWriter writer = null;
			try 
			{
				writer = new BufferedWriter(client);
				while(true)
				{
//					writer.write(FogNodeMain.max_response_time+"\n");
					writer.write(RequestQueue.getQueuingDelay()+"\n");
					writer.flush();
					Thread.sleep(FogNodeMain.update_interval*1000);
				}
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(Exception e)
			{
				e.printStackTrace();
			} finally
			{
				try 
				{
					client.close();
					writer.close();
				} catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	class receiver extends Thread
	{
		InputStreamReader client;
		receiver(InputStream client)
		{
			this.client = new InputStreamReader(client);
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			BufferedReader reader = null;
			try 
			{
				reader = new BufferedReader(client);
				while(true)
				{
					String message = reader.readLine();
					new FogNodeRequestProcessor(message,neighbor).start();
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally 
			{
				try 
				{
					client.close();
					reader.close();
				}
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
}