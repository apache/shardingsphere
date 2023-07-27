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

package org.apache.shardingsphere.infra.binder.context.statement.dcl;

import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLRevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dcl.OracleRevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dcl.PostgreSQLRevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dcl.SQL92RevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerRevokeStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RevokeStatementContextTest {
    
    @Test
    void assertMySQLNewInstance() {
        assertNewInstance(mock(MySQLRevokeStatement.class));
    }
    
    @Test
    void assertPostgreSQLNewInstance() {
        assertNewInstance(mock(PostgreSQLRevokeStatement.class));
    }
    
    @Test
    void assertOracleNewInstance() {
        assertNewInstance(mock(OracleRevokeStatement.class));
    }
    
    @Test
    void assertSQLServerNewInstance() {
        assertNewInstance(mock(SQLServerRevokeStatement.class));
    }
    
    @Test
    void assertSQL92NewInstance() {
        assertNewInstance(mock(SQL92RevokeStatement.class));
    }
    
    private void assertNewInstance(final RevokeStatement revokeStatement) {
        SimpleTableSegment table1 = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_1")));
        SimpleTableSegment table2 = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_2")));
        List<SimpleTableSegment> tables = new LinkedList<>();
        tables.add(table1);
        tables.add(table2);
        when(revokeStatement.getTables()).thenReturn(tables);
        RevokeStatementContext actual = new RevokeStatementContext(revokeStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(revokeStatement));
        assertThat(actual.getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("tbl_1", "tbl_2")));
    }
}
