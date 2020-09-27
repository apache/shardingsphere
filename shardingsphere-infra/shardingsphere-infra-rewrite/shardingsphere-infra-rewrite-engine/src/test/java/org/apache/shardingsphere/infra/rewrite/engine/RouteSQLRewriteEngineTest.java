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

package org.apache.shardingsphere.infra.rewrite.engine;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RouteSQLRewriteEngineTest {
    
    @Test
    public void assertRewriteWithStandardParameterBuilder() {
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(mock(SchemaMetaData.class), mock(SQLStatementContext.class), "SELECT ?", Collections.singletonList(1));
        RouteContext routeContext = new RouteContext();
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        routeContext.getRouteUnits().add(routeUnit);
        RouteSQLRewriteResult actual = new RouteSQLRewriteEngine().rewrite(sqlRewriteContext, routeContext);
        assertThat(actual.getSqlRewriteUnits().size(), is(1));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getSql(), is("SELECT ?"));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getParameters(), is(Collections.singletonList(1)));
    }
    
    @Test
    public void assertRewriteWithGroupedParameterBuilderForBroadcast() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class);
        when(statementContext.getGroupedParameters()).thenReturn(Collections.singletonList(Collections.singletonList(1)));
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(mock(SchemaMetaData.class), statementContext, "INSERT INTO tbl VALUES (?)", Collections.singletonList(1));
        RouteContext routeContext = new RouteContext();
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        routeContext.getRouteUnits().add(routeUnit);
        RouteSQLRewriteResult actual = new RouteSQLRewriteEngine().rewrite(sqlRewriteContext, routeContext);
        assertThat(actual.getSqlRewriteUnits().size(), is(1));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getSql(), is("INSERT INTO tbl VALUES (?)"));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getParameters(), is(Collections.singletonList(1)));
    }
    
    @Test
    public void assertRewriteWithGroupedParameterBuilderForRouteWithSameDataNode() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class);
        when(statementContext.getGroupedParameters()).thenReturn(Collections.singletonList(Collections.singletonList(1)));
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(mock(SchemaMetaData.class), statementContext, "INSERT INTO tbl VALUES (?)", Collections.singletonList(1));
        RouteContext routeContext = new RouteContext();
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        routeContext.getRouteUnits().add(routeUnit);
        // TODO check why data node is "ds.tbl_0", not "ds_0.tbl_0"
        routeContext.getOriginalDataNodes().add(Collections.singletonList(new DataNode("ds.tbl_0")));
        RouteSQLRewriteResult actual = new RouteSQLRewriteEngine().rewrite(sqlRewriteContext, routeContext);
        assertThat(actual.getSqlRewriteUnits().size(), is(1));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getSql(), is("INSERT INTO tbl VALUES (?)"));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getParameters(), is(Collections.singletonList(1)));
    }
    
    @Test
    public void assertRewriteWithGroupedParameterBuilderForRouteWithEmptyDataNode() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class);
        when(statementContext.getGroupedParameters()).thenReturn(Collections.singletonList(Collections.singletonList(1)));
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(mock(SchemaMetaData.class), statementContext, "INSERT INTO tbl VALUES (?)", Collections.singletonList(1));
        RouteContext routeContext = new RouteContext();
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        routeContext.getRouteUnits().add(routeUnit);
        routeContext.getOriginalDataNodes().add(Collections.emptyList());
        RouteSQLRewriteResult actual = new RouteSQLRewriteEngine().rewrite(sqlRewriteContext, routeContext);
        assertThat(actual.getSqlRewriteUnits().size(), is(1));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getSql(), is("INSERT INTO tbl VALUES (?)"));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getParameters(), is(Collections.singletonList(1)));
    }
    
    @Test
    public void assertRewriteWithGroupedParameterBuilderForRouteWithNotSameDataNode() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class);
        when(statementContext.getGroupedParameters()).thenReturn(Collections.singletonList(Collections.singletonList(1)));
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(mock(SchemaMetaData.class), statementContext, "INSERT INTO tbl VALUES (?)", Collections.singletonList(1));
        RouteContext routeContext = new RouteContext();
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        routeContext.getRouteUnits().add(routeUnit);
        routeContext.getOriginalDataNodes().add(Collections.singletonList(new DataNode("ds_1.tbl_1")));
        RouteSQLRewriteResult actual = new RouteSQLRewriteEngine().rewrite(sqlRewriteContext, routeContext);
        assertThat(actual.getSqlRewriteUnits().size(), is(1));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getSql(), is("INSERT INTO tbl VALUES (?)"));
        assertTrue(actual.getSqlRewriteUnits().get(routeUnit).getParameters().isEmpty());
    }
}
