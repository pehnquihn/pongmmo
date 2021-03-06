package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TcpConnection {
	
	private Socket socket;
	private PrintWriter pw;
	private BufferedReader br;
	
	private List<Packet> sendQue = Collections.synchronizedList(new ArrayList<Packet>());
	private List<Packet> recvQue = Collections.synchronizedList(new ArrayList<Packet>());
	private TcpReadThread readThread;
	private TcpWriteThread writeThread;
	
	public TcpConnection(Socket socket, String threadString){
		try {
			this.socket = socket;
			pw = new PrintWriter(socket.getOutputStream(), true);
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		readThread = new TcpReadThread(this, threadString + " read thread");
		writeThread = new TcpWriteThread(this, threadString + " write thread");
		readThread.start();
		writeThread.start();
	}
	
	public void addToSendQue(Packet p){
		sendQue.add(p);
	}
	
	public ArrayList<Packet> getOutstandingPackets(int id){
		ArrayList<Packet> tempList = new ArrayList<Packet>();
		synchronized (recvQue){
			for(Packet p : recvQue){
				if(p.getPacketId() == id){
					tempList.add(p);
				}
			}
			for(Packet p : tempList){
				recvQue.remove(p);
			}
		}
		return tempList;
	}
	
	public boolean readPacket(){
		try {
			if(br.ready()){
				synchronized (recvQue){
					recvQue.add(Packet.readPacket(br));
				}
				return true;
			}
		} catch (IOException e) {
			;
		}
		return false;
	}
	
	public boolean sendPacket(){
		synchronized (sendQue){
			if(sendQue.size() > 0){
				sendQue.remove(0).writePacketData(pw);
				return true;
			}
		}
		return false;
	}
	
	public void shutdownConnection(){
		try {
			br.close();
			pw.close();
			socket.close();
			readThread.terminate();
			writeThread.terminate();
			readThread.join();
			writeThread.join();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isConnected(){
		return socket.isConnected();
	}
	
	static boolean readNetworkPacket(TcpConnection tc){
		return tc.readPacket();
	}
	
	static boolean sendNetworkPacket(TcpConnection tc){
		return tc.sendPacket();
	}
	
}
