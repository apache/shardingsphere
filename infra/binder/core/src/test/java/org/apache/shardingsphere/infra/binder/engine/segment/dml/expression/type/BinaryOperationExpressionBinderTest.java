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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExpressionSegmentBinder.class)
class BinaryOperationExpressionBinderTest {
    
    @Test
    void assertBind() {
        ExpressionSegment leftSegment = mock(ExpressionSegment.class);
        ExpressionSegment rightSegment = mock(ExpressionSegment.class);
        BinaryOperationExpression segment = new BinaryOperationExpression(3, 9, leftSegment, rightSegment, "+", "a + b");
        ExpressionSegment boundLeftSegment = mock(ExpressionSegment.class);
        ExpressionSegment boundRightSegment = mock(ExpressionSegment.class);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        when(ExpressionSegmentBinder.bind(leftSegment, SegmentType.PREDICATE, binderContext, tableBinderContexts, outerTableBinderContexts)).thenReturn(boundLeftSegment);
        when(ExpressionSegmentBinder.bind(rightSegment, SegmentType.PREDICATE, binderContext, tableBinderContexts, outerTableBinderContexts)).thenReturn(boundRightSegment);
        BinaryOperationExpression actual = BinaryOperationExpressionBinder.bind(segment, SegmentType.PREDICATE, binderContext, tableBinderContexts, outerTableBinderContexts);
        assertThat(actual.getStartIndex(), is(3));
        assertThat(actual.getStopIndex(), is(9));
        assertThat(actual.getLeft(), is(boundLeftSegment));
        assertThat(actual.getRight(), is(boundRightSegment));
        assertThat(actual.getOperator(), is("+"));
        assertThat(actual.getText(), is("a + b"));
    }
}
