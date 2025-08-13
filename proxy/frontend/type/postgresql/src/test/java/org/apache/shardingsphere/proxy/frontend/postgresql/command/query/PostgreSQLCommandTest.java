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

import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.RegisterStorageUnitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.MoveStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.AlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.DropProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.AlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.CreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.DropSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.AlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.CreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.DropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.ReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dal.PostgreSQLResetParameterStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dal.PostgreSQLVacuumStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDeclareStatement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PostgreSQLCommandTest {
    
    @Test
    void assertValueOfUnConfiguredSQLStatement() {
        assertFalse(PostgreSQLCommand.valueOf(SQLStatement.class).isPresent());
    }
    
    @Test
    void assertValueOfSelectStatement() {
        assertThat(PostgreSQLCommand.valueOf(SelectStatement.class).orElse(null), is(PostgreSQLCommand.SELECT));
        assertThat(PostgreSQLCommand.SELECT.getTag(), is("SELECT"));
    }
    
    @Test
    void assertValueOfInsertStatement() {
        assertThat(PostgreSQLCommand.valueOf(InsertStatement.class).orElse(null), is(PostgreSQLCommand.INSERT));
        assertThat(PostgreSQLCommand.INSERT.getTag(), is("INSERT"));
    }
    
    @Test
    void assertValueOfUpdateStatement() {
        assertThat(PostgreSQLCommand.valueOf(UpdateStatement.class).orElse(null), is(PostgreSQLCommand.UPDATE));
        assertThat(PostgreSQLCommand.UPDATE.getTag(), is("UPDATE"));
    }
    
    @Test
    void assertValueOfDeleteStatement() {
        assertThat(PostgreSQLCommand.valueOf(DeleteStatement.class).orElse(null), is(PostgreSQLCommand.DELETE));
        assertThat(PostgreSQLCommand.DELETE.getTag(), is("DELETE"));
    }
    
    @Test
    void assertValueOfCallStatement() {
        assertThat(PostgreSQLCommand.valueOf(CallStatement.class).orElse(null), is(PostgreSQLCommand.CALL));
        assertThat(PostgreSQLCommand.CALL.getTag(), is("CALL"));
    }
    
    @Test
    void assertValueOfDoStatement() {
        assertThat(PostgreSQLCommand.valueOf(DoStatement.class).orElse(null), is(PostgreSQLCommand.DO));
        assertThat(PostgreSQLCommand.DO.getTag(), is("DO"));
    }
    
    @Test
    void assertValueOfAnalyzeStatement() {
        assertThat(PostgreSQLCommand.valueOf(AnalyzeTableStatement.class).orElse(null), is(PostgreSQLCommand.ANALYZE));
        assertThat(PostgreSQLCommand.ANALYZE.getTag(), is("ANALYZE"));
    }
    
    @Test
    void assertValueOfVacuumStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLVacuumStatement.class).orElse(null), is(PostgreSQLCommand.VACUUM));
        assertThat(PostgreSQLCommand.VACUUM.getTag(), is("VACUUM"));
    }
    
    @Test
    void assertValueOfAlterFunctionStatement() {
        assertThat(PostgreSQLCommand.valueOf(AlterFunctionStatement.class).orElse(null), is(PostgreSQLCommand.ALTER_FUNCTION));
        assertThat(PostgreSQLCommand.ALTER_FUNCTION.getTag(), is("ALTER FUNCTION"));
    }
    
    @Test
    void assertValueOfAlterIndexStatement() {
        assertThat(PostgreSQLCommand.valueOf(AlterIndexStatement.class).orElse(null), is(PostgreSQLCommand.ALTER_INDEX));
        assertThat(PostgreSQLCommand.ALTER_INDEX.getTag(), is("ALTER INDEX"));
    }
    
    @Test
    void assertValueOfAlterProcedureStatement() {
        assertThat(PostgreSQLCommand.valueOf(AlterProcedureStatement.class).orElse(null), is(PostgreSQLCommand.ALTER_PROCEDURE));
        assertThat(PostgreSQLCommand.ALTER_PROCEDURE.getTag(), is("ALTER PROCEDURE"));
    }
    
    @Test
    void assertValueOfAlterSequenceStatement() {
        assertThat(PostgreSQLCommand.valueOf(AlterSequenceStatement.class).orElse(null), is(PostgreSQLCommand.ALTER_SEQUENCE));
        assertThat(PostgreSQLCommand.ALTER_SEQUENCE.getTag(), is("ALTER SEQUENCE"));
    }
    
    @Test
    void assertValueOfAlterTablespaceStatement() {
        assertThat(PostgreSQLCommand.valueOf(AlterTablespaceStatement.class).orElse(null), is(PostgreSQLCommand.ALTER_TABLESPACE));
        assertThat(PostgreSQLCommand.ALTER_TABLESPACE.getTag(), is("ALTER TABLESPACE"));
    }
    
    @Test
    void assertValueOfAlterTableStatement() {
        assertThat(PostgreSQLCommand.valueOf(AlterTableStatement.class).orElse(null), is(PostgreSQLCommand.ALTER_TABLE));
        assertThat(PostgreSQLCommand.ALTER_TABLE.getTag(), is("ALTER TABLE"));
    }
    
    @Test
    void assertValueOfAlterViewStatement() {
        assertThat(PostgreSQLCommand.valueOf(AlterViewStatement.class).orElse(null), is(PostgreSQLCommand.ALTER_VIEW));
        assertThat(PostgreSQLCommand.ALTER_VIEW.getTag(), is("ALTER VIEW"));
    }
    
    @Test
    void assertValueOfCreateShardingTableRuleOrCreateDataSourcesStatement() {
        assertThat(PostgreSQLCommand.valueOf(RegisterStorageUnitStatement.class).orElse(null), is(PostgreSQLCommand.SUCCESS));
        assertThat(PostgreSQLCommand.valueOf(DistSQLStatement.class).orElse(null), is(PostgreSQLCommand.SUCCESS));
        assertThat(PostgreSQLCommand.SUCCESS.getTag(), is("SUCCESS"));
    }
    
    @Test
    void assertValueOfCreateDatabaseStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateDatabaseStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_DATABASE));
        assertThat(PostgreSQLCommand.CREATE_DATABASE.getTag(), is("CREATE DATABASE"));
    }
    
    @Test
    void assertValueOfCreateFunctionStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateFunctionStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_FUNCTION));
        assertThat(PostgreSQLCommand.CREATE_FUNCTION.getTag(), is("CREATE FUNCTION"));
    }
    
    @Test
    void assertValueOfCreateIndexStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateIndexStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_INDEX));
        assertThat(PostgreSQLCommand.CREATE_INDEX.getTag(), is("CREATE INDEX"));
    }
    
    @Test
    void assertValueOfCreateProcedureStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateProcedureStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_PROCEDURE));
        assertThat(PostgreSQLCommand.CREATE_PROCEDURE.getTag(), is("CREATE PROCEDURE"));
    }
    
    @Test
    void assertValueOfCreateSequenceStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateSequenceStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_SEQUENCE));
        assertThat(PostgreSQLCommand.CREATE_SEQUENCE.getTag(), is("CREATE SEQUENCE"));
    }
    
    @Test
    void assertValueOfCreateTablespaceStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateTablespaceStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_TABLESPACE));
        assertThat(PostgreSQLCommand.CREATE_TABLESPACE.getTag(), is("CREATE TABLESPACE"));
    }
    
    @Test
    void assertValueOfCreateTableStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateTableStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_TABLE));
        assertThat(PostgreSQLCommand.CREATE_TABLE.getTag(), is("CREATE TABLE"));
    }
    
    @Test
    void assertValueOfCreateViewStatement() {
        assertThat(PostgreSQLCommand.valueOf(CreateViewStatement.class).orElse(null), is(PostgreSQLCommand.CREATE_VIEW));
        assertThat(PostgreSQLCommand.CREATE_VIEW.getTag(), is("CREATE VIEW"));
    }
    
    @Test
    void assertValueOfDropDatabaseStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropDatabaseStatement.class).orElse(null), is(PostgreSQLCommand.DROP_DATABASE));
        assertThat(PostgreSQLCommand.DROP_DATABASE.getTag(), is("DROP DATABASE"));
    }
    
    @Test
    void assertValueOfDropFunctionStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropFunctionStatement.class).orElse(null), is(PostgreSQLCommand.DROP_FUNCTION));
        assertThat(PostgreSQLCommand.DROP_FUNCTION.getTag(), is("DROP FUNCTION"));
    }
    
    @Test
    void assertValueOfDropIndexStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropIndexStatement.class).orElse(null), is(PostgreSQLCommand.DROP_INDEX));
        assertThat(PostgreSQLCommand.DROP_INDEX.getTag(), is("DROP INDEX"));
    }
    
    @Test
    void assertValueOfDropProcedureStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropProcedureStatement.class).orElse(null), is(PostgreSQLCommand.DROP_PROCEDURE));
        assertThat(PostgreSQLCommand.DROP_PROCEDURE.getTag(), is("DROP PROCEDURE"));
    }
    
    @Test
    void assertValueOfDropSequenceStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropSequenceStatement.class).orElse(null), is(PostgreSQLCommand.DROP_SEQUENCE));
        assertThat(PostgreSQLCommand.DROP_SEQUENCE.getTag(), is("DROP SEQUENCE"));
    }
    
    @Test
    void assertValueOfDropTablespaceStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropTablespaceStatement.class).orElse(null), is(PostgreSQLCommand.DROP_TABLESPACE));
        assertThat(PostgreSQLCommand.DROP_TABLESPACE.getTag(), is("DROP TABLESPACE"));
    }
    
    @Test
    void assertValueOfDropTableStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropTableStatement.class).orElse(null), is(PostgreSQLCommand.DROP_TABLE));
        assertThat(PostgreSQLCommand.DROP_TABLE.getTag(), is("DROP TABLE"));
    }
    
    @Test
    void assertValueOfDropViewStatement() {
        assertThat(PostgreSQLCommand.valueOf(DropViewStatement.class).orElse(null), is(PostgreSQLCommand.DROP_VIEW));
        assertThat(PostgreSQLCommand.DROP_VIEW.getTag(), is("DROP VIEW"));
    }
    
    @Test
    void assertValueOfTruncateStatement() {
        assertThat(PostgreSQLCommand.valueOf(TruncateStatement.class).orElse(null), is(PostgreSQLCommand.TRUNCATE_TABLE));
        assertThat(PostgreSQLCommand.TRUNCATE_TABLE.getTag(), is("TRUNCATE TABLE"));
    }
    
    @Test
    void assertValueOfBeginStatement() {
        assertThat(PostgreSQLCommand.valueOf(BeginTransactionStatement.class).orElse(null), is(PostgreSQLCommand.BEGIN));
        assertThat(PostgreSQLCommand.BEGIN.getTag(), is("BEGIN"));
    }
    
    @Test
    void assertValueOfCommitStatement() {
        assertThat(PostgreSQLCommand.valueOf(CommitStatement.class).orElse(null), is(PostgreSQLCommand.COMMIT));
        assertThat(PostgreSQLCommand.COMMIT.getTag(), is("COMMIT"));
    }
    
    @Test
    void assertValueOfSavepointStatement() {
        assertThat(PostgreSQLCommand.valueOf(SavepointStatement.class).orElse(null), is(PostgreSQLCommand.SAVEPOINT));
        assertThat(PostgreSQLCommand.SAVEPOINT.getTag(), is("SAVEPOINT"));
    }
    
    @Test
    void assertValueOfRollbackStatement() {
        assertThat(PostgreSQLCommand.valueOf(RollbackStatement.class).orElse(null), is(PostgreSQLCommand.ROLLBACK));
        assertThat(PostgreSQLCommand.ROLLBACK.getTag(), is("ROLLBACK"));
    }
    
    @Test
    void assertValueOfReleaseSavepointStatement() {
        assertThat(PostgreSQLCommand.valueOf(ReleaseSavepointStatement.class).orElse(null), is(PostgreSQLCommand.RELEASE));
        assertThat(PostgreSQLCommand.RELEASE.getTag(), is("RELEASE"));
    }
    
    @Test
    void assertValueOfSetStatement() {
        assertThat(PostgreSQLCommand.valueOf(SetStatement.class).orElse(null), is(PostgreSQLCommand.SET));
        assertThat(PostgreSQLCommand.SET.getTag(), is("SET"));
    }
    
    @Test
    void assertValueOfResetStatement() {
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLResetParameterStatement.class).orElse(null), is(PostgreSQLCommand.RESET));
        assertThat(PostgreSQLCommand.RESET.getTag(), is("RESET"));
    }
    
    @Test
    void assertValueOfCursorStatement() {
        assertThat(PostgreSQLCommand.valueOf(CursorStatement.class).orElse(null), is(PostgreSQLCommand.DECLARE_CURSOR));
        assertThat(PostgreSQLCommand.valueOf(PostgreSQLDeclareStatement.class).orElse(null), is(PostgreSQLCommand.DECLARE_CURSOR));
        assertThat(PostgreSQLCommand.DECLARE_CURSOR.getTag(), is("DECLARE CURSOR"));
    }
    
    @Test
    void assertValueOfMoveStatement() {
        assertThat(PostgreSQLCommand.valueOf(MoveStatement.class).orElse(null), is(PostgreSQLCommand.MOVE));
        assertThat(PostgreSQLCommand.MOVE.getTag(), is("MOVE"));
    }
    
    @Test
    void assertValueOfCloseStatement() {
        assertThat(PostgreSQLCommand.valueOf(CloseStatement.class).orElse(null), is(PostgreSQLCommand.CLOSE_CURSOR));
        assertThat(PostgreSQLCommand.CLOSE_CURSOR.getTag(), is("CLOSE CURSOR"));
    }
}
