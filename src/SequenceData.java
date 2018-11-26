import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SequenceData {
    public static SequenceData instance;

    private HashMap<String, List<ServerDetails>> servers = new HashMap<>();
    private HashMap<Integer, PacketData> requests = new HashMap<>();
    private int sequenceNumber = 0;

    private SequenceData() { }

    public void initializeServers() {
        List<ServerDetails> compServers = new ArrayList<>();
        compServers.add(new ServerDetails("192.168.1.1", 8002));
        compServers.add(new ServerDetails("192.168.1.1", 8002));
        compServers.add(new ServerDetails("192.168.1.1", 8002));
        servers.put("comp", compServers);
    }

    public List<ServerDetails> getServer(String server) {
        return servers.get(server);
    }

    public static SequenceData getInstance() {
        if (instance == null)
            instance = new SequenceData();
        return instance;
    }

    synchronized public void incrementSequenceNumber() {
        this.sequenceNumber++;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void addRequestToQueue(int sequence, String request, String server) {
        this.requests.put(sequence, new PacketData(request, getServer(server)));
    }

    public void removeFromQueue(int sequence, String ip) {
        if (requests.containsKey(sequence)) {
            PacketData data = requests.get(sequence);
            data.servers = removeBasedOnIp(data.servers, ip);
            if (data.servers.size() == 0)
                requests.remove(sequence);
            else
                requests.put(sequence, data);
        }
    }

    public List<ServerDetails> removeBasedOnIp(List<ServerDetails> serverDetails, String ip) {
        int index =  -1;
        for (int i = 0; i < serverDetails.size(); i++) {
            if (serverDetails.get(i).ip.equalsIgnoreCase(ip)) {
                index = i;
                break;
            }
        }
        serverDetails.remove(index);
        return serverDetails;
    }
}
