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

import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.impl.ShardingRuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.refreh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.underlying.common.metadata.schema.RuleSchemaMetaDataLoader;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Alter table statement meta data refresh strategy.
 */
public final class AlterTableStatementMetaDataRefreshStrategy implements MetaDataRefreshStrategy<AlterTableStatementContext> {
    
    @Override
    public void refreshMetaData(final ShardingRuntimeContext shardingRuntimeContext, final AlterTableStatementContext sqlStatementContext) throws SQLException {
        String tableName = sqlStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        loadTableMetaData(tableName, shardingRuntimeContext).ifPresent(tableMetaData -> shardingRuntimeContext.getMetaData().getSchema().getConfiguredSchemaMetaData().put(tableName, tableMetaData));
    }
    
    private Optional<TableMetaData> loadTableMetaData(final String tableName, final ShardingRuntimeContext shardingRuntimeContext) throws SQLException {
        RuleSchemaMetaDataLoader loader = new RuleSchemaMetaDataLoader(shardingRuntimeContext.getRule().toRules());
        return loader.load(shardingRuntimeContext.getDatabaseType(), shardingRuntimeContext.getDataSourceMap(), tableName, shardingRuntimeContext.getProperties());
    }
}
