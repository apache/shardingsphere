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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.excutor;

import lombok.AllArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.LabelInstanceStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceId;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.SetStatementExecutor;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Label instance executor.
 */
@AllArgsConstructor
public final class LabelInstanceExecutor implements SetStatementExecutor {
    
    private final LabelInstanceStatement sqlStatement;
    
    @Override
    public ResponseHeader execute() throws DistSQLException {
        MetaDataPersistService persistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService().orElse(null);
        if (null == persistService || null == persistService.getRepository() || persistService.getRepository() instanceof StandalonePersistRepository) {
            throw new UnsupportedOperationException("Labels can only be added in cluster mode");
        }
        String instanceId = new InstanceId(sqlStatement.getIp(), Integer.valueOf(sqlStatement.getPort())).getId();
        ComputeNodeInstance instances = persistService.getComputeNodePersistService().loadComputeNodeInstance(new InstanceDefinition(InstanceType.PROXY, instanceId));
        Collection<String> labels = new LinkedHashSet<>(sqlStatement.getLabels());
        if (!sqlStatement.isOverwrite()) {
            labels.addAll(instances.getLabels());
        }
        persistService.getComputeNodePersistService().persistInstanceLabels(instanceId, labels, true);
        return new UpdateResponseHeader(sqlStatement);
    }
}
