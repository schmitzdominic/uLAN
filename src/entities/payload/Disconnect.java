package entities.payload;

import com.fasterxml.jackson.core.JsonProcessingException;

public class Disconnect extends DefaultPayload {

    private final String mode = "DISCONNECT";

    public Disconnect() {
        super();
    }

    public Disconnect(final String json) throws JsonProcessingException {
        final Disconnect disconnect = objectMapper.readValue(json, getClass());
        super.setId(disconnect.getId());
        super.setIp(disconnect.getIp());
        super.setHostName(disconnect.getHostName());
    }

    @Override
    public String getMode() {
        return mode;
    }

    @Override
    public String serializeToJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }

    @Override
    public void setParams(final String... params) {
        // No Param to set
    }

    @Override
    public String toString() {
        return "Disconnect{" +
                "mode='" + mode + '\'' +
                ", registry=" + registry +
                '}';
    }
}
