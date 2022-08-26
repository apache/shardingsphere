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

package org.apache.shardingsphere.sharding.route.engine.validator.ddl;

import org.apache.shardingsphere.infra.binder.statement.ddl.CreateViewStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.exception.EngagedViewException;
import org.apache.shardingsphere.sharding.exception.UnsupportedCreateViewException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingCreateViewStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingCreateViewStatementValidatorTest {
    
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
    
    @Before
    public void setUp() {
        when(createViewStatementContext.getSqlStatement()).thenReturn(createViewStatement);
        when(createViewStatement.getSelect()).thenReturn(Optional.of(selectStatement));
        when(selectStatement.getFrom()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        when(createViewStatement.getView()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("order_view"))));
        when(routeContext.getRouteUnits().size()).thenReturn(2);
    }
    
    @Test
    public void assertPreValidateCreateView() {
        when(shardingRule.isShardingTable(any())).thenReturn(true);
        when(shardingRule.isAllBindingTables(any())).thenReturn(true);
        new ShardingCreateViewStatementValidator().preValidate(shardingRule, createViewStatementContext, Collections.emptyList(), mock(ShardingSphereDatabase.class));
    }
    
    @Test(expected = EngagedViewException.class)
    public void assertPreValidateCreateViewWithException() {
        when(shardingRule.isShardingTable(any())).thenReturn(true);
        when(shardingRule.isAllBindingTables(any())).thenReturn(false);
        new ShardingCreateViewStatementValidator().preValidate(shardingRule, createViewStatementContext, Collections.emptyList(), mock(ShardingSphereDatabase.class));
    }
    
    @Test
    public void assertPostValidateCreateView() {
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        new ShardingCreateViewStatementValidator().postValidate(shardingRule, createViewStatementContext, Collections.emptyList(), mock(ShardingSphereDatabase.class),
                mock(ConfigurationProperties.class), routeContext);
    }
    
    @Test(expected = UnsupportedCreateViewException.class)
    public void assertPostValidateCreateViewWithException() {
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.isDistinctRow()).thenReturn(true);
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        new ShardingCreateViewStatementValidator().postValidate(
                shardingRule, createViewStatementContext, Collections.emptyList(), mock(ShardingSphereDatabase.class), mock(ConfigurationProperties.class), routeContext);
    }
    
    @Test(expected = EngagedViewException.class)
    public void assertPreValidateCreateViewWithBroadcastTable() {
        when(shardingRule.isAllBroadcastTables(any())).thenReturn(true);
        when(shardingRule.isBroadcastTable("order_view")).thenReturn(false);
        new ShardingCreateViewStatementValidator().preValidate(shardingRule, createViewStatementContext, Collections.emptyList(), mock(ShardingSphereDatabase.class));
    }
}
