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
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLRevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.dcl.OracleRevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dcl.PostgreSQLRevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.sql92.dcl.SQL92RevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.SQLServerRevokeStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class RevokeStatementContextTest {
    
    @Test
    void assertMySQLNewInstance() {
        assertNewInstance(new MySQLRevokeStatement());
    }
    
    @Test
    void assertPostgreSQLNewInstance() {
        assertNewInstance(new PostgreSQLRevokeStatement());
    }
    
    @Test
    void assertOracleNewInstance() {
        assertNewInstance(new OracleRevokeStatement());
    }
    
    @Test
    void assertSQLServerNewInstance() {
        assertNewInstance(new SQLServerRevokeStatement());
    }
    
    @Test
    void assertSQL92NewInstance() {
        assertNewInstance(new SQL92RevokeStatement());
    }
    
    private void assertNewInstance(final RevokeStatement revokeStatement) {
        SimpleTableSegment table1 = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_1")));
        SimpleTableSegment table2 = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl_2")));
        revokeStatement.getTables().addAll(Arrays.asList(table1, table2));
        RevokeStatementContext actual = new RevokeStatementContext(revokeStatement, DefaultDatabase.LOGIC_NAME);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(revokeStatement));
        assertThat(actual.getTablesContext().getSimpleTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), is(Arrays.asList("tbl_1", "tbl_2")));
    }
}
