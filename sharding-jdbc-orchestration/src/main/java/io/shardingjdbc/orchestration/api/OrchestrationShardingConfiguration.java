package io.shardingjdbc.orchestration.api;

import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Orchestration sharding configuration.
 *
 * @author caohao
 */
@RequiredArgsConstructor
@Getter
public class OrchestrationShardingConfiguration {
    
    private final String name;
    
    private final boolean overwrite;
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRuleConfiguration shardingRuleConfig;
}
