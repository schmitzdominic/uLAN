package registry;

import network.Client;

import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Registry {

    Preferences settings; //Preferences
    final String path = "/ulan/preferences"; // settings.Settings path

    // Default settings
    final private String DEFAULTTRANSFERPATH = System.getProperty("user.home") + "\\downloads";
    final private int DEFAULTMESSAGE = 1;
    final private int PORT = 33123;
    final private int FILEPORT = 33111;
    final private int SEARCHCLIENTSTIME = 3;
    final private int AUTOSTART = 1;
    final private int NOTIFICATIONS = 1;

    // Strings
    final private String clients = "clients";

    /**
     * Default Contructor
     */
    public Registry(){
        this.settings = Preferences.userRoot().node(this.path);
    }

    /**
     * Get the Properties
     * @return list of settings HashMap<String, String>
     */
    public HashMap<String,String> getProperties(){

        if(!this.checkIfPropertiesExist("defaultfiletransferpath")){
            this.setSetting("properties", "defaultfiletransferpath", this.DEFAULTTRANSFERPATH);
        }
        if(!this.checkIfPropertiesExist("defaultmessage")){
            this.setSetting("properties", "defaultmessage", "" + this.DEFAULTMESSAGE);
        }
        if(!this.checkIfPropertiesExist("port")){
            this.setSetting("properties", "port", "" + this.PORT);
        }
        if(!this.checkIfPropertiesExist("fileport")){
            this.setSetting("properties", "fileport", "" + this.FILEPORT);
        }
        if(!this.checkIfPropertiesExist("searchclientstime")){
            this.setSetting("properties", "searchclientstime", "" + this.SEARCHCLIENTSTIME);
        }
        if(!this.checkIfPropertiesExist("autostart")){
            this.setSetting("properties", "autostart", "" + this.AUTOSTART);
        }
        if(!this.checkIfPropertiesExist("notifications")){
            this.setSetting("properties", "notifications", "" + this.NOTIFICATIONS);
        }

        try {
            this.settings = Preferences.userRoot().node(path+"/properties");

            HashMap<String,String> x = new HashMap<>();

            for (String key : this.settings.keys()){
                x.put(key, settings.get(key, ""));
            }
            return x;

        } catch (BackingStoreException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if the Software starts the first time
     * @return true - not the first time / false - yes the first time
     */
    public boolean checkIfPropertiesExist(String setting){
        try {
            if(setting.equals("root")){
                return this.settings.nodeExists(this.path+"/properties");
            }
            else{
                this.settings = Preferences.userRoot().node(this.path+"/properties");
                for(String x : this.settings.keys()){
                    if(x.equals(setting)){
                        return true;
                    }
                }
                this.settings = Preferences.userRoot().node(this.path);
                return false;
            }

        } catch (BackingStoreException e) {
            return false;
        }
    }

    /**
     * Get one Setting
     * @param jobName String
     * @param key String
     * @return one Setting as String
     */
    public String getSetting(String jobName, String key){
        try {
            this.createJob(jobName.toLowerCase());
            key = key.toLowerCase();

            for (String value : settings.keys()){
                if(value.equals(key))
                    return settings.get(value, "");
            }
            return null;
        } catch (BackingStoreException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Set one setting
     * @param jobName String
     * @param set String
     * @param content String NOTE: If it is a path please use / instead of \
     */
    public void setSetting(String jobName, String set, String content){
        jobName = jobName.toLowerCase();
        set = set.toLowerCase();
        content = content.toLowerCase();
        this.createJob(jobName);
        this.settings.put(set, content);
    }

    /**
     * Create a Job and set this.settings to the job
     * @param name String
     */
    public void createJob(String name){
        name = name.toLowerCase();
        this.settings = Preferences.userRoot().node(path+"/"+name);
    }

    public boolean checkIfClientExists(Client client){
        try {
            String clientName = path+"/"+this.clients+"/"+client.getId();
            return Preferences.userRoot().nodeExists(clientName.toLowerCase());
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addClient(Client client){
        String jobName=String.format("%s/%s", this.clients, client.getId()).toLowerCase();
        if(client.getId() != null){
            this.setSetting(jobName, "id", client.getId());
        }

        if(client.getHostname() != null){
            this.setSetting(jobName, "hostname", client.getHostname());
        }

        if(client.getListName() != null){
            this.setSetting(jobName, "listname", client.getListName());
        }

        if(client.getIp() != null){
            this.setSetting(jobName, "ip", client.getIp());
        }
    }

    public void updateClientSettings(Client client){
        String jobName=String.format("%s/%s", this.clients, client.getId()).toLowerCase();
        client.setHostname(this.getSetting(jobName, "hostname"));
        client.setListName(this.getSetting(jobName, "listname"));
        client.setIp(this.getSetting(jobName, "ip"));
    }
}
