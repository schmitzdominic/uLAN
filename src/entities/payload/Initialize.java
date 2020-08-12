package entities.payload;

import com.fasterxml.jackson.core.JsonProcessingException;

public class Initialize extends IncludeReleases {

    private final String mode = "INITIALIZE";

    public Initialize() {
        super();
    }

    public Initialize(final String json) throws JsonProcessingException {
        final Initialize initialize = objectMapper.readValue(json, getClass());
        super.setId(initialize.getId());
        super.setIp(initialize.getIp());
        super.setHostName(initialize.getHostName());
        super.setReleases(initialize.getReleases());
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
        return "Initialize{" +
                "mode='" + mode + '\'' +
                ", registry=" + registry +
                '}';
    }
}
