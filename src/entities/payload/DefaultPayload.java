package entities.payload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import helpers.Info;
import registry.Registry;

public abstract class DefaultPayload {

    private String id = Info.getSettings().get("id");
    private String ip = Info.getIp();
    private String hostName = Info.getHostname();

    final Registry registry = new Registry();
    final ObjectMapper objectMapper = new ObjectMapper();

    public abstract String getMode();

    public abstract String serializeToJson() throws JsonProcessingException;

    public abstract void setParams(final String... params);

    public abstract boolean hasReleases();

    public String getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public String getHostName() {
        return hostName;
    }

    public boolean isValid() {
        return getId() != null & getIp() != null & getHostName() != null;
    }

    protected void setId(final String id) {
        this.id = id;
    }

    protected void setIp(final String ip) {
        this.ip = ip;
    }

    protected void setHostName(final String hostName) {
        this.hostName = hostName;
    }

}
