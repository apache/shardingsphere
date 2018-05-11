package com.saaavsaaa.client.utility.section;

import com.saaavsaaa.client.zookeeper.base.BaseProvider;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aaa
 */
public abstract class ClientTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientTask.class);
    private final BaseProvider provider;
    
    public ClientTask(final BaseProvider provider){
        this.provider = provider;
    }

    public abstract void run(final BaseProvider provider) throws KeeperException, InterruptedException;
    
    @Override
    public void run() {
        try {
            run(provider);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
