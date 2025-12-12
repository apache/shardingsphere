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
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedUpdatingShardingValueException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingUpdateRouteContextCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private QueryContext queryContext;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Test
    void assertCheckWhenNotUpdateShardingColumn() {
        UpdateStatementContext sqlStatementContext = new UpdateStatementContext(createUpdateStatement());
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        assertDoesNotThrow(() -> new ShardingUpdateRouteContextChecker().check(shardingRule, queryContext, database, mock(ConfigurationProperties.class), mock(RouteContext.class)));
    }
    
    @Test
    void assertCheckWhenUpdateShardingColumnWithSameRouteContext() {
        mockShardingRuleForUpdateShardingColumn();
        when(queryContext.getSqlStatementContext()).thenReturn(new UpdateStatementContext(createUpdateStatement()));
        assertDoesNotThrow(() -> new ShardingUpdateRouteContextChecker().check(shardingRule, queryContext, database, mock(ConfigurationProperties.class), createSingleRouteContext()));
    }
    
    @Test
    void assertCheckWhenUpdateShardingColumnWithDifferentRouteContext() {
        mockShardingRuleForUpdateShardingColumn();
        when(queryContext.getSqlStatementContext()).thenReturn(new UpdateStatementContext(createUpdateStatement()));
        assertThrows(UnsupportedUpdatingShardingValueException.class,
                () -> new ShardingUpdateRouteContextChecker().check(shardingRule, queryContext, database, mock(ConfigurationProperties.class), createFullRouteContext()));
    }
    
    private void mockShardingRuleForUpdateShardingColumn() {
        ShardingTable shardingTable = mock(ShardingTable.class);
        when(shardingTable.getActualDataSourceNames()).thenReturn(Arrays.asList("ds_0", "ds_1"));
        when(shardingTable.getActualTableNames("ds_1")).thenReturn(Collections.singleton("user"));
        when(shardingRule.findShardingColumn("id", "user")).thenReturn(Optional.of("id"));
        when(shardingRule.getShardingTable("user")).thenReturn(shardingTable);
        StandardShardingStrategyConfiguration databaseStrategyConfig = mock(StandardShardingStrategyConfiguration.class);
        when(databaseStrategyConfig.getShardingColumn()).thenReturn("id");
        when(databaseStrategyConfig.getShardingAlgorithmName()).thenReturn("database_inline");
        when(shardingRule.getDatabaseShardingStrategyConfiguration(shardingTable)).thenReturn(databaseStrategyConfig);
        when(shardingRule.getShardingAlgorithms()).thenReturn(createShardingAlgorithmMap());
    }
    
    private Map<String, ShardingAlgorithm> createShardingAlgorithmMap() {
        return Collections.singletonMap("database_inline", TypedSPILoader.getService(ShardingAlgorithm.class, "INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${id % 2}"))));
    }
    
    private RouteContext createSingleRouteContext() {
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singleton(new RouteMapper("user", "user"))));
        return result;
    }
    
    private RouteContext createFullRouteContext() {
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.singleton(new RouteMapper("user", "user"))));
        result.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.singleton(new RouteMapper("user", "user"))));
        return result;
    }
    
    private UpdateStatement createUpdateStatement() {
        UpdateStatement result = new UpdateStatement(databaseType);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        List<ColumnSegment> columns = new LinkedList<>();
        columns.add(new ColumnSegment(0, 0, new IdentifierValue("id")));
        ColumnAssignmentSegment assignment = new ColumnAssignmentSegment(0, 0, columns, new LiteralExpressionSegment(0, 0, 1));
        result.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.singleton(assignment)));
        return result;
    }
}
