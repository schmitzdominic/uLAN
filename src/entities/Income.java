package entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.payload.DefaultPayload;

public class Income {

    private Payload mode;
    private String json;

    public Income(final String json) {
        try {
            final JsonNode parent = new ObjectMapper().readTree(json);
            mode = Payload.valueOf(parent.get("mode").asText());
            this.json = json;
        } catch (final JsonProcessingException jpe) {
            jpe.printStackTrace();
        }
    }

    public Payload getMode() {
        return mode;
    }

    public void setMode(final Payload mode) {
        this.mode = mode;
    }

    public String getJson() {
        return json;
    }

    public void setJson(final String json) {
        this.json = json;
    }

    public <T extends DefaultPayload> T getObject() {
        return mode.getPayloadInstanceFromJson(json);
    }

    @Override
    public String toString() {
        return "Income{" +
                "mode='" + mode + '\'' +
                ", json='" + json + '\'' +
                '}';
    }
}
