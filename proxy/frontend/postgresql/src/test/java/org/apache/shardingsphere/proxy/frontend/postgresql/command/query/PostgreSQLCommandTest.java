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

import org.apache.shardingsphere.distsql.parser.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.RegisterStorageUnitStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DeclareStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.MoveStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCursorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLResetParameterStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLVacuumStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLUpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLBeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLRollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLStartTransactionStatement;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;

public final class PostgreSQLCommandTest {
    
    @Test
    public void assertValueOfUnConfiguredSQLStatement() {
        assertFalse(PostgreSQLCommand.valueOf(SQLStatement.class).isPresent());
    }
    
    @Test
    public void assertValueOfSelectStatement() {
        assertThat(PostgreSQLCommand.valueOf(SelectStatement.class).orElse(null), is(PostgreSQLCommand.SELECT));
        assertThat(PostgreSQLCommand.SELECT.getTag(), is("SELECT"));
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
    public void assertValueOfCallStatement() {
        assertThat(PostgreSQLCommand.valueOf(CallStatement.class).orElse(null), is(PostgreSQLCommand.CALL));
        assertThat(PostgreSQLCommand.CALL.getTag(), is("CALL"));
    }
    
    @Test
    public void assertValueOfDoStatement() {
        assertThat(PostgreSQLCommand.valueOf(DoStatement.class).orElse(null), is(PostgreSQLCommand.DO));
        assertThat(PostgreSQLCommand.DO.getTag(), is("DO"));
    }
    
    @Test
    public void assertValueOfAnalyzeStatement() {
        assertThat(PostgreSQLCommand.valueOf(AnalyzeTableStatement.class).orElse(null), is(PostgreSQLCommand.ANALYZE));
        assertThat(PostgreSQLCommand.ANALYZE.getTag(), is("ANALYZE"));
    }
    
    @Test
    public void assertValueOfVacuumStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLVacuumStatement.class).orElse(null), is(PostgreSQLCommand.VACUUM));
        assertThat(PostgreSQLCommand.VACUUM.getTag(), is("VACUUM"));
    }
    
    @Test
    public void assertValueOfAlterFunctionStatement() {
        assertThat(PostgreSQLCommand.valueOf(AlterFunctionStatement.class).orElse(null), is(PostgreSQLCommand.ALTER_FUNCTION));
        assertThat(PostgreSQLCommand.ALTER_FUNCTION.getTag(), is("ALTER FUNCTION"));
    }
    
    @Test
    public void assertValueOfAlterIndexStatement() {
        assertThat(PostgreSQLCommand.valueOf(AlterIndexStatement.class).orElse(null), is(PostgreSQLCommand.ALTER_INDEX));
        assertThat(PostgreSQLCommand.ALTER_INDEX.getTag(), is("ALTER INDEX"));
    }
    
    @Test
    public void assertValueOfAlterProcedureStatement() {
        assertThat(PostgreSQLCommand.valueOf(AlterProcedureStatement.class).orElse(null), is(PostgreSQLCommand.ALTER_PROCEDURE));
        assertThat(PostgreSQLCommand.ALTER_PROCEDURE.getTag(), is("ALTER PROCEDURE"));
    }
    
    @Test
    public void assertValueOfAlterSequenceStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLAlterSequenceStatement.class).orElse(null), is(PostgreSQLCommand.ALTER_SEQUENCE));
        assertThat(PostgreSQLCommand.ALTER_SEQUENCE.getTag(), is("ALTER SEQUENCE"));
    }
    
    @Test
    public void assertValueOfAlterTablespaceStatement() {
        assertThat(PostgreSQLCommand.valueOf(AlterTablespaceStatement.class).orElse(null), is(PostgreSQLCommand.ALTER_TABLESPACE));
        assertThat(PostgreSQLCommand.ALTER_TABLESPACE.getTag(), is("ALTER TABLESPACE"));
    }
    
    @Test
    public void assertValueOfAlterTableStatement() {
        assertThat(PostgreSQLCommand.valueOf(AlterTableStatement.class).orElse(null), is(PostgreSQLCommand.ALTER_TABLE));
        assertThat(PostgreSQLCommand.ALTER_TABLE.getTag(), is("ALTER TABLE"));
    }
    
    @Test
    public void assertValueOfAlterViewStatement() {
        assertThat(PostgreSQLCommand.valueOf(AlterViewStatement.class).orElse(null), is(PostgreSQLCommand.ALTER_VIEW));
        assertThat(PostgreSQLCommand.ALTER_VIEW.getTag(), is("ALTER VIEW"));
    }
    
    @Test
    public void assertValueOfCreateShardingTableRuleOrCreateDataSourcesStatement() {
        assertThat(PostgreSQLCommand.valueOf(RegisterStorageUnitStatement.class).orElse(null), is(PostgreSQLCommand.SUCCESS));
        assertThat(PostgreSQLCommand.valueOf(CreateShardingTableRuleStatement.class).orElse(null), is(PostgreSQLCommand.SUCCESS));
        assertThat(PostgreSQLCommand.valueOf(DistSQLStatement.class).orElse(null), is(PostgreSQLCommand.SUCCESS));
        assertThat(PostgreSQLCommand.SUCCESS.getTag(), is("SUCCESS"));
    }
    
    @Test
    public void assertValueOfCreateDatabaseStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateDatabaseStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_DATABASE));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLCreateDatabaseStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_DATABASE));
        assertThat(PostgreSQLCommand.CREATE_DATABASE.getTag(), is("CREATE DATABASE"));
    }
    
    @Test
    public void assertValueOfCreateFunctionStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateFunctionStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_FUNCTION));
        assertThat(PostgreSQLCommand.CREATE_FUNCTION.getTag(), is("CREATE FUNCTION"));
    }
    
    @Test
    public void assertValueOfCreateIndexStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateIndexStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_INDEX));
        assertThat(PostgreSQLCommand.CREATE_INDEX.getTag(), is("CREATE INDEX"));
    }
    
    @Test
    public void assertValueOfCreateProcedureStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLCreateProcedureStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_PROCEDURE));
        assertThat(PostgreSQLCommand.CREATE_PROCEDURE.getTag(), is("CREATE PROCEDURE"));
    }
    
    @Test
    public void assertValueOfCreateSequenceStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLCreateSequenceStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_SEQUENCE));
        assertThat(PostgreSQLCommand.CREATE_SEQUENCE.getTag(), is("CREATE SEQUENCE"));
    }
    
    @Test
    public void assertValueOfCreateTablespaceStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLCreateTablespaceStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_TABLESPACE));
        assertThat(PostgreSQLCommand.CREATE_TABLESPACE.getTag(), is("CREATE TABLESPACE"));
    }
    
    @Test
    public void assertValueOfCreateTableStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateTableStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_TABLE));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLCreateTableStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_TABLE));
        assertThat(PostgreSQLCommand.CREATE_TABLE.getTag(), is("CREATE TABLE"));
    }
    
    @Test
    public void assertValueOfCreateViewStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateViewStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_VIEW));
        assertThat(PostgreSQLCommand.CREATE_VIEW.getTag(), is("CREATE VIEW"));
    }
    
    @Test
    public void assertValueOfDropDatabaseStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropDatabaseStatement.class).orElse(null), is(PostgreSQLCommand.DROP_DATABASE));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLDropDatabaseStatement.class).orElse(null), is(PostgreSQLCommand.DROP_DATABASE));
        assertThat(PostgreSQLCommand.DROP_DATABASE.getTag(), is("DROP DATABASE"));
    }
    
    @Test
    public void assertValueOfDropFunctionStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropFunctionStatement.class).orElse(null), is(PostgreSQLCommand.DROP_FUNCTION));
        assertThat(PostgreSQLCommand.DROP_FUNCTION.getTag(), is("DROP FUNCTION"));
    }
    
    @Test
    public void assertValueOfDropIndexStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropIndexStatement.class).orElse(null), is(PostgreSQLCommand.DROP_INDEX));
        assertThat(PostgreSQLCommand.DROP_INDEX.getTag(), is("DROP INDEX"));
    }
    
    @Test
    public void assertValueOfDropProcedureStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropProcedureStatement.class).orElse(null), is(PostgreSQLCommand.DROP_PROCEDURE));
        assertThat(PostgreSQLCommand.DROP_PROCEDURE.getTag(), is("DROP PROCEDURE"));
    }
    
    @Test
    public void assertValueOfDropSequenceStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLDropSequenceStatement.class).orElse(null), is(PostgreSQLCommand.DROP_SEQUENCE));
        assertThat(PostgreSQLCommand.DROP_SEQUENCE.getTag(), is("DROP SEQUENCE"));
    }
    
    @Test
    public void assertValueOfDropTablespaceStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropTablespaceStatement.class).orElse(null), is(PostgreSQLCommand.DROP_TABLESPACE));
        assertThat(PostgreSQLCommand.DROP_TABLESPACE.getTag(), is("DROP TABLESPACE"));
    }
    
    @Test
    public void assertValueOfDropTableStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropTableStatement.class).orElse(null), is(PostgreSQLCommand.DROP_TABLE));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLDropTableStatement.class).orElse(null), is(PostgreSQLCommand.DROP_TABLE));
        assertThat(PostgreSQLCommand.DROP_TABLE.getTag(), is("DROP TABLE"));
    }
    
    @Test
    public void assertValueOfDropViewStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropViewStatement.class).orElse(null), is(PostgreSQLCommand.DROP_VIEW));
        assertThat(PostgreSQLCommand.DROP_VIEW.getTag(), is("DROP VIEW"));
    }
    
    @Test
    public void assertValueOfTruncateStatement() {
        assertThat(PostgreSQLCommand.valueOf(TruncateStatement.class).orElse(null), is(PostgreSQLCommand.TRUNCATE_TABLE));
        assertThat(PostgreSQLCommand.TRUNCATE_TABLE.getTag(), is("TRUNCATE TABLE"));
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
    public void assertValueOfSavepointStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLSavepointStatement.class).orElse(null), is(PostgreSQLCommand.SAVEPOINT));
        assertThat(PostgreSQLCommand.SAVEPOINT.getTag(), is("SAVEPOINT"));
    }
    
    @Test
    public void assertValueOfRollbackStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLRollbackStatement.class).orElse(null), is(PostgreSQLCommand.ROLLBACK));
        assertThat(PostgreSQLCommand.ROLLBACK.getTag(), is("ROLLBACK"));
    }
    
    @Test
    public void assertValueOfReleaseSavepointStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLReleaseSavepointStatement.class).orElse(null), is(PostgreSQLCommand.RELEASE));
        assertThat(PostgreSQLCommand.RELEASE.getTag(), is("RELEASE"));
    }
    
    @Test
    public void assertValueOfSetStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLSetStatement.class).orElse(null), is(PostgreSQLCommand.SET));
        assertThat(PostgreSQLCommand.SET.getTag(), is("SET"));
    }
    
    @Test
    public void assertValueOfResetStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLResetParameterStatement.class).orElse(null), is(PostgreSQLCommand.RESET));
        assertThat(PostgreSQLCommand.RESET.getTag(), is("RESET"));
    }
    
    @Test
    public void assertValueOfCursorStatement() {
        assertThat(PostgreSQLCommand.valueOf(OpenGaussCursorStatement.class).orElse(null), is(PostgreSQLCommand.DECLARE_CURSOR));
        assertThat(PostgreSQLCommand.valueOf(DeclareStatement.class).orElse(null), is(PostgreSQLCommand.DECLARE_CURSOR));
        assertThat(PostgreSQLCommand.DECLARE_CURSOR.getTag(), is("DECLARE CURSOR"));
    }
    
    @Test
    public void assertValueOfMoveStatement() {
        assertThat(PostgreSQLCommand.valueOf(MoveStatement.class).orElse(null), is(PostgreSQLCommand.MOVE));
        assertThat(PostgreSQLCommand.MOVE.getTag(), is("MOVE"));
    }
    
    @Test
    public void assertValueOfCloseStatement() {
        assertThat(PostgreSQLCommand.valueOf(CloseStatement.class).orElse(null), is(PostgreSQLCommand.CLOSE_CURSOR));
        assertThat(PostgreSQLCommand.CLOSE_CURSOR.getTag(), is("CLOSE CURSOR"));
    }
}
