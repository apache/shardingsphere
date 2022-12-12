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
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateIndexStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CreateIndexStatementContextTest {
    
    @Test
    public void assertMySQLNewInstance() {
        assertNewInstance(mock(MySQLCreateIndexStatement.class));
    }
    
    @Test
    public void assertPostgreSQLNewInstance() {
        assertNewInstance(mock(PostgreSQLCreateIndexStatement.class));
    }
    
    @Test
    public void assertOracleSQLNewInstance() {
        assertNewInstance(mock(OracleCreateIndexStatement.class));
    }
    
    @Test
    public void assertSQLServerSQLNewInstance() {
        assertNewInstance(mock(SQLServerCreateIndexStatement.class));
    }
    
    private void assertNewInstance(final CreateIndexStatement createIndexStatement) {
        CreateIndexStatementContext actual = new CreateIndexStatementContext(createIndexStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(createIndexStatement));
        assertTrue(actual.isGeneratedIndex());
        assertThat(actual.getAllTables(), is(Collections.emptyList()));
        assertThat(actual.getIndexes(), is(Collections.emptyList()));
        when(createIndexStatement.getIndex()).thenReturn(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("index_2"))));
        CreateIndexStatementContext actual2 = new CreateIndexStatementContext(createIndexStatement);
        assertThat(actual2.getIndexes().stream().map(each -> each.getIndexName().getIdentifier().getValue()).collect(Collectors.toList()), is(Collections.singletonList("index_2")));
    }
}
