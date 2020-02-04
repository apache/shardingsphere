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

package org.apache.shardingsphere.core.shard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.context.ShardingRouteContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.postgresql.ShowStatement;
import org.apache.shardingsphere.underlying.common.constant.properties.PropertiesConstant;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.route.context.RouteResult;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RequiredArgsConstructor
@Getter
public abstract class BaseShardingEngineTest {
    
    private final String sql;
    
    private final List<Object> parameters;
    
    protected final ShardingSphereProperties getProperties() {
        Properties result = new Properties();
        result.setProperty(PropertiesConstant.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        return new ShardingSphereProperties(result);
    }
    
    protected final ShardingRouteContext createSQLRouteContext() {
        RouteResult routeResult = new RouteResult();
        routeResult.getRouteUnits().add(new RouteUnit("ds"));
        return new ShardingRouteContext(new CommonSQLStatementContext(new ShowStatement()), routeResult, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
    }
    
    protected final void assertExecutionContext(final ExecutionContext actual) {
        assertThat(actual.getExecutionUnits().size(), is(1));
        ExecutionUnit actualExecutionUnit = actual.getExecutionUnits().iterator().next();
        assertThat(actualExecutionUnit.getDataSourceName(), is("ds"));
        assertThat(actualExecutionUnit.getSqlUnit().getSql(), is(sql));
        assertThat(actualExecutionUnit.getSqlUnit().getParameters(), is(parameters));
    }
    
    @Test
    public void assertShardWithHintDatabaseShardingOnly() {
        HintManager.getInstance().setDatabaseShardingValue("1");
        assertShard();
        HintManager.clear();
    }
    
    @Test
    public void assertShardWithoutHint() {
        assertShard();
    }
    
    protected abstract void assertShard();
}
