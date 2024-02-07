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

package org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.legacy;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorRequiredChecker;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleAlterExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.rule.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.decorator.RuleConfigurationDecorator;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO Remove when metadata structure adjustment completed. #25485
/**
 * Legacy rule definition execute engine.
 */
@RequiredArgsConstructor
public final class LegacyDatabaseRuleDefinitionExecuteEngine {
    
    private final RuleDefinitionStatement sqlStatement;
    
    private final ContextManager contextManager;
    
    private final ShardingSphereDatabase database;
    
    @SuppressWarnings("rawtypes")
    private final DatabaseRuleDefinitionExecutor executor;
    
    /**
     * Execute update.
     */
    @SuppressWarnings("unchecked")
    public void executeUpdate() {
        executor.setDatabase(database);
        Optional<ShardingSphereRule> rule = database.getRuleMetaData().findSingleRule(executor.getRuleClass());
        executor.setRule(rule.orElse(null));
        checkBeforeUpdate();
        RuleConfiguration currentRuleConfig = rule.map(ShardingSphereRule::getConfiguration).orElse(null);
        if (getRefreshStatus(sqlStatement, executor)) {
            contextManager.getInstanceContext().getModeContextManager().alterRuleConfiguration(database.getName(), processSQLStatement(database, sqlStatement, executor, currentRuleConfig));
        }
    }
    
    @SuppressWarnings("unchecked")
    private void checkBeforeUpdate() {
        new DistSQLExecutorRequiredChecker(executor).check(sqlStatement, contextManager, database);
        executor.checkBeforeUpdate(sqlStatement);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean getRefreshStatus(final SQLStatement sqlStatement, final DatabaseRuleDefinitionExecutor<?, ?> executor) {
        return !(executor instanceof DatabaseRuleDropExecutor) || ((DatabaseRuleDropExecutor) executor).hasAnyOneToBeDropped(sqlStatement);
    }
    
    @SuppressWarnings("rawtypes")
    private Collection<RuleConfiguration> processSQLStatement(final ShardingSphereDatabase database,
                                                              final RuleDefinitionStatement sqlStatement, final DatabaseRuleDefinitionExecutor executor, final RuleConfiguration currentRuleConfig) {
        Collection<RuleConfiguration> result = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        if (executor instanceof DatabaseRuleCreateExecutor) {
            if (null != currentRuleConfig) {
                result.remove(currentRuleConfig);
            }
            RuleConfiguration createdRuleConfig = processCreate(sqlStatement, (DatabaseRuleCreateExecutor) executor, currentRuleConfig);
            result.add(decorateRuleConfiguration(database, createdRuleConfig));
        } else if (executor instanceof DatabaseRuleAlterExecutor) {
            result.remove(currentRuleConfig);
            RuleConfiguration alteredRuleConfig = processAlter(sqlStatement, (DatabaseRuleAlterExecutor) executor, currentRuleConfig);
            result.add(decorateRuleConfiguration(database, alteredRuleConfig));
        } else if (executor instanceof DatabaseRuleDropExecutor) {
            processDrop(database, result, sqlStatement, (DatabaseRuleDropExecutor) executor, currentRuleConfig);
        } else {
            throw new UnsupportedSQLOperationException(String.format("Cannot support RDL executor type `%s`", executor.getClass().getName()));
        }
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private RuleConfiguration processCreate(final RuleDefinitionStatement sqlStatement, final DatabaseRuleCreateExecutor executor, final RuleConfiguration currentRuleConfig) {
        RuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        if (null == currentRuleConfig) {
            return toBeCreatedRuleConfig;
        }
        executor.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        return currentRuleConfig;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private RuleConfiguration processAlter(final RuleDefinitionStatement sqlStatement, final DatabaseRuleAlterExecutor executor, final RuleConfiguration currentRuleConfig) {
        RuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        executor.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        return currentRuleConfig;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processDrop(final ShardingSphereDatabase database, final Collection<RuleConfiguration> configs, final RuleDefinitionStatement sqlStatement,
                             final DatabaseRuleDropExecutor executor, final RuleConfiguration currentRuleConfig) {
        if (!executor.hasAnyOneToBeDropped(sqlStatement)) {
            return;
        }
        if (executor.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig)) {
            configs.remove(currentRuleConfig);
        }
        executor.operate(sqlStatement, database);
    }
    
    @SuppressWarnings("unchecked")
    private RuleConfiguration decorateRuleConfiguration(final ShardingSphereDatabase database, final RuleConfiguration ruleConfig) {
        return TypedSPILoader.findService(RuleConfigurationDecorator.class, ruleConfig.getClass()).map(optional -> optional.decorate(database.getName(),
                database.getResourceMetaData().getStorageUnits().entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)),
                database.getRuleMetaData().getRules(), ruleConfig)).orElse(ruleConfig);
    }
}
