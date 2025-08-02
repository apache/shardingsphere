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

import com.google.common.collect.LinkedHashMultimap;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class NotExpressionBinderTest {
    
    @Test
    void assertBind() {
        NotExpression notExpression = new NotExpression(0, 10, new LiteralExpressionSegment(0, 0, "test"), true);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        NotExpression actual = NotExpressionBinder.bind(notExpression, SegmentType.PROJECTION, binderContext, LinkedHashMultimap.create(), LinkedHashMultimap.create());
        assertThat(actual.getNotSign(), is(notExpression.getNotSign()));
        assertThat(actual.getStartIndex(), is(notExpression.getStartIndex()));
        assertThat(actual.getStopIndex(), is(notExpression.getStopIndex()));
        assertThat(actual.getText(), is(notExpression.getText()));
        assertThat(actual.getExpression(), is(notExpression.getExpression()));
    }
}
