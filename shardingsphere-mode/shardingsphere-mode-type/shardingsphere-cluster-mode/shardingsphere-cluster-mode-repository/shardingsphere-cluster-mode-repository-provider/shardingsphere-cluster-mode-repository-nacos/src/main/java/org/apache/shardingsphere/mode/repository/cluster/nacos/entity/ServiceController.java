package org.apache.shardingsphere.mode.repository.cluster.nacos.entity;

import lombok.Getter;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service controller.
 */
public final class ServiceController {
    
    private static final String PERSISTENT_SERVICE_NAME = "PERSISTENT";
    
    private static final String EPHEMERAL_SERVICE_NAME = "EPHEMERAL";
    
    @Getter
    private final ServiceMetadata persistentService = new ServiceMetadata(PERSISTENT_SERVICE_NAME, false);
    
    @Getter
    private final ServiceMetadata ephemeralService = new ServiceMetadata(EPHEMERAL_SERVICE_NAME, true);
    
    private final Map<Boolean, ServiceMetadata> serviceMap = Stream.of(persistentService, ephemeralService).collect(Collectors.toMap(ServiceMetadata::isEphemeral, Function.identity()));
    
    /**
     * Get all services.
     * 
     * @return all services
     */
    public Collection<ServiceMetadata> getAllServices() {
        return serviceMap.values();
    }
    
    /**
     * Get service.
     * 
     * @param ephemeral is ephemeral service
     * @return ephemeral service or persistent service
     */
    public ServiceMetadata getService(final boolean ephemeral) {
        return serviceMap.get(ephemeral);
    }
}
