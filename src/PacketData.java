import java.util.List;

/**
 * Packet to store it to the Sequence Queue.
 * @author a_hareen
 */
public class PacketData {
	// content to store 
    public String content;
    // list of servers the content has been sent to
    public List<ServerDetails> servers;

    public PacketData(String content, List<ServerDetails> servers) {
        this.content = content;
        this.servers = servers;
    }
}
