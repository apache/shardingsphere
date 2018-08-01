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
import io.shardingsphere.core.metadata.table.AbstractRefreshHandler;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.rule.TableRule;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Sharding table meta data refreshing handler for JDBC.
 *
 * @author zhaojun
 */
public final class JDBCShardingRefreshHandler extends AbstractRefreshHandler {
    
    private final ShardingConnection shardingConnection;
    
    public JDBCShardingRefreshHandler(final SQLStatement sqlStatement, final ShardingConnection shardingConnection) {
        super(sqlStatement, shardingConnection.getShardingContext().getMetaData().getTable(), shardingConnection.getShardingContext().getShardingRule());
        this.shardingConnection = shardingConnection;
    }
    
    @Override
    public void execute() throws SQLException {
        if (isNeedRefresh()) {
            String logicTable = getSqlStatement().getTables().getSingleTableName();
            Map<String, Connection> connectionMap = getConnectionMap(getShardingRule().getTableRule(logicTable));
            getShardingTableMetaData().refresh(getShardingRule().getTableRule(logicTable), getShardingRule(), connectionMap);
        }
    }

    private Map<String, Connection> getConnectionMap(final TableRule tableRule) throws SQLException {
        Map<String, Connection> result = new HashMap<>();
        for (DataNode each : tableRule.getActualDataNodes()) {
            String dataSourceName = getShardingRule().getShardingDataSourceNames().getRawMasterDataSourceName(each.getDataSourceName());
            result.put(dataSourceName, shardingConnection.getConnection(dataSourceName));
        }
        return result;
    }
}
