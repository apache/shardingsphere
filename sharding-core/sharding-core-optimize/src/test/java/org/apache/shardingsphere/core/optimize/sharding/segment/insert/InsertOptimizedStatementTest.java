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

package org.apache.shardingsphere.core.optimize.sharding.segment.insert;

import org.apache.shardingsphere.core.optimize.api.segment.InsertValue;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class InsertOptimizedStatementTest {
    
    @Test
    public void assertAddInsertValueWithSet() {
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizedStatement(
                new InsertStatement(), Collections.<ShardingCondition>emptyList(), Arrays.asList("id", "value", "status"), null);
        ExpressionSegment assignment1 = new LiteralExpressionSegment(0, 0, 1);
        ExpressionSegment assignment2 = new ParameterMarkerExpressionSegment(0, 0, 1);
        ExpressionSegment assignment3 = new LiteralExpressionSegment(0, 0, "test");
        actual.getInsertValues().add(new InsertValue(Arrays.asList(assignment1, assignment2, assignment3), 1, Collections.<Object>singletonList("parameter"), 0));
        assertThat(actual.getInsertValues().get(0).getValueExpressions().size(), is(3));
        assertThat(actual.getInsertValues().get(0).getValueExpressions().get(0), is(assignment1));
        assertThat(actual.getInsertValues().get(0).getValueExpressions().get(1), is(assignment2));
        assertThat(actual.getInsertValues().get(0).getValueExpressions().get(2), is(assignment3));
        assertThat(actual.getInsertValues().get(0).getParameters().get(0), is((Object) "parameter"));
        assertThat(actual.getInsertValues().get(0).getDataNodes().size(), is(0));
        assertThat(actual.getInsertValues().get(0).getValue(1), is((Object) "parameter"));
        assertThat(actual.getInsertValues().get(0).getValue(2), is((Object) "test"));
        actual.getInsertValues().get(0).setValue(0, 2);
        assertThat(actual.getInsertValues().get(0).getValue(0), is((Object) 2));
        actual.getInsertValues().get(0).setValue(1, "parameter1");
        assertThat(actual.getInsertValues().get(0).getValue(1), is((Object) "parameter1"));
    }
}
