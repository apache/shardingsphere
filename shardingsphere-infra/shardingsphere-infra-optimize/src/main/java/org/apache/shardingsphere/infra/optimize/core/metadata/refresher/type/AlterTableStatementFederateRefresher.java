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

package org.apache.shardingsphere.infra.optimize.core.metadata.refresher.type;

import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.TableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.TableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.core.metadata.FederateSchemaMetadata;
import org.apache.shardingsphere.infra.optimize.core.metadata.refresher.FederateRefresher;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;

import java.sql.SQLException;
import java.util.Collection;

/**
 * ShardingSphere federate refresher for alter table statement.
 */
public final class AlterTableStatementFederateRefresher implements FederateRefresher<AlterTableStatement> {

    @Override
    public void refresh(final FederateSchemaMetadata schema, final Collection<String> logicDataSourceNames,
            final AlterTableStatement sqlStatement, final SchemaBuilderMaterials materials) throws SQLException {
        String tableName = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        if (sqlStatement.getRenameTable().isPresent()) {
            String renameTableName = sqlStatement.getRenameTable().get().getTableName().getIdentifier().getValue();
            TableMetaData tableMetaData = buildTableMetaData(logicDataSourceNames, materials, renameTableName);
            schema.renew(renameTableName, tableMetaData);
            schema.remove(tableName);
        } else {
            TableMetaData tableMetaData = buildTableMetaData(logicDataSourceNames, materials, tableName);
            schema.renew(tableName, tableMetaData);
        }
    }
    
    private TableMetaData buildTableMetaData(final Collection<String> logicDataSourceNames,
            final SchemaBuilderMaterials materials, final String tableName) throws SQLException {
        if (!containsInTableContainedRule(tableName, materials)) {
            return TableMetaDataLoader.load(tableName, logicDataSourceNames, materials).orElseGet(TableMetaData::new);
        } else {
            return TableMetaDataBuilder.load(tableName, materials).orElseGet(TableMetaData::new);
        }
    }

    private boolean containsInTableContainedRule(final String tableName, final SchemaBuilderMaterials materials) {
        return findShardingSphereRulesByClass(materials.getRules(), TableContainedRule.class).stream().anyMatch(each -> each.getTables().contains(tableName));
    }
}
