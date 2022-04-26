package org.apache.shardingsphere.dbdiscovery.algorithm;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorCallback;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class DatabaseDiscoveryExecutorCallback implements ExecutorCallback<DataSource, Void> {

    private final DatabaseDiscoveryProviderAlgorithm databaseDiscoveryProviderAlgorithm;

    public static final String DATABASE_NAME = "databaseName";

    @Override
    public Collection<Void> execute(Collection<DataSource> inputs, boolean isTrunkThread, Map<String, Object> dataMap) throws SQLException {
        String databaseName = (String) dataMap.get(DATABASE_NAME);
        inputs.forEach(dataSource -> {
            try {
                databaseDiscoveryProviderAlgorithm.checkEnvironment(databaseName, dataSource);
            } catch (SQLException e) {
                throw new IllegalStateException(String.format("Error while loading highly available Status with %s", dataSource), e);
            }
        });
        return null;
    }
}
