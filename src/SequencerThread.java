import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class SequencerThread implements Runnable {
    private DatagramPacket packetFromFe;

    public SequencerThread(DatagramPacket packetFromFe) {
        this.packetFromFe = packetFromFe;
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
            String server = contents[contents.length - 1];

            SequenceData data = SequenceData.getInstance();

            data.incrementSequenceNumber(server);

            int sequenceNumber = data.getSequenceNumber(server);
            content = String.valueOf(sequenceNumber).concat("-" + content);

            data.addRequestToQueue(sequenceNumber, content, server);

            System.out.println(content);

            List<ServerDetails> serverDetails = data.getServer(server);

            byte[] contentMessage = serialize(content);

            for (ServerDetails details : serverDetails) {
                DatagramSocket socket = new DatagramSocket();

                DatagramPacket packet = new DatagramPacket(contentMessage, contentMessage.length,
                        InetAddress.getByName(details.ip), details.port);

                socket.send(packet);
            }
        } catch (ClassNotFoundException | IOException e) {
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
