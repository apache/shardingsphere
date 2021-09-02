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

package org.apache.shardingsphere.infra.binder.statement.dcl;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLGrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dcl.OracleGrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dcl.PostgreSQLGrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dcl.SQL92GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerGrantStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GrantStatementContextTest {

    @Test
    public void assertMySQLNewInstance() {
        GrantStatementContext actual = assertNewInstance(mock(MySQLGrantStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("MySQL"));
    }

    @Test
    public void assertPostgreSQLNewInstance() {
        GrantStatementContext actual = assertNewInstance(mock(PostgreSQLGrantStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("PostgreSQL"));
    }

    @Test
    public void assertOracleNewInstance() {
        GrantStatementContext actual = assertNewInstance(mock(OracleGrantStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("Oracle"));
    }

    @Test
    public void assertSQLServerNewInstance() {
        GrantStatementContext actual = assertNewInstance(mock(SQLServerGrantStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("SQLServer"));
    }

    @Test
    public void assertSQL92NewInstance() {
        GrantStatementContext actual = assertNewInstance(mock(SQL92GrantStatement.class));
        assertThat(actual.getDatabaseType().getName(), is("SQL92"));
    }

    private GrantStatementContext assertNewInstance(final GrantStatement grantStatement) {
        SimpleTableSegment table1 = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_1")));
        SimpleTableSegment table2 = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_2")));
        List<SimpleTableSegment> tables = new LinkedList<>();
        tables.add(table1);
        tables.add(table2);
        when(grantStatement.getTables()).thenReturn(tables);
        GrantStatementContext actual = new GrantStatementContext(grantStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(grantStatement));
        assertThat(actual.getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("tbl_1", "tbl_2")));
        return actual;
    }
}
