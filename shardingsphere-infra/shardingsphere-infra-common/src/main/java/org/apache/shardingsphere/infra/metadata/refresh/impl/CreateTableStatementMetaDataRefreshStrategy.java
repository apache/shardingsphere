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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.metadata.model.schema.model.table.TableMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.model.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.refresh.TableMetaDataLoaderCallback;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Create table statement meta data refresh strategy.
 */
public final class CreateTableStatementMetaDataRefreshStrategy implements MetaDataRefreshStrategy<CreateTableStatement> {
    
    @Override
    public void refreshMetaData(final ShardingSphereMetaData metaData, final DatabaseType databaseType,
                                final Map<String, DataSource> dataSourceMap, final CreateTableStatement sqlStatement, final TableMetaDataLoaderCallback callback) throws SQLException {
        String tableName = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        Optional<TableMetaData> tableMetaData = callback.load(tableName);
        if (tableMetaData.isPresent()) {
            metaData.getRuleSchemaMetaData().getConfiguredSchemaMetaData().put(tableName, tableMetaData.get());
            metaData.getRuleSchemaMetaData().getSchemaMetaData().put(tableName, tableMetaData.get());
        } else {
            refreshUnconfiguredMetaData(metaData, dataSourceMap, tableName);
            metaData.getRuleSchemaMetaData().getSchemaMetaData().put(tableName, new TableMetaData());
        }
    }
    
    private void refreshUnconfiguredMetaData(final ShardingSphereMetaData metaData, final Map<String, DataSource> dataSourceMap, final String tableName) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            refreshUnconfiguredMetaData(metaData, tableName, entry.getKey());
        }
    }
    
    private void refreshUnconfiguredMetaData(final ShardingSphereMetaData metaData, final String tableName, final String dataSourceName) {
        Collection<String> schemaMetaData = metaData.getRuleSchemaMetaData().getUnconfiguredSchemaMetaDataMap().get(dataSourceName);
        if (null == schemaMetaData) {
            metaData.getRuleSchemaMetaData().getUnconfiguredSchemaMetaDataMap().put(dataSourceName, Lists.newArrayList(tableName));
        } else {
            schemaMetaData.add(tableName);
        }
    }
}
