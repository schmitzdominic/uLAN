package entities.payload;

import com.fasterxml.jackson.core.JsonProcessingException;

public class Disconnect extends DefaultPayload {

    private final String mode = "DISCONNECT";

    public Disconnect() {
        super();
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
    public boolean hasReleases() {
        return false;
    }

    @Override
    public String toString() {
        return "Disconnect{" +
                "mode='" + mode + '\'' +
                ", registry=" + registry +
                '}';
    }
}
