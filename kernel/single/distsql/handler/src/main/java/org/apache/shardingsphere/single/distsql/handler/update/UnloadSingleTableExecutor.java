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

import com.google.common.base.Splitter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleAlterExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.distsql.statement.rdl.UnloadSingleTableStatement;
import org.apache.shardingsphere.single.exception.SingleTableNotFoundException;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.util.Collection;
import java.util.List;
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
            checkTableRuleExist(database.getName(), database.getProtocolType(), singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getDataNodesByTableName(each), each);
        }
    }
    
    private Collection<String> getAllTableNames(final ShardingSphereDatabase database) {
        String defaultSchemaName = new DatabaseTypeRegistry(database.getProtocolType()).getDefaultSchemaName(database.getName());
        return database.getSchema(defaultSchemaName).getAllTables().stream().map(ShardingSphereTable::getName).collect(Collectors.toList());
    }
    
    private void checkTableExist(final Collection<String> allTables, final String tableName) {
        ShardingSpherePreconditions.checkContains(allTables, tableName, () -> new NoSuchTableException(tableName));
    }
    
    private void checkIsSingleTable(final Collection<String> singleTables, final String tableName) {
        ShardingSpherePreconditions.checkContains(singleTables, tableName, () -> new SingleTableNotFoundException(tableName));
    }
    
    private void checkTableRuleExist(final String databaseName, final DatabaseType databaseType, final Collection<DataNode> dataNodes, final String tableName) {
        ShardingSpherePreconditions.checkNotEmpty(dataNodes, () -> new MissingRequiredRuleException("Single", databaseName, tableName));
        DataNode dataNode = dataNodes.iterator().next();
        ShardingSpherePreconditions.checkContains(rule.getConfiguration().getTables(), dataNode.format(databaseType), () -> new MissingRequiredRuleException("Single", databaseName, tableName));
    }
    
    @Override
    public SingleRuleConfiguration buildToBeAlteredRuleConfiguration(final UnloadSingleTableStatement sqlStatement) {
        SingleRuleConfiguration result = new SingleRuleConfiguration();
        if (!sqlStatement.isUnloadAllTables()) {
            result.getTables().addAll(rule.getConfiguration().getTables());
            result.getTables().removeIf(each -> sqlStatement.getTables().contains(extractTableName(each)));
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
    
    private String extractTableName(final String tableNode) {
        List<String> segments = Splitter.on(".").trimResults().splitToList(tableNode);
        return segments.get(segments.size() - 1);
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
