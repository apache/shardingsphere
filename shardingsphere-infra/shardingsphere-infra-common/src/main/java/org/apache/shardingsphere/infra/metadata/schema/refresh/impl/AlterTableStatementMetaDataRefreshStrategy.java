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

package org.apache.shardingsphere.infra.metadata.schema.refresh.impl;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalTableMetaData;
import org.apache.shardingsphere.infra.metadata.schema.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.schema.refresh.TableMetaDataLoaderCallback;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Alter table statement meta data refresh strategy.
 */
public final class AlterTableStatementMetaDataRefreshStrategy implements MetaDataRefreshStrategy<AlterTableStatement> {
    
    @Override
    public void refreshMetaData(final ShardingSphereSchema schema, final DatabaseType databaseType, final Collection<String> routeDataSourceNames,
                                final AlterTableStatement sqlStatement, final TableMetaDataLoaderCallback callback) throws SQLException {
        String tableName = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        PhysicalSchemaMetaData schemaMetaData = schema.getSchemaMetaData();
        if (null != schemaMetaData && schemaMetaData.containsTable(tableName)) {
            callback.load(tableName).ifPresent(tableMetaData -> alterMetaData(schema, tableName, tableMetaData));
        }
    }
    
    private void alterMetaData(final ShardingSphereSchema schema, final String tableName, final PhysicalTableMetaData tableMetaData) {
        schema.getSchemaMetaData().put(tableName, tableMetaData);
    }
}
