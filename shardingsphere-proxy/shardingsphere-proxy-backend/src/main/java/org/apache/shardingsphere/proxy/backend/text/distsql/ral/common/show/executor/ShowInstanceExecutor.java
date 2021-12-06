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

import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.ComputeNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.node.ComputeStatusNode;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.utils.IpUtils;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.sharding.merge.dal.common.MultipleLocalDataMergedResult;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Show instance executor.
 */
public final class ShowInstanceExecutor extends AbstractShowExecutor {
    
    private static final String DELIMITER = "@";
    
    private static final String ID = "instance_id";
    
    private static final String STATUS = "status";
    
    private static final String DISABLE = "disable";
    
    private static final String ENABLE = "enable";
    
    @Override
    protected List<QueryHeader> createQueryHeaders() {
        return Arrays.asList(
                new QueryHeader("", "", ID, ID, Types.VARCHAR, "VARCHAR", 64, 0, false, false, false, false),
                new QueryHeader("", "", STATUS, STATUS, Types.VARCHAR, "VARCHAR", 64, 0, false, false, false, false)
        );
    }
    
    @Override
    protected MergedResult createMergedResult() {
        MetaDataPersistService persistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService().orElse(null);
        if (null == persistService || null == persistService.getRepository()) {
            return new MultipleLocalDataMergedResult(buildInstanceRows());
        }
        Collection<List<Object>> rows = buildInstanceRows(persistService, ENABLE);
        Collection<List<Object>> disableInstanceIds = buildInstanceRows(persistService, DISABLE);
        if (!disableInstanceIds.isEmpty()) {
            rows.addAll(disableInstanceIds);
        }
        return new MultipleLocalDataMergedResult(rows);
    }
    
    private Collection<List<Object>> buildInstanceRows() {
        List<List<Object>> rows = new LinkedList<>();
        // TODO port is not saved in metadata, add port after saving
        String instanceId = String.join(DELIMITER, IpUtils.getIp(), " ");
        rows.add(buildRow(instanceId, ENABLE));
        return rows;
    }
    
    private Collection<List<Object>> buildInstanceRows(final MetaDataPersistService persistService, final String status) {
        String statusPath = ComputeStatusNode.getStatusPath(status.equals(ENABLE) ? ComputeNodeStatus.ONLINE : ComputeNodeStatus.CIRCUIT_BREAKER);
        List<String> instanceIds = persistService.getRepository().getChildrenKeys(statusPath);
        if (!instanceIds.isEmpty()) {
            return instanceIds.stream().filter(Objects::nonNull).map(each -> buildRow(each, status)).collect(Collectors.toCollection(LinkedList::new));
        }
        return Collections.emptyList();
    }
    
    private List<Object> buildRow(final String instanceId, final String status) {
        return Stream.of(instanceId, status).map(each -> (Object) each).collect(Collectors.toCollection(LinkedList::new));
    }
}
