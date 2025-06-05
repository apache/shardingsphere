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

package org.apache.shardingsphere.infra.binder.context.statement.ddl;

import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.PrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrepareStatementContextTest {
    
    @Test
    void assertNewInstance() {
        PrepareStatement sqlStatement = mock(PrepareStatement.class);
        SimpleTableSegment table = new SimpleTableSegment(createTableNameSegment());
        when(sqlStatement.getSelect()).thenReturn(Optional.of(getSelect(table)));
        when(sqlStatement.getInsert()).thenReturn(Optional.of(getInsert(table)));
        when(sqlStatement.getUpdate()).thenReturn(Optional.of(getUpdate(table)));
        when(sqlStatement.getDelete()).thenReturn(Optional.of(getDelete(table)));
        PrepareStatementContext actual = new PrepareStatementContext(mock(), sqlStatement);
        assertThat(actual.getSqlStatement(), is(sqlStatement));
        assertThat(actual.getTablesContext().getSimpleTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()),
                is(Arrays.asList("foo_tbl", "foo_tbl", "foo_tbl", "foo_tbl")));
    }
    
    private static TableNameSegment createTableNameSegment() {
        TableNameSegment result = new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"));
        result.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        return result;
    }
    
    private SelectStatement getSelect(final SimpleTableSegment table) {
        SelectStatement result = new SelectStatement();
        result.setFrom(table);
        return result;
    }
    
    private InsertStatement getInsert(final SimpleTableSegment table) {
        InsertStatement result = new InsertStatement();
        result.setTable(table);
        return result;
    }
    
    private UpdateStatement getUpdate(final SimpleTableSegment table) {
        UpdateStatement result = new UpdateStatement();
        result.setTable(table);
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("foo_col"));
        SetAssignmentSegment setAssignmentSegment = new SetAssignmentSegment(0, 0, Collections.singletonList(new ColumnAssignmentSegment(0, 0, Collections.singletonList(column), column)));
        result.setSetAssignment(setAssignmentSegment);
        return result;
    }
    
    private DeleteStatement getDelete(final SimpleTableSegment table) {
        DeleteStatement result = new DeleteStatement();
        result.setTable(table);
        return result;
    }
}
