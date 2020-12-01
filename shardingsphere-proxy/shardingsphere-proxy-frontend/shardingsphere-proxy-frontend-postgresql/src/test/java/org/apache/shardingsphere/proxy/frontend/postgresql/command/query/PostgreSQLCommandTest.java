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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query;

import org.apache.shardingsphere.distsql.parser.statement.rdl.CreateDataSourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.CreateShardingRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class PostgreSQLCommandTest {

    @Test
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfInsertStatement() {
        PostgreSQLCommand postgreSQLCommand = new PostgreSQLCommand(mock(InsertStatement.class));
        assertThat(postgreSQLCommand.getSQLCommand(), is("INSERT"));
    }

    @Test
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfDeleteStatement() {
        PostgreSQLCommand postgreSQLCommand = new PostgreSQLCommand(mock(DeleteStatement.class));
        assertThat(postgreSQLCommand.getSQLCommand(), is("DELETE"));
    }

    @Test
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfUpdateStatement() {
        PostgreSQLCommand postgreSQLCommand = new PostgreSQLCommand(mock(UpdateStatement.class));
        assertThat(postgreSQLCommand.getSQLCommand(), is("UPDATE"));
    }

    @Test
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfDropDatabaseStatement() {
        PostgreSQLCommand postgreSQLCommand = new PostgreSQLCommand(mock(DropDatabaseStatement.class));
        assertThat(postgreSQLCommand.getSQLCommand(), is("DROP"));
    }

    @Test
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfCreateDatabaseStatement() {
        PostgreSQLCommand postgreSQLCommand = new PostgreSQLCommand(mock(CreateDatabaseStatement.class));
        assertThat(postgreSQLCommand.getSQLCommand(), is("CREATE"));
    }

    @Test
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfCreateDataSourcesStatement() {
        PostgreSQLCommand postgreSQLCommand = new PostgreSQLCommand(mock(CreateDataSourcesStatement.class));
        assertThat(postgreSQLCommand.getSQLCommand(), is("CREATE"));
    }

    @Test
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfCreateShardingRuleStatement() {
        PostgreSQLCommand postgreSQLCommand = new PostgreSQLCommand(mock(CreateShardingRuleStatement.class));
        assertThat(postgreSQLCommand.getSQLCommand(), is("CREATE"));
    }

    @Test
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfUnEnumeration() {
        PostgreSQLCommand postgreSQLCommand = new PostgreSQLCommand(mock(SQLStatement.class));
        assertThat(postgreSQLCommand.getSQLCommand(), is(""));
    }
}
