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

package org.apache.shardingsphere.transaction.core;

import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.transaction.spi.ShardingTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sharding transaction manager adapter.
 *
 * @author zhaojun
 */
public abstract class ShardingTransactionManagerAdapter implements ShardingTransactionManager {
    
    private final String uniqueKey = UUID.randomUUID().toString() + "-";
    
    @Override
    public final void init(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        doInit(databaseType, convertUniqueKeyDataSourceMap(dataSourceMap));
    }
    
    /**
     * Do init.
     *
     * @param databaseType database type
     * @param dataSourceMap data source map
     */
    public abstract void doInit(DatabaseType databaseType, Map<String, DataSource> dataSourceMap);
    
    private Map<String, DataSource> convertUniqueKeyDataSourceMap(final Map<String, DataSource> dataSourceMap) {
        Map<String, DataSource> result = new HashMap<>(dataSourceMap.size(), 1);
        for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            result.put(uniqueKey + entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    @Override
    public final Connection getConnection(final String dataSourceName) {
        return doGetConnection(uniqueKey + dataSourceName);
    }
    
    /**
     * Do get connection.
     *
     * @param dataSourceName data source name
     * @return connection
     */
    public abstract Connection doGetConnection(String dataSourceName);
    
}
