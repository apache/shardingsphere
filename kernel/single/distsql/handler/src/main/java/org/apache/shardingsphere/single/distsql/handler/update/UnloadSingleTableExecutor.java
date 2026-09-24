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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleAlterExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datasource.aggregate.AggregatedDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.distsql.statement.rdl.UnloadSingleTableStatement;
import org.apache.shardingsphere.single.exception.SingleTableNotFoundException;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Unload single table statement executor.
 */
@DistSQLExecutorCurrentRuleRequired(SingleRule.class)
@Setter
public final class UnloadSingleTableExecutor implements DatabaseRuleAlterExecutor<UnloadSingleTableStatement, SingleRule, SingleRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private SingleRule rule;
    
    @Override
    public void checkBeforeUpdate(final UnloadSingleTableStatement sqlStatement) {
        checkTables(sqlStatement);
    }
    
    private void checkTables(final UnloadSingleTableStatement sqlStatement) {
        if (sqlStatement.isUnloadAllTables()) {
            return;
        }
        Collection<String> allTables = getAllTableNames(database);
        SingleRule singleRule = database.getRuleMetaData().getSingleRule(SingleRule.class);
        Collection<String> singleTables = singleRule.getAttributes().getAttribute(TableMapperRuleAttribute.class).getLogicTableNames();
        for (String each : sqlStatement.getTables()) {
            checkTableExist(allTables, each);
            checkIsSingleTable(singleTables, each);
            checkTableRuleExist(database.getName(), singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getDataNodesByTableName(each), each);
        }
    }
    
    private Collection<String> getAllTableNames(final ShardingSphereDatabase database) {
        return database.findDefaultSchema().map(schema -> schema.getAllTables().stream().map(ShardingSphereTable::getName).collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }
    
    private void checkTableExist(final Collection<String> allTables, final String tableName) {
        ShardingSpherePreconditions.checkContains(allTables, tableName, () -> new NoSuchTableException(tableName));
    }
    
    private void checkIsSingleTable(final Collection<String> singleTables, final String tableName) {
        ShardingSpherePreconditions.checkContains(singleTables, tableName, () -> new SingleTableNotFoundException(tableName));
    }
    
    private void checkTableRuleExist(final String databaseName, final Collection<DataNode> dataNodes, final String tableName) {
        ShardingSpherePreconditions.checkNotEmpty(dataNodes, () -> new MissingRequiredRuleException("Single", databaseName, tableName));
        DataNode dataNode = dataNodes.iterator().next();
        String dataNodeString = getDataNodeString(dataNode);
        ShardingSpherePreconditions.checkContains(rule.getConfiguration().getTables(), dataNodeString, () -> new MissingRequiredRuleException("Single", databaseName, tableName));
    }
    
    private String getDataNodeString(final DataNode dataNode) {
        DatabaseType storageType = getStorageType(dataNode.getDataSourceName());
        return SingleTableLoadUtils.getDataNodeString(storageType, dataNode.getDataSourceName(), dataNode.getSchemaName(), dataNode.getTableName());
    }
    
    private DatabaseType getStorageType(final String dataSourceName) {
        Map<String, StorageUnit> storageUnits = database.getResourceMetaData().getStorageUnits();
        StorageUnit storageUnit = storageUnits.get(dataSourceName);
        if (null != storageUnit) {
            return storageUnit.getStorageType();
        }
        DataSource dataSource = rule.getAttributes().getAttribute(AggregatedDataSourceRuleAttribute.class).getAggregatedDataSources().get(dataSourceName);
        return storageUnits.values().stream().filter(each -> dataSource == each.getDataSource()).map(StorageUnit::getStorageType).findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Can not find storage type for data source: %s", dataSourceName)));
    }
    
    @Override
    public SingleRuleConfiguration buildToBeAlteredRuleConfiguration(final UnloadSingleTableStatement sqlStatement) {
        SingleRuleConfiguration result = new SingleRuleConfiguration();
        if (!sqlStatement.isUnloadAllTables()) {
            result.getTables().addAll(rule.getConfiguration().getTables());
            DataNodeRuleAttribute dataNodeRuleAttribute = rule.getAttributes().getAttribute(DataNodeRuleAttribute.class);
            Collection<String> toBeRemovedDataNodes = sqlStatement.getTables().stream().flatMap(each -> dataNodeRuleAttribute.getDataNodesByTableName(each).stream())
                    .map(this::getDataNodeString).collect(Collectors.toSet());
            result.getTables().removeAll(toBeRemovedDataNodes);
        }
        return result;
    }
    
    @Override
    public SingleRuleConfiguration buildToBeDroppedRuleConfiguration(final SingleRuleConfiguration toBeAlteredRuleConfig) {
        if (toBeAlteredRuleConfig.getTables().isEmpty()) {
            SingleRuleConfiguration result = new SingleRuleConfiguration();
            result.getTables().addAll(rule.getConfiguration().getTables());
            return result;
        }
        return null;
    }
    
    @Override
    public Class<SingleRule> getRuleClass() {
        return SingleRule.class;
    }
    
    @Override
    public Class<UnloadSingleTableStatement> getType() {
        return UnloadSingleTableStatement.class;
    }
}
