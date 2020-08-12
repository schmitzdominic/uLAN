package entities.payload;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ReleaseChange extends IncludeReleases {

    private final String mode = "RELEASECHANGE";

    public ReleaseChange() {
        super();
    }

    public ReleaseChange(final String json) throws JsonProcessingException {
        final ReleaseChange releaseChange = objectMapper.readValue(json, getClass());
        super.setId(releaseChange.getId());
        super.setIp(releaseChange.getIp());
        super.setHostName(releaseChange.getHostName());
        super.setReleases(releaseChange.getReleases());
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
        return "ReleaseChange{" +
                "mode='" + mode + '\'' +
                ", registry=" + registry +
                '}';
    }
}
