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

package org.apache.shardingsphere.core.optimize.statement.sharding.dml.insert;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.ShardingCondition;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class InsertOptimizedStatementTest {
    
    private ShardingInsertOptimizedStatement insertClauseOptimizedStatement = new ShardingInsertOptimizedStatement(
            new InsertStatement(), Collections.<ShardingCondition>emptyList(), Lists.newArrayList("id", "value", "status"), null);
    
    @Test
    public void assertAddUnitWithSet() {
        ExpressionSegment[] expressions = {new LiteralExpressionSegment(0, 0, 1), new ParameterMarkerExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, "test")};
        Object[] parameters = {"parameter"};
        insertClauseOptimizedStatement.addUnit(expressions, parameters, 1);
        assertThat(insertClauseOptimizedStatement.getColumnNames().size(), is(3));
        assertThat(insertClauseOptimizedStatement.getUnits().get(0).getValues(), is(expressions));
        assertThat(insertClauseOptimizedStatement.getUnits().get(0).getParameters()[0], is((Object) "parameter"));
        assertThat(insertClauseOptimizedStatement.getUnits().get(0).getDataNodes().size(), is(0));
        assertThat(insertClauseOptimizedStatement.getUnits().get(0).getColumnValue("value"), is((Object) "parameter"));
        assertThat(insertClauseOptimizedStatement.getUnits().get(0).getColumnValue("status"), is((Object) "test"));
        insertClauseOptimizedStatement.getUnits().get(0).setColumnValue("id", 2);
        assertThat(insertClauseOptimizedStatement.getUnits().get(0).getColumnValue("id"), is((Object) 2));
        insertClauseOptimizedStatement.getUnits().get(0).setColumnValue("value", "parameter1");
        assertThat(insertClauseOptimizedStatement.getUnits().get(0).getColumnValue("value"), is((Object) "parameter1"));
    }
}
