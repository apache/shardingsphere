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

package org.apache.shardingsphere.sharding.route.engine.checker.ddl;

import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateViewStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedCreateViewException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingCreateViewRouteContextCheckerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RouteContext routeContext;
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private CreateViewStatementContext createViewStatementContext;
    
    @Mock
    private CreateViewStatement createViewStatement;
    
    @Mock
    private SelectStatement selectStatement;
    
    @Mock
    private QueryContext queryContext;
    
    @BeforeEach
    void setUp() {
        when(createViewStatementContext.getSqlStatement()).thenReturn(createViewStatement);
        when(createViewStatement.getSelect()).thenReturn(selectStatement);
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))));
        when(createViewStatement.getView()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("order_view"))));
        when(routeContext.getRouteUnits().size()).thenReturn(2);
    }
    
    @Test
    void assertCheck() {
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(queryContext.getSqlStatementContext()).thenReturn(createViewStatementContext);
        assertDoesNotThrow(() -> new ShardingCreateViewRouteContextChecker().check(shardingRule, queryContext, mock(ShardingSphereDatabase.class), mock(ConfigurationProperties.class), routeContext));
    }
    
    @Test
    void assertCheckWithException() {
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.isDistinctRow()).thenReturn(true);
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        when(queryContext.getSqlStatementContext()).thenReturn(createViewStatementContext);
        assertThrows(UnsupportedCreateViewException.class,
                () -> new ShardingCreateViewRouteContextChecker().check(shardingRule, queryContext, mock(ShardingSphereDatabase.class), mock(ConfigurationProperties.class), routeContext));
    }
}
