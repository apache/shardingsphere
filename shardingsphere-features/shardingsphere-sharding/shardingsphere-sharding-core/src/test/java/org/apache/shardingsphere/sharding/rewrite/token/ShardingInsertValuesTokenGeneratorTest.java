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

package org.apache.shardingsphere.sharding.rewrite.token;

import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertValuesToken;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingInsertValuesTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingInsertValue;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingInsertValuesTokenGeneratorTest {

    @Test
    public void assertIsGenerateSQLToken() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        ShardingInsertValuesTokenGenerator shardingInsertValuesTokenGenerator = new ShardingInsertValuesTokenGenerator();
        assertFalse(shardingInsertValuesTokenGenerator.isGenerateSQLToken(selectStatementContext));
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getSqlStatement().getValues().isEmpty()).thenReturn(Boolean.TRUE);
        assertFalse(shardingInsertValuesTokenGenerator.isGenerateSQLToken(insertStatementContext));
        when(insertStatementContext.getSqlStatement().getValues().isEmpty()).thenReturn(Boolean.FALSE);
        assertTrue(shardingInsertValuesTokenGenerator.isGenerateSQLToken(insertStatementContext));
    }

    @Test
    public void assertGenerateSQLToken() {
        List<ExpressionSegment> expressionSegmentList = new LinkedList<>();
        InsertValuesSegment insertValuesSegment = new InsertValuesSegment(1, 2, expressionSegmentList);
        Collection<InsertValuesSegment> insertValuesSegmentCollection = new LinkedList<>();
        insertValuesSegmentCollection.add(insertValuesSegment);
        InsertValueContext insertValueContext = new InsertValueContext(expressionSegmentList, null, 4);
        List<InsertValueContext> insertValueContextList = new LinkedList<>();
        insertValueContextList.add(insertValueContext);
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(insertValueContextList);
        when(insertStatementContext.getSqlStatement().getValues()).thenReturn(insertValuesSegmentCollection);
        ShardingInsertValuesTokenGenerator shardingInsertValuesTokenGenerator = new ShardingInsertValuesTokenGenerator();
        InsertValuesToken insertValuesToken = shardingInsertValuesTokenGenerator.generateSQLToken(insertStatementContext);
        assertThat(insertValuesToken.getInsertValues().size(), is(1));
        Collection<DataNode> dataNodes = new LinkedList<>();
        final String testDatasource = "testDatasource";
        final String testTable = "testTable";
        dataNodes.add(new DataNode(testDatasource, testTable));
        RouteContext routeContext = new RouteContext();
        routeContext.getOriginalDataNodes().add(dataNodes);
        shardingInsertValuesTokenGenerator.setRouteContext(routeContext);
        insertValuesToken = shardingInsertValuesTokenGenerator.generateSQLToken(insertStatementContext);
        assertThat(insertValuesToken.getInsertValues().get(0), instanceOf(ShardingInsertValue.class));
        ShardingInsertValue generatedShardingInsertValue = (ShardingInsertValue) insertValuesToken.getInsertValues().get(0);
        assertThat((new LinkedList<>(generatedShardingInsertValue.getDataNodes())).get(0).getDataSourceName(), is(testDatasource));
        assertThat((new LinkedList<>(generatedShardingInsertValue.getDataNodes())).get(0).getTableName(), is(testTable));
    }
}
