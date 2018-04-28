package com.saaavsaaa.client.utility.section;

import com.saaavsaaa.client.zookeeper.Client;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public abstract class ClientTask implements Runnable {
    private final Client client;
    public ClientTask(final Client client){
        this.client = client;
    }

    public abstract void run(final Client client) throws KeeperException, InterruptedException;
    
    @Override
    public void run() {
        try {
            run(client);
        } catch (KeeperException e) {
            System.out.println("ClientTask");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("ClientTask");
            e.printStackTrace();
        }
    }
}
