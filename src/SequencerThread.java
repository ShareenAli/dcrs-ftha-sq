import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Logger;

public class SequencerThread implements Runnable {
    private Logger logs;
    private DatagramPacket packetFromFe;

    public SequencerThread(DatagramPacket packetFromFe, Logger logs) {
        this.packetFromFe = packetFromFe;
        this.logs = logs;
    }

    public void start() {
        Thread thread = new Thread(this, "Sequencer Udp");
        thread.start();
    }

    @Override
    public void run() {
        try {
        	// parse the request from the content
        	String content = new String(packetFromFe.getData());
            String contents[] = content.split("-");
            
            // get the server name to send request to
            String server = contents[1].toLowerCase();
            
            // get the singleton reference of sequence data to increment the sequence number uniquely.
            SequenceData data = SequenceData.getInstance();
            data.incrementSequenceNumber(server);
            int sequenceNumber = data.getSequenceNumber(server);
            
            // attach sequence number to the content
            content = String.valueOf(sequenceNumber).concat("-" + content);
            data.addRequestToQueue(sequenceNumber, content, server);

            logs.info("\nSequence number of request : " + sequenceNumber + "\nMessage from FE : " + content + "\nRequest for " + server + " server.");
            System.out.println("\nSequence number of request : " + sequenceNumber + "\nMessage from FE : " + content + "\nRequest for " + server + " server.");

            // shoot them to relevant replica
            List<ServerDetails> serverDetails = data.getServer(server);
            List<ServerDetails> rmDetails = data.getServer("replicaManagers");
            byte[] contentMessage = content.getBytes();
            System.out.println("ServerDetails: ");
        	send(serverDetails, contentMessage);
        	send(rmDetails, contentMessage);
            
        } catch (Exception e) {
            logs.warning("Class not found.");
            e.printStackTrace();
        }
    }
    
    private void send(List<ServerDetails> serverDetails, byte[] contentMessage) throws SocketException, UnknownHostException, IOException {
    	for (ServerDetails details : serverDetails) {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(contentMessage, contentMessage.length, InetAddress.getByName(details.ip), details.port);
            String receiverName;
            switch (details.port) {
            case 8004 :
            	receiverName = "COMP";
            	break;
            case 8005 :
            	receiverName = "SOEN";
            	break;
            case 8006 : 
            	receiverName = "INSE";
            	break;
            case 8007 : 
            	receiverName = "REPLICA";
            	break;
        	default:
        		receiverName = "INTRUDERRRRR!";
        		break;
            }
            System.out.println("\nSequencer sending request to : " + receiverName + "\nIP : " + details.ip + "\nPORT : " + details.port);
            logs.info("\nInformation about packet : " + "\nContent Message : " + contentMessage + "\nContent message length : " + contentMessage.length
            + "\nIP Address : " + InetAddress.getByName(details.ip) + "\nPort Number : " + details.port);
//            System.out.println("\nInformation about packet : " + "\nContent Message : " + contentMessage + "\nContent message length : " + contentMessage.length
//                    + "\nIP Address : " + InetAddress.getByName(details.ip) + "\nPort Number : " + details.port);
            socket.send(packet);
        }
    }
}
