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

package org.apache.shardingsphere.infra.binder.statement.ddl;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.ValidStatementSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateFunctionStatement;
import org.junit.Test;

import java.util.Optional;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CreateFunctionStatementContextTest {

    @Test
    public void assertMySQLNewInstance() {
        AlterTableStatement alterTableStatement = mock(AlterTableStatement.class);
        ValidStatementSegment validStatement = mock(ValidStatementSegment.class);
        SimpleTableSegment table1 = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_1")));
        when(alterTableStatement.getTable()).thenReturn(table1);
        when(validStatement.getAlterTable()).thenReturn(Optional.of(alterTableStatement));
        SimpleTableSegment table2 = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_2")));
        List<SimpleTableSegment> tables = new LinkedList<>();
        tables.add(table1);
        tables.add(table2);
        DropTableStatement dropTableStatement = mock(DropTableStatement.class);
        when(dropTableStatement.getTables()).thenReturn(tables);
        when(validStatement.getDropTable()).thenReturn(Optional.of(dropTableStatement));
        TruncateStatement truncateStatement = mock(TruncateStatement.class);
        when(truncateStatement.getTables()).thenReturn(tables);
        when(validStatement.getTruncate()).thenReturn(Optional.of(truncateStatement));
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 0);
        Collection<ValidStatementSegment> validStatements = routineBody.getValidStatements();
        validStatements.add(validStatement);
        MySQLCreateFunctionStatement mySQLCreateFunctionStatement = mock(MySQLCreateFunctionStatement.class);
        when(mySQLCreateFunctionStatement.getRoutineBody()).thenReturn(Optional.of(routineBody));
        CreateFunctionStatementContext actual = assertNewInstance(mySQLCreateFunctionStatement);
        assertThat(actual.getDatabaseType().getName(), is("MySQL"));
        assertThat(actual.getTablesContext().getTableNames(), is(Arrays.asList("tbl_1", "tbl_2")));
        assertThat(actual.getTablesContext().getOriginalTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()),
                is(Arrays.asList("tbl_1", "tbl_1", "tbl_2", "tbl_1", "tbl_2")));
    }

    @Test
    public void assertPostgreSQLNewInstance() {
        CreateFunctionStatementContext actual = assertNewInstance(mock(PostgreSQLCreateFunctionStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("PostgreSQL"));
        assertThat(actual.getTablesContext().getUniqueTables(), is(Collections.emptyMap()));
    }

    @Test
    public void assertSQLServerNewInstance() {
        CreateFunctionStatementContext actual = assertNewInstance(mock(SQLServerCreateFunctionStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("SQLServer"));
        assertThat(actual.getTablesContext().getUniqueTables(), is(Collections.emptyMap()));
    }

    private CreateFunctionStatementContext assertNewInstance(final CreateFunctionStatement createDatabaseStatement) {
        CreateFunctionStatementContext actual = new CreateFunctionStatementContext(createDatabaseStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(createDatabaseStatement));
        return actual;
    }
}
