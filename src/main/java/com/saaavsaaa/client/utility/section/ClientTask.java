package com.saaavsaaa.client.utility.section;

import com.saaavsaaa.client.zookeeper.Client;
import com.saaavsaaa.client.zookeeper.Provider;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public abstract class ClientTask implements Runnable {
    private final Provider provider;
    public ClientTask(final Provider provider){
        this.provider = provider;
    }

    public abstract void run(final Provider provider) throws KeeperException, InterruptedException;
    
    @Override
    public void run() {
        try {
            run(provider);
        } catch (KeeperException e) {
            System.out.println("ClientTask");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("ClientTask");
            e.printStackTrace();
        }
    }
}
