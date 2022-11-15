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
 * Unregister storage unit backend handler.
 */
@Slf4j
public final class UnregisterStorageUnitBackendHandler extends DatabaseRequiredBackendHandler<UnregisterStorageUnitStatement> implements StorageUnitBackendHandler<UnregisterStorageUnitStatement> {
    
    public UnregisterStorageUnitBackendHandler(final UnregisterStorageUnitStatement sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession);
    }
    
    @Override
    public ResponseHeader execute(final String databaseName, final UnregisterStorageUnitStatement sqlStatement) {
        checkSQLStatement(databaseName, sqlStatement);
        try {
            ProxyContext.getInstance().getContextManager().dropResources(databaseName, sqlStatement.getStorageUnitNames());
        } catch (final SQLException | ShardingSphereServerException ex) {
            log.error("Unregister storage unit failed", ex);
            throw new InvalidResourcesException(Collections.singleton(ex.getMessage()));
        }
        return new UpdateResponseHeader(sqlStatement);
    }
    
    @Override
    public void checkSQLStatement(final String databaseName, final UnregisterStorageUnitStatement sqlStatement) {
        if (!sqlStatement.isIfExists()) {
            checkExisted(databaseName, sqlStatement.getStorageUnitNames());
        }
        checkInUsed(databaseName, sqlStatement);
    }
    
    private void checkExisted(final String databaseName, final Collection<String> storageUnitNames) {
        Map<String, DataSource> dataSources = ProxyContext.getInstance().getDatabase(databaseName).getResourceMetaData().getDataSources();
        Collection<String> notExistedStorageUnits = storageUnitNames.stream().filter(each -> !dataSources.containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedStorageUnits.isEmpty(), () -> new MissingRequiredResourcesException(databaseName, notExistedStorageUnits));
    }
    
    private void checkInUsed(final String databaseName, final UnregisterStorageUnitStatement sqlStatement) {
        Multimap<String, String> inUsedStorageUnits = getInUsedResources(databaseName);
        Collection<String> inUsedStorageUnitNames = inUsedStorageUnits.keySet();
        inUsedStorageUnitNames.retainAll(sqlStatement.getStorageUnitNames());
        if (!inUsedStorageUnitNames.isEmpty()) {
            if (sqlStatement.isIgnoreSingleTables()) {
                checkInUsedIgnoreSingleTables(new HashSet<>(inUsedStorageUnitNames), inUsedStorageUnits);
            } else {
                String firstResource = inUsedStorageUnitNames.iterator().next();
                throw new ResourceInUsedException(firstResource, inUsedStorageUnits.get(firstResource));
            }
        }
    }
    
    private Multimap<String, String> getInUsedResources(final String databaseName) {
        Multimap<String, String> result = LinkedListMultimap.create();
        for (ShardingSphereRule each : ProxyContext.getInstance().getDatabase(databaseName).getRuleMetaData().getRules()) {
            if (each instanceof DataSourceContainedRule) {
                getInUsedResourceNames((DataSourceContainedRule) each).forEach(eachResource -> result.put(eachResource, each.getType()));
            }
            if (each instanceof DataNodeContainedRule) {
                getInUsedResourceNames((DataNodeContainedRule) each).forEach(eachResource -> result.put(eachResource, each.getType()));
            }
        }
        return result;
    }
    
    private void checkInUsedIgnoreSingleTables(final Collection<String> inUsedResourceNames, final Multimap<String, String> inUsedStorageUnits) {
        for (String each : inUsedResourceNames) {
            Collection<String> inUsedRules = inUsedStorageUnits.get(each);
            inUsedRules.remove(SingleTableRule.class.getSimpleName());
            ShardingSpherePreconditions.checkState(inUsedRules.isEmpty(), () -> new ResourceInUsedException(each, inUsedRules));
        }
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
