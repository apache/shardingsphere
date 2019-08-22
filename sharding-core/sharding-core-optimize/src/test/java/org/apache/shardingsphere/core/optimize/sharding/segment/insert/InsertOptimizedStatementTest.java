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

import org.apache.shardingsphere.core.optimize.api.segment.OptimizedInsertValue;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class InsertOptimizedStatementTest {
    
    @Test
    public void assertAddOptimizedInsertValueWithSet() {
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Arrays.asList("id", "value", "status"));
        ShardingInsertOptimizedStatement insertClauseOptimizedStatement = new ShardingInsertOptimizedStatement(new InsertStatement(), Collections.<ShardingCondition>emptyList(), insertColumns, null);
        ExpressionSegment[] expressions = {new LiteralExpressionSegment(0, 0, 1), new ParameterMarkerExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, "test")};
        Object[] parameters = {"parameter"};
        OptimizedInsertValue optimizedInsertValue = insertClauseOptimizedStatement.createOptimizedInsertValue("id", Collections.<String>emptyList(), expressions, parameters, 1);
        insertClauseOptimizedStatement.addOptimizedInsertValue(optimizedInsertValue);
        assertThat(insertClauseOptimizedStatement.getOptimizedInsertValues().get(0).getValueExpressions(), is(expressions));
        assertThat(insertClauseOptimizedStatement.getOptimizedInsertValues().get(0).getParameters()[0], is((Object) "parameter"));
        assertThat(insertClauseOptimizedStatement.getOptimizedInsertValues().get(0).getDataNodes().size(), is(0));
        assertThat(insertClauseOptimizedStatement.getOptimizedInsertValues().get(0).getValue("value"), is((Object) "parameter"));
        assertThat(insertClauseOptimizedStatement.getOptimizedInsertValues().get(0).getValue("status"), is((Object) "test"));
        insertClauseOptimizedStatement.getOptimizedInsertValues().get(0).setValue("id", 2);
        assertThat(insertClauseOptimizedStatement.getOptimizedInsertValues().get(0).getValue("id"), is((Object) 2));
        insertClauseOptimizedStatement.getOptimizedInsertValues().get(0).setValue("value", "parameter1");
        assertThat(insertClauseOptimizedStatement.getOptimizedInsertValues().get(0).getValue("value"), is((Object) "parameter1"));
    }
}
