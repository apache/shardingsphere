package io.shardingjdbc.orchestration.api;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Orchestration master slave configuration.
 *
 * @author caohao
 */
@RequiredArgsConstructor
@Getter
public class OrchestrationMasterSlaveConfiguration {
    
    private final String name;
    
    private final boolean overwrite;
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final MasterSlaveRuleConfiguration masterSlaveRuleConfiguration;
}
