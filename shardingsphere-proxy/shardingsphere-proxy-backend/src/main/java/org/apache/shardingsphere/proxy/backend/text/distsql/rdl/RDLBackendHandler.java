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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl;

import org.apache.shardingsphere.governance.core.registry.config.event.rule.RuleConfigurationsAlteredSQLNotificationEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.update.RDLAlterUpdater;
import org.apache.shardingsphere.infra.distsql.update.RDLCreateUpdater;
import org.apache.shardingsphere.infra.distsql.update.RDLDropUpdater;
import org.apache.shardingsphere.infra.distsql.update.RDLUpdater;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.exception.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Optional;
import java.util.Properties;

/**
 * RDL backend handler.
 *
 * @param <T> type of SQL statement
 */
public final class RDLBackendHandler<T extends SQLStatement> extends SchemaRequiredBackendHandler<T> {
    
    static {
        ShardingSphereServiceLoader.register(RDLUpdater.class);
    }
    
    public RDLBackendHandler(final T sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected ResponseHeader execute(final String schemaName, final T sqlStatement) throws ShardingSphereSQLException {
        RDLUpdater rdlUpdater = TypedSPIRegistry.getRegisteredService(RDLUpdater.class, sqlStatement.getClass().getCanonicalName(), new Properties());
        Class<? extends RuleConfiguration> ruleConfigClass = rdlUpdater.getRuleConfigurationClass();
        RuleConfiguration currentRuleConfig = findCurrentRuleConfiguration(schemaName, ruleConfigClass).orElse(null);
        rdlUpdater.checkSQLStatement(schemaName, sqlStatement, currentRuleConfig, ProxyContext.getInstance().getMetaData(schemaName).getResource());
        processSQLStatement(schemaName, sqlStatement, rdlUpdater, currentRuleConfig);
        postRuleConfigurationChange(schemaName);
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
    private void processSQLStatement(final String schemaName, final T sqlStatement, final RDLUpdater updater, final RuleConfiguration currentRuleConfig) throws RuleDefinitionViolationException {
        if (updater instanceof RDLCreateUpdater) {
            processCreate(schemaName, sqlStatement, (RDLCreateUpdater) updater, currentRuleConfig);
        } else if (updater instanceof RDLAlterUpdater) {
            processAlter(schemaName, sqlStatement, (RDLAlterUpdater) updater, currentRuleConfig);
        } else if (updater instanceof RDLDropUpdater) {
            processDrop(schemaName, sqlStatement, (RDLDropUpdater) updater, currentRuleConfig);
        } else {
            throw new UnsupportedOperationException(String.format("Cannot support RDLUpdater type `%s`", updater.getClass().getCanonicalName()));
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processCreate(final String schemaName, final T sqlStatement, final RDLCreateUpdater updater, final RuleConfiguration currentRuleConfig) throws RuleDefinitionViolationException {
        RuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(schemaName, sqlStatement);
        updater.updateCurrentRuleConfiguration(schemaName, sqlStatement, currentRuleConfig, toBeCreatedRuleConfig);
        if (null == currentRuleConfig && null != toBeCreatedRuleConfig) {
            ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().add(toBeCreatedRuleConfig);
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processAlter(final String schemaName, final T sqlStatement, final RDLAlterUpdater updater, final RuleConfiguration currentRuleConfig) {
        updater.updateCurrentRuleConfiguration(schemaName, sqlStatement, currentRuleConfig);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processDrop(final String schemaName, final T sqlStatement, final RDLDropUpdater rdlUpdater, final RuleConfiguration currentRuleConfig) {
        if (rdlUpdater.updateCurrentRuleConfiguration(schemaName, sqlStatement, currentRuleConfig)) {
            ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().remove(currentRuleConfig);
        }
    }
    
    private void postRuleConfigurationChange(final String schemaName) {
        ShardingSphereEventBus.getInstance().post(
                new RuleConfigurationsAlteredSQLNotificationEvent(schemaName, ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations()));
    }
}
