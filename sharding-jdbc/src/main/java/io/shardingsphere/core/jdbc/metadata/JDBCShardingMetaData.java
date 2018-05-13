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

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.metadata.ColumnMetaData;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.rule.ShardingDataSourceNames;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
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
    public Collection<ColumnMetaData> getColumnMetaDataList(final DataNode dataNode, final ShardingDataSourceNames shardingDataSourceNames,
                                                            final Map<String, Connection> connectionMap) throws SQLException {
        String dataSourceName = shardingDataSourceNames.getRawMasterDataSourceName(dataNode.getDataSourceName());
        if (connectionMap.containsKey(dataSourceName)) {
            return ShardingMetaDataHandlerFactory.newInstance(dataNode.getTableName(), databaseType).getColumnMetaDataList(connectionMap.get(dataSourceName));
        } else {
            return ShardingMetaDataHandlerFactory.newInstance(dataSourceMap.get(dataSourceName), dataNode.getTableName(), databaseType).getColumnMetaDataList();
        }
    }
}
