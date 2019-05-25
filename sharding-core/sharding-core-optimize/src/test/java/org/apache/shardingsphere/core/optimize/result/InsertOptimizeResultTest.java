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

package org.apache.shardingsphere.core.optimize.result;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResult;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class InsertOptimizeResultTest {
    
    private InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Lists.newArrayList("id", "value", "status"));
    
    @Test
    public void assertAddUnitWithSet() {
        ExpressionSegment[] expressions = {new LiteralExpressionSegment(0, 0, 1), new ParameterMarkerExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, "test")};
        Object[] parameters = {"parameter"};
        insertOptimizeResult.addUnit(expressions, parameters, 1);
        assertThat(insertOptimizeResult.getColumnNames().size(), is(3));
        assertThat(insertOptimizeResult.getUnits().get(0).getValues(), is(expressions));
        assertThat(insertOptimizeResult.getUnits().get(0).getParameters()[0], is((Object) "parameter"));
        assertThat(insertOptimizeResult.getUnits().get(0).getDataNodes().size(), is(0));
        assertThat(insertOptimizeResult.getUnits().get(0).getColumnValue("value"), is((Object) "parameter"));
        assertThat(insertOptimizeResult.getUnits().get(0).getColumnValue("status"), is((Object) "test"));
        insertOptimizeResult.getUnits().get(0).setColumnValue("id", 2);
        assertThat(insertOptimizeResult.getUnits().get(0).getColumnValue("id"), is((Object) 2));
        insertOptimizeResult.getUnits().get(0).setColumnValue("value", "parameter1");
        assertThat(insertOptimizeResult.getUnits().get(0).getColumnValue("value"), is((Object) "parameter1"));
    }
}
