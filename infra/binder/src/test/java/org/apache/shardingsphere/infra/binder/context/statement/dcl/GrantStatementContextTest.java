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
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLGrantStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.dcl.OracleGrantStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dcl.PostgreSQLGrantStatement;
import org.apache.shardingsphere.sql.parser.statement.sql92.dcl.SQL92GrantStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.SQLServerGrantStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class GrantStatementContextTest {
    
    @Test
    void assertMySQLNewInstance() {
        assertNewInstance(new MySQLGrantStatement());
    }
    
    @Test
    void assertPostgreSQLNewInstance() {
        assertNewInstance(new PostgreSQLGrantStatement());
    }
    
    @Test
    void assertOracleNewInstance() {
        assertNewInstance(new OracleGrantStatement());
    }
    
    @Test
    void assertSQLServerNewInstance() {
        assertNewInstance(new SQLServerGrantStatement());
    }
    
    @Test
    void assertSQL92NewInstance() {
        assertNewInstance(new SQL92GrantStatement());
    }
    
    private void assertNewInstance(final GrantStatement grantStatement) {
        TableNameSegment tableNameSegment1 = new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"));
        tableNameSegment1.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        TableNameSegment tableNameSegment2 = new TableNameSegment(0, 0, new IdentifierValue("bar_tbl"));
        tableNameSegment2.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        SimpleTableSegment table1 = new SimpleTableSegment(tableNameSegment1);
        SimpleTableSegment table2 = new SimpleTableSegment(tableNameSegment2);
        grantStatement.getTables().addAll(Arrays.asList(table1, table2));
        GrantStatementContext actual = new GrantStatementContext(grantStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(grantStatement));
        assertThat(actual.getTablesContext().getSimpleTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()),
                is(Arrays.asList("foo_tbl", "bar_tbl")));
    }
}
