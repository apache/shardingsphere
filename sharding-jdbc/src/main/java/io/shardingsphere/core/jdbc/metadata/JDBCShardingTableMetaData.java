/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.jdbc.metadata;

import com.google.common.util.concurrent.ListeningExecutorService;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.Getter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Sharding table meta data for JDBC.
 *
 * @author panjuan
 */
@Getter
public final class JDBCShardingTableMetaData extends ShardingTableMetaData {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRule shardingRule;
    
    private final DatabaseType databaseType;
    
    public JDBCShardingTableMetaData(final ListeningExecutorService executorService, final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule, final DatabaseType databaseType) {
        super(executorService);
        this.dataSourceMap = dataSourceMap;
        this.shardingRule = shardingRule;
        this.databaseType = databaseType;
    }
    
    @Override
    public Collection<String> getTableNamesFromDefaultDataSource(final String defaultDataSourceName) throws SQLException {
        return ShardingMetaDataHandlerFactory.newInstance(dataSourceMap.get(defaultDataSourceName), "", databaseType).getTableNamesFromDefaultDataSource();
    }
    
    @Override
    public TableMetaData loadTableMetaData(final DataNode dataNode, final Map<String, Connection> connectionMap) throws SQLException {
        if (connectionMap.containsKey(dataNode.getDataSourceName())) {
            return ShardingMetaDataHandlerFactory.newInstance(dataNode.getTableName(), databaseType).getTableMetaData(connectionMap.get(dataNode.getDataSourceName()));
        }
        return ShardingMetaDataHandlerFactory.newInstance(dataSourceMap.get(dataNode.getDataSourceName()), dataNode.getTableName(), databaseType).getTableMetaData();
    }
}
