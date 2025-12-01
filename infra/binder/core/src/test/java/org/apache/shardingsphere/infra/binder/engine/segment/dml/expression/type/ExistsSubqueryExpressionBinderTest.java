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
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(SubquerySegmentBinder.class)
class ExistsSubqueryExpressionBinderTest {
    
    @Test
    void assertBind() {
        SubquerySegment subquerySegment = new SubquerySegment(0, 5, mock(SelectStatement.class), "SELECT 1");
        ExistsSubqueryExpression segment = new ExistsSubqueryExpression(1, 10, subquerySegment);
        segment.setNot(true);
        SubquerySegment boundSubquerySegment = new SubquerySegment(0, 5, mock(SelectStatement.class), "SELECT 1");
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        when(SubquerySegmentBinder.bind(subquerySegment, binderContext, tableBinderContexts)).thenReturn(boundSubquerySegment);
        ExistsSubqueryExpression actual = ExistsSubqueryExpressionBinder.bind(segment, binderContext, tableBinderContexts);
        assertThat(actual.getStartIndex(), is(1));
        assertThat(actual.getStopIndex(), is(10));
        assertThat(actual.getSubquery(), is(boundSubquerySegment));
        assertTrue(actual.isNot());
    }
}
