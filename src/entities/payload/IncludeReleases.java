package entities.payload;

import entities.Release;

import java.util.List;

public abstract class IncludeReleases extends DefaultPayload {

    private List<Release> releases = registry.getReleasesList();

    public IncludeReleases() {
        super();
    }

    public List<Release> getReleases() {
        return releases;
    }

    public void setReleases(final List<Release> releases) {
        this.releases = releases;
    }

    @Override
    public boolean hasReleases() {
        return releases != null && releases.size() > 0;
    }
}
