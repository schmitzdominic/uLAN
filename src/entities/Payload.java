package entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import entities.payload.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

public enum Payload {

    INITIALIZE(new Initialize()),
    INIT_REPEAT(new InitRepeat()),
    RELEASE_CHANGE(new ReleaseChange()),
    DOWNLOAD_FOLDER(new DownloadData()),
    PROVIDE(new ProvideData()),
    DISCONNECT(new Disconnect());

    private final DefaultPayload defaultPayload;

    Payload(final DefaultPayload defaultPayload) {
        this.defaultPayload = defaultPayload;
    }

    public Payload setParams(final String... params) {
        defaultPayload.setParams(params);
        return this;
    }

    public String serializeToJson() {
        try {
            return defaultPayload.serializeToJson();
        } catch (final JsonProcessingException jpe) {
            jpe.printStackTrace();
        }
        return "{}";
    }

    public <T extends DefaultPayload> T getPayloadInstanceFromJson(final String json) {
        try {
            return (T) defaultPayload.getClass().getConstructor(String.class).newInstance(json);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PrintWriter send(final Socket socket) {
        try {
            final PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(serializeToJson());
            out.flush();
            return out;
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
