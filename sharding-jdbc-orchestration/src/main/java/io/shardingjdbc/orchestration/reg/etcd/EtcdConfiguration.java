package io.shardingjdbc.orchestration.reg.etcd;

import lombok.Getter;
import lombok.Setter;

/**
 * Etcd configuration.
 *
 * @author junxiong
 */
@Getter
@Setter
public final class EtcdConfiguration {
    
    /**
     * Etcd server list.
     * 
     * <p>Include ip address and port, multiple servers split by comma. Etc: {@code http://host1:2379,http://host2:2379}</p>
     */
    private String serverLists;
    
    /**
     * Root namespace of etcd cluster.
     */
    private String namespace;
    
    /**
     * Time to live of ephemeral keys.
     */
    private long timeToLive;
    
    /**
     * Maximal retries when calling a etcd method.
     */
    private int maxRetries = 3;
    
    /**
     * Timeout when calling a etcd method in milliseconds.
     */
    private long timeout;
    
    /**
     * Username of etcd cluster.
     * 
     * <p>Default is not need digest</p>
     */
    private String username;
    
    /**
     * Password of etcd cluster.
     */
    private String password;
}
