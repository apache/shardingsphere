/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.metadata;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.metadata.dialect.ShardingMetaDataHandlerFactory;
import io.shardingjdbc.core.rule.DataNode;
import io.shardingjdbc.core.rule.ShardingDataSourceNames;
import io.shardingjdbc.core.rule.ShardingRule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Sharding metadata for JDBC.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class JDBCShardingMetaData extends ShardingMetaData {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRule shardingRule;
    
    private final DatabaseType databaseType;
    
    @Override
    public Collection<ColumnMetaData> getColumnMetaDataList(final DataNode dataNode, final ShardingDataSourceNames shardingDataSourceNames) throws SQLException {
        String dataSourceName = shardingDataSourceNames.getRawMasterDataSourceName(dataNode.getDataSourceName());

        if (getCachedConnectionMap().containsKey(dataSourceName)) {
            return ShardingMetaDataHandlerFactory.newInstance(dataNode.getTableName(), databaseType).getColumnMetaDataList(getCachedConnectionMap().get(dataSourceName));
        } else {
            return ShardingMetaDataHandlerFactory.newInstance(dataSourceMap.get(dataSourceName), dataNode.getTableName(), databaseType).getColumnMetaDataList();
        }
    }
}
