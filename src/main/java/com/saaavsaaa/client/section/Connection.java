package com.saaavsaaa.client.section;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.zookeeper.base.BaseClient;
import org.apache.zookeeper.KeeperException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by aaa
 */
public class Connection {
    
    //is need reset
    private static final Map<Integer, Boolean> exceptionResets = new ConcurrentHashMap<>();
    private final IClient client;
    
    static {
        exceptionResets.put(KeeperException.Code.SESSIONEXPIRED.intValue(), true);
        exceptionResets.put(KeeperException.Code.SESSIONMOVED.intValue(), true);
        exceptionResets.put(KeeperException.Code.CONNECTIONLOSS.intValue(), false);
        exceptionResets.put(KeeperException.Code.OPERATIONTIMEOUT.intValue(), false);
    }
    
    public Connection(final IClient client){
        this.client = client;
    }
    
    public void check(KeeperException e) throws KeeperException {
        int code = e.code().intValue();
        if (!exceptionResets.containsKey(code)){
            throw e;
        }
        boolean reset = exceptionResets.get(code);
        if (reset){
//            this.client = ((BaseClient)client).getContext()
        } else {
            // block
        }
    }
}
