package entities.payload;

import com.fasterxml.jackson.core.JsonProcessingException;

public class DownloadData extends DefaultPayload {

    private final String mode = "DOWNLOAD";

    private String port = null;
    private String folderName = null;
    private String size = null;

    public DownloadData() {
        super();
    }

    public DownloadData(final String json) throws JsonProcessingException {
        final DownloadData downloadData = objectMapper.readValue(json, getClass());
        super.setId(downloadData.getId());
        super.setIp(downloadData.getIp());
        super.setHostName(downloadData.getHostName());
        port = downloadData.getPort();
        folderName = downloadData.getFolderName();
        size = downloadData.getSize();
    }

    public String getPort() {
        return port;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getSize() {
        return size;
    }

    @Override
    public String getMode() {
        return mode;
    }

    @Override
    public String serializeToJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }

    /**
     * Please be sure that the following parameter come in the following sequence
     * 0 = PORT
     * 1 = FOLDER NAME
     * 2 = SIZE
     *
     * @param params
     */
    @Override
    public void setParams(final String... params) {
        if (params.length == 3) {
            port = params[0];
            folderName = params[1];
            size = params[2];
        }
    }

    @Override
    public String toString() {
        return "DownloadData{" +
                "mode='" + mode + '\'' +
                ", port='" + port + '\'' +
                ", folderName='" + folderName + '\'' +
                ", size='" + size + '\'' +
                ", registry=" + registry +
                '}';
    }

}
