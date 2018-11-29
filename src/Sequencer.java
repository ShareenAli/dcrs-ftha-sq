import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sequencer {
    public static void main(String[] args) {
        SequenceData sequenceData = SequenceData.getInstance();
        sequenceData.initializeServers();

        RepToSeqThread repToSeqThread = new RepToSeqThread();
        repToSeqThread.start();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {
                executeTimelyThread();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 30, 30, TimeUnit.SECONDS);

        try {
            DatagramSocket socket = new DatagramSocket(8033);
            System.out.println("Sequencer listening to FE requests on: " + (8033));

            byte[] buffer = new byte[1000];

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                SequencerThread sequencerThread = new SequencerThread(request);
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

                    DatagramPacket packet = new DatagramPacket(contentMessage, contentMessage.length,
                            InetAddress.getByName(details.ip), details.port);

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

                    DatagramPacket packet = new DatagramPacket(contentMessage, contentMessage.length,
                            InetAddress.getByName(details.ip), details.port);

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

                    DatagramPacket packet = new DatagramPacket(contentMessage, contentMessage.length,
                            InetAddress.getByName(details.ip), details.port);

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
