package org.apache.shardingsphere.shardingjdbc.jdbc.refreh;

import org.apache.shardingsphere.core.metadata.ShardingMetaDataLoader;
import org.apache.shardingsphere.core.metadata.ShardingTableMetaDataDecorator;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.encrypt.metadata.EncryptTableMetaDataDecorator;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;

import java.sql.SQLException;

/**
 * The type Abstract table statement meta data.
 */
public abstract class AbstractTableStatementMetaData {
    
    /**
     * Load TableMetaData.
     *
     * @param tableName              the table name
     * @param shardingRuntimeContext the sharding runtime context
     * @return the table meta data
     * @throws SQLException the sql exception
     */
    protected TableMetaData loadTableMeta(final String tableName, final ShardingRuntimeContext shardingRuntimeContext) throws SQLException {
        ShardingRule shardingRule = shardingRuntimeContext.getRule();
        int maxConnectionsSizePerQuery = shardingRuntimeContext.getProperties().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        boolean isCheckingMetaData = shardingRuntimeContext.getProperties().<Boolean>getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED);
        TableMetaData result = new ShardingMetaDataLoader(shardingRuntimeContext.getDataSourceMap(), shardingRule, maxConnectionsSizePerQuery, isCheckingMetaData)
                .load(tableName, shardingRuntimeContext.getDatabaseType());
        result = new ShardingTableMetaDataDecorator().decorate(result, tableName, shardingRule);
        if (!shardingRule.getEncryptRule().getEncryptTableNames().isEmpty()) {
            result = new EncryptTableMetaDataDecorator().decorate(result, tableName, shardingRule.getEncryptRule());
        }
        return result;
    }
}
