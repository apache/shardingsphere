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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.distsql.exception.resource.InvalidResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.resource.ResourceInUsedException;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.server.ShardingSphereServerException;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.DatabaseRequiredBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Drop resource backend handler.
 */
@Slf4j
public final class DropResourceBackendHandler extends DatabaseRequiredBackendHandler<UnregisterStorageUnitStatement> {
    
    public DropResourceBackendHandler(final UnregisterStorageUnitStatement sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession);
    }
    
    @Override
    public ResponseHeader execute(final String databaseName, final UnregisterStorageUnitStatement sqlStatement) {
        Collection<String> toBeDroppedResourceNames = sqlStatement.getNames();
        check(databaseName, toBeDroppedResourceNames, sqlStatement.isIgnoreSingleTables(), sqlStatement.isIfExists());
        try {
            ProxyContext.getInstance().getContextManager().dropResources(databaseName, toBeDroppedResourceNames);
        } catch (final SQLException | ShardingSphereServerException ex) {
            log.error("Drop resource failed", ex);
            throw new InvalidResourcesException(Collections.singleton(ex.getMessage()));
        }
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final String databaseName, final Collection<String> toBeDroppedResourceNames,
                       final boolean ignoreSingleTables, final boolean allowNotExist) {
        if (!allowNotExist) {
            checkResourceNameExisted(databaseName, toBeDroppedResourceNames);
        }
        checkResourceNameNotInUse(databaseName, toBeDroppedResourceNames, ignoreSingleTables);
    }
    
    private void checkResourceNameExisted(final String databaseName, final Collection<String> resourceNames) {
        Map<String, DataSource> resources = ProxyContext.getInstance().getDatabase(databaseName).getResourceMetaData().getDataSources();
        Collection<String> notExistedResourceNames = resourceNames.stream().filter(each -> !resources.containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedResourceNames.isEmpty(), () -> new MissingRequiredResourcesException(databaseName, notExistedResourceNames));
    }
    
    private void checkResourceNameNotInUse(final String databaseName, final Collection<String> toBeDroppedResourceNames, final boolean ignoreSingleTables) {
        Multimap<String, String> inUsedMultimap = getInUsedResources(databaseName);
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
    
    private void checkResourceNameNotInUseIgnoreSingleTableRule(final Collection<String> inUsedResourceNames, final Multimap<String, String> inUsedMultimap) {
        for (String each : inUsedResourceNames) {
            Collection<String> inUsedRules = inUsedMultimap.get(each);
            inUsedRules.remove(SingleTableRule.class.getSimpleName());
            ShardingSpherePreconditions.checkState(inUsedRules.isEmpty(), () -> new ResourceInUsedException(each, inUsedRules));
        }
    }
    
    private Multimap<String, String> getInUsedResources(final String databaseName) {
        Multimap<String, String> result = LinkedListMultimap.create();
        for (ShardingSphereRule each : ProxyContext.getInstance().getDatabase(databaseName).getRuleMetaData().getRules()) {
            if (each instanceof DataSourceContainedRule) {
                Collection<String> inUsedResourceNames = getInUsedResourceNames((DataSourceContainedRule) each);
                inUsedResourceNames.forEach(eachResource -> result.put(eachResource, each.getType()));
            }
            if (each instanceof DataNodeContainedRule) {
                Collection<String> inUsedResourceNames = getInUsedResourceNames((DataNodeContainedRule) each);
                inUsedResourceNames.forEach(eachResource -> result.put(eachResource, each.getType()));
            }
        }
        return result;
    }
    
    private Collection<String> getInUsedResourceNames(final DataSourceContainedRule rule) {
        Collection<String> result = new HashSet<>();
        for (Collection<String> each : rule.getDataSourceMapper().values()) {
            result.addAll(each);
        }
        return result;
    }
    
    private Collection<String> getInUsedResourceNames(final DataNodeContainedRule rule) {
        Collection<String> result = new HashSet<>();
        for (Collection<DataNode> each : rule.getAllDataNodes().values()) {
            result.addAll(each.stream().map(DataNode::getDataSourceName).collect(Collectors.toList()));
        }
        return result;
    }
}
