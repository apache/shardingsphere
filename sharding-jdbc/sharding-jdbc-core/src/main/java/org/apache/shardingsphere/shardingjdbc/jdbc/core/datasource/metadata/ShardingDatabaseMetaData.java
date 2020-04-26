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

import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.DatabaseMetaDataResultSet;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.common.rule.TablesAggregationRule;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Sharding database meta data.
 */
public final class ShardingDatabaseMetaData extends MultipleDatabaseMetaData<ShardingConnection> {
    
    private final Collection<BaseRule> rules;
    
    public ShardingDatabaseMetaData(final ShardingConnection connection) {
        super(connection, connection.getDataSourceMap().keySet(), connection.getRuntimeContext().getCachedDatabaseMetaData(), connection.getRuntimeContext().getMetaData());
        rules = connection.getRuntimeContext().getRules();
    }
    
    @Override
    public String getActualTableNamePattern(final String tableNamePattern) {
        if (null == tableNamePattern) {
            return null;
        }
        Optional<TablesAggregationRule> tablesAggregationRule = findTablesAggregationRule();
        if (tablesAggregationRule.isPresent()) {
            return tablesAggregationRule.get().findFirstActualTable(tableNamePattern).isPresent() ? "%" + tableNamePattern + "%" : tableNamePattern;
        }
        return tableNamePattern;
    }
    
    @Override
    public String getActualTable(final String table) {
        if (null == table) {
            return null;
        }
        Optional<TablesAggregationRule> tablesAggregationRule = findTablesAggregationRule();
        return tablesAggregationRule.isPresent() ? tablesAggregationRule.get().findFirstActualTable(table).orElse(table) : table;
    }
    
    private Optional<TablesAggregationRule> findTablesAggregationRule() {
        return rules.stream().filter(each -> each instanceof TablesAggregationRule).findFirst().map(rule -> (TablesAggregationRule) rule);
    }
    
    @Override
    protected ResultSet createDatabaseMetaDataResultSet(final ResultSet resultSet) throws SQLException {
        return new DatabaseMetaDataResultSet(resultSet, rules);
    }
}
