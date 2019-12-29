package registry;

import info.Info;
import network.Client;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Registry {

    Preferences settings; //Preferences
    final String path = "/ulan/preferences"; // settings.Settings path

    // Default settings
    final private String DEFAULTTRANSFERPATH = System.getProperty("user.home") + "\\downloads"; // NOT USED
    final private String DEFAULTICON = "/icons/baseline_account_tree_white_18dp.png";
    final private String ID = "";
    final private int DEFAULTMESSAGE = 1; // NOT USED
    final private int PORT = 33123;
    final private int FILEPORT = 33111; // NOT USED
    final private int CLIENTSCOUNT = 253;
    final private int AUTOSTART = 1; // NOT USED
    final private int NOTIFICATIONS = 1; // NOT USED

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
        if(!this.checkIfPropertiesExist("defaulticon")){
            this.setSetting("properties", "defaulticon", this.DEFAULTICON);
        }
        if(!this.checkIfPropertiesExist("defaultmessage")){
            this.setSetting("properties", "defaultmessage", "" + this.DEFAULTMESSAGE);
        }
        if(!this.checkIfPropertiesExist("id")){
            this.setSetting("properties", "id", this.getId());
        }
        if(!this.checkIfPropertiesExist("port")){
            this.setSetting("properties", "port", "" + this.PORT);
        }
        if(!this.checkIfPropertiesExist("fileport")){
            this.setSetting("properties", "fileport", "" + this.FILEPORT);
        }
        if(!this.checkIfPropertiesExist("clientscount")){
            this.setSetting("properties", "clientscount", "" + this.CLIENTSCOUNT);
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

    private String getId() {

        String tString = "";
        String host = "";
        String ip = "";

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        tString = sdf.format(date);

        host = Info.getHostname();
        ip = Info.getIp();

        if(host == null) {
            host = "NA";
        }

        if(ip == null) {
            ip = "NA";
        }

        return this.encryptString(String.format("%s;%s;%s", tString, host, ip));
    }

    public String encryptString(String input)
    {
        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
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

    public void removeClient(Client client){
        String name = client.getId();
        this.settings = Preferences.userRoot().node(path+"/clients/"+name);
        try {
            this.settings.removeNode();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

    }

    public void updateClientSettings(Client client){
        String jobName=String.format("%s/%s", this.clients, client.getId()).toLowerCase();
        client.setHostname(this.getSetting(jobName, "hostname"));
        client.setListName(this.getSetting(jobName, "listname"));
        client.setIp(this.getSetting(jobName, "ip"));
    }
}
