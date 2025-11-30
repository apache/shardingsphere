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

import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.datanode.InvalidDataNodeFormatException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.InvalidStorageUnitStatusException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.PhysicalDataSourceAggregator;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.constant.SingleTableConstants;
import org.apache.shardingsphere.single.datanode.SingleTableDataNodeLoader;
import org.apache.shardingsphere.single.distsql.segment.SingleTableSegment;
import org.apache.shardingsphere.single.distsql.statement.rdl.LoadSingleTableStatement;
import org.apache.shardingsphere.single.rule.SingleRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Load single table statement executor.
 */
@Setter
public final class LoadSingleTableExecutor implements DatabaseRuleCreateExecutor<LoadSingleTableStatement, SingleRule, SingleRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private SingleRule rule;
    
    @Override
    public void checkBeforeUpdate(final LoadSingleTableStatement sqlStatement) {
        Collection<String> storageUnitNames = getStorageUnitNames(sqlStatement);
        if (!storageUnitNames.isEmpty()) {
            ShardingSpherePreconditions.checkNotEmpty(database.getResourceMetaData().getStorageUnits(), () -> new EmptyStorageUnitException(database.getName()));
            database.checkStorageUnitsExisted(storageUnitNames);
        }
        String defaultSchemaName = new DatabaseTypeRegistry(database.getProtocolType()).getDefaultSchemaName(database.getName());
        checkShouldNotExistLogicTables(sqlStatement, defaultSchemaName);
        if (!storageUnitNames.isEmpty()) {
            checkShouldExistActualTables(sqlStatement, storageUnitNames, defaultSchemaName);
        }
    }
    
    private Collection<String> getStorageUnitNames(final LoadSingleTableStatement sqlStatement) {
        return sqlStatement.getTables().stream().map(SingleTableSegment::getStorageUnitName).filter(each -> !SingleTableConstants.ASTERISK.equals(each)).collect(Collectors.toSet());
    }
    
    private void checkShouldNotExistLogicTables(final LoadSingleTableStatement sqlStatement, final String defaultSchemaName) {
        Collection<SingleTableSegment> tableSegments = sqlStatement.getTables();
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(database.getProtocolType()).getDialectDatabaseMetaData();
        boolean isSchemaSupportedDatabaseType = dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent();
        ShardingSphereSchema schema = database.getSchema(defaultSchemaName);
        for (SingleTableSegment each : tableSegments) {
            checkTableNodeFormat(isSchemaSupportedDatabaseType, each);
            if (!SingleTableConstants.ASTERISK.equals(each.getTableName())) {
                ShardingSpherePreconditions.checkState(!schema.containsTable(each.getTableName()), () -> new TableExistsException(each.getTableName()));
            }
        }
    }
    
    private void checkTableNodeFormat(final boolean isSchemaSupportedDatabaseType, final SingleTableSegment singleTableSegment) {
        if (SingleTableConstants.ALL_TABLES.equals(singleTableSegment.toString()) || SingleTableConstants.ALL_SCHEMA_TABLES.equals(singleTableSegment.toString())) {
            return;
        }
        if (isSchemaSupportedDatabaseType) {
            ShardingSpherePreconditions.checkState(singleTableSegment.containsSchema(),
                    () -> new InvalidDataNodeFormatException(singleTableSegment.toString(), "Current database is schema required, please use format `db.schema.table`"));
        } else {
            ShardingSpherePreconditions.checkState(!singleTableSegment.containsSchema(),
                    () -> new InvalidDataNodeFormatException(singleTableSegment.toString(), "Current database does not support schema, please use format `db.table`"));
        }
    }
    
    private void checkShouldExistActualTables(final LoadSingleTableStatement sqlStatement, final Collection<String> storageUnitNames, final String defaultSchemaName) {
        Map<String, DataSource> dataSourceMap = database.getResourceMetaData().getStorageUnits().entrySet()
                .stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource()));
        Map<String, DataSource> aggregatedDataSourceMap = PhysicalDataSourceAggregator.getAggregatedDataSources(dataSourceMap, database.getRuleMetaData().getRules());
        Collection<String> invalidDataSources = storageUnitNames.stream().filter(each -> !aggregatedDataSourceMap.containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(invalidDataSources.isEmpty(), () -> new InvalidStorageUnitStatusException(String.format("`%s` is invalid, please use `%s`",
                String.join(",", invalidDataSources), String.join(",", aggregatedDataSourceMap.keySet()))));
        Map<String, Map<String, Collection<String>>> actualTableNodes = getActualTableNodes(storageUnitNames, aggregatedDataSourceMap);
        for (SingleTableSegment each : sqlStatement.getTables()) {
            String tableName = each.getTableName();
            if (!SingleTableConstants.ASTERISK.equals(tableName)) {
                String storageUnitName = each.getStorageUnitName();
                ShardingSpherePreconditions.checkState(actualTableNodes.containsKey(storageUnitName) && actualTableNodes.get(storageUnitName).get(defaultSchemaName).contains(tableName),
                        () -> new TableNotFoundException(tableName, storageUnitName));
            }
        }
    }
    
    private Map<String, Map<String, Collection<String>>> getActualTableNodes(final Collection<String> storageUnitNames, final Map<String, DataSource> aggregatedDataSourceMap) {
        Map<String, Map<String, Collection<String>>> result = new LinkedHashMap<>(storageUnitNames.size(), 1F);
        for (String each : storageUnitNames) {
            DataSource dataSource = aggregatedDataSourceMap.get(each);
            Map<String, Collection<String>> schemaTableNames =
                    SingleTableDataNodeLoader.loadSchemaTableNames(database.getName(), DatabaseTypeEngine.getStorageType(dataSource), dataSource, each, Collections.emptyList());
            if (!schemaTableNames.isEmpty()) {
                result.put(each, schemaTableNames);
            }
        }
        return result;
    }
    
    @Override
    public SingleRuleConfiguration buildToBeCreatedRuleConfiguration(final LoadSingleTableStatement sqlStatement) {
        SingleRuleConfiguration result = new SingleRuleConfiguration();
        if (null != rule) {
            result.getTables().addAll(rule.getConfiguration().getTables());
        }
        result.getTables().addAll(getRequiredTables(sqlStatement));
        return result;
    }
    
    private Collection<String> getRequiredTables(final LoadSingleTableStatement sqlStatement) {
        if (null != rule) {
            return sqlStatement.getTables().stream().map(SingleTableSegment::toString).filter(each -> !rule.getConfiguration().getTables().contains(each)).collect(Collectors.toSet());
        }
        return sqlStatement.getTables().stream().map(SingleTableSegment::toString).collect(Collectors.toSet());
    }
    
    @Override
    public Class<SingleRule> getRuleClass() {
        return SingleRule.class;
    }
    
    @Override
    public Class<LoadSingleTableStatement> getType() {
        return LoadSingleTableStatement.class;
    }
}
