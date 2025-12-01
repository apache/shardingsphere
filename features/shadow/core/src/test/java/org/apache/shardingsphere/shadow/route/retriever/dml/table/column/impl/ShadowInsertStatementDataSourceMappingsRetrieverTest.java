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

import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.exception.syntax.UnsupportedShadowInsertValueException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShadowInsertStatementDataSourceMappingsRetrieverTest {
    
    @Test
    void assertRetrieve() {
        InsertStatementContext sqlStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getInsertColumnNames()).thenReturn(Arrays.asList("foo_col", "bar_col"));
        InsertValueContext insertValueContext = mock(InsertValueContext.class);
        when(insertValueContext.getLiteralValue(0)).thenReturn(Optional.of("foo"));
        when(sqlStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singleton("foo_tbl"));
        ShadowInsertStatementDataSourceMappingsRetriever retriever = new ShadowInsertStatementDataSourceMappingsRetriever(sqlStatementContext);
        Collection<ShadowColumnCondition> actual = retriever.getShadowColumnConditions("foo_col");
        Collection<ShadowColumnCondition> expected = Collections.singletonList(new ShadowColumnCondition("foo_tbl", "foo_col", Collections.singletonList("foo")));
        assertThat(actual, deepEqual(expected));
    }
    
    @Test
    void assertRetrieveWithoutLiteralShadowValue() {
        InsertStatementContext sqlStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getInsertColumnNames()).thenReturn(Arrays.asList("foo_col", "bar_col"));
        InsertValueContext insertValueContext = mock(InsertValueContext.class);
        when(insertValueContext.getLiteralValue(0)).thenReturn(Optional.empty());
        when(sqlStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        ShadowInsertStatementDataSourceMappingsRetriever retriever = new ShadowInsertStatementDataSourceMappingsRetriever(sqlStatementContext);
        assertThrows(UnsupportedShadowInsertValueException.class, () -> retriever.getShadowColumnConditions("foo_col"));
    }
    
    @Test
    void assertRetrieveWithNotComparableLiteralShadowValue() {
        InsertStatementContext sqlStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getInsertColumnNames()).thenReturn(Arrays.asList("foo_col", "bar_col"));
        InsertValueContext insertValueContext = mock(InsertValueContext.class);
        when(insertValueContext.getLiteralValue(0)).thenReturn(Optional.of(new Object()));
        when(sqlStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        ShadowInsertStatementDataSourceMappingsRetriever retriever = new ShadowInsertStatementDataSourceMappingsRetriever(sqlStatementContext);
        assertThrows(UnsupportedShadowInsertValueException.class, () -> retriever.getShadowColumnConditions("foo_col"));
    }
}
