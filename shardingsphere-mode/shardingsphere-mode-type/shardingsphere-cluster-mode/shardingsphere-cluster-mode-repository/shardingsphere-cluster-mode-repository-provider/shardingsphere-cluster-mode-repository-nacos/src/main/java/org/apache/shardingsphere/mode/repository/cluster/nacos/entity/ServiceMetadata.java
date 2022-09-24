package org.apache.shardingsphere.mode.repository.cluster.nacos.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.mode.repository.cluster.nacos.listener.NamingEventListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service metadata.
 */
@RequiredArgsConstructor
public final class ServiceMetadata {
    
    @Getter
    private final String serviceName;
    
    @Getter
    @Setter
    private String ip;
    
    @Setter
    private AtomicInteger port;
    
    @Getter
    @Setter
    private NamingEventListener listener;
    
    @Getter
    private final boolean ephemeral;
    
    /**
     * Get incremental port.
     * 
     * @return incremental port
     */
    public int getPort() {
        int result = port.incrementAndGet();
        if (result == Integer.MIN_VALUE) {
            throw new IllegalStateException("Specified cluster ip exceeded the maximum number of persisting");
        }
        return result;
    }
}
