import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Logger;

public class RepToSeqThread implements Runnable {
    private Logger logs;

    public RepToSeqThread(Logger logs) {
        this.logs = logs;
    }

    public void start() {
        Thread thread = new Thread(this, "Replica To Sequencer");
        thread.start();
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(8044);
            
            System.out.println("Sequencer listening to replica requests on: " + (8044));

            while (true) {
            	// receive incoming ack.
                byte[] incoming = new byte[1000];

                DatagramPacket packet = new DatagramPacket(incoming, incoming.length);
                socket.receive(packet);

                // parse the content. it will be sequence-serverName format.
                String content = (String) deserialize(packet.getData());
                String contents[] = content.split("-");

                int sequence = Integer.parseInt(contents[0]);
                String server = contents[1];

                // remove from the queue accordingly.
                SequenceData data = SequenceData.getInstance();
                data.removeFromQueue(sequence, packet.getAddress().getHostAddress(), server);
            }
        } catch (Exception e) {
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
