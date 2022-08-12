package org.apache.shardingsphere.test.integration.env.container.atomic.adapter.config.impl;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.config.AdaptorContainerConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Proxy cluster container configuration.
 */
public class DefaultProxyClusterContainerConfiguration implements AdaptorContainerConfiguration {
    
    @Override
    public Map<String, String> getWaitStrategyInfo() {
        return Collections.singletonMap("dataSourceName", "");
    }

    @Override
    public Map<String, String> getResourceMappings(String scenario, DatabaseType databaseType) {
        Map<String, String> result = new HashMap<>(2);
        String pathInContainer = "/opt/shardingsphere-proxy/conf";
        result.put("/env/common/standalone/proxy/conf/", pathInContainer);
        result.put("/env/scenario/" + scenario + "/proxy/conf/" + databaseType.getType().toLowerCase(), pathInContainer);
        return result;
    }
}
