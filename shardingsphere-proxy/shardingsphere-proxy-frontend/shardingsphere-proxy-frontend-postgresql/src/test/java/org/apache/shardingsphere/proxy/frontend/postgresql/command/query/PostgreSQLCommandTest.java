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

import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLUpdateStatement;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class PostgreSQLCommandTest {
    
    @Test
    public void assertValueOfUnConfiguredSQLStatement() {
        assertFalse(PostgreSQLCommand.valueOf(SQLStatement.class).isPresent());
    }
    
    @Test
    public void assertValueOfInsertStatement() {
        assertThat(PostgreSQLCommand.valueOf(InsertStatement.class).orElse(null), is(PostgreSQLCommand.INSERT));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLInsertStatement.class).orElse(null), is(PostgreSQLCommand.INSERT));
    }
    
    @Test
    public void assertValueOfUpdateStatement() {
        assertThat(PostgreSQLCommand.valueOf(UpdateStatement.class).orElse(null), is(PostgreSQLCommand.UPDATE));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLUpdateStatement.class).orElse(null), is(PostgreSQLCommand.UPDATE));
    }
    
    @Test
    public void assertValueOfDeleteStatement() {
        assertThat(PostgreSQLCommand.valueOf(DeleteStatement.class).orElse(null), is(PostgreSQLCommand.DELETE));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLDeleteStatement.class).orElse(null), is(PostgreSQLCommand.DELETE));
    }
    
    @Test
    public void assertValueOfDropDatabaseStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropDatabaseStatement.class).orElse(null), is(PostgreSQLCommand.DROP));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLDropDatabaseStatement.class).orElse(null), is(PostgreSQLCommand.DROP));
    }
    
    @Test
    public void assertValueOfCreateDatabaseStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateDatabaseStatement.class).orElse(null), is(PostgreSQLCommand.CREATE));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLCreateDatabaseStatement.class).orElse(null), is(PostgreSQLCommand.CREATE));
    }
    
    @Test
    public void assertValueOfCreateDataSourcesStatement() {
        assertThat(PostgreSQLCommand.valueOf(AddResourceStatement.class).orElse(null), is(PostgreSQLCommand.CREATE));
    }
    
    @Test
    public void assertValueOfCreateShardingTableRuleStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateShardingTableRuleStatement.class).orElse(null), is(PostgreSQLCommand.CREATE));
    }
}
