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

package org.apache.shardingsphere.sharding.checker.sql.ddl;

import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateViewStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.exception.metadata.EngagedViewException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingCreateViewSupportedCheckerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RouteContext routeContext;
    
    @Mock
    private ShardingRule rule;
    
    @Mock
    private CreateViewStatementContext createViewStatementContext;
    
    @Mock
    private CreateViewStatement createViewStatement;
    
    @Mock
    private SelectStatement selectStatement;
    
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
        assertDoesNotThrow(() -> new ShardingCreateViewSupportedChecker().check(rule, mock(), mock(), createViewStatementContext));
    }
    
    @Test
    void assertCheckWithException() {
        when(rule.isShardingTable(any())).thenReturn(true);
        when(rule.isAllConfigBindingTables(any())).thenReturn(false);
        assertThrows(EngagedViewException.class, () -> new ShardingCreateViewSupportedChecker().check(rule, mock(), mock(), createViewStatementContext));
    }
}
