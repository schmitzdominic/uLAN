package network;

public class Client {
    private String id;
    private String ip;
    private String hostname;
    private String listName;

    public Client(String id, String ip, String hostname){
        // TODO: Search in Registry, if found -> get information from registry, if not -> get parameter
        // TODO: If not found -> set registry parameter
        this.id = id;
        this.ip = ip;
        this.hostname = hostname;
        this.listName = hostname;
    }

    public String getId() {
        return this.id;
    }

    public String getHostname() {
        return this.hostname;
    }

    public String getListName() {
        return this.listName;
    }

    public String getIp() {
        return this.ip;
    }

    public void setId(String id) {
        this.id = id;
        // TODO: Set in Registry
    }

    public void setIp(String ip) {
        this.ip = ip;
        // TODO: Set in Registry
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
        // TODO: Set in Registry
    }

    public void setListName(String listName) {
        this.listName = listName;
        // TODO: Set in Registry
    }
}
