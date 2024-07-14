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
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLAlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterViewStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlterViewStatementContextTest {
    
    private SimpleTableSegment view;
    
    @BeforeEach
    void setUp() {
        view = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("view")));
    }
    
    @Test
    void assertMySQLNewInstance() {
        SelectStatement select = mock(MySQLSelectStatement.class);
        when(select.getFrom()).thenReturn(Optional.of(view));
        MySQLAlterViewStatement alterViewStatement = new MySQLAlterViewStatement();
        alterViewStatement.setView(view);
        alterViewStatement.setSelect(select);
        assertNewInstance(alterViewStatement);
    }
    
    @Test
    void assertPostgreSQLNewInstance() {
        PostgreSQLAlterViewStatement alterViewStatement = new PostgreSQLAlterViewStatement();
        alterViewStatement.setView(view);
        alterViewStatement.setRenameView(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("view"))));
        assertNewInstance(alterViewStatement);
    }
    
    private void assertNewInstance(final AlterViewStatement alterViewStatement) {
        AlterViewStatementContext actual = new AlterViewStatementContext(alterViewStatement, DefaultDatabase.LOGIC_NAME);
        assertThat(actual, instanceOf(CommonSQLStatementContext.class));
        assertThat(actual.getSqlStatement(), is(alterViewStatement));
        assertThat(actual.getTablesContext().getSimpleTables().size(), is(2));
    }
}
