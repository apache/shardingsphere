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

package org.apache.shardingsphere.infra.metadata.schema.refresher.type;

import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.TableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;

import java.sql.SQLException;
import java.util.Collection;

/**
 * ShardingSphere schema refresher for alter table statement.
 */
public final class AlterTableStatementSchemaRefresher implements SchemaRefresher<AlterTableStatement> {
    
    @Override
    public void refresh(final ShardingSphereSchema schema, 
                        final Collection<String> routeDataSourceNames, final AlterTableStatement sqlStatement, final SchemaBuilderMaterials materials) throws SQLException {
        String tableName = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        if (null != schema && schema.containsTable(tableName)) {
            TableMetaDataBuilder.build(tableName, materials).ifPresent(tableMetaData -> schema.put(tableName, tableMetaData));
        }
    }
}
