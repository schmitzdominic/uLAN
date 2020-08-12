package entities;

public class Release {

    private final String name;
    private final String path;

    public Release(final String name, final String path) {
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
