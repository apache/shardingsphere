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

package org.apache.shardingsphere.core.parse.filler.common.dml;

import static org.junit.Assert.assertEquals;

import java.util.Stack;
import java.util.Vector;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.complex.SubquerySegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.junit.Test;

/**
 * Unit tests for class {@link PredicateUtils}.
 *
 * @see PredicateUtils
 */
public class PredicateUtilsTest {

    @Test
    public void testCreateInConditionOne() {
        Vector<ExpressionSegment> vector = new Vector<>();
        vector.add(new SubquerySegment(-32, -32, "test"));
        Column column = new Column(null, "");
        assertEquals(Optional.<Condition>absent(), PredicateUtils.createInCondition(new PredicateInRightValue(vector), column));
    }

    @Test
    public void testCreateInConditionTwo() {
        Column column = new Column("!fE3s*#0- ece4_ :", "!fE3s*#0- ece4_ :");
        Stack<ExpressionSegment> stack = new Stack<ExpressionSegment>();
        stack.add(new ParameterMarkerExpressionSegment((-3652), (-3652), 0));
        assertEquals("Optional.of(Condition(column=Column(name=!fE3s*#0- ece4_ :, tableName=!fE3s*#0- ece4_ :)," +
                        " operator=IN, compareOperator=null, positionValueMap={}, positionIndexMap={0=0}))",
                PredicateUtils.createInCondition(new PredicateInRightValue(stack), column).toString()
        );
    }

    @Test
    public void testCreateCompareConditionWithNonNull() {
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment = new ParameterMarkerExpressionSegment(0, 0, 0);
        PredicateCompareRightValue predicateCompareRightValue = new PredicateCompareRightValue("Z}4,%s6+6mvg{", parameterMarkerExpressionSegment);
        Column column = new Column("Z}4,%s6+6mvg{ a", "Z}4,%s6+6mvg{");
        assertEquals("Optional.of(Condition(column=Column(name=Z}4,%s6+6mvg" +
                        "{ a, tableName=Z}4,%s6+6mvg{), operator=EQUAL, compareOperator=null, " +
                        "positionValueMap={}, positionIndexMap={0=0}))",
                PredicateUtils.createCompareCondition(predicateCompareRightValue, column).toString());
    }

    @Test
    public void testCreateCompareConditionWithNull() {
        assertEquals(Optional.absent(), PredicateUtils.createCompareCondition(new PredicateCompareRightValue("$VALUES", (ExpressionSegment) null), null));
    }
}
