package org.apache.shardingsphere.proxy.backend.collector;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereStatisticsCollector;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereTableDataCollectorUtils;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.sql.SQLException;
import java.util.*;

/**
 * @author sheldon
 * @date 2023-09-28
 */
public final class MySQLInformationSchemaParameterTableCollector implements ShardingSphereStatisticsCollector {

    private final static String PARAMETER_TABLE_NAME = "PARAMETERS";

    private final static String COLLECT_SQL = "select * from information_schema.PARAMETERS";

    @Override
    public Optional<ShardingSphereTableData> collect(String databaseName, ShardingSphereTable table, Map<String, ShardingSphereDatabase> shardingSphereDatabases) throws SQLException {
        Optional<String> databaseWithDatasource = ProxyContext.getInstance().getAllDatabaseNames().stream().filter(MySQLInformationSchemaParameterTableCollector::hasDataSource).findFirst();
        if (databaseWithDatasource.isPresent()) {
            Collection<ShardingSphereRowData> rows = ShardingSphereTableDataCollectorUtils.collectRowData(shardingSphereDatabases.get(databaseWithDatasource.get()),
                    table, table.getColumnNames(), COLLECT_SQL);
            ShardingSphereTableData result = new ShardingSphereTableData(PARAMETER_TABLE_NAME);
            result.getRows().addAll(rows);
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private static Boolean hasDataSource(final String databaseName) {
        return ProxyContext.getInstance().getDatabase(databaseName).containsDataSource();
    }

    @Override
    public Object getType() {
        return PARAMETER_TABLE_NAME;
    }

}
