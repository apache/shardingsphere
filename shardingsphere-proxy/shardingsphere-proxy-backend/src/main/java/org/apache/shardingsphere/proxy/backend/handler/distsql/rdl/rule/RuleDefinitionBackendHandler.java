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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.preprocess.RuleDefinitionAlterPreprocessor;
import org.apache.shardingsphere.infra.distsql.preprocess.RuleDefinitionAlterPreprocessorFactory;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionUpdater;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionUpdaterFactory;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.version.MetadataVersionPreparedEvent;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.DatabaseRequiredBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Rule definition backend handler.
 *
 * @param <T> type of SQL statement
 */
@Slf4j
public final class RuleDefinitionBackendHandler<T extends RuleDefinitionStatement> extends DatabaseRequiredBackendHandler<T> {
    
    public RuleDefinitionBackendHandler(final T sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected ResponseHeader execute(final String databaseName, final T sqlStatement) throws DistSQLException {
        RuleDefinitionUpdater ruleDefinitionUpdater = RuleDefinitionUpdaterFactory.getInstance(sqlStatement);
        Class<? extends RuleConfiguration> ruleConfigClass = ruleDefinitionUpdater.getRuleConfigurationClass();
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(databaseName);
        RuleConfiguration currentRuleConfig = findCurrentRuleConfiguration(database, ruleConfigClass).orElse(null);
        ruleDefinitionUpdater.checkSQLStatement(database, sqlStatement, currentRuleConfig);
        Optional<RuleDefinitionAlterPreprocessor> preprocessor = RuleDefinitionAlterPreprocessorFactory.findInstance(sqlStatement);
        if (preprocessor.isPresent()) {
            prepareScaling(database, sqlStatement, (RuleDefinitionAlterUpdater) ruleDefinitionUpdater, currentRuleConfig, preprocessor.get());
            return new UpdateResponseHeader(sqlStatement);
        }
        if (getRefreshStatus(sqlStatement, currentRuleConfig, ruleDefinitionUpdater)) {
            Collection<RuleConfiguration> alteredConfigs = processSQLStatement(database, sqlStatement, ruleDefinitionUpdater, currentRuleConfig);
            persistRuleConfigurationChange(databaseName, alteredConfigs);
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
    private Collection<RuleConfiguration> processSQLStatement(final ShardingSphereDatabase database,
                                                              final T sqlStatement, final RuleDefinitionUpdater updater, final RuleConfiguration currentRuleConfig) {
        Collection<RuleConfiguration> result = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        if (updater instanceof RuleDefinitionCreateUpdater) {
            if (null != currentRuleConfig) {
                result.remove(currentRuleConfig);
            }
            result.add(processCreate(sqlStatement, (RuleDefinitionCreateUpdater) updater, currentRuleConfig));
        } else if (updater instanceof RuleDefinitionAlterUpdater) {
            result.remove(currentRuleConfig);
            result.add(processAlter(sqlStatement, (RuleDefinitionAlterUpdater) updater, currentRuleConfig));
        } else if (updater instanceof RuleDefinitionDropUpdater) {
            processDrop(result, sqlStatement, (RuleDefinitionDropUpdater) updater, currentRuleConfig);
        } else {
            throw new UnsupportedOperationException(String.format("Cannot support RDL updater type `%s`", updater.getClass().getCanonicalName()));
        }
        ProxyContext.getInstance().getContextManager().alterRuleConfiguration(database.getName(), result);
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private RuleConfiguration processCreate(final T sqlStatement, final RuleDefinitionCreateUpdater updater, final RuleConfiguration currentRuleConfig) {
        RuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(sqlStatement);
        if (null == currentRuleConfig) {
            return toBeCreatedRuleConfig;
        }
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        return currentRuleConfig;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private RuleConfiguration processAlter(final T sqlStatement, final RuleDefinitionAlterUpdater updater, final RuleConfiguration currentRuleConfig) {
        RuleConfiguration toBeAlteredRuleConfig = updater.buildToBeAlteredRuleConfiguration(sqlStatement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        return currentRuleConfig;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processDrop(final Collection<RuleConfiguration> configs, final T sqlStatement, final RuleDefinitionDropUpdater updater, final RuleConfiguration currentRuleConfig) {
        if (!updater.hasAnyOneToBeDropped(sqlStatement, currentRuleConfig)) {
            return;
        }
        if (updater.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig)) {
            configs.remove(currentRuleConfig);
        }
    }
    
    private void prepareScaling(final ShardingSphereDatabase database, final T sqlStatement, final RuleDefinitionAlterUpdater<?, ?> updater, final RuleConfiguration currentRuleConfig,
                                final RuleDefinitionAlterPreprocessor<?> preprocessor) {
        MetaDataPersistService persistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getPersistService();
        Optional<String> newVersion = persistService.getMetaDataVersionPersistService().createNewVersion(database.getName());
        if (!newVersion.isPresent()) {
            throw new RuntimeException(String.format("Unable to get a new version for database: %s", database.getName()));
        }
        persistRuleConfigurationChange(persistService, newVersion.get(), database, currentRuleConfig, getAlteredRuleConfig(sqlStatement, updater, currentRuleConfig, preprocessor));
    }
    
    private void persistRuleConfigurationChange(final MetaDataPersistService persistService, final String version, final ShardingSphereDatabase database,
                                                final RuleConfiguration currentRuleConfig, final RuleConfiguration alteredRuleConfig) {
        Collection<RuleConfiguration> configs = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        configs.remove(currentRuleConfig);
        configs.add(alteredRuleConfig);
        persistService.getDatabaseRulePersistService().persist(database.getName(), version, configs);
        ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext().post(new MetadataVersionPreparedEvent(version, database.getName()));
    }
    
    private void persistRuleConfigurationChange(final String databaseName, final Collection<RuleConfiguration> alteredConfigs) {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        metaDataContexts.getPersistService().getDatabaseRulePersistService().persist(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), alteredConfigs);
    }
    
    private RuleConfiguration getAlteredRuleConfig(final T sqlStatement, final RuleDefinitionAlterUpdater updater,
                                                   final RuleConfiguration currentRuleConfig, final RuleDefinitionAlterPreprocessor preprocessor) {
        RuleConfiguration toBeAlteredRuleConfig = updater.buildToBeAlteredRuleConfiguration(sqlStatement);
        RuleConfiguration result = preprocessor.preprocess(currentRuleConfig, toBeAlteredRuleConfig);
        updater.updateCurrentRuleConfiguration(result, toBeAlteredRuleConfig);
        return result;
    }
    
    private boolean getRefreshStatus(final SQLStatement sqlStatement, final RuleConfiguration currentRuleConfig, final RuleDefinitionUpdater<?, ?> updater) {
        if (updater instanceof RuleDefinitionDropUpdater) {
            return ((RuleDefinitionDropUpdater) updater).hasAnyOneToBeDropped(sqlStatement, currentRuleConfig);
        }
        return true;
    }
}
