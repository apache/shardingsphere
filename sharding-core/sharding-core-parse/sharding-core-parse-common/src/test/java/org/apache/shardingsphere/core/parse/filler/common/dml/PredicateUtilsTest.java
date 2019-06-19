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

import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.complex.SubquerySegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class PredicateUtilsTest {
    
    @Test
    public void assertCreateInConditionOne() {
        assertFalse(
                PredicateUtils.createInCondition(new PredicateInRightValue(Collections.<ExpressionSegment>singletonList(new SubquerySegment(0, 0, "test"))), new Column(null, ""), null).isPresent());
    }
    
    @Test
    public void assertCreateInConditionTwo() {
        assertThat(PredicateUtils.createInCondition(
                new PredicateInRightValue(Collections.<ExpressionSegment>singletonList(new ParameterMarkerExpressionSegment(0, 0, 1))), new Column("id", "tbl"), null).toString(),
                is("Optional.of(Condition(column=Column(name=id, tableName=tbl), predicateSegment=null, operator=IN, positionValueMap={}, positionIndexMap={0=1}))"));
    }
    
    @Test
    public void assertCreateCompareConditionWithNonNull() {
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment = new ParameterMarkerExpressionSegment(0, 0, 1);
        PredicateCompareRightValue predicateCompareRightValue = new PredicateCompareRightValue("=", parameterMarkerExpressionSegment);
        assertThat(PredicateUtils.createCompareCondition(predicateCompareRightValue, new Column("id", "tbl"), null).toString(),
                is("Optional.of(Condition(column=Column(name=id, tableName=tbl), predicateSegment=null, operator=EQUAL, positionValueMap={}, positionIndexMap={0=1}))"));
    }
    
    @Test
    public void assertCreateCompareConditionWithNull() {
        assertFalse(PredicateUtils.createCompareCondition(new PredicateCompareRightValue("=", null), null, null).isPresent());
    }
}
