package entities.payload;

import com.fasterxml.jackson.core.JsonProcessingException;

public class InitRepeat extends IncludeReleases {

    private final String mode = "REPEAT";

    public InitRepeat() {
        super();
    }

    public InitRepeat(final String json) throws JsonProcessingException {
        final InitRepeat initRepeat = objectMapper.readValue(json, getClass());
        super.setId(initRepeat.getId());
        super.setIp(initRepeat.getIp());
        super.setHostName(initRepeat.getHostName());
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
        return "InitRepeat{" +
                "mode='" + mode + '\'' +
                ", registry=" + registry +
                '}';
    }
}
