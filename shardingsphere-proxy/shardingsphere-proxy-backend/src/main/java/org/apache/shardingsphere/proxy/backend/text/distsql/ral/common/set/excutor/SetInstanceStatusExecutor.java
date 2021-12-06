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
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.SetInstanceStatusStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.ComputeNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ComputeNodeStatusChangedEvent;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.SetStatementExecutor;

/**
 * Set instance status executor.
 */
@AllArgsConstructor
public final class SetInstanceStatusExecutor implements SetStatementExecutor {
    
    private final SetInstanceStatusStatement sqlStatement;
    
    @Override
    public ResponseHeader execute() throws DistSQLException {
        // add more instance check here
        ShardingSphereEventBus.getInstance().post(new ComputeNodeStatusChangedEvent("DISABLE".equals(sqlStatement.getStatus()) ? ComputeNodeStatus.CIRCUIT_BREAKER : ComputeNodeStatus.ONLINE, 
                sqlStatement.getIp(), sqlStatement.getPort()));
        return new UpdateResponseHeader(sqlStatement);
    }
}
