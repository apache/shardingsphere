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

package org.apache.shardingsphere.shadow.route.retriever.dml.table.column.impl;

import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.route.util.ShadowExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShadowExtractor.class)
class ShadowDeleteStatementDataSourceMappingsRetrieverTest {
    
    @Test
    void assertRetrieve() {
        DeleteStatementContext sqlStatementContext = mock(DeleteStatementContext.class, RETURNS_DEEP_STUBS);
        WhereSegment whereSegment = mock(WhereSegment.class);
        ExpressionSegment expressionSegment = mock(ExpressionSegment.class);
        when(whereSegment.getExpr()).thenReturn(expressionSegment);
        when(sqlStatementContext.getWhereSegments()).thenReturn(Collections.singleton(whereSegment));
        when(ShadowExtractor.extractValues(expressionSegment, Collections.singletonList("foo"))).thenReturn(Optional.of(Collections.singleton("foo")));
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singleton("foo_tbl"));
        ShadowDeleteStatementDataSourceMappingsRetriever retriever = new ShadowDeleteStatementDataSourceMappingsRetriever(sqlStatementContext, Collections.singletonList("foo"));
        Collection<ShadowColumnCondition> actual = retriever.getShadowColumnConditions("foo_col");
        Collection<ShadowColumnCondition> expected = Collections.singletonList(new ShadowColumnCondition("foo_tbl", "foo_col", Collections.singleton("foo")));
        assertThat(actual, deepEqual(expected));
    }
}
