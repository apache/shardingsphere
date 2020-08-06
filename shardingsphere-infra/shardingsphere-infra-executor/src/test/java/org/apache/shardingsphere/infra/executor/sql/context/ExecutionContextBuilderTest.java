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

package org.apache.shardingsphere.infra.executor.sql.context;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ExecutionContextBuilderTest {
    
    @Test
    public void assertBuildGenericSQLRewriteResult() {
        String sql = "sql";
        List<Object> parameters = Collections.singletonList("parameter");
        GenericSQLRewriteResult genericSQLRewriteResult = new GenericSQLRewriteResult(new SQLRewriteUnit(sql, parameters));
        DataSourceMetas dataSourceMetas = mock(DataSourceMetas.class);
        String firstDataSourceName = "firstDataSourceName";
        when(dataSourceMetas.getAllInstanceDataSourceNames()).thenReturn(Arrays.asList(firstDataSourceName, "lastDataSourceName"));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(dataSourceMetas, null);
        Collection<ExecutionUnit> actual = ExecutionContextBuilder.build(metaData, genericSQLRewriteResult);
        Collection<ExecutionUnit> expected = Collections.singletonList(new ExecutionUnit(firstDataSourceName, new SQLUnit(sql, parameters)));
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertBuildRouteSQLRewriteResult() {
        RouteUnit routeUnit1 = new RouteUnit(new RouteMapper("logicName1", "actualName1"), null);
        SQLRewriteUnit sqlRewriteUnit1 = new SQLRewriteUnit("sql1", Collections.singletonList("parameter1"));
        RouteUnit routeUnit2 = new RouteUnit(new RouteMapper("logicName2", "actualName2"), null);
        SQLRewriteUnit sqlRewriteUnit2 = new SQLRewriteUnit("sql2", Collections.singletonList("parameter2"));
        Map<RouteUnit, SQLRewriteUnit> sqlRewriteUnits = new HashMap<>(2, 1);
        sqlRewriteUnits.put(routeUnit1, sqlRewriteUnit1);
        sqlRewriteUnits.put(routeUnit2, sqlRewriteUnit2);
        Collection<ExecutionUnit> actual = ExecutionContextBuilder.build(null, new RouteSQLRewriteResult(sqlRewriteUnits));
        ExecutionUnit expectedUnit1 = new ExecutionUnit("actualName1", new SQLUnit("sql1", Collections.singletonList("parameter1")));
        ExecutionUnit expectedUnit2 = new ExecutionUnit("actualName2", new SQLUnit("sql2", Collections.singletonList("parameter2")));
        Collection<ExecutionUnit> expected = new LinkedHashSet<>();
        expected.add(expectedUnit1);
        expected.add(expectedUnit2);
        assertThat(actual, is(expected));
    }
}
