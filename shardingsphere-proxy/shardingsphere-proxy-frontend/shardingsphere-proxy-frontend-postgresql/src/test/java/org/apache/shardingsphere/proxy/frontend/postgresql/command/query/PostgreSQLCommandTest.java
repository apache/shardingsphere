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
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfUnEnumeration() {
        assertThat(new PostgreSQLCommand(mock(SQLStatement.class)).getSQLCommand(), is(""));
    }
    
    @Test
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfInsertStatement() {
        assertThat(new PostgreSQLCommand(mock(InsertStatement.class)).getSQLCommand(), is("INSERT"));
    }
    
    @Test
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfDeleteStatement() {
        assertThat(new PostgreSQLCommand(mock(DeleteStatement.class)).getSQLCommand(), is("DELETE"));
    }
    
    @Test
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfUpdateStatement() {
        assertThat(new PostgreSQLCommand(mock(UpdateStatement.class)).getSQLCommand(), is("UPDATE"));
    }
    
    @Test
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfDropDatabaseStatement() {
        assertThat(new PostgreSQLCommand(mock(DropDatabaseStatement.class)).getSQLCommand(), is("DROP"));
    }
    
    @Test
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfCreateDatabaseStatement() {
        assertThat(new PostgreSQLCommand(mock(CreateDatabaseStatement.class)).getSQLCommand(), is("CREATE"));
    }
    
    @Test
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfCreateDataSourcesStatement() {
        assertThat(new PostgreSQLCommand(mock(CreateDataSourcesStatement.class)).getSQLCommand(), is("CREATE"));
    }
    
    @Test
    public void assertPostgreSQLCommandWhenSQLStatementInstanceOfCreateShardingRuleStatement() {
        assertThat(new PostgreSQLCommand(mock(CreateShardingRuleStatement.class)).getSQLCommand(), is("CREATE"));
    }
}
