/**
 * Server details of a particular replica
 * @author a_hareen
 *
 */
public class ServerDetails {
	// address of the server
    public String ip;
    // port number of the server
    public int port;

    ServerDetails(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}
