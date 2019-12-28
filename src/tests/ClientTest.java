import network.Client;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import registry.Registry;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    Client client, client2;

    final String clientID = "123";
    final String clientID2 = "456";
    final String clientIP = "127.0.0.1";
    final String clientHostname = "hanspeterspc";
    InetAddress clientIPAddress;

    @BeforeEach
    public void setUp() {
        try {
            this.clientIPAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.client = new Client(this.clientID, this.clientIP, this.clientHostname);
        this.client2 = new Client(this.clientID2, this.clientIPAddress, this.clientHostname);
    }

    @Test
    void getId() {
        assertEquals(this.clientID, client.getId());
        assertEquals(this.clientID2, client2.getId());
    }

    @Test
    void getHostname() {
        assertEquals(this.clientHostname, this.client.getHostname());
        assertEquals(this.clientHostname, this.client2.getHostname());
    }

    @Test
    void getListName() {
        assertEquals(this.clientHostname, this.client.getListName());
        assertEquals(this.clientHostname, this.client2.getListName());
    }

    @Test
    void getIp() {
        assertEquals(this.clientIP, this.client.getIp());
    }

    @Test
    void getIpAddress() {
        assertEquals(this.clientIPAddress, this.client2.getIpAddress());
    }

    @Test
    void getReleases() {
    }

    @Test
    void setId() {
    }

    @Test
    void setIp() {
    }

    @Test
    void setHostname() {
    }

    @Test
    void setListName() {
    }

    @Test
    void setIpAddress() {
    }

    @Test
    void addRelease() {
    }

    @Test
    void removeRelease() {
    }

    @AfterEach
    void tearDown() {
        Registry reg = new Registry();
        reg.removeClient(this.client);
        reg.removeClient(this.client2);
    }
}