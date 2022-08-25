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
import org.apache.shardingsphere.distsql.parser.statement.DistSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ResetParameterStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.VacuumStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateSequenceStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DeclareStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropSequenceStatement;
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
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.ReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.StartTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCursorStatement;

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
    VACUUM(VacuumStatement.class),
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
    START_TRANSACTION(StartTransactionStatement.class),
    COMMIT(CommitStatement.class),
    SAVEPOINT(SavepointStatement.class),
    ROLLBACK(RollbackStatement.class),
    RELEASE(ReleaseSavepointStatement.class),
    SET(SetStatement.class, SetTransactionStatement.class),
    RESET(ResetParameterStatement.class),
    DECLARE_CURSOR(OpenGaussCursorStatement.class, DeclareStatement.class),
    MOVE(MoveStatement.class),
    CLOSE_CURSOR(CloseStatement.class),
    SUCCESS(DistSQLStatement.class);
    
    private static final Map<Class<? extends SQLStatement>, Optional<PostgreSQLCommand>> COMPUTED_CLASSES = new ConcurrentHashMap<>(64, 1);
    
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
    
    /**
     * Refer to <a href="https://bugs.openjdk.java.net/browse/JDK-8161372">JDK-8161372</a>. 
     *
     * @param sqlStatementClass sql statement class
     * @return optional PostgreSQLCommand
     */
    @SuppressWarnings("OptionalAssignedToNull")
    private static Optional<PostgreSQLCommand> getPostgreSQLCommand(final Class<? extends SQLStatement> sqlStatementClass) {
        Optional<PostgreSQLCommand> result;
        if (null == (result = COMPUTED_CLASSES.get(sqlStatementClass))) {
            result = COMPUTED_CLASSES.computeIfAbsent(sqlStatementClass, target -> Arrays.stream(PostgreSQLCommand.values()).filter(each -> matches(target, each)).findAny());
        }
        return result;
    }
    
    private static boolean matches(final Class<? extends SQLStatement> sqlStatementClass, final PostgreSQLCommand postgreSQLCommand) {
        return postgreSQLCommand.sqlStatementClasses.stream().anyMatch(each -> each.isAssignableFrom(sqlStatementClass));
    }
}
