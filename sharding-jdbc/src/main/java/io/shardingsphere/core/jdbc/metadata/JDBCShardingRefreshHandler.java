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

import io.shardingsphere.core.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.core.metadata.table.RefreshHandler;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.rule.TableRule;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Sharding table meta data refreshing handler for JDBC.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class JDBCShardingRefreshHandler implements RefreshHandler {
    
    private final String logicTableName;
    
    private final ShardingConnection shardingConnection;
    
    @Override
    public void execute() throws SQLException {
        Map<String, Connection> connectionMap = getConnectionMap(shardingConnection.getShardingContext().getShardingRule().getTableRule(logicTableName));
        shardingConnection.getShardingContext().getMetaData().getTable().refresh(
                shardingConnection.getShardingContext().getShardingRule().getTableRule(logicTableName), shardingConnection.getShardingContext().getShardingRule(), connectionMap);
    }

    private Map<String, Connection> getConnectionMap(final TableRule tableRule) throws SQLException {
        Map<String, Connection> result = new HashMap<>();
        for (DataNode each : tableRule.getActualDataNodes()) {
            String dataSourceName = shardingConnection.getShardingContext().getShardingRule().getShardingDataSourceNames().getRawMasterDataSourceName(each.getDataSourceName());
            result.put(dataSourceName, shardingConnection.getConnection(dataSourceName));
        }
        return result;
    }
}
