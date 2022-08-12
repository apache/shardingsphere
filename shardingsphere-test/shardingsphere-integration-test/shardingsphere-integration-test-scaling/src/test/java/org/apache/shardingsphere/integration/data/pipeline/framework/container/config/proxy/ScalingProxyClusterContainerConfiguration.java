package org.apache.shardingsphere.integration.data.pipeline.framework.container.config.proxy;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Scaling proxy cluster container configuration.
 */
@RequiredArgsConstructor
public class ScalingProxyClusterContainerConfiguration implements AdaptorContainerConfiguration {
    
    private final DatabaseType databaseType;
    
    private final String dockerImageName;
    
    @Override
    public Map<String, String> getWaitStrategyInfo() {
        return Collections.singletonMap("dataSourceName", DatabaseTypeUtil.isPostgreSQL(databaseType) ? "postgres" : "");
    }
    
    @Override
    public Map<String, String> getResourceMappings(String scenario, DatabaseType databaseType) {
        Map<String, String> result = new HashMap<>(2);
        result.put("/env/logback.xml", "/opt/shardingsphere-proxy/conf/logback.xml");
        if (DatabaseTypeUtil.isMySQL(databaseType)) {
            String majorVersion = DatabaseTypeUtil.parseMajorVersion(dockerImageName);
            result.put(String.format("/env/%s/server-%s.yaml", databaseType.getType().toLowerCase(), majorVersion), "/opt/shardingsphere-proxy/conf/server.yaml");
        } else {
            result.put(String.format("/env/%s/server.yaml", databaseType.getType().toLowerCase()), "/opt/shardingsphere-proxy/conf/server.yaml");
        }
        return result;
    }
}
