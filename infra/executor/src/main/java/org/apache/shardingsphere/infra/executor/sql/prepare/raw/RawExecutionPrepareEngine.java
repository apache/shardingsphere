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

package org.apache.shardingsphere.infra.executor.sql.prepare.raw;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.prepare.AbstractExecutionPrepareEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Raw execution prepare engine.
 */
@HighFrequencyInvocation
public final class RawExecutionPrepareEngine extends AbstractExecutionPrepareEngine<RawSQLExecutionUnit> {
    
    public RawExecutionPrepareEngine(final int maxConnectionsSizePerQuery, final Collection<ShardingSphereRule> rules) {
        super(maxConnectionsSizePerQuery, rules);
    }
    
    @Override
    protected List<ExecutionGroup<RawSQLExecutionUnit>> group(final String databaseName, final String dataSourceName, final int connectionOffset, final List<List<ExecutionUnit>> executionUnitGroups,
                                                              final ConnectionMode connectionMode) {
        return executionUnitGroups.stream().map(each -> createExecutionGroup(each, connectionMode)).collect(Collectors.toList());
    }
    
    private ExecutionGroup<RawSQLExecutionUnit> createExecutionGroup(final List<ExecutionUnit> executionUnitGroup, final ConnectionMode connectionMode) {
        return new ExecutionGroup<>(executionUnitGroup.stream().map(each -> new RawSQLExecutionUnit(each, connectionMode)).collect(Collectors.toList()));
    }
}
