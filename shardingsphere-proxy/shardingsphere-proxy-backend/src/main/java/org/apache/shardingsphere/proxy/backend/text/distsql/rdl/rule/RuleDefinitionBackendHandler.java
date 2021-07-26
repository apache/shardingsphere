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
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionUpdater;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;

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
    }
    
    public RuleDefinitionBackendHandler(final T sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected ResponseHeader execute(final String schemaName, final T sqlStatement) throws DistSQLException {
        RuleDefinitionUpdater ruleDefinitionUpdater = TypedSPIRegistry.getRegisteredService(RuleDefinitionUpdater.class, sqlStatement.getClass().getCanonicalName(), new Properties());
        Class<? extends RuleConfiguration> ruleConfigClass = ruleDefinitionUpdater.getRuleConfigurationClass();
        RuleConfiguration currentRuleConfig = findCurrentRuleConfiguration(schemaName, ruleConfigClass).orElse(null);
        ruleDefinitionUpdater.checkSQLStatement(schemaName, sqlStatement, currentRuleConfig, ProxyContext.getInstance().getMetaData(schemaName).getResource());
        processSQLStatement(schemaName, sqlStatement, ruleDefinitionUpdater, currentRuleConfig);
        persistRuleConfigurationChange(schemaName);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private Optional<RuleConfiguration> findCurrentRuleConfiguration(final String schemaName, final Class<? extends RuleConfiguration> ruleConfigClass) {
        for (RuleConfiguration each : ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations()) {
            if (ruleConfigClass.isAssignableFrom(each.getClass())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    @SuppressWarnings("rawtypes")
    private void processSQLStatement(final String schemaName, final T sqlStatement, final RuleDefinitionUpdater updater, final RuleConfiguration currentRuleConfig) {
        if (updater instanceof RuleDefinitionCreateUpdater) {
            processCreate(schemaName, sqlStatement, (RuleDefinitionCreateUpdater) updater, currentRuleConfig);
        } else if (updater instanceof RuleDefinitionAlterUpdater) {
            processAlter(sqlStatement, (RuleDefinitionAlterUpdater) updater, currentRuleConfig);
        } else if (updater instanceof RuleDefinitionDropUpdater) {
            processDrop(schemaName, sqlStatement, (RuleDefinitionDropUpdater) updater, currentRuleConfig);
        } else {
            throw new UnsupportedOperationException(String.format("Cannot support RDL updater type `%s`", updater.getClass().getCanonicalName()));
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processCreate(final String schemaName, final T sqlStatement, final RuleDefinitionCreateUpdater updater, final RuleConfiguration currentRuleConfig) {
        RuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(sqlStatement);
        if (null == currentRuleConfig) {
            ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().add(toBeCreatedRuleConfig);
        } else {
            updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processAlter(final T sqlStatement, final RuleDefinitionAlterUpdater updater, final RuleConfiguration currentRuleConfig) {
        RuleConfiguration toBeAlteredRuleConfig = updater.buildToBeAlteredRuleConfiguration(sqlStatement);
        updater.updateCurrentRuleConfiguration(toBeAlteredRuleConfig, currentRuleConfig);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processDrop(final String schemaName, final T sqlStatement, final RuleDefinitionDropUpdater updater, final RuleConfiguration currentRuleConfig) {
        if (updater.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig)) {
            ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().remove(currentRuleConfig);
        }
    }
    
    private void persistRuleConfigurationChange(final String schemaName) {
        ProxyContext.getInstance().getMetaDataContexts().getDistMetaDataPersistService().getSchemaRuleService().persist(
                schemaName, ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations());
    }
}
