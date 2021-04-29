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

import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropResourceStatement;
import org.apache.shardingsphere.governance.core.registry.listener.event.datasource.DataSourceAlteredEvent;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.type.DataSourceContainedRule;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ResourceInUsedException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Drop resource backend handler.
 */
public final class DropResourceBackendHandler extends SchemaRequiredBackendHandler<DropResourceStatement> {

    public DropResourceBackendHandler(final DropResourceStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final DropResourceStatement sqlStatement) {
        Collection<String> resourceNames = sqlStatement.getResourceNames();
        check(schemaName, resourceNames);
        Map<String, DataSource> resourceMap = drop(schemaName, resourceNames);
        post(schemaName, resourceMap);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final String schemaName, final Collection<String> resourceNames) {
        Map<String, DataSource> resourceMap = ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources();
        if (null == resourceMap || resourceMap.isEmpty()) {
            throw new ResourceNotExistedException(resourceNames);
        }
        Collection<String> notExistedResourceNames = resourceNames.stream().filter(each -> !resourceMap.containsKey(each)).collect(Collectors.toList());
        if (!notExistedResourceNames.isEmpty()) {
            throw new ResourceNotExistedException(notExistedResourceNames);
        }
        Collection<ShardingSphereRule> ruleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getRules();
        Set<String> useResources = new HashSet<>();
        for (ShardingSphereRule each : ruleConfig) {
            if (each instanceof DataSourceContainedRule) {
                useResources = getResources((DataSourceContainedRule) each);
            } else if (each instanceof DataNodeContainedRule) {
                useResources = getResources((DataNodeContainedRule) each);
            }
        }
        Collection<String> conflictResources = new LinkedList<>();
        for (String each : resourceNames) {
            if (useResources.contains(each)) {
                conflictResources.add(each);
            }
        }
        if (!conflictResources.isEmpty()) {
            throw new ResourceInUsedException(conflictResources);
        }
    }

    private Set<String> getResources(final DataSourceContainedRule rule) {
        Set<String> result = new HashSet<>();
        for (Collection<String> each : rule.getDataSourceMapper().values()) {
            result.addAll(each);
        }
        return result;
    }

    private Set<String> getResources(final DataNodeContainedRule rule) {
        Set<String> result = new HashSet<>();
        for (Collection<DataNode> each : rule.getAllDataNodes().values()) {
            result.addAll(each.stream().map(DataNode::getDataSourceName).collect(Collectors.toList()));
        }
        return result;
    }

    private Map<String, DataSource> drop(final String schemaName, final Collection<String> resourceNames) {
        Map<String, DataSource> result = ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources();
        for (String each : resourceNames) {
            result.remove(each);
        }
        return result;
    }
    
    private void post(final String schemaName, final Map<String, DataSource> resourceMap) {
        Map<String, DataSourceConfiguration> datasourceMap = DataSourceConverter.getDataSourceConfigurationMap(resourceMap);
        ShardingSphereEventBus.getInstance().post(new DataSourceAlteredEvent(schemaName, datasourceMap));
    }
}
