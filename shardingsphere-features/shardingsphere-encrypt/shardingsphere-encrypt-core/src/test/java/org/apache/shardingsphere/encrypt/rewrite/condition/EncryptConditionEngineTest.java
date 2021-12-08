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

package org.apache.shardingsphere.encrypt.rewrite.condition;

import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptConditionEngineTest {

    @InjectMocks
    private EncryptConditionEngine encryptConditionEngine;

    @Mock
    private SQLStatementContext sqlStatementContext;

    @Mock
    private InsertStatementContext insertStatementContext;

    @Mock
    private InsertSelectContext insertSelectContext;

    @Mock
    private SelectStatementContext selectStatementContext;

    @Mock
    private TablesContext tablesContext;

    @Test
    public void createEncryptConditionsWithEmptyContextTest() {
        final Collection<EncryptCondition> encryptConditions = encryptConditionEngine.createEncryptConditions(sqlStatementContext);
        assertEquals(0, encryptConditions.size());
    }

    @Test
    public void createEncryptConditionsFromInsertStatementContextTest() {
        when(insertStatementContext.getInsertSelectContext()).thenReturn(insertSelectContext);
        final Collection<EncryptCondition> encryptConditions = encryptConditionEngine.createEncryptConditions(insertStatementContext);
        assertEquals(0, encryptConditions.size());
    }

    @Test
    public void createEncryptConditionsFromInsertIncludingSelectWithoutWhereTest() {
        when(insertStatementContext.getInsertSelectContext()).thenReturn(insertSelectContext);
        when(insertSelectContext.getSelectStatementContext()).thenReturn(selectStatementContext);
        final Collection<EncryptCondition> encryptConditions = encryptConditionEngine.createEncryptConditions(insertStatementContext);
        assertEquals(0, encryptConditions.size());
    }

    @Test
    public void createEncryptConditionsFromInsertIncludingSelectWhereTest() {
        when(insertStatementContext.getInsertSelectContext()).thenReturn(insertSelectContext);
        when(insertSelectContext.getSelectStatementContext()).thenReturn(selectStatementContext);
        ExpressionSegment expressionSegment = new InExpression(0, 0, null, null, false);
        WhereSegment whereSegment = new WhereSegment(0, 1, expressionSegment);
        when(selectStatementContext.getWhere()).thenReturn(Optional.of(whereSegment));
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        final Collection<EncryptCondition> encryptConditions = encryptConditionEngine.createEncryptConditions(insertStatementContext);
        assertEquals(0, encryptConditions.size());
    }

    @Test
    public void createEncryptConditionsFromInsertTest() {
        when(insertStatementContext.getInsertSelectContext()).thenReturn(insertSelectContext);
        when(selectStatementContext.getSchemaName()).thenReturn("schema");
        when(insertSelectContext.getSelectStatementContext()).thenReturn(selectStatementContext);
        ExpressionSegment expressionSegment = new InExpression(0, 0, null, null, false);
        WhereSegment whereSegment = new WhereSegment(0, 1, expressionSegment);
        when(selectStatementContext.getWhere()).thenReturn(Optional.of(whereSegment));

        Map<String, String> columnTablesNames = new HashMap<>();
        columnTablesNames.put("table1", "column1");

        when(tablesContext.findTableName(anyList(), any())).thenReturn(columnTablesNames);
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        final Collection<EncryptCondition> encryptConditions = encryptConditionEngine.createEncryptConditions(insertStatementContext);
        assertEquals(0, encryptConditions.size());
    }
}
