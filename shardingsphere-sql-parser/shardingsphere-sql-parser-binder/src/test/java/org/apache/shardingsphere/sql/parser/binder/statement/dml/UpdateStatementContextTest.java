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

package org.apache.shardingsphere.sql.parser.binder.statement.dml;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class UpdateStatementContextTest {
    
    @Mock
    private WhereSegment whereSegment;
    
    @Mock
    private ColumnSegment columnSegment;
    
    @Test
    public void assertNewInstance() {
        when(columnSegment.getOwner()).thenReturn(Optional.of(new OwnerSegment(0, 0, new IdentifierValue("tbl_2"))));
        BinaryOperationExpression expression = new BinaryOperationExpression();
        expression.setLeft(columnSegment);
        
        when(whereSegment.getExpr()).thenReturn(expression);
        SimpleTableSegment table1 = new SimpleTableSegment(0, 0, new IdentifierValue("tbl_1"));
        SimpleTableSegment table2 = new SimpleTableSegment(0, 0, new IdentifierValue("tbl_2"));
        List<SimpleTableSegment> tables = new LinkedList<>();
        tables.add(table1);
        tables.add(table2);
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        joinTableSegment.setLeft(table1);
        joinTableSegment.setRight(table2);
        UpdateStatement updateStatement = new UpdateStatement();
        updateStatement.setWhere(whereSegment);
        updateStatement.setTableSegment(joinTableSegment);
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertTrue(actual.toString().startsWith(String.format("%s(super", UpdateStatementContext.class.getSimpleName())));
        assertThat(actual.getTablesContext().getTables().stream().map(a -> a.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("tbl_1", "tbl_2")));
        assertThat(actual.getWhere(), is(Optional.of(whereSegment)));
        assertThat(actual.getAllTables().stream().map(a -> a.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("tbl_1", "tbl_2", "tbl_2")));
    }
}
