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

import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionUpdater;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.decorator.RuleConfigurationDecorator;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.rule.identifier.type.StaticDataSourceContainedRule;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.RDLBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.update.DropReadwriteSplittingRuleStatementUpdater;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.single.distsql.statement.rdl.LoadSingleTableStatement;
import org.apache.shardingsphere.single.distsql.statement.rdl.SetDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.single.distsql.statement.rdl.UnloadSingleTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

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
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected ResponseHeader execute(final String databaseName, final T sqlStatement) {
        RuleDefinitionUpdater ruleDefinitionUpdater = TypedSPILoader.getService(RuleDefinitionUpdater.class, sqlStatement.getClass().getName());
        Class<? extends RuleConfiguration> ruleConfigClass = ruleDefinitionUpdater.getRuleConfigurationClass();
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(databaseName);
        RuleConfiguration currentRuleConfig = findCurrentRuleConfiguration(database, ruleConfigClass).orElse(null);
        ruleDefinitionUpdater.checkSQLStatement(database, sqlStatement, currentRuleConfig);
        if (getRefreshStatus(sqlStatement, currentRuleConfig, ruleDefinitionUpdater)) {
            ProxyContext.getInstance().getContextManager().getMetaDataContexts().getPersistService().getMetaDataVersionPersistService().switchActiveVersion(
                    processSQLStatement(database, sqlStatement, ruleDefinitionUpdater, currentRuleConfig));
        }
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private Optional<RuleConfiguration> findCurrentRuleConfiguration(final ShardingSphereDatabase database, final Class<? extends RuleConfiguration> ruleConfigClass) {
        for (RuleConfiguration each : database.getRuleMetaData().getConfigurations()) {
            if (ruleConfigClass.isAssignableFrom(each.getClass())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    @SuppressWarnings("rawtypes")
    private Collection<MetaDataVersion> processSQLStatement(final ShardingSphereDatabase database,
                                                            final T sqlStatement, final RuleDefinitionUpdater updater, final RuleConfiguration currentRuleConfig) {
        if (updater instanceof RuleDefinitionCreateUpdater) {
            return processCreate(database, sqlStatement, (RuleDefinitionCreateUpdater) updater, currentRuleConfig);
        }
        if (updater instanceof RuleDefinitionAlterUpdater) {
            return processAlter(database, sqlStatement, (RuleDefinitionAlterUpdater) updater, currentRuleConfig);
        }
        if (updater instanceof RuleDefinitionDropUpdater) {
            return processDrop(database, sqlStatement, (RuleDefinitionDropUpdater) updater, currentRuleConfig);
        }
        throw new UnsupportedSQLOperationException(String.format("Cannot support RDL updater type `%s`", updater.getClass().getName()));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Collection<MetaDataVersion> processCreate(final ShardingSphereDatabase database, final T sqlStatement, final RuleDefinitionCreateUpdater updater,
                                                      final RuleConfiguration currentRuleConfig) {
        RuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentRuleConfig, sqlStatement);
        if (null != currentRuleConfig) {
            updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        }
        if (sqlStatement instanceof LoadSingleTableStatement || sqlStatement instanceof SetDefaultSingleTableStorageUnitStatement) {
            return ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().alterRuleConfiguration(database.getName(),
                    null == currentRuleConfig ? decorateRuleConfiguration(database, toBeCreatedRuleConfig) : decorateRuleConfiguration(database, currentRuleConfig));
        }
        return ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().alterRuleConfiguration(database.getName(), toBeCreatedRuleConfig);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Collection<MetaDataVersion> processAlter(final ShardingSphereDatabase database, final T sqlStatement, final RuleDefinitionAlterUpdater updater, final RuleConfiguration currentRuleConfig) {
        RuleConfiguration toBeAlteredRuleConfig = updater.buildToBeAlteredRuleConfiguration(sqlStatement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        if (sqlStatement instanceof UnloadSingleTableStatement) {
            return ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager()
                    .alterRuleConfiguration(database.getName(), decorateRuleConfiguration(database, currentRuleConfig));
        }
        RuleConfiguration toBeDroppedRuleConfig = updater.buildToBeDroppedRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().removeRuleConfigurationItem(database.getName(), toBeDroppedRuleConfig);
        return ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().alterRuleConfiguration(database.getName(), toBeAlteredRuleConfig);
    }
    
    @SuppressWarnings("unchecked")
    private RuleConfiguration decorateRuleConfiguration(final ShardingSphereDatabase database, final RuleConfiguration ruleConfig) {
        if (TypedSPILoader.contains(RuleConfigurationDecorator.class, ruleConfig.getClass().getName())) {
            return TypedSPILoader.getService(RuleConfigurationDecorator.class, ruleConfig.getClass().getName()).decorate(database.getName(),
                    database.getResourceMetaData().getDataSources(), database.getRuleMetaData().getRules(), ruleConfig);
        }
        return ruleConfig;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Collection<MetaDataVersion> processDrop(final ShardingSphereDatabase database, final T sqlStatement, final RuleDefinitionDropUpdater updater, final RuleConfiguration currentRuleConfig) {
        if (!updater.hasAnyOneToBeDropped(sqlStatement, currentRuleConfig)) {
            return Collections.emptyList();
        }
        ModeContextManager modeContextManager = ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager();
        final RuleConfiguration toBeDroppedRuleConfig = updater.buildToBeDroppedRuleConfiguration(currentRuleConfig, sqlStatement);
        // TODO remove updateCurrentRuleConfiguration after update refactor completed.
        if (updater.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig) && ((DatabaseRuleConfiguration) currentRuleConfig).isEmpty()) {
            modeContextManager.removeRuleConfigurationItem(database.getName(), toBeDroppedRuleConfig);
            new NewYamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(Collections.singleton(currentRuleConfig)).values().stream().findFirst()
                    .ifPresent(swapper -> modeContextManager.removeRuleConfiguration(database.getName(), swapper.getRuleTagName().toLowerCase()));
            return Collections.emptyList();
        }
        if (updater instanceof DropReadwriteSplittingRuleStatementUpdater) {
            database.getRuleMetaData().findSingleRule(StaticDataSourceContainedRule.class)
                    .ifPresent(optional -> ((DropReadwriteSplittingRuleStatement) sqlStatement).getNames().forEach(optional::cleanStorageNodeDataSource));
            // TODO refactor to new metadata refresh way
        }
        modeContextManager.removeRuleConfigurationItem(database.getName(), toBeDroppedRuleConfig);
        final RuleConfiguration toBeAlteredRuleConfig = updater.buildToBeAlteredRuleConfiguration(currentRuleConfig, sqlStatement);
        return modeContextManager.alterRuleConfiguration(database.getName(), toBeAlteredRuleConfig);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean getRefreshStatus(final SQLStatement sqlStatement, final RuleConfiguration currentRuleConfig, final RuleDefinitionUpdater<?, ?> updater) {
        return !(updater instanceof RuleDefinitionDropUpdater) || ((RuleDefinitionDropUpdater) updater).hasAnyOneToBeDropped(sqlStatement, currentRuleConfig);
    }
}
