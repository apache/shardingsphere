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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.governance.core.registry.config.event.rule.RuleConfigurationsAlteredSQLNotificationEvent;
import org.apache.shardingsphere.infra.config.scope.SchemaRuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RDL backend handler.
 *
 * @param <T> type of SQL statement
 */
public abstract class RDLBackendHandler<T extends SQLStatement> extends SchemaRequiredBackendHandler<T> {
    
    public RDLBackendHandler(final T sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    protected final ResponseHeader execute(final String schemaName, final T sqlStatement) {
        check(schemaName, sqlStatement);
        doExecute(schemaName, sqlStatement);
        postChange(schemaName);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    protected abstract void check(String schemaName, T sqlStatement);
    
    protected abstract void doExecute(String schemaName, T sqlStatement);
    
    private void postChange(final String schemaName) {
        ShardingSphereEventBus.getInstance().post(
                new RuleConfigurationsAlteredSQLNotificationEvent(schemaName, ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations()));
    }
    
    @SuppressWarnings("unchecked")
    protected final <R extends SchemaRuleConfiguration> Optional<R> findCurrentRuleConfiguration(final String schemaName, final Class<R> configRuleClass) {
        return ProxyContext.getInstance().getMetaData(schemaName)
                .getRuleMetaData().getConfigurations().stream().filter(each -> configRuleClass.isAssignableFrom(each.getClass())).map(each -> (R) each).findFirst();
    }
    
    protected final <R extends SchemaRuleConfiguration> R getCurrentRuleConfiguration(final String schemaName, final Class<R> configRuleClass) {
        Optional<R> result = findCurrentRuleConfiguration(schemaName, configRuleClass);
        Preconditions.checkState(result.isPresent(), "Can not find rule type: `%s`.", configRuleClass);
        return result.get();
    }
    
    protected final Collection<String> getNotExistedResources(final String schemaName, final Collection<String> resourceNames) {
        return resourceNames.stream().filter(each -> !isExistedResource(schemaName, each)).collect(Collectors.toSet());
    }
    
    private boolean isExistedResource(final String schemaName, final String resourceName) {
        ShardingSphereResource resource = ProxyContext.getInstance().getMetaData(schemaName).getResource();
        return null != resource && resource.getDataSources().containsKey(resourceName);
    }
}
