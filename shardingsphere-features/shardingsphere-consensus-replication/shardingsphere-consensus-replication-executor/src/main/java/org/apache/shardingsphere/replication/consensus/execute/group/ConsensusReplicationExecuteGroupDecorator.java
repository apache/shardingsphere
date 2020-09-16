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

package org.apache.shardingsphere.replication.consensus.execute.group;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.kernel.InputGroup;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLRuntimeContext;
import org.apache.shardingsphere.infra.executor.sql.raw.RawSQLExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.raw.group.RawExecuteGroupDecorator;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteStageContext;
import org.apache.shardingsphere.replication.consensus.constant.ConsensusReplicationOrder;
import org.apache.shardingsphere.replication.consensus.route.engine.ConsensusReplicationGroup;
import org.apache.shardingsphere.replication.consensus.route.engine.ConsensusReplicationRouteStageContext;
import org.apache.shardingsphere.replication.consensus.rule.ConsensusReplicationRule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Execute group decorator for consensus replication.
 */
@Slf4j
public final class ConsensusReplicationExecuteGroupDecorator implements RawExecuteGroupDecorator<RawSQLExecuteUnit, ConsensusReplicationRule> {
    
    /**
     * TODO FIXED ME when the proxy is capable of handling tableless operation commands, it can be removed.
     */
    private final boolean supportWithoutTableCommand = true;
    
    @Override
    public Collection<InputGroup<RawSQLExecuteUnit>> decorate(final RouteContext routeContext, final ConsensusReplicationRule rule, final Collection<InputGroup<RawSQLExecuteUnit>> inputGroups) {
        if (!inputGroups.isEmpty()) {
            if (!(inputGroups.iterator().next().getInputs().get(0) instanceof RawSQLExecuteUnit)) {
                log.debug("inputGroups ExecuteUnit is not RawSQLExecuteUnit, ignore decorate");
                return inputGroups;
            }
        }
        RouteStageContext routeStageContext = routeContext.getRouteStageContext(getTypeClass());
        ConsensusReplicationRouteStageContext replicaRouteStageContext = (ConsensusReplicationRouteStageContext) routeStageContext;
        Map<String, ConsensusReplicationGroup> replicaGroups = replicaRouteStageContext.getReplicaGroups();
        for (InputGroup<RawSQLExecuteUnit> each : inputGroups) {
            routeReplicaGroup(each, replicaRouteStageContext.getSchemaName(), replicaGroups, replicaRouteStageContext.isReadOnly());
        }
        return inputGroups;
    }
    
    private void routeReplicaGroup(final InputGroup<RawSQLExecuteUnit> inputGroup, final String schemaName, final Map<String, ConsensusReplicationGroup> replicaGroups, final boolean readOnly) {
        for (RawSQLExecuteUnit each : inputGroup.getInputs()) {
            ExecutionUnit executionUnit = each.getExecutionUnit();
            SQLRuntimeContext sqlRuntimeContext = executionUnit.getSqlUnit().getSqlRuntimeContext();
            List<String> actualTables = sqlRuntimeContext.getActualTables();
            if ((null == actualTables || actualTables.isEmpty()) && !supportWithoutTableCommand) {
                throw new ShardingSphereException("route fail: actual tables is empty");
            }
            ConsensusReplicationGroup replicaGroup = getReplicaGroup(actualTables, replicaGroups);
            each.setRawGroup(replicaGroup);
            sqlRuntimeContext.setSchemaName(schemaName);
            sqlRuntimeContext.setReadOnly(readOnly);
        }
    }
    
    private ConsensusReplicationGroup getReplicaGroup(final List<String> actualTables, final Map<String, ConsensusReplicationGroup> replicaGroups) {
        ConsensusReplicationGroup replicaGroup = null;
        if (null != actualTables && !actualTables.isEmpty()) {
            for (String each : actualTables) {
                replicaGroup = replicaGroups.get(each);
                if (null != replicaGroup) {
                    break;
                }
            }
        } else {
            if (!replicaGroups.isEmpty()) {
                replicaGroup = replicaGroups.entrySet().iterator().next().getValue();
            }
        }
        if (null == replicaGroup) {
            throw new ShardingSphereException("route fail: route result is empty");
        }
        return replicaGroup;
    }
    
    @Override
    public int getOrder() {
        return ConsensusReplicationOrder.ORDER;
    }
    
    @Override
    public Class<ConsensusReplicationRule> getTypeClass() {
        return ConsensusReplicationRule.class;
    }
}
