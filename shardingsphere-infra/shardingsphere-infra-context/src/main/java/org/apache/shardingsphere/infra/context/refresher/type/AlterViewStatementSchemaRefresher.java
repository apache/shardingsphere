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
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.AlterViewStatementHandler;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Schema refresher for alter view statement.
 */
public final class AlterViewStatementSchemaRefresher implements MetaDataRefresher<AlterViewStatement> {
    
    private static final String TYPE = AlterViewStatement.class.getName();
    
    @Override
    public void refresh(final ShardingSphereMetaData schemaMetaData, final FederationDatabaseMetaData database, final Map<String, OptimizerPlannerContext> optimizerPlanners, 
                        final Collection<String> logicDataSourceNames, final AlterViewStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        String viewName = sqlStatement.getView().getTableName().getIdentifier().getValue();
        SchemaAlteredEvent event = new SchemaAlteredEvent(schemaMetaData.getName());
        Optional<SimpleTableSegment> renameView = AlterViewStatementHandler.getRenameView(sqlStatement);
        if (renameView.isPresent()) {
            String renameViewName = renameView.get().getTableName().getIdentifier().getValue();
            putTableMetaData(schemaMetaData, database, optimizerPlanners, logicDataSourceNames, renameViewName, props);
            removeTableMetaData(schemaMetaData, database, optimizerPlanners, viewName);
            event.getAlteredTables().add(schemaMetaData.getDefaultSchema().get(renameViewName));
            event.getDroppedTables().add(viewName);
        } else {
            putTableMetaData(schemaMetaData, database, optimizerPlanners, logicDataSourceNames, viewName, props);
            event.getAlteredTables().add(schemaMetaData.getDefaultSchema().get(viewName));
        }
        ShardingSphereEventBus.getInstance().post(event);
    }
    
    private void removeTableMetaData(final ShardingSphereMetaData schemaMetaData, final FederationDatabaseMetaData database, 
                                     final Map<String, OptimizerPlannerContext> optimizerPlanners, final String viewName) {
        schemaMetaData.getDefaultSchema().remove(viewName);
        schemaMetaData.getRuleMetaData().findRules(MutableDataNodeRule.class).forEach(each -> each.remove(viewName));
        database.remove(viewName);
        optimizerPlanners.put(database.getName(), OptimizerPlannerContextFactory.create(database));
    }
    
    private void putTableMetaData(final ShardingSphereMetaData schemaMetaData, final FederationDatabaseMetaData database, final Map<String, OptimizerPlannerContext> optimizerPlanners, 
                                  final Collection<String> logicDataSourceNames, final String viewName, final ConfigurationProperties props) throws SQLException {
        if (!containsInDataNodeContainedRule(viewName, schemaMetaData)) {
            schemaMetaData.getRuleMetaData().findRules(MutableDataNodeRule.class).forEach(each -> each.put(viewName, logicDataSourceNames.iterator().next()));
        }
        SchemaBuilderMaterials materials = new SchemaBuilderMaterials(
                schemaMetaData.getResource().getDatabaseType(), schemaMetaData.getResource().getDataSources(), schemaMetaData.getRuleMetaData().getRules(), props);
        Optional<TableMetaData> actualViewMetaData = Optional.ofNullable(TableMetaDataBuilder.load(Collections.singletonList(viewName), materials).get(viewName));
        actualViewMetaData.ifPresent(viewMetaData -> {
            schemaMetaData.getDefaultSchema().put(viewName, viewMetaData);
            database.put(viewMetaData);
            optimizerPlanners.put(database.getName(), OptimizerPlannerContextFactory.create(database));
        });
    }
    
    private boolean containsInDataNodeContainedRule(final String viewName, final ShardingSphereMetaData schemaMetaData) {
        return schemaMetaData.getRuleMetaData().findRules(DataNodeContainedRule.class).stream().anyMatch(each -> each.getAllTables().contains(viewName));
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
