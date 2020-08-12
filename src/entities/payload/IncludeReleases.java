package entities.payload;

import entities.Release;

import java.util.List;

abstract class IncludeReleases extends DefaultPayload {

    private List<Release> releases = registry.getReleasesList();

    public List<Release> getReleases() {
        return releases;
    }

    public void setReleases(final List<Release> releases) {
        this.releases = releases;
    }
}
