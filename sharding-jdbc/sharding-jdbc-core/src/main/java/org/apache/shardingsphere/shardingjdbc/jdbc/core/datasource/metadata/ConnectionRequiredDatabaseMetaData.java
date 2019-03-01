/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.WrapperAdapter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Connection required database meta data.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class ConnectionRequiredDatabaseMetaData extends WrapperAdapter implements DatabaseMetaData {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRule shardingRule;
    
    private Connection currentConnection;
    
    @Getter
    private String currentDataSourceName;
    
    @Override
    public final Connection getConnection() throws SQLException {
        return getCurrentConnection();
    }
    
    private Connection getCurrentConnection() throws SQLException {
        if (null == currentConnection || currentConnection.isClosed()) {
            DataSource dataSource = null == shardingRule ? dataSourceMap.values().iterator().next()
                : dataSourceMap.get(currentDataSourceName = shardingRule.getShardingDataSourceNames().getRandomDataSourceName());
            currentConnection = dataSource.getConnection();
        }
        return currentConnection;
    }
}
