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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.DropSchemaStatement;
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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dal.PostgreSQLResetParameterStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dal.PostgreSQLVacuumStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDeclareStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PostgreSQL command.
 *
 * @see <a href="https://www.postgresql.org/docs/13/sql-commands.html">SQL Commands</a>
 */
@Getter
public enum PostgreSQLCommand {
    
    SELECT(SelectStatement.class),
    INSERT(InsertStatement.class),
    UPDATE(UpdateStatement.class),
    DELETE(DeleteStatement.class),
    CALL(CallStatement.class),
    DO(DoStatement.class),
    ANALYZE(AnalyzeTableStatement.class),
    VACUUM(PostgreSQLVacuumStatement.class),
    ALTER_FUNCTION(AlterFunctionStatement.class),
    ALTER_INDEX(AlterIndexStatement.class),
    ALTER_PROCEDURE(AlterProcedureStatement.class),
    ALTER_SCHEMA(AlterSchemaStatement.class),
    ALTER_SEQUENCE(AlterSequenceStatement.class),
    ALTER_TABLESPACE(AlterTablespaceStatement.class),
    ALTER_TABLE(AlterTableStatement.class),
    ALTER_VIEW(AlterViewStatement.class),
    CREATE_DATABASE(CreateDatabaseStatement.class),
    CREATE_FUNCTION(CreateFunctionStatement.class),
    CREATE_INDEX(CreateIndexStatement.class),
    CREATE_PROCEDURE(CreateProcedureStatement.class),
    CREATE_SCHEMA(CreateSchemaStatement.class),
    CREATE_SEQUENCE(CreateSequenceStatement.class),
    CREATE_TABLESPACE(CreateTablespaceStatement.class),
    CREATE_TABLE(CreateTableStatement.class),
    CREATE_VIEW(CreateViewStatement.class),
    DROP_DATABASE(DropDatabaseStatement.class),
    DROP_FUNCTION(DropFunctionStatement.class),
    DROP_INDEX(DropIndexStatement.class),
    DROP_PROCEDURE(DropProcedureStatement.class),
    DROP_SCHEMA(DropSchemaStatement.class),
    DROP_SEQUENCE(DropSequenceStatement.class),
    DROP_TABLESPACE(DropTablespaceStatement.class),
    DROP_TABLE(DropTableStatement.class),
    DROP_VIEW(DropViewStatement.class),
    TRUNCATE_TABLE(TruncateStatement.class),
    BEGIN(BeginTransactionStatement.class),
    START_TRANSACTION(BeginTransactionStatement.class),
    COMMIT(CommitStatement.class),
    SAVEPOINT(SavepointStatement.class),
    ROLLBACK(RollbackStatement.class),
    RELEASE(ReleaseSavepointStatement.class),
    SET(SetStatement.class, SetTransactionStatement.class),
    RESET(PostgreSQLResetParameterStatement.class),
    DECLARE_CURSOR(CursorStatement.class, PostgreSQLDeclareStatement.class),
    MOVE(MoveStatement.class),
    CLOSE_CURSOR(CloseStatement.class),
    SUCCESS(DistSQLStatement.class);
    
    private static final Map<Class<? extends SQLStatement>, CachedResult> COMPUTED_STATEMENTS = new ConcurrentHashMap<>(64);
    
    private final Collection<Class<? extends SQLStatement>> sqlStatementClasses;
    
    private final String tag;
    
    @SafeVarargs
    PostgreSQLCommand(final Class<? extends SQLStatement>... sqlStatementClasses) {
        this.sqlStatementClasses = Arrays.asList(sqlStatementClasses);
        tag = name().replaceAll("_", " ");
    }
    
    /**
     * Value of PostgreSQL command via SQL statement class.
     *
     * @param sqlStatementClass SQL statement class
     * @return PostgreSQL command
     */
    public static Optional<PostgreSQLCommand> valueOf(final Class<? extends SQLStatement> sqlStatementClass) {
        return getPostgreSQLCommand(sqlStatementClass);
    }
    
    /*
     * Refer to <a href="https://bugs.openjdk.java.net/browse/JDK-8161372">JDK-8161372</a>.
     */
    private static Optional<PostgreSQLCommand> getPostgreSQLCommand(final Class<? extends SQLStatement> sqlStatementClass) {
        CachedResult result = COMPUTED_STATEMENTS.get(sqlStatementClass);
        return null == result ? COMPUTED_STATEMENTS.computeIfAbsent(sqlStatementClass, PostgreSQLCommand::compute).get() : result.get();
    }
    
    private static CachedResult compute(final Class<? extends SQLStatement> target) {
        Optional<PostgreSQLCommand> result = Arrays.stream(values()).filter(each -> matches(target, each)).findAny();
        return result.map(CachedResult::new).orElse(CachedResult.EMPTY);
    }
    
    private static boolean matches(final Class<? extends SQLStatement> sqlStatementClass, final PostgreSQLCommand command) {
        return command.sqlStatementClasses.stream().anyMatch(each -> each.isAssignableFrom(sqlStatementClass));
    }
    
    @RequiredArgsConstructor
    private static final class CachedResult {
        
        private static final CachedResult EMPTY = new CachedResult(null);
        
        private final PostgreSQLCommand result;
        
        private Optional<PostgreSQLCommand> get() {
            return Optional.ofNullable(result);
        }
    }
}
