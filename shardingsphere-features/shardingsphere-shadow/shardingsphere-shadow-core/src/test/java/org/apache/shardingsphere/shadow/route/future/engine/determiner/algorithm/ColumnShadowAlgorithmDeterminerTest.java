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

package org.apache.shardingsphere.shadow.route.future.engine.determiner.algorithm;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnRegexMatchShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ColumnShadowAlgorithmDeterminerTest {
    
    @Test
    public void assertIsShadow() {
        assertTrueCase();
        assertFalseCase();
    }
    
    private void assertFalseCase() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        when(insertStatementContext.getInsertColumnNames()).thenReturn(createColumnNames());
        when(insertStatementContext.getInsertValueContexts()).thenReturn(createInsertValueContextsFalseCase());
        ColumnShadowAlgorithmDeterminer columnShadowAlgorithmDeterminer = new ColumnShadowAlgorithmDeterminer(createColumnShadowAlgorithm());
        assertThat(columnShadowAlgorithmDeterminer.isShadow(insertStatementContext, createRelatedShadowTables(), "t_user"), is(false));
    }
    
    private List<InsertValueContext> createInsertValueContextsFalseCase() {
        List<InsertValueContext> result = new LinkedList<>();
        result.add(new InsertValueContext(createAssignmentsFalseCase(), Lists.newLinkedList(), 0));
        return result;
    }
    
    private Collection<ExpressionSegment> createAssignmentsFalseCase() {
        Collection<ExpressionSegment> result = new LinkedList<>();
        result.add(new LiteralExpressionSegment(0, 1, 1));
        result.add(new LiteralExpressionSegment(2, 6, "jack"));
        result.add(new LiteralExpressionSegment(7, 8, 2));
        return result;
    }
    
    private void assertTrueCase() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        when(insertStatementContext.getInsertColumnNames()).thenReturn(createColumnNames());
        when(insertStatementContext.getInsertValueContexts()).thenReturn(createInsertValueContextsTrueCase());
        ColumnShadowAlgorithmDeterminer columnShadowAlgorithmDeterminer = new ColumnShadowAlgorithmDeterminer(createColumnShadowAlgorithm());
        assertThat(columnShadowAlgorithmDeterminer.isShadow(insertStatementContext, createRelatedShadowTables(), "t_user"), is(true));
    }
    
    private List<InsertValueContext> createInsertValueContextsTrueCase() {
        List<InsertValueContext> result = new LinkedList<>();
        result.add(new InsertValueContext(createAssignmentsTrueCase(), Lists.newLinkedList(), 0));
        return result;
    }
    
    private Collection<ExpressionSegment> createAssignmentsTrueCase() {
        Collection<ExpressionSegment> result = new LinkedList<>();
        result.add(new LiteralExpressionSegment(0, 1, 1));
        result.add(new LiteralExpressionSegment(2, 6, "jack"));
        result.add(new LiteralExpressionSegment(7, 8, 1));
        return result;
    }
    
    private Collection<String> createRelatedShadowTables() {
        Collection<String> result = new LinkedList<>();
        result.add("t_user");
        result.add("t_order");
        return result;
    }
    
    private ColumnShadowAlgorithm<Comparable<?>> createColumnShadowAlgorithm() {
        ColumnRegexMatchShadowAlgorithm result = new ColumnRegexMatchShadowAlgorithm();
        result.setProps(createProperties());
        result.init();
        return result;
    }
    
    private Properties createProperties() {
        Properties properties = new Properties();
        properties.setProperty("column", "age");
        properties.setProperty("operation", "insert");
        properties.setProperty("regex", "[1]");
        return properties;
    }
    
    private List<String> createColumnNames() {
        List<String> result = new LinkedList<>();
        result.add("id");
        result.add("name");
        result.add("age");
        return result;
    }
}
