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
import org.apache.shardingsphere.dialect.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.api.constant.SingleTableConstants;
import org.apache.shardingsphere.single.distsql.statement.rdl.UnloadSingleTableStatement;
import org.apache.shardingsphere.single.exception.SingleTableNotFoundException;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unload single table statement updater.
 */
public final class UnloadSingleTableStatementUpdater implements RuleDefinitionAlterUpdater<UnloadSingleTableStatement, SingleRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final UnloadSingleTableStatement sqlStatement, final SingleRuleConfiguration currentRuleConfig) {
        checkCurrentRuleConfig(database.getName(), currentRuleConfig);
        checkTables(database, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfig(final String databaseName, final SingleRuleConfiguration currentRuleConfig) {
        ShardingSpherePreconditions.checkState(null != currentRuleConfig, () -> new MissingRequiredRuleException("Single", databaseName));
    }
    
    private void checkTables(final ShardingSphereDatabase database, final UnloadSingleTableStatement sqlStatement, final SingleRuleConfiguration currentRuleConfig) {
        if (sqlStatement.isUnloadAllTables()) {
            return;
        }
        Collection<String> allTables = getAllTableNames(database);
        SingleRule singleRule = database.getRuleMetaData().getSingleRule(SingleRule.class);
        Collection<String> singleTables = singleRule.getLogicTableMapper().getTableNames();
        for (String each : sqlStatement.getTables()) {
            checkTableExist(allTables, each);
            checkIsSingleTable(singleTables, each);
            checkTableRuleExist(database.getName(), database.getProtocolType(), currentRuleConfig, singleRule.getDataNodesByTableName(each), each);
        }
    }
    
    private void checkTableExist(final Collection<String> allTables, final String tableName) {
        ShardingSpherePreconditions.checkState(allTables.contains(tableName), () -> new NoSuchTableException(tableName));
    }
    
    private void checkIsSingleTable(final Collection<String> singleTables, final String tableName) {
        ShardingSpherePreconditions.checkState(singleTables.contains(tableName), () -> new SingleTableNotFoundException(tableName));
    }
    
    private Collection<String> getAllTableNames(final ShardingSphereDatabase database) {
        String defaultSchemaName = DatabaseTypeEngine.getDefaultSchemaName(database.getProtocolType(), database.getName());
        return database.getSchema(defaultSchemaName).getTables().values().stream().map(ShardingSphereTable::getName).collect(Collectors.toList());
    }
    
    private void checkTableRuleExist(final String databaseName, final DatabaseType databaseType, final SingleRuleConfiguration currentRuleConfig,
                                     final Collection<DataNode> dataNodes, final String tableName) {
        ShardingSpherePreconditions.checkState(!dataNodes.isEmpty(), () -> new MissingRequiredRuleException("Single", databaseName, tableName));
        DataNode dataNode = dataNodes.iterator().next();
        ShardingSpherePreconditions.checkState(currentRuleConfig.getTables().contains(dataNode.format(databaseType)), () -> new MissingRequiredRuleException("Single", databaseName, tableName));
    }
    
    @Override
    public SingleRuleConfiguration buildToBeAlteredRuleConfiguration(final UnloadSingleTableStatement sqlStatement) {
        SingleRuleConfiguration result = new SingleRuleConfiguration();
        result.getTables().addAll(sqlStatement.isUnloadAllTables() ? Collections.singletonList(SingleTableConstants.ASTERISK) : sqlStatement.getTables());
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final SingleRuleConfiguration currentRuleConfig, final SingleRuleConfiguration toBeAlteredRuleConfig) {
        if (toBeAlteredRuleConfig.getTables().contains(SingleTableConstants.ASTERISK)) {
            currentRuleConfig.getTables().clear();
        } else {
            currentRuleConfig.getTables().removeIf(each -> toBeAlteredRuleConfig.getTables().contains(extractTableName(each)));
        }
    }
    
    private String extractTableName(final String tableNode) {
        List<String> segments = Splitter.on(".").trimResults().splitToList(tableNode);
        return segments.get(segments.size() - 1);
    }
    
    @Override
    public Class<SingleRuleConfiguration> getRuleConfigurationClass() {
        return SingleRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return UnloadSingleTableStatement.class.getName();
    }
}
