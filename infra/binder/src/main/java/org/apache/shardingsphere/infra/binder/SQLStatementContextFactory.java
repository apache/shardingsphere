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

package org.apache.shardingsphere.infra.binder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.AnalyzeTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.FlushStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.KillStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.OptimizeTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.ShowColumnsStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.ShowCreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.ShowIndexStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.ShowTableStatusStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.ShowTablesStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dcl.DenyUserStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dcl.GrantStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dcl.RevokeStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.AlterIndexStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.AlterViewStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CommentStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateFunctionStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateProcedureStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateSchemaStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateViewStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.DropIndexStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.DropTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.DropViewStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.FetchStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.MoveStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.PrepareStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.RenameTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.TruncateStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.CallStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.CopyStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.DoStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.FlushStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CommentStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.MoveStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.PrepareStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.CopyStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLKillStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLOptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCursorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerDenyUserStatement;

import java.util.Collections;
import java.util.List;

/**
 * SQL statement context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementContextFactory {
    
    /**
     * Create SQL statement context.
     *
     * @param metaData metaData
     * @param sqlStatement SQL statement
     * @param defaultDatabaseName default database name
     * @return SQL statement context
     */
    public static SQLStatementContext<?> newInstance(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement, final String defaultDatabaseName) {
        return newInstance(metaData, Collections.emptyList(), sqlStatement, defaultDatabaseName);
    }
    
    /**
     * Create SQL statement context.
     *
     * @param metaData metaData
     * @param params SQL parameters
     * @param sqlStatement SQL statement
     * @param defaultDatabaseName default database name
     * @return SQL statement context
     */
    public static SQLStatementContext<?> newInstance(final ShardingSphereMetaData metaData,
                                                     final List<Object> params, final SQLStatement sqlStatement, final String defaultDatabaseName) {
        if (sqlStatement instanceof DMLStatement) {
            return getDMLStatementContext(metaData, params, (DMLStatement) sqlStatement, defaultDatabaseName);
        }
        if (sqlStatement instanceof DDLStatement) {
            return getDDLStatementContext(metaData, params, (DDLStatement) sqlStatement, defaultDatabaseName);
        }
        if (sqlStatement instanceof DCLStatement) {
            return getDCLStatementContext((DCLStatement) sqlStatement);
        }
        if (sqlStatement instanceof DALStatement) {
            return getDALStatementContext((DALStatement) sqlStatement);
        }
        return new CommonSQLStatementContext<>(sqlStatement);
    }
    
    private static SQLStatementContext<?> getDMLStatementContext(final ShardingSphereMetaData metaData,
                                                                 final List<Object> params, final DMLStatement sqlStatement, final String defaultDatabaseName) {
        if (sqlStatement instanceof SelectStatement) {
            return new SelectStatementContext(metaData, params, (SelectStatement) sqlStatement, defaultDatabaseName);
        }
        if (sqlStatement instanceof UpdateStatement) {
            return new UpdateStatementContext((UpdateStatement) sqlStatement);
        }
        if (sqlStatement instanceof DeleteStatement) {
            return new DeleteStatementContext((DeleteStatement) sqlStatement);
        }
        if (sqlStatement instanceof InsertStatement) {
            return new InsertStatementContext(metaData, params, (InsertStatement) sqlStatement, defaultDatabaseName);
        }
        if (sqlStatement instanceof CallStatement) {
            return new CallStatementContext((CallStatement) sqlStatement);
        }
        if (sqlStatement instanceof CopyStatement) {
            return new CopyStatementContext((CopyStatement) sqlStatement);
        }
        if (sqlStatement instanceof DoStatement) {
            return new DoStatementContext((DoStatement) sqlStatement);
        }
        throw new UnsupportedSQLOperationException(String.format("Unsupported SQL statement `%s`", sqlStatement.getClass().getSimpleName()));
    }
    
    private static SQLStatementContext<?> getDDLStatementContext(final ShardingSphereMetaData metaData, final List<Object> params,
                                                                 final DDLStatement sqlStatement, final String defaultDatabaseName) {
        if (sqlStatement instanceof CreateSchemaStatement) {
            return new CreateSchemaStatementContext((CreateSchemaStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateTableStatement) {
            return new CreateTableStatementContext((CreateTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof AlterTableStatement) {
            return new AlterTableStatementContext((AlterTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof RenameTableStatement) {
            return new RenameTableStatementContext((RenameTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof DropTableStatement) {
            return new DropTableStatementContext((DropTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateIndexStatement) {
            return new CreateIndexStatementContext((CreateIndexStatement) sqlStatement);
        }
        if (sqlStatement instanceof AlterIndexStatement) {
            return new AlterIndexStatementContext((AlterIndexStatement) sqlStatement);
        }
        if (sqlStatement instanceof DropIndexStatement) {
            return new DropIndexStatementContext((DropIndexStatement) sqlStatement);
        }
        if (sqlStatement instanceof TruncateStatement) {
            return new TruncateStatementContext((TruncateStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateFunctionStatement) {
            return new CreateFunctionStatementContext((CreateFunctionStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateProcedureStatement) {
            return new CreateProcedureStatementContext((CreateProcedureStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateViewStatement) {
            return new CreateViewStatementContext((CreateViewStatement) sqlStatement);
        }
        if (sqlStatement instanceof AlterViewStatement) {
            return new AlterViewStatementContext((AlterViewStatement) sqlStatement);
        }
        if (sqlStatement instanceof DropViewStatement) {
            return new DropViewStatementContext((DropViewStatement) sqlStatement);
        }
        if (sqlStatement instanceof PrepareStatement) {
            return new PrepareStatementContext((PrepareStatement) sqlStatement);
        }
        if (sqlStatement instanceof CommentStatement) {
            return new CommentStatementContext((CommentStatement) sqlStatement);
        }
        if (sqlStatement instanceof OpenGaussCursorStatement) {
            return new CursorStatementContext(metaData, params, (OpenGaussCursorStatement) sqlStatement, defaultDatabaseName);
        }
        if (sqlStatement instanceof CloseStatement) {
            return new CloseStatementContext((CloseStatement) sqlStatement);
        }
        if (sqlStatement instanceof MoveStatement) {
            return new MoveStatementContext((MoveStatement) sqlStatement);
        }
        if (sqlStatement instanceof FetchStatement) {
            return new FetchStatementContext((FetchStatement) sqlStatement);
        }
        return new CommonSQLStatementContext<>(sqlStatement);
    }
    
    private static SQLStatementContext<?> getDCLStatementContext(final DCLStatement sqlStatement) {
        if (sqlStatement instanceof GrantStatement) {
            return new GrantStatementContext((GrantStatement) sqlStatement);
        }
        if (sqlStatement instanceof RevokeStatement) {
            return new RevokeStatementContext((RevokeStatement) sqlStatement);
        }
        if (sqlStatement instanceof SQLServerDenyUserStatement) {
            return new DenyUserStatementContext((SQLServerDenyUserStatement) sqlStatement);
        }
        return new CommonSQLStatementContext<>(sqlStatement);
    }
    
    private static SQLStatementContext<?> getDALStatementContext(final DALStatement sqlStatement) {
        if (sqlStatement instanceof ExplainStatement) {
            return new ExplainStatementContext((ExplainStatement) sqlStatement);
        }
        if (sqlStatement instanceof MySQLShowCreateTableStatement) {
            return new ShowCreateTableStatementContext((MySQLShowCreateTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof MySQLShowColumnsStatement) {
            return new ShowColumnsStatementContext((MySQLShowColumnsStatement) sqlStatement);
        }
        if (sqlStatement instanceof MySQLShowTablesStatement) {
            return new ShowTablesStatementContext((MySQLShowTablesStatement) sqlStatement);
        }
        if (sqlStatement instanceof MySQLShowTableStatusStatement) {
            return new ShowTableStatusStatementContext((MySQLShowTableStatusStatement) sqlStatement);
        }
        if (sqlStatement instanceof MySQLShowIndexStatement) {
            return new ShowIndexStatementContext((MySQLShowIndexStatement) sqlStatement);
        }
        if (sqlStatement instanceof AnalyzeTableStatement) {
            return new AnalyzeTableStatementContext((AnalyzeTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof FlushStatement) {
            return new FlushStatementContext((FlushStatement) sqlStatement);
        }
        if (sqlStatement instanceof MySQLOptimizeTableStatement) {
            return new OptimizeTableStatementContext((MySQLOptimizeTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof MySQLKillStatement) {
            return new KillStatementContext((MySQLKillStatement) sqlStatement);
        }
        return new CommonSQLStatementContext<>(sqlStatement);
    }
}
