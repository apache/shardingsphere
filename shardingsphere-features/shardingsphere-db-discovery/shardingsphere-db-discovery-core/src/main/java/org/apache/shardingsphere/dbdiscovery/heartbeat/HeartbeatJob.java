package org.apache.shardingsphere.dbdiscovery.heartbeat;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * HA heartbeat job.
 */
@RequiredArgsConstructor
public final class HeartbeatJob implements SimpleJob {
    
    private final String schemaName;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final String groupName;
    
    private final DatabaseDiscoveryType databaseDiscoveryType;
    
    private final Collection<String> disabledDataSourceNames;
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        databaseDiscoveryType.updatePrimaryDataSource(schemaName, dataSourceMap, disabledDataSourceNames, groupName);
        databaseDiscoveryType.updateMemberState(schemaName, dataSourceMap, disabledDataSourceNames);
    }
}
