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

package org.apache.shardingsphere.replica.execute.group;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.kernel.InputGroup;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.raw.RawSQLExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.raw.group.RawExecuteGroupDecorator;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteStageContext;
import org.apache.shardingsphere.replica.constant.ReplicaOrder;
import org.apache.shardingsphere.replica.route.engine.ReplicaGroup;
import org.apache.shardingsphere.replica.route.engine.ReplicaRouteStageContext;
import org.apache.shardingsphere.replica.rule.ReplicaRule;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Execute group decorator for replica.
 */
@Slf4j
public final class ReplicaExecuteGroupDecorator implements RawExecuteGroupDecorator<RawSQLExecuteUnit, ReplicaRule> {

    private final boolean supportWithoutTableCommand = true;

    @Override
    public Collection<InputGroup<RawSQLExecuteUnit>> decorate(final RouteContext routeContext, final ReplicaRule rule, final Collection<InputGroup<RawSQLExecuteUnit>> inputGroups) {
        if (null != inputGroups && !inputGroups.isEmpty()) {
            if (!(inputGroups.iterator().next().getInputs().get(0) instanceof RawSQLExecuteUnit)) {
                log.info("inputGroups ExecuteUnit is not RawSQLExecuteUnit, ignore decorate");
                return inputGroups;
            }
        }
        RouteStageContext preRouteStageContext = routeContext.lastRouteStageContext();
        String currentSchemaName = preRouteStageContext.getCurrentSchemaName();
        RouteStageContext routeStageContext = routeContext.getRouteStageContexts().get(getOrder());
        ReplicaRouteStageContext replicaRouteStageContext = (ReplicaRouteStageContext) routeStageContext;
        Map<String, ReplicaGroup> replicaGroups = replicaRouteStageContext.getReplicaGroups();
        boolean readOnly = replicaRouteStageContext.isReadOnly();
        for (InputGroup<RawSQLExecuteUnit> each : inputGroups) {
            routeReplicaGroup(each, currentSchemaName, replicaGroups, readOnly);
        }
        return inputGroups;
    }

    private void routeReplicaGroup(final InputGroup<RawSQLExecuteUnit> inputGroup, final String currentSchemaName, final Map<String, ReplicaGroup> replicaGroups, final boolean readOnly) {
        for (RawSQLExecuteUnit each: inputGroup.getInputs()) {
            ExecutionUnit executionUnit = each.getExecutionUnit();
            String schemaName = currentSchemaName;
            SQLUnit sqlUnit = executionUnit.getSqlUnit();
            Set<String> actualTables = sqlUnit.getActualTables();
            if ((null == actualTables || actualTables.isEmpty()) && !supportWithoutTableCommand) {
                throw new ShardingSphereException("route fail: actual tables is empty");
            }
            ReplicaGroup replicaGroup = getReplicaGroup(actualTables, replicaGroups);
            each.setRawGroup(replicaGroup);
            executionUnit.setSchemaName(schemaName);
            sqlUnit.setReadOnly(readOnly);
        }
    }

    private ReplicaGroup getReplicaGroup(final Set<String> actualTables, final Map<String, ReplicaGroup> replicaGroups) {
        ReplicaGroup replicaGroup = null;
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
        return ReplicaOrder.ORDER;
    }
    
    @Override
    public Class<ReplicaRule> getTypeClass() {
        return ReplicaRule.class;
    }
}
