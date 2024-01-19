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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule;

import org.apache.shardingsphere.distsql.handler.type.rdl.database.DatabaseRuleRDLAlterExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.database.DatabaseRuleRDLCreateExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.database.DatabaseRuleRDLDropExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.database.DatabaseRuleRDLExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.decorator.RuleConfigurationDecorator;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.rule.identifier.type.StaticDataSourceContainedRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.NewGlobalRuleRDLBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.GlobalRuleRDLBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.RDLBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.update.DropReadwriteSplittingRuleExecutor;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.single.distsql.statement.rdl.LoadSingleTableStatement;
import org.apache.shardingsphere.single.distsql.statement.rdl.SetDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.single.distsql.statement.rdl.UnloadSingleTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TODO Rename to RuleDefinitionBackendHandler when metadata structure adjustment completed. #25485
 * Rule definition backend handler.
 *
 * @param <T> type of rule definition statement
 */
public final class NewRuleDefinitionBackendHandler<T extends RuleDefinitionStatement> extends RDLBackendHandler<T> {
    
    public NewRuleDefinitionBackendHandler(final T sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession);
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    protected ResponseHeader execute(final ShardingSphereDatabase database, final T sqlStatement) {
        Optional<DatabaseRuleRDLExecutor> executor = TypedSPILoader.findService(DatabaseRuleRDLExecutor.class, sqlStatement.getClass());
        if (executor.isPresent()) {
            execute(database, sqlStatement, executor.get());
            return new UpdateResponseHeader(sqlStatement);
        }
        String modeType = ProxyContext.getInstance().getContextManager().getInstanceContext().getModeConfiguration().getType();
        return "Cluster".equals(modeType) || "Standalone".equals(modeType) ? new NewGlobalRuleRDLBackendHandler(sqlStatement).execute() : new GlobalRuleRDLBackendHandler(sqlStatement).execute();
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void execute(final ShardingSphereDatabase database, final T sqlStatement, final DatabaseRuleRDLExecutor executor) {
        Class<? extends RuleConfiguration> ruleConfigClass = executor.getRuleConfigurationClass();
        RuleConfiguration currentRuleConfig = findCurrentRuleConfiguration(database, ruleConfigClass).orElse(null);
        executor.checkSQLStatement(database, sqlStatement, currentRuleConfig);
        if (getRefreshStatus(sqlStatement, currentRuleConfig, executor)) {
            ProxyContext.getInstance().getContextManager().getMetaDataContexts().getPersistService().getMetaDataVersionPersistService().switchActiveVersion(
                    processSQLStatement(database, sqlStatement, executor, currentRuleConfig));
        }
    }
    
    private Optional<RuleConfiguration> findCurrentRuleConfiguration(final ShardingSphereDatabase database, final Class<? extends RuleConfiguration> ruleConfigClass) {
        return database.getRuleMetaData().getConfigurations().stream().filter(each -> ruleConfigClass.isAssignableFrom(each.getClass())).findFirst();
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean getRefreshStatus(final SQLStatement sqlStatement, final RuleConfiguration currentRuleConfig, final DatabaseRuleRDLExecutor<?, ?> executor) {
        return !(executor instanceof DatabaseRuleRDLDropExecutor) || ((DatabaseRuleRDLDropExecutor) executor).hasAnyOneToBeDropped(sqlStatement, currentRuleConfig);
    }
    
    @SuppressWarnings("rawtypes")
    private Collection<MetaDataVersion> processSQLStatement(final ShardingSphereDatabase database,
                                                            final T sqlStatement, final DatabaseRuleRDLExecutor executor, final RuleConfiguration currentRuleConfig) {
        if (executor instanceof DatabaseRuleRDLCreateExecutor) {
            return processCreate(database, sqlStatement, (DatabaseRuleRDLCreateExecutor) executor, currentRuleConfig);
        }
        if (executor instanceof DatabaseRuleRDLAlterExecutor) {
            return processAlter(database, sqlStatement, (DatabaseRuleRDLAlterExecutor) executor, currentRuleConfig);
        }
        if (executor instanceof DatabaseRuleRDLDropExecutor) {
            return processDrop(database, sqlStatement, (DatabaseRuleRDLDropExecutor) executor, currentRuleConfig);
        }
        throw new UnsupportedSQLOperationException(String.format("Cannot support RDL executor type `%s`", executor.getClass().getName()));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Collection<MetaDataVersion> processCreate(final ShardingSphereDatabase database, final T sqlStatement, final DatabaseRuleRDLCreateExecutor executor,
                                                      final RuleConfiguration currentRuleConfig) {
        RuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(currentRuleConfig, sqlStatement);
        if (null != currentRuleConfig) {
            executor.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        }
        if (sqlStatement instanceof LoadSingleTableStatement || sqlStatement instanceof SetDefaultSingleTableStorageUnitStatement) {
            return ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().alterRuleConfiguration(database.getName(),
                    null == currentRuleConfig ? decorateRuleConfiguration(database, toBeCreatedRuleConfig) : decorateRuleConfiguration(database, currentRuleConfig));
        }
        return ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().alterRuleConfiguration(database.getName(), toBeCreatedRuleConfig);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Collection<MetaDataVersion> processAlter(final ShardingSphereDatabase database,
                                                     final T sqlStatement, final DatabaseRuleRDLAlterExecutor executor, final RuleConfiguration currentRuleConfig) {
        RuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        executor.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        if (sqlStatement instanceof UnloadSingleTableStatement) {
            return ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager()
                    .alterRuleConfiguration(database.getName(), decorateRuleConfiguration(database, currentRuleConfig));
        }
        RuleConfiguration toBeDroppedRuleConfig = executor.buildToBeDroppedRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().removeRuleConfigurationItem(database.getName(), toBeDroppedRuleConfig);
        return ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().alterRuleConfiguration(database.getName(), toBeAlteredRuleConfig);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Collection<MetaDataVersion> processDrop(final ShardingSphereDatabase database,
                                                    final T sqlStatement, final DatabaseRuleRDLDropExecutor executor, final RuleConfiguration currentRuleConfig) {
        if (!executor.hasAnyOneToBeDropped(sqlStatement, currentRuleConfig)) {
            return Collections.emptyList();
        }
        ModeContextManager modeContextManager = ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager();
        RuleConfiguration toBeDroppedRuleConfig = executor.buildToBeDroppedRuleConfiguration(currentRuleConfig, sqlStatement);
        // TODO remove updateCurrentRuleConfiguration after update refactor completed.
        if (executor.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig) && ((DatabaseRuleConfiguration) currentRuleConfig).isEmpty()) {
            modeContextManager.removeRuleConfigurationItem(database.getName(), toBeDroppedRuleConfig);
            new NewYamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(Collections.singleton(currentRuleConfig)).values().stream().findFirst()
                    .ifPresent(swapper -> modeContextManager.removeRuleConfiguration(database.getName(), swapper.getRuleTagName().toLowerCase()));
            return Collections.emptyList();
        }
        if (executor instanceof DropReadwriteSplittingRuleExecutor) {
            database.getRuleMetaData().findSingleRule(StaticDataSourceContainedRule.class)
                    .ifPresent(optional -> ((DropReadwriteSplittingRuleStatement) sqlStatement).getNames().forEach(optional::cleanStorageNodeDataSource));
            // TODO refactor to new metadata refresh way
        }
        modeContextManager.removeRuleConfigurationItem(database.getName(), toBeDroppedRuleConfig);
        RuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(currentRuleConfig, sqlStatement);
        return modeContextManager.alterRuleConfiguration(database.getName(), toBeAlteredRuleConfig);
    }
    
    @SuppressWarnings("unchecked")
    private RuleConfiguration decorateRuleConfiguration(final ShardingSphereDatabase database, final RuleConfiguration ruleConfig) {
        return TypedSPILoader.findService(RuleConfigurationDecorator.class, ruleConfig.getClass()).map(optional -> optional.decorate(database.getName(),
                database.getResourceMetaData().getStorageUnits().entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)),
                database.getRuleMetaData().getRules(), ruleConfig)).orElse(ruleConfig);
    }
}
