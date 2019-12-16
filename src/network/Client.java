package network;

public class Client {
    private String id;
    private String ip;
    private String hostname;

    public Client(String id, String ip, String hostname){
        this.id = id;
        this.ip = ip;
        this.hostname = hostname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
