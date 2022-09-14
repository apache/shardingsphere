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
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterViewStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlterViewStatementContextTest {
    
    private SimpleTableSegment view;
    
    @Before
    public void setUp() {
        view = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("view")));
    }
    
    @Test
    public void assertMySQLNewInstance() {
        SelectStatement select = mock(MySQLSelectStatement.class);
        when(select.getFrom()).thenReturn(view);
        MySQLAlterViewStatement mySQLAlterViewStatement = mock(MySQLAlterViewStatement.class);
        when(mySQLAlterViewStatement.getView()).thenReturn(view);
        when(mySQLAlterViewStatement.getSelect()).thenReturn(Optional.of(select));
        assertNewInstance(mySQLAlterViewStatement);
    }
    
    @Test
    public void assertPostgreSQLNewInstance() {
        PostgreSQLAlterViewStatement postgreSQLAlterViewStatement = mock(PostgreSQLAlterViewStatement.class);
        when(postgreSQLAlterViewStatement.getView()).thenReturn(view);
        when(postgreSQLAlterViewStatement.getRenameView()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("view")))));
        assertNewInstance(postgreSQLAlterViewStatement);
    }
    
    private void assertNewInstance(final AlterViewStatement alterViewStatement) {
        AlterViewStatementContext actual = new AlterViewStatementContext(alterViewStatement);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(alterViewStatement));
        assertThat(actual.getAllTables().size(), is(2));
    }
}
