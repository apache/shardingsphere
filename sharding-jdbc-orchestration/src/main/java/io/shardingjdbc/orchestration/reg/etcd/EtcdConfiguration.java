package io.shardingjdbc.orchestration.reg.etcd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * etcd configuration
 *
 * @author junxiong
 */
@Value
@Builder
@AllArgsConstructor
public class EtcdConfiguration {
    /**
     * etcd server list.
     * <p>
     * <p>Include ip address and port, multiple servers split by comma. Etc: {@code http://host1:2379,http://host2:2379}</p>
     */
    private final String serverLists;

    /**
     * root namespace of etcd cluster.
     */
    private final String namespace;

    /**
     * maximal retries.
     */
    private int maxRetries = 3;

    /**
     * username of etcd cluster
     * <p>
     * <p>Default is not need digest</p>
     */
    private String username;

    /**
     * password of etcd cluster
     */
    private String password;
}
