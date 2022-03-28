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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContextFactory;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.TableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Schema refresher for alter table statement.
 */
public final class AlterTableStatementSchemaRefresher implements MetaDataRefresher<AlterTableStatement> {
    
    private static final String TYPE = AlterTableStatement.class.getName();
    
    @Override
    public void refresh(final ShardingSphereMetaData schemaMetaData, final FederationDatabaseMetaData database, final Map<String, OptimizerPlannerContext> optimizerPlanners,
                        final Collection<String> logicDataSourceNames, final AlterTableStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        String tableName = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        SchemaAlteredEvent event = new SchemaAlteredEvent(schemaMetaData.getName());
        if (sqlStatement.getRenameTable().isPresent()) {
            String renameTable = sqlStatement.getRenameTable().get().getTableName().getIdentifier().getValue();
            putTableMetaData(schemaMetaData, database, optimizerPlanners, logicDataSourceNames, renameTable, props);
            removeTableMetaData(schemaMetaData, database, optimizerPlanners, tableName);
            event.getAlteredTables().add(schemaMetaData.getSchema().get(renameTable));
            event.getDroppedTables().add(tableName);
        } else {
            putTableMetaData(schemaMetaData, database, optimizerPlanners, logicDataSourceNames, tableName, props);
            event.getAlteredTables().add(schemaMetaData.getSchema().get(tableName));
        }
        ShardingSphereEventBus.getInstance().post(event);
    }
    
    private void removeTableMetaData(final ShardingSphereMetaData schemaMetaData, final FederationDatabaseMetaData database, 
                                     final Map<String, OptimizerPlannerContext> optimizerPlanners, final String tableName) {
        schemaMetaData.getSchema().remove(tableName);
        schemaMetaData.getRuleMetaData().findRules(MutableDataNodeRule.class).forEach(each -> each.remove(tableName));
        database.remove(tableName);
        optimizerPlanners.put(database.getName(), OptimizerPlannerContextFactory.create(database));
    }
    
    private void putTableMetaData(final ShardingSphereMetaData schemaMetaData, final FederationDatabaseMetaData database, final Map<String, OptimizerPlannerContext> optimizerPlanners, 
                                  final Collection<String> logicDataSourceNames, final String tableName, final ConfigurationProperties props) throws SQLException {
        if (!containsInDataNodeContainedRule(tableName, schemaMetaData)) {
            schemaMetaData.getRuleMetaData().findRules(MutableDataNodeRule.class).forEach(each -> each.put(tableName, logicDataSourceNames.iterator().next()));
        }
        SchemaBuilderMaterials materials = new SchemaBuilderMaterials(
                schemaMetaData.getResource().getDatabaseType(), schemaMetaData.getResource().getDataSources(), schemaMetaData.getRuleMetaData().getRules(), props);
        Optional<TableMetaData> actualTableMetaData = Optional.ofNullable(TableMetaDataBuilder.load(Collections.singletonList(tableName), materials).get(tableName));
        actualTableMetaData.ifPresent(tableMetaData -> {
            schemaMetaData.getSchema().put(tableName, tableMetaData);
            database.put(tableMetaData);
            optimizerPlanners.put(database.getName(), OptimizerPlannerContextFactory.create(database));
        });
    }
    
    private boolean containsInDataNodeContainedRule(final String tableName, final ShardingSphereMetaData schemaMetaData) {
        return schemaMetaData.getRuleMetaData().findRules(DataNodeContainedRule.class).stream().anyMatch(each -> each.getAllTables().contains(tableName));
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
