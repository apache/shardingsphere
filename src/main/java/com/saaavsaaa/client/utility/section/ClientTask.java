package com.saaavsaaa.client.utility.section;

import com.saaavsaaa.client.action.IProvider;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aaa
 */
public abstract class ClientTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientTask.class);
    private final IProvider provider;
    
    public ClientTask(final IProvider provider){
        this.provider = provider;
    }

    public abstract void run(final IProvider provider) throws KeeperException, InterruptedException;
    
    @Override
    public void run() {
        try {
            run(provider);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
