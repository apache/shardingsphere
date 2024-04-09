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

package org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ExpressionRowNumberValueSegmentTest {
    
    @Test
    void assertGetValueWithLiteralExpression() {
        ExpressionRowNumberValueSegment actual = new ExpressionRowNumberValueSegment(0, 0, new BinaryOperationExpression(0, 0, new LiteralExpressionSegment(0, 0, 1),
                new LiteralExpressionSegment(0, 0, 1), "+", "1 + 1"), false);
        assertThat(actual.getValue(Collections.emptyList()), is(2L));
    }
    
    @Test
    void assertGetValueWithParameterMarker() {
        ExpressionRowNumberValueSegment actual = new ExpressionRowNumberValueSegment(0, 0, new BinaryOperationExpression(0, 0, new ParameterMarkerExpressionSegment(0, 0, 0),
                new ParameterMarkerExpressionSegment(0, 0, 1), "+", "? + ?"), false);
        assertThat(actual.getValue(Arrays.asList(1, 1)), is(2L));
    }
    
    @Test
    void assertGetValueWithMixed() {
        ExpressionRowNumberValueSegment actual = new ExpressionRowNumberValueSegment(0, 0, new BinaryOperationExpression(0, 0, new LiteralExpressionSegment(0, 0, 1),
                new BinaryOperationExpression(0, 0, new ParameterMarkerExpressionSegment(0, 0, 0), new LiteralExpressionSegment(0, 0, 2), "+", "? + 2"), "+", "1 + ? + 2"), false);
        assertThat(actual.getValue(Collections.singletonList(1)), is(4L));
    }
}
