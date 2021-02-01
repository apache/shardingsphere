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

package org.apache.shardingsphere.sharding.route.engine.type.standard;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.Assert;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * HintSQLRouteTest.
 */
public final class HintRouteTest extends AbstractSQLRouteTest {
    
    @Test
    public void assertDatabaseHint() {
        String sql = "/* !databaseSharding(ds_1)*/ select * from t_order_1";
        RouteContext routeContext = getRouteContext(sql, Collections.emptyList());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = routeContext.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("ds_1"));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnit.getTableMappers().size(), is(1));
        assertThat(routeUnit.getTableMappers().iterator().next().getLogicName(), is("t_order_1"));
        assertThat(routeUnit.getTableMappers().iterator().next().getActualName(), is("t_order_1"));
    }

    @Test
    public void assertShardingHintWithDatabase() {
        String sql = "/* !shardingDatabaseValue(1) */ select * from t_order";
        RouteContext routeContext = getRouteContext(sql, Collections.emptyList());
        Assert.assertEquals(2, routeContext.getRouteUnits().size());
        Collection<RouteUnit> routeUnits = routeContext.getRouteUnits();
        int i = 0;
        for (RouteUnit routeUnit : routeUnits) {
            assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("ds_1"));
            assertThat(routeUnit.getDataSourceMapper().getActualName(), is("ds_1"));
            assertThat(routeUnit.getTableMappers().iterator().next().getLogicName(), is("t_order"));
            assertThat(routeUnit.getTableMappers().iterator().next().getActualName(), is("t_order_" + i));
            i++;
        }
    }

    @Test
    public void assertShardingHintWithTable() {
        String sql = "/* !shardingTableValue(1) */ select * from t_order";
        RouteContext routeContext = getRouteContext(sql, Collections.emptyList());
        Assert.assertEquals(2, routeContext.getRouteUnits().size());
        Collection<RouteUnit> routeUnits = routeContext.getRouteUnits();
        int i = 0;
        for (RouteUnit routeUnit : routeUnits) {
            assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("ds_" + i));
            assertThat(routeUnit.getDataSourceMapper().getActualName(), is("ds_" + i));
            assertThat(routeUnit.getTableMappers().iterator().next().getLogicName(), is("t_order"));
            assertThat(routeUnit.getTableMappers().iterator().next().getActualName(), is("t_order_1"));
            i++;
        }
    }

    @Test
    public void assertShardingHintWithDatabaseAndTable() {
        String sql = "/* !shardingDatabaseValue(1) shardingTableValue(1) */select * from t_order";
        RouteContext routeContext = getRouteContext(sql, Collections.emptyList());
        Assert.assertEquals(1, routeContext.getRouteUnits().size());
        RouteUnit routeUnit = routeContext.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("ds_1"));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnit.getTableMappers().iterator().next().getLogicName(), is("t_order"));
        assertThat(routeUnit.getTableMappers().iterator().next().getActualName(), is("t_order_1"));
    }

    @Test
    public void assertShardingHintWithDatabaseAndTableForMultiValue() {
        String sql = "/* !shardingDatabaseValue(1,2) shardingTableValue(1,2) */select * from t_order";
        RouteContext routeContext = getRouteContext(sql, Collections.emptyList());
        List<RouteUnit> routeUnits = new LinkedList<>(routeContext.getRouteUnits());
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_order_0"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_order"));
        assertThat(routeUnits.get(1).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(1).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(1).getTableMappers().iterator().next().getActualName(), is("t_order_1"));
        assertThat(routeUnits.get(1).getTableMappers().iterator().next().getLogicName(), is("t_order"));
        assertThat(routeUnits.get(2).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(2).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(2).getTableMappers().iterator().next().getActualName(), is("t_order_0"));
        assertThat(routeUnits.get(2).getTableMappers().iterator().next().getLogicName(), is("t_order"));
        assertThat(routeUnits.get(3).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(3).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(3).getTableMappers().iterator().next().getActualName(), is("t_order_1"));
        assertThat(routeUnits.get(3).getTableMappers().iterator().next().getLogicName(), is("t_order"));
    }

    @Test
    public void assertShardingHintWithConditions() {
        /*sql condition will be ignored*/
        String sql = "/* !shardingDatabaseValue(1) shardingTableValue(1) */select * from t_order where order_id = 0 and user_id = 0";
        RouteContext routeContext = getRouteContext(sql, Collections.emptyList());
        Assert.assertEquals(1, routeContext.getRouteUnits().size());
        RouteUnit routeUnit = routeContext.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("ds_1"));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnit.getTableMappers().iterator().next().getLogicName(), is("t_order"));
        assertThat(routeUnit.getTableMappers().iterator().next().getActualName(), is("t_order_1"));
    }
}
