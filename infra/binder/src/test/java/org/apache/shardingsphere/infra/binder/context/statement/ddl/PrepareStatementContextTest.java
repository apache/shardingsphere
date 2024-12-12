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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLPrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dml.PostgreSQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dml.PostgreSQLUpdateStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PrepareStatementContextTest {
    
    private ColumnSegment column;
    
    private SimpleTableSegment table;
    
    @BeforeEach
    void setUp() {
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("tbl_1"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        table = new SimpleTableSegment(tableNameSegment);
        column = new ColumnSegment(0, 0, new IdentifierValue("col_1"));
    }
    
    @Test
    void assertNewInstance() {
        PostgreSQLPrepareStatement sqlStatement = new PostgreSQLPrepareStatement();
        sqlStatement.setSelect(getSelect());
        sqlStatement.setInsert(getInsert());
        sqlStatement.setUpdate(getUpdate());
        sqlStatement.setDelete(getDelete());
        PrepareStatementContext actual = new PrepareStatementContext(sqlStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(sqlStatement));
        assertThat(actual.getTablesContext().getSimpleTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()),
                is(Arrays.asList("tbl_1", "tbl_1", "tbl_1", "tbl_1")));
    }
    
    private SelectStatement getSelect() {
        SelectStatement result = new PostgreSQLSelectStatement();
        result.setFrom(table);
        return result;
    }
    
    private InsertStatement getInsert() {
        InsertStatement result = new PostgreSQLInsertStatement();
        result.setTable(table);
        return result;
    }
    
    private UpdateStatement getUpdate() {
        UpdateStatement result = new PostgreSQLUpdateStatement();
        result.setTable(table);
        SetAssignmentSegment setAssignmentSegment = new SetAssignmentSegment(0, 0, Collections.singletonList(new ColumnAssignmentSegment(0, 0, Collections.singletonList(column), column)));
        result.setSetAssignment(setAssignmentSegment);
        return result;
    }
    
    private DeleteStatement getDelete() {
        DeleteStatement result = new PostgreSQLDeleteStatement();
        result.setTable(table);
        return result;
    }
}
