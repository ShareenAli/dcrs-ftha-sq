import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Sequencer {
    public static void main(String[] args) {
        Logger logs = Logger.getLogger("sequencer");

        try {
            FileHandler handler = new FileHandler("sequencer.log", true);
            logs.addHandler(handler);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // get the reference of the singleton data class to initialize the possible servers in the network.
        SequenceData sequenceData = SequenceData.getInstance();
        sequenceData.initializeServers();
        
        // start the thread to listen to the replica ack.
        RepToSeqThread repToSeqThread = new RepToSeqThread(logs);
        repToSeqThread.start();
        
        // check and clean the queue every thirty seconds. 
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {
//                executeTimelyThread(); // acknowledge replica
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 30, 30, TimeUnit.SECONDS);

        try {
        	// open the multi cast socket to listen to Front End
        	MulticastSocket socket = new MulticastSocket(8033);
        	socket.joinGroup(InetAddress.getByName("230.1.1.5"));
            System.out.println("Sequencer listening to FE requests on: " + (8033));
            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                // new thread per request. because jungle ka ek hi sher hota hai.
                SequencerThread sequencerThread = new SequencerThread(request, logs);
                sequencerThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("Duplicates")
    private static void executeTimelyThread() throws IOException {
        SequenceData data = SequenceData.getInstance();
        // this thread runs every 30 seconds
        for (Map.Entry<Integer, PacketData> compEntry : data.getCompRequestTrack().entrySet()) {
            PacketData packetData = compEntry.getValue();
            byte[] contentMessage = serialize(packetData.content);
            for (ServerDetails details : packetData.servers) {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(contentMessage, contentMessage.length, InetAddress.getByName(details.ip), details.port);
                    socket.send(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        for (Map.Entry<Integer, PacketData> soenEntry : data.getSoenRequestTrack().entrySet()) {
            PacketData packetData = soenEntry.getValue();
            byte[] contentMessage = serialize(packetData.content);
            for (ServerDetails details : packetData.servers) {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(contentMessage, contentMessage.length, InetAddress.getByName(details.ip), details.port);
                    socket.send(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        for (Map.Entry<Integer, PacketData> inseEntry : data.getInseRequestTrack().entrySet()) {
            PacketData packetData = inseEntry.getValue();
            byte[] contentMessage = serialize(packetData.content);
            for (ServerDetails details : packetData.servers) {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(contentMessage, contentMessage.length, InetAddress.getByName(details.ip), details.port);
                    socket.send(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        data.clearHashMaps();
    }

    private static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
            try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }
}
