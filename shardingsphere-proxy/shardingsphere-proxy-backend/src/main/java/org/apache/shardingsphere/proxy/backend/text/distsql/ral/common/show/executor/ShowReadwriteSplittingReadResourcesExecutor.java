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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.SchemaNotExistedException;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node.StorageStatusNode;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingReadResourcesStatement;
import org.apache.shardingsphere.sharding.merge.dal.common.MultipleLocalDataMergedResult;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Show readwrite-splitting read resources executor.
 */
@RequiredArgsConstructor
public final class ShowReadwriteSplittingReadResourcesExecutor extends AbstractShowExecutor {
    
    private static final String DELIMITER = "\\.";
    
    private static final String RESOURCE = "resource";
    
    private static final String STATUS = "status";
    
    private static final String DISABLE = "disable";
    
    private static final String ENABLE = "enable";
    
    private final ShowReadwriteSplittingReadResourcesStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    @Override
    protected List<QueryHeader> createQueryHeaders() {
        return Arrays.asList(
                new QueryHeader("", "", RESOURCE, RESOURCE, Types.VARCHAR, "VARCHAR", 64, 0, false, false, false, false),
                new QueryHeader("", "", STATUS, STATUS, Types.VARCHAR, "VARCHAR", 64, 0, false, false, false, false));
    }
    
    @Override
    protected MergedResult createMergedResult() {
        String schemaName = sqlStatement.getSchema().isPresent() ? sqlStatement.getSchema().get().getIdentifier().getValue() : backendConnection.getSchemaName();
        if (null == schemaName) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().getAllSchemaNames().contains(schemaName)) {
            throw new SchemaNotExistedException(schemaName);
        }
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(schemaName);
        Collection<List<Object>> rows = buildResourceRows(metaData, ENABLE);
        MetaDataPersistService persistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService().orElse(null);
        if (null == persistService || null == persistService.getRepository()) {
            return new MultipleLocalDataMergedResult(rows);
        }
        Collection<List<Object>> disabledResourceRows = buildResourceRows(persistService, DISABLE);
        return new MultipleLocalDataMergedResult(mergeRows(rows, disabledResourceRows));
    }
    
    private Collection<List<Object>> buildResourceRows(final ShardingSphereMetaData metaData, final String status) {
        Set<String> allResources = metaData.getResource().getDataSources().keySet();
        return allResources.stream().map(each -> buildRow(each, status)).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private Collection<List<Object>> buildResourceRows(final MetaDataPersistService persistService, final String status) {
        List<String> instanceIds = persistService.getRepository().getChildrenKeys(StorageStatusNode.getStatusPath(status.equals(DISABLE) ? StorageNodeStatus.DISABLE : StorageNodeStatus.PRIMARY));
        if (!instanceIds.isEmpty()) {
            return instanceIds.stream().filter(Objects::nonNull).map(each -> each.split(DELIMITER)[1]).map(each -> buildRow(each, status)).collect(Collectors.toCollection(LinkedList::new));
        }
        return Collections.emptyList();
    }
    
    private Collection<List<Object>> mergeRows(final Collection<List<Object>> rows, final Collection<List<Object>> disabledResourceRows) {
        Collection<List<Object>> result;
        Set<Object> disabledResourceNames = disabledResourceRows.stream().map(each -> getResourceName(each)).collect(Collectors.toSet());
        result = rows.stream().filter(each -> !disabledResourceNames.contains(getResourceName(each))).collect(Collectors.toCollection(LinkedList::new));
        result.addAll(disabledResourceRows);
        return result;
    }
    
    private List<Object> buildRow(final String resource, final String status) {
        return Arrays.asList(resource, status);
    }
    
    private Object getResourceName(final List<Object> row) {
        return row.get(0);
    }
}
