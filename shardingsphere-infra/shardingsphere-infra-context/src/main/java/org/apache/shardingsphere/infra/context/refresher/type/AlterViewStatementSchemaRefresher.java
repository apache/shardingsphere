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
import org.apache.shardingsphere.infra.metadata.ShardingSphereDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.TableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.metadata.schema.model.SchemaMetaData;
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
    public void refresh(final ShardingSphereDatabaseMetaData databaseMetaData, final FederationDatabaseMetaData database, final Map<String, OptimizerPlannerContext> optimizerPlanners,
                        final Collection<String> logicDataSourceNames, final String schemaName, final AlterViewStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        String viewName = sqlStatement.getView().getTableName().getIdentifier().getValue();
        SchemaAlteredEvent event = new SchemaAlteredEvent(databaseMetaData.getDatabase().getName(), schemaName);
        Optional<SimpleTableSegment> renameView = AlterViewStatementHandler.getRenameView(sqlStatement);
        if (renameView.isPresent()) {
            String renameViewName = renameView.get().getTableName().getIdentifier().getValue();
            putTableMetaData(databaseMetaData, database, optimizerPlanners, logicDataSourceNames, schemaName, renameViewName, props);
            removeTableMetaData(databaseMetaData, database, optimizerPlanners, schemaName, viewName);
            event.getAlteredTables().add(databaseMetaData.getDatabase().getSchema(schemaName).get(renameViewName));
            event.getDroppedTables().add(viewName);
        } else {
            putTableMetaData(databaseMetaData, database, optimizerPlanners, logicDataSourceNames, schemaName, viewName, props);
            event.getAlteredTables().add(databaseMetaData.getDatabase().getSchema(schemaName).get(viewName));
        }
        ShardingSphereEventBus.getInstance().post(event);
    }
    
    private void removeTableMetaData(final ShardingSphereDatabaseMetaData databaseMetaData, final FederationDatabaseMetaData database,
                                     final Map<String, OptimizerPlannerContext> optimizerPlanners, final String schemaName, final String viewName) {
        databaseMetaData.getDatabase().getSchema(schemaName).remove(viewName);
        databaseMetaData.getRuleMetaData().findRules(MutableDataNodeRule.class).forEach(each -> each.remove(schemaName, viewName));
        database.removeTableMetadata(schemaName, viewName);
        optimizerPlanners.put(database.getName(), OptimizerPlannerContextFactory.create(database));
    }
    
    private void putTableMetaData(final ShardingSphereDatabaseMetaData databaseMetaData, final FederationDatabaseMetaData database, final Map<String, OptimizerPlannerContext> optimizerPlanners,
                                  final Collection<String> logicDataSourceNames, final String schemaName, final String viewName, final ConfigurationProperties props) throws SQLException {
        if (!containsInImmutableDataNodeContainedRule(viewName, databaseMetaData)) {
            databaseMetaData.getRuleMetaData().findRules(MutableDataNodeRule.class).forEach(each -> each.put(logicDataSourceNames.iterator().next(), schemaName, viewName));
        }
        SchemaBuilderMaterials materials = new SchemaBuilderMaterials(databaseMetaData.getProtocolType(),
                databaseMetaData.getResource().getDatabaseType(), databaseMetaData.getResource().getDataSources(), databaseMetaData.getRuleMetaData().getRules(), props, schemaName);
        Map<String, SchemaMetaData> metaDataMap = TableMetaDataBuilder.load(Collections.singletonList(viewName), materials);
        Optional<TableMetaData> actualViewMetaData = Optional.ofNullable(metaDataMap.get(schemaName)).map(optional -> optional.getTables().get(viewName));
        actualViewMetaData.ifPresent(optional -> {
            databaseMetaData.getDatabase().getSchema(schemaName).put(viewName, optional);
            database.putTableMetadata(schemaName, optional);
            optimizerPlanners.put(database.getName(), OptimizerPlannerContextFactory.create(database));
        });
    }
    
    private boolean containsInImmutableDataNodeContainedRule(final String viewName, final ShardingSphereDatabaseMetaData databaseMetaData) {
        return databaseMetaData.getRuleMetaData().findRules(DataNodeContainedRule.class).stream()
                .filter(each -> !(each instanceof MutableDataNodeRule)).anyMatch(each -> each.getAllTables().contains(viewName));
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
