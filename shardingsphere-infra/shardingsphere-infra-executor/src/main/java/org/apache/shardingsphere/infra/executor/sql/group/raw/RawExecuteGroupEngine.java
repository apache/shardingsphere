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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.executor.kernel.InputGroup;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.group.AbstractExecuteGroupEngine;
import org.apache.shardingsphere.infra.executor.sql.raw.RawSQLExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Raw execute group engine.
 */
public final class RawExecuteGroupEngine extends AbstractExecuteGroupEngine<RawSQLExecuteUnit> {
    
    private final int maxConnectionsSizePerQuery;
    
    public RawExecuteGroupEngine(final int maxConnectionsSizePerQuery, final Collection<ShardingSphereRule> rules) {
        super(rules);
        this.maxConnectionsSizePerQuery = maxConnectionsSizePerQuery;
    }
    
    @Override
    protected List<InputGroup<RawSQLExecuteUnit>> group(final String dataSourceName, final List<SQLUnit> sqlUnits) {
        List<InputGroup<RawSQLExecuteUnit>> result = new LinkedList<>();
        int desiredPartitionSize = Math.max(0 == sqlUnits.size() % maxConnectionsSizePerQuery ? sqlUnits.size() / maxConnectionsSizePerQuery : sqlUnits.size() / maxConnectionsSizePerQuery + 1, 1);
        List<List<SQLUnit>> sqlUnitPartitions = Lists.partition(sqlUnits, desiredPartitionSize);
        ConnectionMode connectionMode = maxConnectionsSizePerQuery < sqlUnits.size() ? ConnectionMode.CONNECTION_STRICTLY : ConnectionMode.MEMORY_STRICTLY;
        for (List<SQLUnit> each : sqlUnitPartitions) {
            result.add(generateSQLExecuteGroup(dataSourceName, each, connectionMode));
        }
        return result;
    }
    
    private InputGroup<RawSQLExecuteUnit> generateSQLExecuteGroup(final String dataSourceName, final List<SQLUnit> sqlUnitGroup, final ConnectionMode connectionMode) {
        List<RawSQLExecuteUnit> rawSQLExecuteUnits = new LinkedList<>();
        for (SQLUnit each : sqlUnitGroup) {
            rawSQLExecuteUnits.add(new RawSQLExecuteUnit(new ExecutionUnit(dataSourceName, each), connectionMode));
        }
        return new InputGroup<>(rawSQLExecuteUnits);
    }
}
