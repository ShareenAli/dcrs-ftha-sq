import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
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
            String content = (String) deserialize(packetFromFe.getData());
            String contents[] = content.split("-");
            String server = contents[1];
            SequenceData data = SequenceData.getInstance();
            data.incrementSequenceNumber(server);
            int sequenceNumber = data.getSequenceNumber(server);
            content = String.valueOf(sequenceNumber).concat("-" + content);
            data.addRequestToQueue(sequenceNumber, content, server);

            logs.info("\nSequence number of request : " + sequenceNumber + "\nMessage from FE : " + content + "\nRequest for " + server + " server.");
            System.out.println("\nSequence number of request : " + sequenceNumber + "\nMessage from FE : " + content + "\nRequest for " + server + " server.");

            List<ServerDetails> serverDetails = data.getServer(server);
            byte[] contentMessage = serialize(content);

            serverDetails.addAll(data.getServer("replicaManagers"));

            for (ServerDetails details : serverDetails) {
                DatagramSocket socket = new DatagramSocket();
                DatagramPacket packet = new DatagramPacket(contentMessage, contentMessage.length, InetAddress.getByName(details.ip), details.port);
                logs.info("\nInformation about packet : " + "\nContent Message : " + contentMessage + "\nContent message length : " + contentMessage.length
                + "\nIP Address : " + InetAddress.getByName(details.ip) + "\nPort Number : " + details.port);
                socket.send(packet);
            }
        } catch (ClassNotFoundException | IOException e) {
            logs.warning("Class not found.");
            e.printStackTrace();
        }
    }

    private static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
            try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream o = new ObjectInputStream(b)) {
                return o.readObject();
            }
        }
    }
}
