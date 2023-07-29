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

import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLPrepareStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLUpdateStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrepareStatementContextTest {
    
    private ColumnSegment column;
    
    private SimpleTableSegment table;
    
    @BeforeEach
    void setUp() {
        table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_1")));
        column = new ColumnSegment(0, 0, new IdentifierValue("col_1"));
    }
    
    @Test
    void assertNewInstance() {
        PostgreSQLPrepareStatement postgreSQLPrepare = mock(PostgreSQLPrepareStatement.class);
        when(postgreSQLPrepare.getSelect()).thenReturn(Optional.of(getSelect()));
        when(postgreSQLPrepare.getInsert()).thenReturn(Optional.of(getInsert()));
        when(postgreSQLPrepare.getUpdate()).thenReturn(Optional.of(getUpdate()));
        when(postgreSQLPrepare.getDelete()).thenReturn(Optional.of(getDelete()));
        PrepareStatementContext actual = new PrepareStatementContext(postgreSQLPrepare);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(postgreSQLPrepare));
        assertThat(actual.getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()),
                is(Arrays.asList("tbl_1", "tbl_1", "tbl_1", "tbl_1")));
    }
    
    private SelectStatement getSelect() {
        SelectStatement select = new PostgreSQLSelectStatement();
        select.setFrom(table);
        return select;
    }
    
    private InsertStatement getInsert() {
        InsertStatement insert = new PostgreSQLInsertStatement();
        insert.setTable(table);
        return insert;
    }
    
    private UpdateStatement getUpdate() {
        UpdateStatement update = new PostgreSQLUpdateStatement();
        update.setTable(table);
        SetAssignmentSegment setAssignmentSegment = new SetAssignmentSegment(0, 0, Collections.singletonList(new ColumnAssignmentSegment(0, 0, Collections.singletonList(column), column)));
        update.setSetAssignment(setAssignmentSegment);
        return update;
    }
    
    private DeleteStatement getDelete() {
        DeleteStatement delete = new PostgreSQLDeleteStatement();
        delete.setTable(table);
        return delete;
    }
}
