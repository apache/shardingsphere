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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.rule;

import org.apache.shardingsphere.distsql.parser.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.preprocess.RuleDefinitionAlterPreprocessor;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;

/**
 * Rule definition backend handler.
 *
 * @param <T> type of SQL statement
 */
public final class RuleDefinitionBackendHandler<T extends RuleDefinitionStatement> extends SchemaRequiredBackendHandler<T> {
    
    static {
        ShardingSphereServiceLoader.register(RuleDefinitionUpdater.class);
        ShardingSphereServiceLoader.register(RuleDefinitionAlterPreprocessor.class);
    }
    
    public RuleDefinitionBackendHandler(final T sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected ResponseHeader execute(final String schemaName, final T sqlStatement) throws DistSQLException {
        RuleDefinitionUpdater ruleDefinitionUpdater = TypedSPIRegistry.getRegisteredService(RuleDefinitionUpdater.class, sqlStatement.getClass().getCanonicalName(), new Properties());
        Class<? extends RuleConfiguration> ruleConfigClass = ruleDefinitionUpdater.getRuleConfigurationClass();
        ShardingSphereMetaData shardingSphereMetaData = ProxyContext.getInstance().getMetaData(schemaName);
        RuleConfiguration currentRuleConfig = findCurrentRuleConfiguration(shardingSphereMetaData, ruleConfigClass).orElse(null);
        ruleDefinitionUpdater.checkSQLStatement(shardingSphereMetaData, sqlStatement, currentRuleConfig);
        Optional<RuleDefinitionAlterPreprocessor> preprocessor = TypedSPIRegistry.findRegisteredService(RuleDefinitionAlterPreprocessor.class, sqlStatement.getClass().getCanonicalName(), 
                new Properties());
        if (ProxyContext.getInstance().isScalingEnabled() && preprocessor.isPresent()) {
            processCache(shardingSphereMetaData, sqlStatement, (RuleDefinitionAlterUpdater) ruleDefinitionUpdater, currentRuleConfig, preprocessor.get());
            return new UpdateResponseHeader(sqlStatement);
        }
        processSQLStatement(shardingSphereMetaData, sqlStatement, ruleDefinitionUpdater, currentRuleConfig);
        persistRuleConfigurationChange(shardingSphereMetaData);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private Optional<RuleConfiguration> findCurrentRuleConfiguration(final ShardingSphereMetaData shardingSphereMetaData, final Class<? extends RuleConfiguration> ruleConfigClass) {
        for (RuleConfiguration each : shardingSphereMetaData.getRuleMetaData().getConfigurations()) {
            if (ruleConfigClass.isAssignableFrom(each.getClass())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    @SuppressWarnings("rawtypes")
    private void processSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final T sqlStatement, final RuleDefinitionUpdater updater, final RuleConfiguration currentRuleConfig) {
        if (updater instanceof RuleDefinitionCreateUpdater) {
            processCreate(shardingSphereMetaData, sqlStatement, (RuleDefinitionCreateUpdater) updater, currentRuleConfig);
        } else if (updater instanceof RuleDefinitionAlterUpdater) {
            processAlter(sqlStatement, (RuleDefinitionAlterUpdater) updater, currentRuleConfig);
        } else if (updater instanceof RuleDefinitionDropUpdater) {
            processDrop(shardingSphereMetaData, sqlStatement, (RuleDefinitionDropUpdater) updater, currentRuleConfig);
        } else {
            throw new UnsupportedOperationException(String.format("Cannot support RDL updater type `%s`", updater.getClass().getCanonicalName()));
        }
        ProxyContext.getInstance().getContextManager().alterRule(shardingSphereMetaData.getName());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processCreate(final ShardingSphereMetaData shardingSphereMetaData, final T sqlStatement, final RuleDefinitionCreateUpdater updater, final RuleConfiguration currentRuleConfig) {
        RuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(sqlStatement);
        if (null == currentRuleConfig) {
            shardingSphereMetaData.getRuleMetaData().getConfigurations().add(toBeCreatedRuleConfig);
        } else {
            updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processAlter(final T sqlStatement, final RuleDefinitionAlterUpdater updater, final RuleConfiguration currentRuleConfig) {
        RuleConfiguration toBeAlteredRuleConfig = updater.buildToBeAlteredRuleConfiguration(sqlStatement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processDrop(final ShardingSphereMetaData shardingSphereMetaData, final T sqlStatement, final RuleDefinitionDropUpdater updater, final RuleConfiguration currentRuleConfig) {
        if (updater.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig)) {
            shardingSphereMetaData.getRuleMetaData().getConfigurations().remove(currentRuleConfig);
        }
    }
    
    private void processCache(final ShardingSphereMetaData shardingSphereMetaData, final T sqlStatement, final RuleDefinitionAlterUpdater updater, final RuleConfiguration currentRuleConfig, 
                              final RuleDefinitionAlterPreprocessor preprocessor) {
        RuleConfiguration toBeAlteredRuleConfig = updater.buildToBeAlteredRuleConfiguration(sqlStatement);
        RuleConfiguration alteredRuleConfig = preprocessor.preprocess(currentRuleConfig, toBeAlteredRuleConfig);
        updater.updateCurrentRuleConfiguration(alteredRuleConfig, toBeAlteredRuleConfig);
        Collection<RuleConfiguration> alteredConfigs = new LinkedList<>(shardingSphereMetaData.getRuleMetaData().getConfigurations());
        alteredConfigs.remove(currentRuleConfig);
        alteredConfigs.add(alteredRuleConfig);
        cacheRuleConfigurationChange(shardingSphereMetaData.getName(), alteredConfigs);
    }
    
    private void persistRuleConfigurationChange(final ShardingSphereMetaData shardingSphereMetaData) {
        ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService().ifPresent(optional -> optional.getSchemaRuleService().persist(
                shardingSphereMetaData.getName(), shardingSphereMetaData.getRuleMetaData().getConfigurations()));
    }
    
    private void cacheRuleConfigurationChange(final String schemaName, final Collection<RuleConfiguration> ruleConfigurations) {
        ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService().ifPresent(optional -> optional.getSchemaRuleService().cache(
                schemaName, ruleConfigurations));
    }
}
