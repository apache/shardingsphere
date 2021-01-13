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

package org.apache.shardingsphere.infra.optimize.schema.table.execute;

import org.apache.calcite.DataContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class CalciteExecutionContextGeneratorTest {
    
    private ExecutionContext initialExecutionContext;
    
    @Before
    public void setUp() {
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().addAll(getRouteUnits());
        initialExecutionContext = new ExecutionContext(mock(SQLStatementContext.class), Collections.emptyList(), routeContext);
    }
    
    private Collection<RouteUnit> getRouteUnits() {
        Collection<RouteMapper> tables = new LinkedList<>();
        tables.add(new RouteMapper("t_order", "t_order_0"));
        tables.add(new RouteMapper("t_order", "t_order_1"));
        RouteUnit unit1 = new RouteUnit(new RouteMapper("ds", "ds0"), tables);
        Collection<RouteUnit> result = new LinkedHashSet<>();
        result.add(unit1);
        tables = new LinkedList<>();
        tables.add(new RouteMapper("t_order", "t_order_3"));
        tables.add(new RouteMapper("t_order", "t_order_4"));
        RouteUnit unit2 = new RouteUnit(new RouteMapper("ds", "ds1"), tables);
        result.add(unit2);
        return result;
    }
    
    @Test
    public void assertGenerate() {
        CalciteExecutionContextGenerator generator =
                new CalciteExecutionContextGenerator("t_order", initialExecutionContext, new CalciteExecutionSQLGenerator(mock(DataContext.class), Collections.emptyList(), new int[]{}));
        ExecutionContext actual = generator.generate();
        assertThat(actual.getExecutionUnits().size(), is(4));
        assertThat(actual.getExecutionUnits().iterator().next().getDataSourceName(), is("ds0"));
        assertThat(actual.getExecutionUnits().iterator().next().getSqlUnit().getSql(), is("SELECT * FROM t_order_0"));
    }
}
