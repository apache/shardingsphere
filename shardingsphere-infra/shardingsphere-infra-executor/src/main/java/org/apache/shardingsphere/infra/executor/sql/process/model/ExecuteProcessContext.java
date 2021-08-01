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

package org.apache.shardingsphere.infra.executor.sql.process.model;

import lombok.Getter;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;

import java.util.Collection;
import java.util.LinkedList;
import org.apache.shardingsphere.infra.metadata.user.Grantee;

/**
 * Execute process context.
 */
@Getter
public final class ExecuteProcessContext {
    
    private final String executionID;
    
    private final String schemaName;
    
    private final String username;
    
    private final String hostname;
    
    private final String sql;
    
    private final Collection<ExecuteProcessUnit> unitStatuses;
    
    private final long startTimeMillis = System.currentTimeMillis();
    
    public ExecuteProcessContext(final String sql, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, final ExecuteProcessConstants constants) {
        this.executionID = executionGroupContext.getExecutionID();
        this.sql = sql;
        this.schemaName = executionGroupContext.getSchemaName();
        Grantee grantee = executionGroupContext.getGrantee();
        this.username = null != grantee ? grantee.getUsername() : null;
        this.hostname = null != grantee ? grantee.getHostname() : null;
        unitStatuses = createExecutionUnitStatuses(executionGroupContext, constants);
    }
    
    private Collection<ExecuteProcessUnit> createExecutionUnitStatuses(final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, final ExecuteProcessConstants constants) {
        Collection<ExecuteProcessUnit> result = new LinkedList<>();
        for (ExecutionGroup<? extends SQLExecutionUnit> group : executionGroupContext.getInputGroups()) {
            for (SQLExecutionUnit each : group.getInputs()) {
                result.add(new ExecuteProcessUnit(each.getExecutionUnit(), constants));
            }
        }
        return result;
    }
}
