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

package org.apache.shardingsphere.sharding.route.engine.checker.dml;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.exception.algorithm.DuplicateInsertDataRecordException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedUpdatingShardingValueException;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingInsertRouteContextCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private QueryContext queryContext;
    
    @Mock
    private RouteContext routeContext;
    
    @Mock
    private ShardingConditions shardingConditions;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertCheckWhenInsertWithSingleRouting() {
        SQLStatementContext sqlStatementContext = createInsertStatementContext(createInsertStatement());
        when(routeContext.isSingleRouting()).thenReturn(true);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        assertDoesNotThrow(() -> new ShardingInsertRouteContextChecker(shardingConditions).check(shardingRule, queryContext, database, mock(), routeContext));
    }
    
    private InsertStatementContext createInsertStatementContext(final InsertStatement insertStatement) {
        when(database.getName()).thenReturn("foo_db");
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock());
        return new InsertStatementContext(insertStatement, metaData, "foo_db");
    }
    
    @Test
    void assertCheckWhenInsertWithBroadcastTable() {
        SQLStatementContext sqlStatementContext = createInsertStatementContext(createInsertStatement());
        when(routeContext.isSingleRouting()).thenReturn(false);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        assertDoesNotThrow(() -> new ShardingInsertRouteContextChecker(shardingConditions).check(shardingRule, queryContext, database, mock(), routeContext));
    }
    
    @Test
    void assertCheckWhenInsertWithRoutingToSingleDataNode() {
        SQLStatementContext sqlStatementContext = createInsertStatementContext(createInsertStatement());
        when(routeContext.isSingleRouting()).thenReturn(false);
        when(routeContext.getOriginalDataNodes()).thenReturn(getSingleRouteDataNodes());
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        assertDoesNotThrow(() -> new ShardingInsertRouteContextChecker(shardingConditions).check(shardingRule, queryContext, database, mock(), routeContext));
    }
    
    @Test
    void assertCheckWhenInsertWithRoutingToMultipleDataNodes() {
        SQLStatementContext sqlStatementContext = createInsertStatementContext(createInsertStatement());
        when(routeContext.getOriginalDataNodes()).thenReturn(getMultipleRouteDataNodes());
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        assertThrows(DuplicateInsertDataRecordException.class, () -> new ShardingInsertRouteContextChecker(shardingConditions).check(shardingRule, queryContext, database, mock(), routeContext));
    }
    
    @Test
    void assertCheckWhenNotOnDuplicateKeyUpdateShardingColumn() {
        List<Object> params = Collections.singletonList(1);
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.isSingleRouting()).thenReturn(true);
        InsertStatementContext insertStatementContext = createInsertStatementContext(createInsertStatement());
        when(queryContext.getSqlStatementContext()).thenReturn(insertStatementContext);
        when(queryContext.getParameters()).thenReturn(params);
        assertDoesNotThrow(() -> new ShardingInsertRouteContextChecker(mock(ShardingConditions.class)).check(shardingRule, queryContext, database, mock(), routeContext));
    }
    
    @Test
    void assertCheckWhenOnDuplicateKeyUpdateShardingColumnWithSameRouteContext() {
        mockShardingRuleForUpdateShardingColumn();
        List<Object> params = Collections.singletonList(1);
        InsertStatementContext insertStatementContext = createInsertStatementContext(createInsertStatement());
        when(queryContext.getSqlStatementContext()).thenReturn(insertStatementContext);
        when(queryContext.getParameters()).thenReturn(params);
        assertDoesNotThrow(() -> new ShardingInsertRouteContextChecker(mock(ShardingConditions.class)).check(shardingRule, queryContext, database, mock(), createSingleRouteContext()));
    }
    
    @Test
    void assertCheckWhenOnDuplicateKeyUpdateShardingColumnWithDifferentRouteContext() {
        mockShardingRuleForUpdateShardingColumn();
        List<Object> params = Collections.singletonList(1);
        InsertStatementContext insertStatementContext = createInsertStatementContext(createInsertStatement());
        when(queryContext.getSqlStatementContext()).thenReturn(insertStatementContext);
        when(queryContext.getParameters()).thenReturn(params);
        assertThrows(UnsupportedUpdatingShardingValueException.class,
                () -> new ShardingInsertRouteContextChecker(mock()).check(shardingRule, queryContext, database, mock(), createFullRouteContext()));
    }
    
    private void mockShardingRuleForUpdateShardingColumn() {
        ShardingTable shardingTable = mock(ShardingTable.class);
        when(shardingTable.getActualDataSourceNames()).thenReturn(Arrays.asList("ds_0", "ds_1"));
        when(shardingTable.getActualTableNames("ds_1")).thenReturn(Collections.singletonList("user"));
        when(shardingRule.findShardingColumn("id", "user")).thenReturn(Optional.of("id"));
        when(shardingRule.getShardingTable("user")).thenReturn(shardingTable);
        StandardShardingStrategyConfiguration databaseStrategyConfig = mock(StandardShardingStrategyConfiguration.class);
        when(databaseStrategyConfig.getShardingColumn()).thenReturn("id");
        when(databaseStrategyConfig.getShardingAlgorithmName()).thenReturn("database_inline");
        when(shardingRule.getDatabaseShardingStrategyConfiguration(shardingTable)).thenReturn(databaseStrategyConfig);
        when(shardingRule.getShardingAlgorithms()).thenReturn(Collections.singletonMap("database_inline",
                TypedSPILoader.getService(ShardingAlgorithm.class, "INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${id % 2}")))));
    }
    
    private RouteContext createSingleRouteContext() {
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singletonList(new RouteMapper("user", "user"))));
        return result;
    }
    
    private RouteContext createFullRouteContext() {
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singletonList(new RouteMapper("user", "user"))));
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singletonList(new RouteMapper("user", "user"))));
        return result;
    }
    
    private Collection<Collection<DataNode>> getMultipleRouteDataNodes() {
        Collection<DataNode> value1DataNodes = new LinkedList<>();
        value1DataNodes.add(new DataNode("ds_0", (String) null, "user_0"));
        Collection<DataNode> value2DataNodes = new LinkedList<>();
        value2DataNodes.add(new DataNode("ds_0", (String) null, "user_0"));
        value2DataNodes.add(new DataNode("ds_0", (String) null, "user_1"));
        Collection<Collection<DataNode>> result = new LinkedList<>();
        result.add(value1DataNodes);
        result.add(value2DataNodes);
        return result;
    }
    
    private Collection<Collection<DataNode>> getSingleRouteDataNodes() {
        Collection<DataNode> value1DataNodes = new LinkedList<>();
        value1DataNodes.add(new DataNode("ds_0", (String) null, "user_0"));
        Collection<DataNode> value2DataNodes = new LinkedList<>();
        value2DataNodes.add(new DataNode("ds_0", (String) null, "user_0"));
        Collection<Collection<DataNode>> result = new LinkedList<>();
        result.add(value1DataNodes);
        result.add(value2DataNodes);
        return result;
    }
    
    private InsertStatement createInsertStatement() {
        InsertStatement result = mock(InsertStatement.class);
        when(result.getDatabaseType()).thenReturn(databaseType);
        when(result.getTable()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user")))));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("id"));
        ColumnAssignmentSegment assignmentSegment = new ColumnAssignmentSegment(0, 0, Collections.singletonList(columnSegment), new ParameterMarkerExpressionSegment(0, 0, 0));
        when(result.getOnDuplicateKeyColumns()).thenReturn(Optional.of(new OnDuplicateKeyColumnsSegment(0, 0, Collections.singletonList(assignmentSegment))));
        when(result.getInsertColumns()).thenReturn(Optional.of(new InsertColumnsSegment(0, 0, Collections.singleton(columnSegment))));
        return result;
    }
}
