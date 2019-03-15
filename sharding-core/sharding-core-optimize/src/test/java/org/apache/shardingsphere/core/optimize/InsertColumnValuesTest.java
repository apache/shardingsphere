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

package org.apache.shardingsphere.core.optimize;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.optimize.result.InsertColumnValues;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLTextExpression;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InsertColumnValuesTest {
    
    private InsertColumnValues insertColumnValuesWithSet = new InsertColumnValues(DefaultKeyword.SET, Lists.newArrayList("id", "value", "status"));
    
    private InsertColumnValues insertColumnValuesWithValues = new InsertColumnValues(DefaultKeyword.VALUES, Lists.newArrayList("id", "value", "status"));
    
    @Test
    public void assertAddInsertColumnValueWithSet() {
        List<SQLExpression> expressions = new LinkedList<>();
        expressions.add(new SQLNumberExpression(1));
        expressions.add(new SQLPlaceholderExpression(1));
        expressions.add(new SQLTextExpression("test"));
        insertColumnValuesWithSet.addInsertColumnValue(expressions, Collections.singletonList((Object) "parameter"));
        assertThat(insertColumnValuesWithSet.getColumnNames().size(), is(3));
        assertThat(insertColumnValuesWithSet.getType(), is(DefaultKeyword.SET));
        assertThat(insertColumnValuesWithSet.getColumnValues().get(0).getValues(), is(expressions));
        assertThat(insertColumnValuesWithSet.getColumnValues().get(0).getParameters().get(0), is((Object) "parameter"));
        assertThat(insertColumnValuesWithSet.getColumnValues().get(0).getDataNodes().size(), is(0));
        assertThat(insertColumnValuesWithSet.getColumnValues().get(0).getColumnValue("value"), is((Object) "parameter"));
        assertThat(insertColumnValuesWithSet.getColumnValues().get(0).getColumnValue("status"), is((Object) "test"));
        assertThat(insertColumnValuesWithSet.getColumnValues().get(0).toString(), is("id = 1, value = ?, status = 'test'"));
        insertColumnValuesWithSet.getColumnValues().get(0).setColumnValue("id", 2);
        assertThat(insertColumnValuesWithSet.getColumnValues().get(0).getColumnValue("id"), is((Object) 2));
        insertColumnValuesWithSet.getColumnValues().get(0).setColumnValue("value", "parameter1");
        assertThat(insertColumnValuesWithSet.getColumnValues().get(0).getColumnValue("value"), is((Object) "parameter1"));
    }
    
    @Test
    public void assertAddInsertColumnValueWithValues() {
        List<SQLExpression> expressions = new LinkedList<>();
        expressions.add(new SQLNumberExpression(1));
        expressions.add(new SQLPlaceholderExpression(1));
        expressions.add(new SQLTextExpression("test"));
        insertColumnValuesWithValues.addInsertColumnValue(expressions, Collections.singletonList((Object) "parameter"));
        assertThat(insertColumnValuesWithValues.getColumnValues().get(0).toString(), is("(1, ?, 'test')"));
    }
}
