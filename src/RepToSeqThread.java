import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class RepToSeqThread implements Runnable {
    public RepToSeqThread() { }

    public void start() {
        Thread thread = new Thread(this, "Replica To Sequencer");
        thread.start();
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(8044);

            while (true) {
                byte[] incoming = new byte[1000];

                DatagramPacket packet = new DatagramPacket(incoming, incoming.length);
                socket.receive(packet);

                int sequence = (int) deserialize(packet.getData());

                SequenceData data = SequenceData.getInstance();
                data.removeFromQueue(sequence, packet.getAddress().getHostAddress());
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
