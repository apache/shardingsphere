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
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.DatabaseMetaDataResultSet;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * {@code ResultSet} returned database meta data.
 *
 * @author yangyi
 */
public abstract class ResultSetReturnedDatabaseMetaData extends ConnectionRequiredDatabaseMetaData {
    
    private final ShardingRule shardingRule;
    
    public ResultSetReturnedDatabaseMetaData(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) {
        super(dataSourceMap, shardingRule);
        this.shardingRule = shardingRule;
    }
    
    @Override
    public final ResultSet getTables(final String catalog, final String schemaPattern, final String tableNamePattern, final String[] types) throws SQLException {
        String shardingTableNamePattern = getShardingTableNamePattern(tableNamePattern);
        ResultSet result = new DatabaseMetaDataResultSet(getConnection().getMetaData().getTables(catalog, schemaPattern, shardingTableNamePattern, types), shardingRule);
        getConnection().close();
        return result;
    }
    
    @Override
    public final ResultSet getColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        String shardingTableNamePattern = getShardingTableNamePattern(tableNamePattern);
        ResultSet result = new DatabaseMetaDataResultSet(getConnection().getMetaData().getColumns(catalog, schemaPattern, shardingTableNamePattern, columnNamePattern), shardingRule);
        getConnection().close();
        return result;
    }
    
    private String getShardingTableNamePattern(final String tableNamePattern) {
        return null == tableNamePattern ? tableNamePattern : (shardingRule.findTableRule(tableNamePattern).isPresent() ? "%" + tableNamePattern + "%" : tableNamePattern);
    }
}
