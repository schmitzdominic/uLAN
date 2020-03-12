package interfaces;

public interface ClientsCallback {
    void notifyClientHasChanged();

    void removeClient(String id);
}
