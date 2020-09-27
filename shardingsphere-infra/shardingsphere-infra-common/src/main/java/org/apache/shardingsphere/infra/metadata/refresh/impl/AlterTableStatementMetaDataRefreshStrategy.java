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

package org.apache.shardingsphere.infra.metadata.refresh.impl;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.refresh.TableMetaDataLoaderCallback;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.AlterTableStatementContext;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * Alter table statement meta data refresh strategy.
 */
public final class AlterTableStatementMetaDataRefreshStrategy implements MetaDataRefreshStrategy<AlterTableStatementContext> {
    
    @Override
    public void refreshMetaData(final ShardingSphereMetaData metaData, final DatabaseType databaseType,
                                final Map<String, DataSource> dataSourceMap, final AlterTableStatementContext sqlStatementContext, final TableMetaDataLoaderCallback callback) throws SQLException {
        String tableName = sqlStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        SchemaMetaData schemaMetaData = metaData.getRuleSchemaMetaData().getConfiguredSchemaMetaData();
        if (null != schemaMetaData && schemaMetaData.containsTable(tableName)) {
            callback.load(tableName).ifPresent(tableMetaData -> alterMetaData(metaData, tableName, tableMetaData));
        }
    }
    
    private void alterMetaData(final ShardingSphereMetaData metaData, final String tableName, final TableMetaData tableMetaData) {
        metaData.getRuleSchemaMetaData().getConfiguredSchemaMetaData().put(tableName, tableMetaData);
        metaData.getRuleSchemaMetaData().getSchemaMetaData().put(tableName, tableMetaData);
    }
}
