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

package org.apache.shardingsphere.infra.context.refresher.type;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.TableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Schema refresher for alter table statement.
 */
public final class AlterTableStatementSchemaRefresher implements MetaDataRefresher<AlterTableStatement> {
    
    @Override
    public void refresh(final ShardingSphereMetaData schemaMetaData, final FederationSchemaMetaData schema, final Collection<String> logicDataSourceNames, final AlterTableStatement sqlStatement, 
                        final ConfigurationProperties props) throws SQLException {
        String tableName = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        if (sqlStatement.getRenameTable().isPresent()) {
            putTableMetaData(schemaMetaData, schema, logicDataSourceNames, sqlStatement.getRenameTable().get().getTableName().getIdentifier().getValue(), props);
            removeTableMetaData(schemaMetaData, schema, tableName);
        } else {
            putTableMetaData(schemaMetaData, schema, logicDataSourceNames, tableName, props);
        }
    }
    
    private void removeTableMetaData(final ShardingSphereMetaData schemaMetaData, final FederationSchemaMetaData schema, final String tableName) {
        schemaMetaData.getSchema().remove(tableName);
        schemaMetaData.getRuleMetaData().findRules(MutableDataNodeRule.class).forEach(each -> each.remove(tableName));
        schema.remove(tableName);
    }
    
    private void putTableMetaData(final ShardingSphereMetaData schemaMetaData, final FederationSchemaMetaData schema, final Collection<String> logicDataSourceNames, final String tableName, 
                                  final ConfigurationProperties props) throws SQLException {
        if (!containsInDataNodeContainedRule(tableName, schemaMetaData)) {
            schemaMetaData.getRuleMetaData().findRules(MutableDataNodeRule.class).forEach(each -> each.put(tableName, logicDataSourceNames.iterator().next()));
        }
        SchemaBuilderMaterials materials = new SchemaBuilderMaterials(
                schemaMetaData.getResource().getDatabaseType(), schemaMetaData.getResource().getDataSources(), schemaMetaData.getRuleMetaData().getRules(), props);
        Optional<TableMetaData> actualTableMetaData = Optional.ofNullable(TableMetaDataBuilder.load(Collections.singletonList(tableName), materials).get(tableName));
        actualTableMetaData.ifPresent(tableMetaData -> {
            schemaMetaData.getSchema().put(tableName, TableMetaDataBuilder.decorateKernelTableMetaData(tableMetaData, materials.getRules()));
            schema.put(TableMetaDataBuilder.decorateFederationTableMetaData(tableMetaData, materials.getRules()));
        });
    }
    
    private boolean containsInDataNodeContainedRule(final String tableName, final ShardingSphereMetaData schemaMetaData) {
        return schemaMetaData.getRuleMetaData().findRules(DataNodeContainedRule.class).stream().anyMatch(each -> each.getAllTables().contains(tableName));
    }
    
    @Override
    public String getType() {
        return AlterTableStatement.class.getCanonicalName();
    }
}
