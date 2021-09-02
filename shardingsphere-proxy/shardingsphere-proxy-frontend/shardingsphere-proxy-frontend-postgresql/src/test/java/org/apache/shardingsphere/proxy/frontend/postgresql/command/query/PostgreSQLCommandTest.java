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
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLUpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLBeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLRollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLStartTransactionStatement;
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
        assertThat(PostgreSQLCommand.INSERT.getTag(), is("INSERT"));
    }
    
    @Test
    public void assertValueOfUpdateStatement() {
        assertThat(PostgreSQLCommand.valueOf(UpdateStatement.class).orElse(null), is(PostgreSQLCommand.UPDATE));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLUpdateStatement.class).orElse(null), is(PostgreSQLCommand.UPDATE));
        assertThat(PostgreSQLCommand.UPDATE.getTag(), is("UPDATE"));
    }
    
    @Test
    public void assertValueOfDeleteStatement() {
        assertThat(PostgreSQLCommand.valueOf(DeleteStatement.class).orElse(null), is(PostgreSQLCommand.DELETE));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLDeleteStatement.class).orElse(null), is(PostgreSQLCommand.DELETE));
        assertThat(PostgreSQLCommand.DELETE.getTag(), is("DELETE"));
    }
    
    @Test
    public void assertValueOfCreateShardingTableRuleOrCreateDataSourcesStatement() {
        assertThat(PostgreSQLCommand.valueOf(AddResourceStatement.class).orElse(null), is(PostgreSQLCommand.CREATE));
        assertThat(PostgreSQLCommand.valueOf(CreateShardingTableRuleStatement.class).orElse(null), is(PostgreSQLCommand.CREATE));
        assertThat(PostgreSQLCommand.CREATE.getTag(), is("CREATE"));
    }
    
    @Test
    public void assertValueOfCreateDatabaseStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateDatabaseStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_DATABASE));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLCreateDatabaseStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_DATABASE));
        assertThat(PostgreSQLCommand.CREATE_DATABASE.getTag(), is("CREATE DATABASE"));
    }
    
    @Test
    public void assertValueOfCreateTableStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateTableStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_TABLE));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLCreateTableStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_TABLE));
        assertThat(PostgreSQLCommand.CREATE_TABLE.getTag(), is("CREATE TABLE"));
    }
    
    @Test
    public void assertValueOfDropDatabaseStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropDatabaseStatement.class).orElse(null), is(PostgreSQLCommand.DROP_DATABASE));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLDropDatabaseStatement.class).orElse(null), is(PostgreSQLCommand.DROP_DATABASE));
        assertThat(PostgreSQLCommand.DROP_DATABASE.getTag(), is("DROP DATABASE"));
    }
    
    @Test
    public void assertValueOfDropTableStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropTableStatement.class).orElse(null), is(PostgreSQLCommand.DROP_TABLE));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLDropTableStatement.class).orElse(null), is(PostgreSQLCommand.DROP_TABLE));
    }
    
    @Test
    public void assertValueOfBeginStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLBeginTransactionStatement.class).orElse(null), is(PostgreSQLCommand.BEGIN));
        assertThat(PostgreSQLCommand.BEGIN.getTag(), is("BEGIN"));
    }
    
    @Test
    public void assertValueOfStartTransactionStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLStartTransactionStatement.class).orElse(null), is(PostgreSQLCommand.START_TRANSACTION));
        assertThat(PostgreSQLCommand.START_TRANSACTION.getTag(), is("START TRANSACTION"));
    }
    
    @Test
    public void assertValueOfCommitStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLCommitStatement.class).orElse(null), is(PostgreSQLCommand.COMMIT));
        assertThat(PostgreSQLCommand.COMMIT.getTag(), is("COMMIT"));
    }
    
    @Test
    public void assertValueOfRollbackStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLRollbackStatement.class).orElse(null), is(PostgreSQLCommand.ROLLBACK));
        assertThat(PostgreSQLCommand.ROLLBACK.getTag(), is("ROLLBACK"));
    }
    
    @Test
    public void assertValueOfSetStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLSetStatement.class).orElse(null), is(PostgreSQLCommand.SET));
        assertThat(PostgreSQLCommand.SET.getTag(), is("SET"));
    }
}
