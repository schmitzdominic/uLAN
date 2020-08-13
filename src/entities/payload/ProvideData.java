package entities.payload;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ProvideData extends DefaultPayload {

    private final String mode = "PROVIDE_DATA";

    private String path = null;

    public ProvideData() {
        super();
    }

    public ProvideData(final String json) throws JsonProcessingException {
        final ProvideData provideData = objectMapper.readValue(json, getClass());
        super.setId(provideData.getId());
        super.setIp(provideData.getIp());
        super.setHostName(provideData.getHostName());
        setPath(provideData.getPath());
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
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
        if (params.length == 1) {
            path = params[0];
        }
    }

    @Override
    public boolean hasReleases() {
        return false;
    }

    @Override
    public String toString() {
        return "ProvideData{" +
                "mode='" + mode + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
