package packets;

import java.util.ArrayList;

public class Packet {
	
	private ArrayList<Packet> packets = new ArrayList<Packet>();
	
	public Packet(){
		packets.add(new Packet01Handshake());
	}
	
	public ArrayList<Packet> getPacketList(){
		return packets;
	}
	
	public int getPacketID(){
		return -1;
	}
	public String getPacketData(){
		return null;
	}
	
}
