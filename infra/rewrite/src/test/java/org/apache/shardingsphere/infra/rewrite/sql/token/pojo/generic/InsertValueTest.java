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

package org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic;

import org.apache.shardingsphere.sql.parser.sql.common.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class InsertValueTest {
    
    @Test
    void assertToString() {
        List<ExpressionSegment> expressionSegments = new ArrayList<>(4);
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment = new ParameterMarkerExpressionSegment(1, 1, 1);
        ParameterMarkerExpressionSegment positionalParameterMarkerExpressionSegment = new ParameterMarkerExpressionSegment(1, 1, 0, ParameterMarkerType.DOLLAR);
        LiteralExpressionSegment literalExpressionSegment = new LiteralExpressionSegment(2, 2, "literals");
        ComplexExpressionSegment complexExpressionSegment = new ComplexExpressionSegment() {
            
            @Override
            public String getText() {
                return "complexExpressionSegment";
            }
            
            @Override
            public int getStartIndex() {
                return 3;
            }
            
            @Override
            public int getStopIndex() {
                return 3;
            }
        };
        expressionSegments.add(parameterMarkerExpressionSegment);
        expressionSegments.add(positionalParameterMarkerExpressionSegment);
        expressionSegments.add(literalExpressionSegment);
        expressionSegments.add(complexExpressionSegment);
        expressionSegments.add(new TypeCastExpression(0, 0, "$2::varchar::jsonb", new TypeCastExpression(0, 0, "$2::varchar",
                new ParameterMarkerExpressionSegment(0, 0, 1, ParameterMarkerType.DOLLAR), "varchar"), "jsonb"));
        InsertValue insertValue = new InsertValue(expressionSegments);
        String actualToString = insertValue.toString();
        String expectedToString = "(?, $1, 'literals', complexExpressionSegment, $2::varchar::jsonb)";
        assertThat(actualToString, is(expectedToString));
    }
}
