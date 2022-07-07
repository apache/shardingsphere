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
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;

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
        ShardingInsertValuesTokenGenerator generator = new ShardingInsertValuesTokenGenerator();
        assertFalse(generator.isGenerateSQLToken(mock(SelectStatementContext.class)));
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getSqlStatement().getValues().isEmpty()).thenReturn(Boolean.TRUE);
        assertFalse(generator.isGenerateSQLToken(insertStatementContext));
        when(insertStatementContext.getSqlStatement().getValues().isEmpty()).thenReturn(Boolean.FALSE);
        assertTrue(generator.isGenerateSQLToken(insertStatementContext));
    }
    
    @Test
    public void assertGenerateSQLToken() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(new InsertValueContext(Collections.emptyList(), Collections.emptyList(), 4)));
        when(insertStatementContext.getSqlStatement().getValues()).thenReturn(Collections.singleton(new InsertValuesSegment(1, 2, Collections.emptyList())));
        ShardingInsertValuesTokenGenerator generator = new ShardingInsertValuesTokenGenerator();
        InsertValuesToken insertValuesToken = generator.generateSQLToken(insertStatementContext);
        assertThat(insertValuesToken.getInsertValues().size(), is(1));
        String testDataSource = "testDataSource";
        String testTable = "testTable";
        RouteContext routeContext = new RouteContext();
        routeContext.getOriginalDataNodes().add(Collections.singleton(new DataNode(testDataSource, testTable)));
        generator.setRouteContext(routeContext);
        insertValuesToken = generator.generateSQLToken(insertStatementContext);
        assertThat(insertValuesToken.getInsertValues().get(0), instanceOf(ShardingInsertValue.class));
        ShardingInsertValue generatedShardingInsertValue = (ShardingInsertValue) insertValuesToken.getInsertValues().get(0);
        assertThat((new LinkedList<>(generatedShardingInsertValue.getDataNodes())).get(0).getDataSourceName(), is(testDataSource));
        assertThat((new LinkedList<>(generatedShardingInsertValue.getDataNodes())).get(0).getTableName(), is(testTable));
    }
}
