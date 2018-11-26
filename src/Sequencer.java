import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Sequencer {
    public static void main(String[] args) {
        SequenceData sequenceData = SequenceData.getInstance();
        sequenceData.initializeServers();

        RepToSeqThread repToSeqThread = new RepToSeqThread();
        repToSeqThread.start();

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
}
