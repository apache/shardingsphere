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

package org.apache.shardingsphere.core.rewrite.token;

import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.SelectItem;
import org.apache.shardingsphere.core.parse.sql.segment.dml.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SQLTokenGenerateEngineTest {
    
    private SQLTokenGenerateEngine sqlTokenGenerateEngine = new ShardingTokenGenerateEngine();
    
    @Before
    public void setUp() {
        SelectItemsSegment selectItemsSegment = mock(SelectItemsSegment.class);
        when(selectItemsSegment.getStartIndex()).thenReturn(1);
        when(selectItemsSegment.getSelectItems()).thenReturn(Collections.<SelectItemSegment>emptyList());
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getSQLSegments().add(selectItemsSegment);
        ShardingSelectOptimizedStatement optimizedStatement = 
                new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), null, Collections.<SelectItem>emptyList());
        
    }
    
    @Test
    public void testGenerateSQLTokens() {
    }
    
    @Test
    public void testGetSQLTokenGenerators() {
    }
}
