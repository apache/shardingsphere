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

package org.apache.shardingsphere.infra.executor.sql.group.raw;

import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.group.AbstractExecutionGroupEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Raw execution group engine.
 */
public final class RawExecutionGroupEngine extends AbstractExecutionGroupEngine<RawSQLExecutionUnit> {
    
    public RawExecutionGroupEngine(final int maxConnectionsSizePerQuery, final Collection<ShardingSphereRule> rules) {
        super(maxConnectionsSizePerQuery, rules);
    }
    
    @Override
    protected List<ExecutionGroup<RawSQLExecutionUnit>> group(final String dataSourceName, final List<List<SQLUnit>> sqlUnitGroups, final ConnectionMode connectionMode) {
        return sqlUnitGroups.stream().map(each -> createExecutionGroup(dataSourceName, each, connectionMode)).collect(Collectors.toList());
    }
    
    private ExecutionGroup<RawSQLExecutionUnit> createExecutionGroup(final String dataSourceName, final List<SQLUnit> sqlUnitGroup, final ConnectionMode connectionMode) {
        return new ExecutionGroup<>(sqlUnitGroup.stream().map(each -> new RawSQLExecutionUnit(new ExecutionUnit(dataSourceName, each), connectionMode)).collect(Collectors.toList()));
    }
}
