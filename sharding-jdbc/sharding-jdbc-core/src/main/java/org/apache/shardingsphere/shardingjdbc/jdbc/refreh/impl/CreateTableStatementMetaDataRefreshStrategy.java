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

package org.apache.shardingsphere.shardingjdbc.jdbc.refreh.impl;

import org.apache.shardingsphere.core.metadata.ShardingMetaDataLoader;
import org.apache.shardingsphere.core.metadata.ShardingTableMetaDataDecorator;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.encrypt.metadata.EncryptTableMetaDataDecorator;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.refreh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;

import java.sql.SQLException;

/**
 * Create table statement meta data refresh strategy.
 */
public final class CreateTableStatementMetaDataRefreshStrategy implements MetaDataRefreshStrategy<CreateTableStatementContext> {
    
    @Override
    public void refreshMetaData(final ShardingRuntimeContext shardingRuntimeContext, final CreateTableStatementContext sqlStatementContext) throws SQLException {
        String tableName = sqlStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        shardingRuntimeContext.getMetaData().getSchema().put(tableName, loadTableMetaData(tableName, shardingRuntimeContext));
    }
    
    private TableMetaData loadTableMetaData(final String tableName, final ShardingRuntimeContext shardingRuntimeContext) throws SQLException {
        ShardingRule shardingRule = shardingRuntimeContext.getRule();
        int maxConnectionsSizePerQuery = shardingRuntimeContext.getProperties().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        boolean isCheckingMetaData = shardingRuntimeContext.getProperties().<Boolean>getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED);
        TableMetaData result = new ShardingMetaDataLoader(shardingRuntimeContext.getDataSourceMap(), shardingRule, maxConnectionsSizePerQuery, isCheckingMetaData)
                .load(tableName, shardingRuntimeContext.getDatabaseType());
        result = new ShardingTableMetaDataDecorator().decorate(result, tableName, shardingRule);
        if (!shardingRule.getEncryptRule().getEncryptTableNames().isEmpty()) {
            result = new EncryptTableMetaDataDecorator().decorate(result, tableName, shardingRule.getEncryptRule());
        }
        return result;
    }
}
