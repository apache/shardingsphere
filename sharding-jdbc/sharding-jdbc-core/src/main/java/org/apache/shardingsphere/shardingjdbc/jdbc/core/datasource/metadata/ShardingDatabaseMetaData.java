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

import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.underlying.common.rule.DataNode;

/**
 * Sharding database meta data.
 */
public final class ShardingDatabaseMetaData extends MultipleDatabaseMetaData<ShardingRule, ShardingConnection> {
    
    public ShardingDatabaseMetaData(final ShardingConnection connection) {
        super(connection, connection.getRuntimeContext().getRule(), connection.getDataSourceMap().keySet(), connection.getRuntimeContext().getCachedDatabaseMetaData());
    }
    
    @Override
    public String getActualTableNamePattern(final String tableNamePattern) {
        if (null == tableNamePattern) {
            return null;
        }
        return getRule().findTableRule(tableNamePattern).isPresent() ? "%" + tableNamePattern + "%" : tableNamePattern;
    }
    
    @Override
    public String getActualTable(final String table) {
        if (null == table) {
            return null;
        }
        String result = table;
        if (getRule().findTableRule(table).isPresent()) {
            DataNode dataNode = getRule().getDataNode(table);
            result = dataNode.getTableName();
        }
        return result;
    }
}
