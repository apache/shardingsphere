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
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.event.AlterSchemaEvent;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.AlterSchemaStatementHandler;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Schema refresher for alter schema statement.
 */
public final class AlterSchemaStatementSchemaRefresher implements MetaDataRefresher<AlterSchemaStatement> {
    
    private static final String TYPE = AlterSchemaStatement.class.getName();
    
    @Override
    public void refresh(final ShardingSphereDatabaseMetaData databaseMetaData, final FederationDatabaseMetaData database, final Map<String, OptimizerPlannerContext> optimizerPlanners,
                        final Collection<String> logicDataSourceNames, final String schemaName, final AlterSchemaStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        Optional<IdentifierValue> renameSchemaName = AlterSchemaStatementHandler.getRenameSchema(sqlStatement);
        if (!renameSchemaName.isPresent()) {
            return;
        }
        String actualSchemaName = sqlStatement.getSchemaName().getValue();
        putSchemaMetaData(databaseMetaData, database, optimizerPlanners, actualSchemaName, renameSchemaName.get().getValue(), logicDataSourceNames);
        removeSchemaMetaData(databaseMetaData, database, optimizerPlanners, actualSchemaName);
        AlterSchemaEvent event = new AlterSchemaEvent(databaseMetaData.getDatabase().getName(),
                actualSchemaName, renameSchemaName.get().getValue(), databaseMetaData.getDatabase().getSchema(renameSchemaName.get().getValue()));
        ShardingSphereEventBus.getInstance().post(event);
    }
    
    private void removeSchemaMetaData(final ShardingSphereDatabaseMetaData databaseMetaData, final FederationDatabaseMetaData database,
                                      final Map<String, OptimizerPlannerContext> optimizerPlanners, final String schemaName) {
        ShardingSphereSchema schema = databaseMetaData.getDatabase().getSchemas().remove(schemaName);
        database.removeSchemaMetadata(schemaName);
        optimizerPlanners.put(database.getName(), OptimizerPlannerContextFactory.create(database));
        Collection<MutableDataNodeRule> rules = databaseMetaData.getRuleMetaData().findRules(MutableDataNodeRule.class);
        for (String each : schema.getAllTableNames()) {
            removeDataNode(rules, schemaName, each);
        }
    }
    
    private void removeDataNode(final Collection<MutableDataNodeRule> rules, final String schemaName, final String tableName) {
        for (MutableDataNodeRule each : rules) {
            each.remove(schemaName, tableName);
        }
    }
    
    private void putSchemaMetaData(final ShardingSphereDatabaseMetaData databaseMetaData, final FederationDatabaseMetaData database, final Map<String, OptimizerPlannerContext> optimizerPlanners,
                                   final String schemaName, final String renameSchemaName, final Collection<String> logicDataSourceNames) {
        ShardingSphereSchema schema = databaseMetaData.getDatabase().getSchema(schemaName);
        databaseMetaData.getDatabase().getSchemas().put(renameSchemaName, schema);
        database.getSchemaMetadata(schemaName).ifPresent(optional -> database.putSchemaMetadata(renameSchemaName, optional));
        optimizerPlanners.put(database.getName(), OptimizerPlannerContextFactory.create(database));
        Collection<MutableDataNodeRule> rules = databaseMetaData.getRuleMetaData().findRules(MutableDataNodeRule.class);
        for (String each : schema.getAllTableNames()) {
            if (containsInImmutableDataNodeContainedRule(each, databaseMetaData)) {
                continue;
            }
            putDataNode(rules, logicDataSourceNames.iterator().next(), renameSchemaName, each);
        }
    }
    
    private void putDataNode(final Collection<MutableDataNodeRule> rules, final String dataSourceName, final String schemaName, final String tableName) {
        for (MutableDataNodeRule each : rules) {
            each.put(dataSourceName, schemaName, tableName);
        }
    }
    
    private boolean containsInImmutableDataNodeContainedRule(final String tableName, final ShardingSphereDatabaseMetaData databaseMetaData) {
        return databaseMetaData.getRuleMetaData().findRules(DataNodeContainedRule.class).stream()
                .filter(each -> !(each instanceof MutableDataNodeRule)).anyMatch(each -> each.getAllTables().contains(tableName));
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
