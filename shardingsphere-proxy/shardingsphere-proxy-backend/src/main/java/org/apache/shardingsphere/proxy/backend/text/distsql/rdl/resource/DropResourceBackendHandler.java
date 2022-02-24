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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.resource.ResourceDefinitionViolationException;
import org.apache.shardingsphere.infra.distsql.exception.resource.ResourceInUsedException;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Drop resource backend handler.
 */
public final class DropResourceBackendHandler extends SchemaRequiredBackendHandler<DropResourceStatement> {
    
    public DropResourceBackendHandler(final DropResourceStatement sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final DropResourceStatement sqlStatement) throws ResourceDefinitionViolationException {
        Collection<String> toBeDroppedResourceNames = sqlStatement.getNames();
        check(schemaName, toBeDroppedResourceNames, sqlStatement.isIgnoreSingleTables(), sqlStatement.isContainsExistClause());
        ProxyContext.getInstance().getContextManager().dropResource(schemaName, toBeDroppedResourceNames);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final String schemaName, final Collection<String> toBeDroppedResourceNames, 
                       final boolean ignoreSingleTables, final boolean allowNotExist) throws RequiredResourceMissedException, ResourceInUsedException {
        if (!allowNotExist) {
            checkResourceNameExisted(schemaName, toBeDroppedResourceNames);
        }
        checkResourceNameNotInUse(schemaName, toBeDroppedResourceNames, ignoreSingleTables);
    }
    
    private void checkResourceNameExisted(final String schemaName, final Collection<String> resourceNames) throws RequiredResourceMissedException {
        Map<String, DataSource> resources = ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources();
        Collection<String> notExistedResourceNames = resourceNames.stream().filter(each -> !resources.containsKey(each)).collect(Collectors.toList());
        if (!notExistedResourceNames.isEmpty()) {
            throw new RequiredResourceMissedException(schemaName, notExistedResourceNames);
        }
    }
    
    private void checkResourceNameNotInUse(final String schemaName, final Collection<String> toBeDroppedResourceNames, final boolean ignoreSingleTables) throws ResourceInUsedException {
        Multimap<String, String> inUsedMultimap = getInUsedResources(schemaName);
        Collection<String> inUsedResourceNames = inUsedMultimap.keySet();
        inUsedResourceNames.retainAll(toBeDroppedResourceNames);
        if (!inUsedResourceNames.isEmpty()) {
            if (ignoreSingleTables) {
                checkResourceNameNotInUseIgnoreSingleTableRule(new HashSet<>(inUsedResourceNames), inUsedMultimap);
            } else {
                String firstResource = inUsedResourceNames.iterator().next();
                throw new ResourceInUsedException(firstResource, inUsedMultimap.get(firstResource));
            }
        }
    }
    
    private void checkResourceNameNotInUseIgnoreSingleTableRule(final Collection<String> inUsedResourceNames, final Multimap<String, String> inUsedMultimap) throws ResourceInUsedException {
        for (String each : inUsedResourceNames) {
            Collection<String> inUsedRules = inUsedMultimap.get(each);
            inUsedRules.remove(SingleTableRule.class.getSimpleName());
            if (!inUsedRules.isEmpty()) {
                throw new ResourceInUsedException(each, inUsedRules);
            }
        }
    }
    
    private Multimap<String, String> getInUsedResources(final String schemaName) {
        Multimap<String, String> result = LinkedListMultimap.create();
        for (ShardingSphereRule each : ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getRules()) {
            if (each instanceof DataSourceContainedRule) {
                Set<String> inUsedResourceNames = getInUsedResourceNames((DataSourceContainedRule) each);
                inUsedResourceNames.forEach(eachResource -> result.put(eachResource, each.getType()));
            }
            if (each instanceof DataNodeContainedRule) {
                Set<String> inUsedResourceNames = getInUsedResourceNames((DataNodeContainedRule) each);
                inUsedResourceNames.forEach(eachResource -> result.put(eachResource, each.getType()));
            }
        }
        return result;
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
}
