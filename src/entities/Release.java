package entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Release {

    private final String name;
    private final String path;

    @JsonCreator
    public Release(@JsonProperty("name") final String name, @JsonProperty("path") final String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "Release{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
