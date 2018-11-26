import java.util.ArrayList;
import java.util.List;

public class PacketData {
    public String content;
    public List<ServerDetails> servers;

    public PacketData(String content, List<ServerDetails> servers) {
        this.content = content;
        this.servers = servers;
    }
}
