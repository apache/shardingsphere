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

package org.apache.shardingsphere.single.distsql.handler.update;

import org.apache.shardingsphere.dialect.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.exception.InvalidDataNodesFormatException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.api.constant.SingleTableConstants;
import org.apache.shardingsphere.single.datanode.SingleTableDataNodeLoader;
import org.apache.shardingsphere.single.distsql.handler.exception.MissingRequiredSingleTableException;
import org.apache.shardingsphere.single.distsql.segment.SingleTableSegment;
import org.apache.shardingsphere.single.distsql.statement.rdl.LoadSingleTableStatement;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Load single table statement updater.
 */
public final class LoadSingleTableStatementUpdater implements RuleDefinitionCreateUpdater<LoadSingleTableStatement, SingleRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final LoadSingleTableStatement sqlStatement, final SingleRuleConfiguration currentRuleConfig) {
        String defaultSchemaName = DatabaseTypeEngine.getDefaultSchemaName(database.getProtocolType(), database.getName());
        checkDuplicatedTables(database, sqlStatement, defaultSchemaName);
        checkStorageUnits(database, sqlStatement);
        checkActualTableExist(database, sqlStatement, defaultSchemaName);
    }
    
    private void checkDuplicatedTables(final ShardingSphereDatabase database, final LoadSingleTableStatement sqlStatement, final String defaultSchemaName) {
        Collection<SingleTableSegment> tableSegments = sqlStatement.getTables();
        boolean isSchemaSupportedDatabaseType = database.getProtocolType().getDefaultSchema().isPresent();
        ShardingSphereSchema schema = database.getSchema(defaultSchemaName);
        for (SingleTableSegment each : tableSegments) {
            checkDatabaseTypeAndTableNodeStyle(isSchemaSupportedDatabaseType, each);
            if (SingleTableConstants.ASTERISK.equals(each.getTableName())) {
                continue;
            }
            ShardingSpherePreconditions.checkState(!schema.containsTable(each.getTableName()), () -> new TableExistsException(each.getTableName()));
        }
    }
    
    private void checkDatabaseTypeAndTableNodeStyle(final boolean isSchemaSupportedDatabaseType, final SingleTableSegment singleTableSegment) {
        if (SingleTableConstants.ALL_TABLES.equals(singleTableSegment.toString()) || SingleTableConstants.ALL_SCHEMA_TABLES.equals(singleTableSegment.toString())) {
            return;
        }
        if (isSchemaSupportedDatabaseType) {
            ShardingSpherePreconditions.checkState(singleTableSegment.getSchemaName().isPresent(),
                    () -> new InvalidDataNodesFormatException(singleTableSegment.toString(), "Current database is schema required, please use format `db.schema.table`"));
        } else {
            ShardingSpherePreconditions.checkState(!singleTableSegment.getSchemaName().isPresent(),
                    () -> new InvalidDataNodesFormatException(singleTableSegment.toString(), "Current database does not support schema, please use format `db.table`"));
        }
    }
    
    private Collection<String> getRequiredTables(final SingleRuleConfiguration currentRuleConfig, final LoadSingleTableStatement sqlStatement) {
        if (null != currentRuleConfig) {
            return sqlStatement.getTables().stream().map(SingleTableSegment::toString).filter(each -> !currentRuleConfig.getTables().contains(each)).collect(Collectors.toSet());
        }
        return sqlStatement.getTables().stream().map(SingleTableSegment::toString).collect(Collectors.toSet());
    }
    
    private Collection<String> getRequiredDataSources(final LoadSingleTableStatement sqlStatement) {
        return sqlStatement.getTables().stream().map(SingleTableSegment::getStorageUnitName)
                .filter(each -> !SingleTableConstants.ASTERISK.equals(each)).collect(Collectors.toSet());
    }
    
    private void checkStorageUnits(final ShardingSphereDatabase database, final LoadSingleTableStatement sqlStatement) {
        Collection<String> requiredDataSources = getRequiredDataSources(sqlStatement);
        if (requiredDataSources.isEmpty()) {
            return;
        }
        Collection<String> notExistedDataSources = database.getResourceMetaData().getNotExistedDataSources(requiredDataSources);
        Collection<String> logicDataSources = getLogicDataSources(database);
        notExistedDataSources.removeIf(logicDataSources::contains);
        ShardingSpherePreconditions.checkState(notExistedDataSources.isEmpty(), () -> new MissingRequiredStorageUnitsException(database.getName(), notExistedDataSources));
    }
    
    private void checkActualTableExist(final ShardingSphereDatabase database, final LoadSingleTableStatement sqlStatement, final String defaultSchemaName) {
        Collection<String> requiredDataSources = getRequiredDataSources(sqlStatement);
        if (requiredDataSources.isEmpty()) {
            return;
        }
        ShardingSphereResourceMetaData resourceMetaData = database.getResourceMetaData();
        Map<String, DataSource> aggregateDataSourceMap = SingleTableLoadUtils.getAggregatedDataSourceMap(resourceMetaData.getDataSources(), database.getRuleMetaData().getRules());
        Map<String, Map<String, Collection<String>>> actualTableNodes = new LinkedHashMap<>();
        for (String each : requiredDataSources) {
            Map<String, Collection<String>> schemaTableNames = SingleTableDataNodeLoader.loadSchemaTableNames(database.getName(), database.getProtocolType(), aggregateDataSourceMap.get(each), each);
            if (!schemaTableNames.isEmpty()) {
                actualTableNodes.put(each, schemaTableNames);
            }
        }
        for (SingleTableSegment each : sqlStatement.getTables()) {
            if (SingleTableConstants.ASTERISK.equals(each.getTableName())) {
                continue;
            }
            Map<String, Collection<String>> schemaTableMap = actualTableNodes.getOrDefault(each.getStorageUnitName(), new LinkedHashMap<>());
            ShardingSpherePreconditions.checkState(!schemaTableMap.isEmpty(), () -> new MissingRequiredSingleTableException(each.getStorageUnitName(), each.getTableName()));
            Collection<String> schemaTables = schemaTableMap.getOrDefault(defaultSchemaName, new LinkedList<>());
            ShardingSpherePreconditions.checkState(!schemaTables.isEmpty() && schemaTables.contains(each.getTableName()),
                    () -> new MissingRequiredSingleTableException(each.getStorageUnitName(), each.getTableName()));
        }
    }
    
    private static Collection<String> getLogicDataSources(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findRules(DataSourceContainedRule.class).stream()
                .map(each -> each.getDataSourceMapper().keySet()).flatMap(Collection::stream).collect(Collectors.toSet());
    }
    
    @Override
    public SingleRuleConfiguration buildToBeCreatedRuleConfiguration(final SingleRuleConfiguration currentRuleConfig, final LoadSingleTableStatement sqlStatement) {
        SingleRuleConfiguration result = new SingleRuleConfiguration();
        result.getTables().addAll(getRequiredTables(currentRuleConfig, sqlStatement));
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final SingleRuleConfiguration currentRuleConfig, final SingleRuleConfiguration toBeCreatedRuleConfig) {
        currentRuleConfig.getTables().addAll(toBeCreatedRuleConfig.getTables());
    }
    
    @Override
    public Class<SingleRuleConfiguration> getRuleConfigurationClass() {
        return SingleRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return LoadSingleTableStatement.class.getName();
    }
}
