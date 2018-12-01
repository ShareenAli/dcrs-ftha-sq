import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SequenceData {
    public static SequenceData instance;

    private HashMap<String, List<ServerDetails>> servers = new HashMap<>();
    private HashMap<Integer, PacketData> compRequestTrack = new HashMap<>();
    private HashMap<Integer, PacketData> soenRequestTrack = new HashMap<>();
    private HashMap<Integer, PacketData> inseRequestTrack = new HashMap<>();
    private int compSequenceNumber = 0;
    private int soenSequenceNumber = 0;
    private int inseSequenceNumber = 0;

    private SequenceData() { }

    public void initializeServers() {
        List<ServerDetails> compServers = new ArrayList<>();
        compServers.add(new ServerDetails("230.1.1.1", 8004));
        compServers.add(new ServerDetails("230.1.1.2", 8004));
        compServers.add(new ServerDetails("230.1.1.3", 8004));
        servers.put("comp", compServers);

        List<ServerDetails> soenServers = new ArrayList<>();
        soenServers.add(new ServerDetails("230.1.1.1", 8005));
        soenServers.add(new ServerDetails("230.1.1.2", 8005));
        soenServers.add(new ServerDetails("230.1.1.3", 8005));
        servers.put("soen", soenServers);

        List<ServerDetails> inseServers = new ArrayList<>();
        inseServers.add(new ServerDetails("230.1.1.1", 8006));
        inseServers.add(new ServerDetails("230.1.1.2", 8006));
        inseServers.add(new ServerDetails("230.1.1.3", 8006));
        servers.put("inse", inseServers);

        List<ServerDetails> replicaManagers = new ArrayList<>();
        replicaManagers.add(new ServerDetails("230.1.1.1", 8007));
        replicaManagers.add(new ServerDetails("230.1.1.2", 8007));
        replicaManagers.add(new ServerDetails("230.1.1.3", 8007));
        servers.put("replicaManagers", replicaManagers);
    }

    public void clearHashMaps() {
        this.compRequestTrack.clear();
        this.soenRequestTrack.clear();
        this.inseRequestTrack.clear();
    }

    public HashMap<Integer, PacketData> getCompRequestTrack() {
        return compRequestTrack;
    }

    public HashMap<Integer, PacketData> getSoenRequestTrack() {
        return soenRequestTrack;
    }

    public HashMap<Integer, PacketData> getInseRequestTrack() {
        return inseRequestTrack;
    }

    public List<ServerDetails> getServer(String server) {
        return servers.get(server);
    }

    public static SequenceData getInstance() {
        if (instance == null)
            instance = new SequenceData();
        return instance;
    }

    public int getSequenceNumber(String server) {
        switch (server.toUpperCase()) {
            case "COMP":
                return compSequenceNumber;
            case "SOEN":
                return soenSequenceNumber;
            case "INSE":
                return inseSequenceNumber;
        }

        return 0;
    }

    synchronized public void incrementSequenceNumber(String server) {
        switch (server.toUpperCase()) {
            case "COMP":
                this.compSequenceNumber++;
                break;
            case "SOEN":
                this.soenSequenceNumber++;
                break;
            case "INSE":
                this.inseSequenceNumber++;
                break;
        }
    }

    public void addRequestToQueue(int sequence, String request, String server) {
        switch (server.toUpperCase()) {
            case "COMP":
                this.compRequestTrack.put(sequence, new PacketData(request, getServer(server)));
                break;
            case "SOEN":
                this.soenRequestTrack.put(sequence, new PacketData(request, getServer(server)));
                break;
            case "INSE":
                this.inseRequestTrack.put(sequence, new PacketData(request, getServer(server)));
                break;
        }
    }

    public void removeFromQueue(int sequence, String ip, String server) {
        switch (server.toUpperCase()) {
            case "COMP":
                removeFromCompQueue(sequence, ip);
                break;
            case "SOEN":
                removeFromSoenQueue(sequence, ip);
                break;
            case "INSE":
                removeFromInseQueue(sequence, ip);
                break;
        }
    }

    @SuppressWarnings("Duplicates")
    public void removeFromCompQueue(int sequence, String ip) {
        if (compRequestTrack.containsKey(sequence)) {
            PacketData data = compRequestTrack.get(sequence);
            data.servers = removeBasedOnIp(data.servers, ip);
            if (data.servers.size() == 0)
                compRequestTrack.remove(sequence);
            else
                compRequestTrack.put(sequence, data);
        }
    }

    @SuppressWarnings("Duplicates")
    public void removeFromSoenQueue(int sequence, String ip) {
        if (soenRequestTrack.containsKey(sequence)) {
            PacketData data = soenRequestTrack.get(sequence);
            data.servers = removeBasedOnIp(data.servers, ip);
            if (data.servers.size() == 0)
                soenRequestTrack.remove(sequence);
            else
                soenRequestTrack.put(sequence, data);
        }
    }

    @SuppressWarnings("Duplicates")
    public void removeFromInseQueue(int sequence, String ip) {
        if (inseRequestTrack.containsKey(sequence)) {
            PacketData data = inseRequestTrack.get(sequence);
            data.servers = removeBasedOnIp(data.servers, ip);
            if (data.servers.size() == 0)
                inseRequestTrack.remove(sequence);
            else
                inseRequestTrack.put(sequence, data);
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
