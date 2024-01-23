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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.legacy;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.type.rdl.database.DatabaseRuleRDLAlterExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.database.DatabaseRuleRDLCreateExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.database.DatabaseRuleRDLDropExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.database.DatabaseRuleRDLExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.decorator.RuleConfigurationDecorator;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.StaticDataSourceContainedRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.DatabaseNameUtils;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.update.DropReadwriteSplittingRuleExecutor;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Legacy rule definition backend handler.
 *
 * @param <T> type of rule definition statement
 */
@RequiredArgsConstructor
public final class LegacyRuleDefinitionBackendHandler<T extends RuleDefinitionStatement> implements DistSQLBackendHandler {
    
    private final T sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ResponseHeader execute() {
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(DatabaseNameUtils.getDatabaseName(sqlStatement, connectionSession));
        DatabaseRuleRDLExecutor executor = TypedSPILoader.getService(DatabaseRuleRDLExecutor.class, sqlStatement.getClass());
        Class<? extends RuleConfiguration> ruleConfigClass = executor.getRuleConfigurationClass();
        RuleConfiguration currentRuleConfig = findCurrentRuleConfiguration(database, ruleConfigClass).orElse(null);
        executor.checkSQLStatement(database, sqlStatement, currentRuleConfig);
        if (getRefreshStatus(sqlStatement, currentRuleConfig, executor)) {
            ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager()
                    .alterRuleConfiguration(database.getName(), processSQLStatement(database, sqlStatement, executor, currentRuleConfig));
        }
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private Optional<RuleConfiguration> findCurrentRuleConfiguration(final ShardingSphereDatabase database, final Class<? extends RuleConfiguration> ruleConfigClass) {
        return database.getRuleMetaData().getConfigurations().stream().filter(each -> ruleConfigClass.isAssignableFrom(each.getClass())).findFirst();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean getRefreshStatus(final SQLStatement sqlStatement, final RuleConfiguration currentRuleConfig, final DatabaseRuleRDLExecutor<?, ?> executor) {
        return !(executor instanceof DatabaseRuleRDLDropExecutor) || ((DatabaseRuleRDLDropExecutor) executor).hasAnyOneToBeDropped(sqlStatement, currentRuleConfig);
    }
    
    @SuppressWarnings("rawtypes")
    private Collection<RuleConfiguration> processSQLStatement(final ShardingSphereDatabase database,
                                                              final T sqlStatement, final DatabaseRuleRDLExecutor executor, final RuleConfiguration currentRuleConfig) {
        Collection<RuleConfiguration> result = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        if (executor instanceof DatabaseRuleRDLCreateExecutor) {
            if (null != currentRuleConfig) {
                result.remove(currentRuleConfig);
            }
            RuleConfiguration createdRuleConfig = processCreate(sqlStatement, (DatabaseRuleRDLCreateExecutor) executor, currentRuleConfig);
            result.add(decorateRuleConfiguration(database, createdRuleConfig));
        } else if (executor instanceof DatabaseRuleRDLAlterExecutor) {
            result.remove(currentRuleConfig);
            RuleConfiguration alteredRuleConfig = processAlter(sqlStatement, (DatabaseRuleRDLAlterExecutor) executor, currentRuleConfig);
            result.add(decorateRuleConfiguration(database, alteredRuleConfig));
        } else if (executor instanceof DatabaseRuleRDLDropExecutor) {
            processDrop(database, result, sqlStatement, (DatabaseRuleRDLDropExecutor) executor, currentRuleConfig);
        } else {
            throw new UnsupportedSQLOperationException(String.format("Cannot support RDL executor type `%s`", executor.getClass().getName()));
        }
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private RuleConfiguration processCreate(final T sqlStatement, final DatabaseRuleRDLCreateExecutor executor, final RuleConfiguration currentRuleConfig) {
        RuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(currentRuleConfig, sqlStatement);
        if (null == currentRuleConfig) {
            return toBeCreatedRuleConfig;
        }
        executor.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        return currentRuleConfig;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private RuleConfiguration processAlter(final T sqlStatement, final DatabaseRuleRDLAlterExecutor executor, final RuleConfiguration currentRuleConfig) {
        RuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        executor.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        return currentRuleConfig;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processDrop(final ShardingSphereDatabase database, final Collection<RuleConfiguration> configs, final T sqlStatement,
                             final DatabaseRuleRDLDropExecutor executor, final RuleConfiguration currentRuleConfig) {
        if (!executor.hasAnyOneToBeDropped(sqlStatement, currentRuleConfig)) {
            return;
        }
        if (executor.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig)) {
            configs.remove(currentRuleConfig);
        }
        if (executor instanceof DropReadwriteSplittingRuleExecutor) {
            database.getRuleMetaData().findSingleRule(StaticDataSourceContainedRule.class)
                    .ifPresent(optional -> ((DropReadwriteSplittingRuleStatement) sqlStatement).getNames().forEach(optional::cleanStorageNodeDataSource));
        }
    }
    
    @SuppressWarnings("unchecked")
    private RuleConfiguration decorateRuleConfiguration(final ShardingSphereDatabase database, final RuleConfiguration ruleConfig) {
        return TypedSPILoader.findService(RuleConfigurationDecorator.class, ruleConfig.getClass()).map(optional -> optional.decorate(database.getName(),
                database.getResourceMetaData().getStorageUnits().entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)),
                database.getRuleMetaData().getRules(), ruleConfig)).orElse(ruleConfig);
    }
}
