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

package org.apache.shardingsphere.core.parsing.parser.token;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class InsertValuesTokenTest {
    
    private InsertValuesToken insertValuesTokeWithSet = new InsertValuesToken(1, DefaultKeyword.SET);
    
    private InsertValuesToken insertValuesTokeWithValue = new InsertValuesToken(1, DefaultKeyword.VALUES);
    
    @Before
    public void setUp() {
        insertValuesTokeWithSet.getColumnNames().addAll(Lists.newArrayList("id", "value", "status"));
        insertValuesTokeWithValue.getColumnNames().addAll(Lists.newArrayList("id", "value", "status"));
    }
    
    @Test
    public void assertAddInsertColumnValueWithSet() {
        List<SQLExpression> expressions = new LinkedList<>();
        expressions.add(new SQLNumberExpression(1));
        expressions.add(new SQLPlaceholderExpression(1));
        expressions.add(new SQLTextExpression("test"));
        insertValuesTokeWithSet.addInsertColumnValue(expressions, Collections.singletonList((Object) "parameter"));
        
    }
    
    @Test
    public void assertGetColumnName() {
        
    }
}
