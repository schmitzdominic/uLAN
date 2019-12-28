package network;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import Interfaces.ClientFoundListener;

public class Finder {

    private ClientFoundListener clientListener;
    private int count;
    public AtomicInteger counter;
    public boolean active = false;

    public Finder() {
        this.counter = new AtomicInteger();
    }

    public void registerClientFoundListener(ClientFoundListener clientListener) {
        this.clientListener = clientListener;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void searchClients() {
        this.active = true;
        this.counter.set(0);
        for(int i = 0; i < count; i++) {
            final int TESTID = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO: Search for Client
                    // If Client found, call clientListener.onClientFound and add the client

                    /*
                     * FROM HERE
                     */
                    Random randomGenerator = new Random();
                    int randomInt = randomGenerator.nextInt(20) + 1;
                    int wait = randomInt * 1000;

                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    /*
                     * TILL HERE! All is experimental!
                     */

                    counter.addAndGet(1);
                    if (clientListener != null) {
                        if (count == counter.get()) {
                            active = false;
                        }
                        // TODO: if client found -> add
                        Client client = new Client(String.format("%s", TESTID), String.format("10.20.30.%s", TESTID), String.format("COOLER_PC_%s", TESTID));
                        clientListener.onClientFound(client);
                    }
                }
            }).start();
        }
    }
}
