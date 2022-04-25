package org.apache.shardingsphere.dbdiscovery.algorithm;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.dbdiscovery.spi.status.HighlyAvailableStatus;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorCallback;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class DatabaseDiscoveryExecutorCallback implements ExecutorCallback<DataSource, HighlyAvailableStatus> {

    private final DatabaseDiscoveryType databaseDiscoveryType;

    @Override
    public Collection<HighlyAvailableStatus> execute(Collection<DataSource> inputs, boolean isTrunkThread, Map<String, Object> dataMap) throws SQLException {
        Collection<HighlyAvailableStatus> result = new ArrayList<>(inputs.size());
        inputs.forEach(dataSource -> {
            try {
                result.add(databaseDiscoveryType.loadHighlyAvailableStatus(dataSource));
            } catch (SQLException e) {
                throw new IllegalStateException(String.format("Error while loading highly available Status with %s", dataSource), e);
            }
        });
        return result;
    }
}
