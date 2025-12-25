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
package org.apache.shardingsphere.single.route.engine;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleRouteEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertRouteInSameDataSource() throws SQLException {
        SingleRouteEngine engine = new SingleRouteEngine(mockQualifiedTables(), null, mock());
        SingleRule singleRule = new SingleRule(new SingleRuleConfiguration(), "foo_db", mock(), createDataSourceMap(), Collections.emptyList());
        singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes().put("t_order", Collections.singleton(new DataNode("ds_0", "foo_db", "t_order")));
        singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes().put("t_order_item", Collections.singleton(new DataNode("ds_0", "foo_db", "t_order_item")));
        RouteContext routeContext = new RouteContext();
        engine.route(routeContext, singleRule);
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(2));
        Iterator<RouteMapper> tableMappers = routeUnits.get(0).getTableMappers().iterator();
        RouteMapper tableMapper0 = tableMappers.next();
        assertThat(tableMapper0.getActualName(), is("t_order"));
        assertThat(tableMapper0.getLogicName(), is("t_order"));
        RouteMapper tableMapper1 = tableMappers.next();
        assertThat(tableMapper1.getActualName(), is("t_order_item"));
        assertThat(tableMapper1.getLogicName(), is("t_order_item"));
    }
    
    private Collection<QualifiedTable> mockQualifiedTables() {
        return Arrays.asList(new QualifiedTable("foo_db", "t_order"), new QualifiedTable("foo_db", "t_order_item"));
    }
    
    @Test
    void assertRouteWithoutSingleRule() throws SQLException {
        CreateTableStatement sqlStatement = new CreateTableStatement(databaseType);
        SingleRouteEngine engine = new SingleRouteEngine(mockQualifiedTables(), sqlStatement, mock());
        SingleRule singleRule = new SingleRule(new SingleRuleConfiguration(), "foo_db", mock(), createDataSourceMap(), Collections.emptyList());
        RouteContext routeContext = new RouteContext();
        engine.route(routeContext, singleRule);
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        Iterator<RouteMapper> tableMappers = routeUnits.get(0).getTableMappers().iterator();
        RouteMapper tableMapper0 = tableMappers.next();
        assertThat(tableMapper0.getActualName(), is("t_order"));
        assertThat(tableMapper0.getLogicName(), is("t_order"));
    }
    
    @Test
    void assertRouteWithDefaultSingleRule() throws SQLException {
        CreateTableStatement sqlStatement = new CreateTableStatement(databaseType);
        SingleRouteEngine engine = new SingleRouteEngine(mockQualifiedTables(), sqlStatement, mock());
        SingleRule singleRule = new SingleRule(new SingleRuleConfiguration(Collections.emptyList(), "ds_0"), "foo_db", mock(), createDataSourceMap(), Collections.emptyList());
        RouteContext routeContext = new RouteContext();
        engine.route(routeContext, singleRule);
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        Iterator<RouteMapper> tableMappers = routeUnits.get(0).getTableMappers().iterator();
        RouteMapper tableMapper0 = tableMappers.next();
        assertThat(tableMapper0.getActualName(), is("t_order"));
        assertThat(tableMapper0.getLogicName(), is("t_order"));
    }
    
    private Map<String, DataSource> createDataSourceMap() throws SQLException {
        Map<String, DataSource> result = new HashMap<>(2, 1F);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/db");
        result.put("ds_0", new MockedDataSource(connection));
        result.put("ds_1", new MockedDataSource(connection));
        return result;
    }
    
    @Test
    void assertRouteDuplicateSingleTable() {
        SingleRouteEngine engine = new SingleRouteEngine(Collections.singleton(new QualifiedTable("foo_db", "t_order")), mockStatement(false), mock(HintValueContext.class));
        assertThrows(TableExistsException.class, () -> engine.route(new RouteContext(), mockSingleRule()));
    }
    
    @Test
    void assertRouteIfNotExistsDuplicateSingleTable() {
        SingleRouteEngine engine = new SingleRouteEngine(Collections.singleton(new QualifiedTable("foo_db", "t_order")), mockStatement(true), mock(HintValueContext.class));
        assertDoesNotThrow(() -> engine.route(new RouteContext(), mockSingleRule()));
    }
    
    private SQLStatement mockStatement(final boolean ifNotExists) {
        CreateTableStatement result = mock(CreateTableStatement.class);
        when(result.isIfNotExists()).thenReturn(ifNotExists);
        when(result.getTable()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        return result;
    }
    
    private SingleRule mockSingleRule() {
        SingleRule result = mock(SingleRule.class);
        DataNode dataNode = mock(DataNode.class);
        MutableDataNodeRuleAttribute ruleAttribute = mock(MutableDataNodeRuleAttribute.class);
        when(ruleAttribute.findTableDataNode("foo_db", "t_order")).thenReturn(Optional.of(dataNode));
        when(result.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        return result;
    }
}
