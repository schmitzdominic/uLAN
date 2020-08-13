package entities.payload;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ReleaseChange extends IncludeReleases {

    private final String mode = "RELEASE_CHANGE";

    public ReleaseChange() {
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
        return super.hasReleases();
    }

    @Override
    public String toString() {
        return "ReleaseChange{" +
                "mode='" + mode + '\'' +
                ", registry=" + registry +
                '}';
    }
}
