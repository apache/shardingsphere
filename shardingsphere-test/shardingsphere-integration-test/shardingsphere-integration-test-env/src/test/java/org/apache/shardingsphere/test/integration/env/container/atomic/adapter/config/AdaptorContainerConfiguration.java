package org.apache.shardingsphere.test.integration.env.container.atomic.adapter.config;

import org.apache.shardingsphere.infra.database.type.DatabaseType;

import java.util.Map;

/**
 * Adaptor container configuration.
 */
public interface AdaptorContainerConfiguration {

    /**
     * get wait strategy info
     * 
     * @return waitStrategy
     */
    Map<String, String> getWaitStrategyInfo();

    /**
     * get docker container mapping resources.
     * 
     * @param scenario scenario
     * @param databaseType database type
     * @return docker container resource mapping
     */
    Map<String, String> getResourceMappings(String scenario, DatabaseType databaseType);
}
