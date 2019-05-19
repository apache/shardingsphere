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
import org.apache.shardingsphere.core.parse.sql.context.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.sql.context.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.sql.context.expression.SQLParameterMarkerExpression;
import org.apache.shardingsphere.core.parse.sql.context.expression.SQLTextExpression;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class InsertOptimizeResultTest {
    
    private InsertOptimizeResult setInsertOptimizeResult = new InsertOptimizeResult(Lists.newArrayList("id", "value", "status"));
    
    private InsertOptimizeResult valuesInsertOptimizeResult = new InsertOptimizeResult(Lists.newArrayList("id", "value", "status"));
    
    @Test
    public void assertAddUnitWithSet() {
        SQLExpression[] expressions = {new SQLNumberExpression(1), new SQLParameterMarkerExpression(1), new SQLTextExpression("test")};
        Object[] parameters = {"parameter"};
        setInsertOptimizeResult.addUnit(expressions, parameters, 1);
        assertThat(setInsertOptimizeResult.getColumnNames().size(), is(3));
        assertThat(setInsertOptimizeResult.getUnits().get(0).getValues(), is(expressions));
        assertThat(setInsertOptimizeResult.getUnits().get(0).getParameters()[0], is((Object) "parameter"));
        assertThat(setInsertOptimizeResult.getUnits().get(0).getDataNodes().size(), is(0));
        assertThat(setInsertOptimizeResult.getUnits().get(0).getColumnValue("value"), is((Object) "parameter"));
        assertThat(setInsertOptimizeResult.getUnits().get(0).getColumnValue("status"), is((Object) "test"));
        assertThat(setInsertOptimizeResult.getUnits().get(0).toString(), is("id = 1, value = ?, status = 'test'"));
        setInsertOptimizeResult.getUnits().get(0).setColumnValue("id", 2);
        assertThat(setInsertOptimizeResult.getUnits().get(0).getColumnValue("id"), is((Object) 2));
        setInsertOptimizeResult.getUnits().get(0).setColumnValue("value", "parameter1");
        assertThat(setInsertOptimizeResult.getUnits().get(0).getColumnValue("value"), is((Object) "parameter1"));
    }
    
    @Test
    public void assertAddUnitWithValues() {
        SQLExpression[] expressions = {new SQLNumberExpression(1), new SQLParameterMarkerExpression(1), new SQLTextExpression("test")};
        Object[] parameters = {"parameter"};
        valuesInsertOptimizeResult.addUnit(expressions, parameters, 1);
        assertThat(valuesInsertOptimizeResult.getUnits().get(0).toString(), is("(1, ?, 'test')"));
    }
}
