package network;

public class Download {
    private String id;
    private String path;

    public Download(String id, String path) {
        this.id = id;
        this.path = path;
    }

    public String getId() {
        return this.id;
    }

    public String getPath() {
        return this.path;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
