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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.resource;

import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.resource.ResourceDefinitionViolationException;
import org.apache.shardingsphere.infra.distsql.exception.resource.ResourceInUsedException;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
    public ResponseHeader execute(final String schemaName, final DropResourceStatement sqlStatement) throws ResourceDefinitionViolationException {
        Collection<String> toBeDroppedResourceNames = sqlStatement.getNames();
        check(schemaName, toBeDroppedResourceNames);
        drop(schemaName, toBeDroppedResourceNames);
        // TODO update meta data context in memory
        ProxyContext.getInstance().getContextManager().getMetaDataContexts().getDistMetaDataPersistService().ifPresent(
            optional -> optional.getDataSourceService().drop(schemaName, toBeDroppedResourceNames));
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final String schemaName, final Collection<String> toBeDroppedResourceNames) throws RequiredResourceMissedException, ResourceInUsedException {
        checkResourceNameExisted(schemaName, toBeDroppedResourceNames);
        checkResourceNameNotInUse(schemaName, toBeDroppedResourceNames);
    }
    
    private void checkResourceNameExisted(final String schemaName, final Collection<String> resourceNames) throws RequiredResourceMissedException {
        Map<String, DataSource> resources = ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources();
        Collection<String> notExistedResourceNames = resourceNames.stream().filter(each -> !resources.containsKey(each)).collect(Collectors.toList());
        if (!notExistedResourceNames.isEmpty()) {
            throw new RequiredResourceMissedException(schemaName, notExistedResourceNames);
        }
    }
    
    private void checkResourceNameNotInUse(final String schemaName, final Collection<String> toBeDroppedResourceNames) throws ResourceInUsedException {
        Collection<String> inUsedResourceNames = getInUsedResourceNames(schemaName);
        inUsedResourceNames.retainAll(toBeDroppedResourceNames);
        if (!inUsedResourceNames.isEmpty()) {
            throw new ResourceInUsedException(inUsedResourceNames);
        }
    }
    
    private Collection<String> getInUsedResourceNames(final String schemaName) {
        for (ShardingSphereRule each : ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getRules()) {
            if (each instanceof DataSourceContainedRule) {
                return getInUsedResourceNames((DataSourceContainedRule) each);
            }
            if (each instanceof DataNodeContainedRule) {
                return getInUsedResourceNames((DataNodeContainedRule) each);
            }
        }
        return Collections.emptyList();
    }
    
    private Set<String> getInUsedResourceNames(final DataSourceContainedRule rule) {
        Set<String> result = new HashSet<>();
        for (Collection<String> each : rule.getDataSourceMapper().values()) {
            result.addAll(each);
        }
        return result;
    }
    
    private Set<String> getInUsedResourceNames(final DataNodeContainedRule rule) {
        Set<String> result = new HashSet<>();
        for (Collection<DataNode> each : rule.getAllDataNodes().values()) {
            result.addAll(each.stream().map(DataNode::getDataSourceName).collect(Collectors.toList()));
        }
        return result;
    }
    
    private void drop(final String schemaName, final Collection<String> toBeDroppedResourceNames) {
        for (String each : toBeDroppedResourceNames) {
            ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources().remove(each);
        }
    }
}
