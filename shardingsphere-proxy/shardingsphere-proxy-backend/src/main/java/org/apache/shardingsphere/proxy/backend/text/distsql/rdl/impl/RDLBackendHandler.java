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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl;

import org.apache.shardingsphere.governance.core.registry.config.event.rule.RuleConfigurationsAlteredSQLNotificationEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.scope.SchemaRuleConfiguration;
import org.apache.shardingsphere.infra.distsql.RDLUpdater;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.Properties;

/**
 * RDL backend handler.
 *
 * @param <T> type of SQL statement
 * @param <R> type of rule configuration
 */
public abstract class RDLBackendHandler<T extends SQLStatement, R extends SchemaRuleConfiguration> extends SchemaRequiredBackendHandler<T> {
    
    static {
        ShardingSphereServiceLoader.register(RDLUpdater.class);
    }
    
    public RDLBackendHandler(final T sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected final ResponseHeader execute(final String schemaName, final T sqlStatement) {
        R currentRuleConfig = findCurrentRuleConfiguration(schemaName).orElse(null);
        RDLUpdater rdlUpdater = TypedSPIRegistry.getRegisteredService(RDLUpdater.class, sqlStatement.getClass().getCanonicalName(), new Properties());
        rdlUpdater.checkSQLStatement(schemaName, sqlStatement, currentRuleConfig, ProxyContext.getInstance().getMetaData(schemaName).getResource());
        if (rdlUpdater.updateCurrentRuleConfiguration(schemaName, sqlStatement, currentRuleConfig)) {
            ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().remove(currentRuleConfig);
        }
        postRuleConfigurationChange(schemaName);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    @SuppressWarnings("unchecked")
    private Optional<R> findCurrentRuleConfiguration(final String schemaName) {
        Class<R> ruleConfigClass = (Class<R>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        for (RuleConfiguration each : ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations()) {
            if (ruleConfigClass.isAssignableFrom(each.getClass())) {
                return Optional.of((R) each);
            }
        }
        return Optional.empty();
    }
    
    protected abstract void checkSQLStatement(String schemaName, T sqlStatement, R currentRuleConfig);
    
    protected abstract void updateCurrentRuleConfiguration(String schemaName, T sqlStatement, R currentRuleConfig);
    
    private void postRuleConfigurationChange(final String schemaName) {
        ShardingSphereEventBus.getInstance().post(
                new RuleConfigurationsAlteredSQLNotificationEvent(schemaName, ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations()));
    }
}
