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

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobWorker;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.preprocess.RuleDefinitionAlterPreprocessor;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionUpdater;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.version.SchemaVersionPreparedEvent;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingAlgorithmStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.typed.TypedSPIRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

/**
 * Rule definition backend handler.
 *
 * @param <T> type of SQL statement
 */
@Slf4j
public final class RuleDefinitionBackendHandler<T extends RuleDefinitionStatement> extends SchemaRequiredBackendHandler<T> {
    
    static {
        ShardingSphereServiceLoader.register(RuleDefinitionUpdater.class);
        ShardingSphereServiceLoader.register(RuleDefinitionAlterPreprocessor.class);
    }
    
    private static final Set<String> RULE_ALTERED_ACTION_LIST = Sets.newHashSet(AlterShardingTableRuleStatement.class.getName(), AlterShardingAlgorithmStatement.class.getName(),
            AlterDefaultShardingStrategyStatement.class.getName(), AlterShardingBindingTableRulesStatement.class.getName(), AlterShardingBroadcastTableRulesStatement.class.getName());
    
    public RuleDefinitionBackendHandler(final T sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected ResponseHeader execute(final String schemaName, final T sqlStatement) throws DistSQLException {
        RuleDefinitionUpdater ruleDefinitionUpdater = TypedSPIRegistry.getRegisteredService(RuleDefinitionUpdater.class, sqlStatement.getClass().getCanonicalName());
        Class<? extends RuleConfiguration> ruleConfigClass = ruleDefinitionUpdater.getRuleConfigurationClass();
        ShardingSphereMetaData shardingSphereMetaData = ProxyContext.getInstance().getMetaData(schemaName);
        RuleConfiguration currentRuleConfig = findCurrentRuleConfiguration(shardingSphereMetaData, ruleConfigClass).orElse(null);
        ruleDefinitionUpdater.checkSQLStatement(shardingSphereMetaData, sqlStatement, currentRuleConfig);
        Optional<RuleDefinitionAlterPreprocessor> preprocessor = TypedSPIRegistry.findRegisteredService(RuleDefinitionAlterPreprocessor.class, sqlStatement.getClass().getCanonicalName());
        if (!RuleAlteredJobWorker.isOnRuleAlteredActionEnabled(currentRuleConfig)) {
            if (RULE_ALTERED_ACTION_LIST.contains(sqlStatement.getClass().getCanonicalName())) {
                // TODO throw new RuntimeException("scaling is not enabled");
                log.warn("rule altered and scaling is not enabled.");
            }
        } else if (preprocessor.isPresent()) {
            prepareScaling(shardingSphereMetaData, sqlStatement, (RuleDefinitionAlterUpdater) ruleDefinitionUpdater, currentRuleConfig, preprocessor.get());
            return new UpdateResponseHeader(sqlStatement);
        }
        if (getRefreshStatus(sqlStatement, currentRuleConfig, ruleDefinitionUpdater)) {
            processSQLStatement(shardingSphereMetaData, sqlStatement, ruleDefinitionUpdater, currentRuleConfig);
            persistRuleConfigurationChange(shardingSphereMetaData);
        }
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
        ProxyContext.getInstance().getContextManager().alterRuleConfiguration(shardingSphereMetaData.getName(), shardingSphereMetaData.getRuleMetaData().getConfigurations());
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
        if (!updater.hasAnyOneToBeDropped(sqlStatement, currentRuleConfig)) {
            return;
        }
        if (updater.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig)) {
            shardingSphereMetaData.getRuleMetaData().getConfigurations().remove(currentRuleConfig);
        }
    }
    
    private void prepareScaling(final ShardingSphereMetaData shardingSphereMetaData, final T sqlStatement, final RuleDefinitionAlterUpdater updater, final RuleConfiguration currentRuleConfig,
                              final RuleDefinitionAlterPreprocessor preprocessor) {
        Optional<MetaDataPersistService> metaDataPersistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService();
        if (metaDataPersistService.isPresent()) {
            Optional<String> newVersion = metaDataPersistService.get().getSchemaVersionPersistService().createNewVersion(shardingSphereMetaData.getName());
            if (!newVersion.isPresent()) {
                throw new RuntimeException(String.format("Unable to get a new version for schema: %s", shardingSphereMetaData.getName()));
            }
            metaDataPersistService.get().getSchemaRuleService().persist(shardingSphereMetaData.getName(), newVersion.get(), buildAlteredRuleConfigurations(shardingSphereMetaData, sqlStatement, 
                    updater, currentRuleConfig, preprocessor));
            ShardingSphereEventBus.getInstance().post(new SchemaVersionPreparedEvent(newVersion.get(), shardingSphereMetaData.getName()));
        }
    }
    
    private void persistRuleConfigurationChange(final ShardingSphereMetaData shardingSphereMetaData) {
        ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService().ifPresent(optional -> optional.getSchemaRuleService().persist(
                shardingSphereMetaData.getName(), shardingSphereMetaData.getRuleMetaData().getConfigurations()));
    }
    
    private Collection<RuleConfiguration> buildAlteredRuleConfigurations(final ShardingSphereMetaData shardingSphereMetaData, final T sqlStatement, final RuleDefinitionAlterUpdater updater, 
                                                final RuleConfiguration currentRuleConfig, 
                                                final RuleDefinitionAlterPreprocessor preprocessor) {
        RuleConfiguration toBeAlteredRuleConfig = updater.buildToBeAlteredRuleConfiguration(sqlStatement);
        RuleConfiguration alteredRuleConfig = preprocessor.preprocess(currentRuleConfig, toBeAlteredRuleConfig);
        updater.updateCurrentRuleConfiguration(alteredRuleConfig, toBeAlteredRuleConfig);
        Collection<RuleConfiguration> result = new LinkedList<>(shardingSphereMetaData.getRuleMetaData().getConfigurations());
        result.remove(currentRuleConfig);
        result.add(alteredRuleConfig);
        return result;
    }
    
    private boolean getRefreshStatus(final SQLStatement sqlStatement, final RuleConfiguration currentRuleConfig, final RuleDefinitionUpdater updater) {
        if (updater instanceof RuleDefinitionDropUpdater) {
            return ((RuleDefinitionDropUpdater) updater).hasAnyOneToBeDropped(sqlStatement, currentRuleConfig);
        }
        return true;
    }
}
