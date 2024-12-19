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

package org.apache.shardingsphere.encrypt.rewrite.condition.impl;

import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class EncryptBinaryConditionTest {
    
    @Test
    void assertNewInstanceWithParameterMarkerExpression() {
        EncryptBinaryCondition actual = new EncryptBinaryCondition(new ColumnSegment(0, 0, new IdentifierValue("col")), null, null, 0, 0, new ParameterMarkerExpressionSegment(0, 0, 1));
        assertThat(actual.getPositionIndexMap(), is(Collections.singletonMap(0, 1)));
        assertTrue(actual.getPositionValueMap().isEmpty());
    }
    
    @Test
    void assertNewInstanceWithLiteralExpression() {
        EncryptBinaryCondition actual = new EncryptBinaryCondition(new ColumnSegment(0, 0, new IdentifierValue("col")), null, null, 0, 0, new LiteralExpressionSegment(0, 0, "foo"));
        assertTrue(actual.getPositionIndexMap().isEmpty());
        assertThat(actual.getPositionValueMap(), is(Collections.singletonMap(0, "foo")));
    }
    
    @Test
    void assertNewInstanceWithConcatFunctionExpression() {
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "CONCAT", "");
        functionSegment.getParameters().add(new LiteralExpressionSegment(0, 0, "foo"));
        functionSegment.getParameters().add(new ParameterMarkerExpressionSegment(0, 0, 1));
        functionSegment.getParameters().add(mock(ExpressionSegment.class));
        EncryptBinaryCondition actual = new EncryptBinaryCondition(new ColumnSegment(0, 0, new IdentifierValue("col")), null, null, 0, 0, functionSegment);
        assertThat(actual.getPositionIndexMap(), is(Collections.singletonMap(1, 1)));
        assertThat(actual.getPositionValueMap(), is(Collections.singletonMap(0, "foo")));
    }
    
    @Test
    void assertNewInstanceWithNotConcatFunctionExpression() {
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "SUBSTR", "");
        functionSegment.getParameters().add(new LiteralExpressionSegment(0, 0, "foo"));
        functionSegment.getParameters().add(new ParameterMarkerExpressionSegment(0, 0, 1));
        functionSegment.getParameters().add(mock(ExpressionSegment.class));
        EncryptBinaryCondition actual = new EncryptBinaryCondition(new ColumnSegment(0, 0, new IdentifierValue("col")), null, null, 0, 0, functionSegment);
        assertTrue(actual.getPositionIndexMap().isEmpty());
        assertTrue(actual.getPositionValueMap().isEmpty());
    }
}
