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

package org.apache.shardingsphere.infra.binder.context.statement;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.dal.AnalyzeTableStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dal.FlushStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dal.OptimizeTableStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dal.ShowColumnsStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dal.ShowCreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dal.ShowIndexStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dal.ShowTableStatusStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dal.ShowTablesStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dcl.DenyUserStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dcl.GrantStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dcl.RevokeStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.AlterIndexStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.AlterViewStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CommentStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateFunctionStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateProcedureStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateSchemaStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateViewStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.DropIndexStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.DropTableStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.DropViewStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.FetchStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.MoveStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.PrepareStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.RenameTableStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.TruncateStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.CopyStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.LoadDataStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.LoadXMLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.FlushStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.OptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowTablesStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.DenyUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CommentStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.MoveStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.PrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.CopyStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.LoadDataStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.LoadXMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.UpdateStatement;

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
     * @param metaData metadata
     * @param databaseType database type
     * @param sqlStatement SQL statement
     * @param params SQL parameters
     * @param currentDatabaseName current database name
     * @return SQL statement context
     */
    public static SQLStatementContext newInstance(final ShardingSphereMetaData metaData,
                                                  final DatabaseType databaseType, final SQLStatement sqlStatement, final List<Object> params, final String currentDatabaseName) {
        if (sqlStatement instanceof DMLStatement) {
            return getDMLStatementContext(metaData, databaseType, (DMLStatement) sqlStatement, params, currentDatabaseName);
        }
        if (sqlStatement instanceof DDLStatement) {
            return getDDLStatementContext(metaData, databaseType, (DDLStatement) sqlStatement, params, currentDatabaseName);
        }
        if (sqlStatement instanceof DCLStatement) {
            return getDCLStatementContext(databaseType, (DCLStatement) sqlStatement);
        }
        if (sqlStatement instanceof DALStatement) {
            return getDALStatementContext(metaData, databaseType, (DALStatement) sqlStatement, params, currentDatabaseName);
        }
        return new UnknownSQLStatementContext(databaseType, sqlStatement);
    }
    
    private static SQLStatementContext getDMLStatementContext(final ShardingSphereMetaData metaData, final DatabaseType databaseType,
                                                              final DMLStatement sqlStatement, final List<Object> params, final String currentDatabaseName) {
        if (sqlStatement instanceof SelectStatement) {
            return new SelectStatementContext(databaseType, (SelectStatement) sqlStatement, params, metaData, currentDatabaseName, Collections.emptyList());
        }
        if (sqlStatement instanceof UpdateStatement) {
            return new UpdateStatementContext(databaseType, (UpdateStatement) sqlStatement);
        }
        if (sqlStatement instanceof DeleteStatement) {
            return new DeleteStatementContext(databaseType, (DeleteStatement) sqlStatement);
        }
        if (sqlStatement instanceof InsertStatement) {
            return new InsertStatementContext(databaseType, (InsertStatement) sqlStatement, params, metaData, currentDatabaseName);
        }
        if (sqlStatement instanceof CopyStatement) {
            return new CopyStatementContext(databaseType, (CopyStatement) sqlStatement);
        }
        if (sqlStatement instanceof LoadDataStatement) {
            return new LoadDataStatementContext(databaseType, (LoadDataStatement) sqlStatement);
        }
        if (sqlStatement instanceof LoadXMLStatement) {
            return new LoadXMLStatementContext(databaseType, (LoadXMLStatement) sqlStatement);
        }
        if (sqlStatement instanceof CallStatement || sqlStatement instanceof DoStatement || sqlStatement instanceof MergeStatement) {
            return new UnknownSQLStatementContext(databaseType, sqlStatement);
        }
        throw new UnsupportedSQLOperationException(String.format("Unsupported SQL statement `%s`", sqlStatement.getClass().getSimpleName()));
    }
    
    private static SQLStatementContext getDDLStatementContext(final ShardingSphereMetaData metaData, final DatabaseType databaseType,
                                                              final DDLStatement sqlStatement, final List<Object> params, final String currentDatabaseName) {
        if (sqlStatement instanceof CreateSchemaStatement) {
            return new CreateSchemaStatementContext(databaseType, (CreateSchemaStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateTableStatement) {
            return new CreateTableStatementContext(databaseType, (CreateTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof AlterTableStatement) {
            return new AlterTableStatementContext(databaseType, (AlterTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof RenameTableStatement) {
            return new RenameTableStatementContext(databaseType, (RenameTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof DropTableStatement) {
            return new DropTableStatementContext(databaseType, (DropTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateIndexStatement) {
            return new CreateIndexStatementContext(databaseType, (CreateIndexStatement) sqlStatement);
        }
        if (sqlStatement instanceof AlterIndexStatement) {
            return new AlterIndexStatementContext(databaseType, (AlterIndexStatement) sqlStatement);
        }
        if (sqlStatement instanceof DropIndexStatement) {
            return new DropIndexStatementContext(databaseType, (DropIndexStatement) sqlStatement);
        }
        if (sqlStatement instanceof TruncateStatement) {
            return new TruncateStatementContext(databaseType, (TruncateStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateFunctionStatement) {
            return new CreateFunctionStatementContext(databaseType, (CreateFunctionStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateProcedureStatement) {
            return new CreateProcedureStatementContext(databaseType, (CreateProcedureStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateViewStatement) {
            return new CreateViewStatementContext(metaData, databaseType, params, (CreateViewStatement) sqlStatement, currentDatabaseName);
        }
        if (sqlStatement instanceof AlterViewStatement) {
            return new AlterViewStatementContext(metaData, databaseType, params, (AlterViewStatement) sqlStatement, currentDatabaseName);
        }
        if (sqlStatement instanceof DropViewStatement) {
            return new DropViewStatementContext(databaseType, (DropViewStatement) sqlStatement);
        }
        if (sqlStatement instanceof PrepareStatement) {
            return new PrepareStatementContext(databaseType, (PrepareStatement) sqlStatement);
        }
        if (sqlStatement instanceof CommentStatement) {
            return new CommentStatementContext(databaseType, (CommentStatement) sqlStatement);
        }
        if (sqlStatement instanceof CursorStatement) {
            return new CursorStatementContext(metaData, databaseType, params, (CursorStatement) sqlStatement, currentDatabaseName);
        }
        if (sqlStatement instanceof CloseStatement) {
            return new CloseStatementContext(databaseType, (CloseStatement) sqlStatement);
        }
        if (sqlStatement instanceof MoveStatement) {
            return new MoveStatementContext(databaseType, (MoveStatement) sqlStatement);
        }
        if (sqlStatement instanceof FetchStatement) {
            return new FetchStatementContext(databaseType, (FetchStatement) sqlStatement);
        }
        return new UnknownSQLStatementContext(databaseType, sqlStatement);
    }
    
    private static SQLStatementContext getDCLStatementContext(final DatabaseType databaseType, final DCLStatement sqlStatement) {
        if (sqlStatement instanceof GrantStatement) {
            return new GrantStatementContext(databaseType, (GrantStatement) sqlStatement);
        }
        if (sqlStatement instanceof RevokeStatement) {
            return new RevokeStatementContext(databaseType, (RevokeStatement) sqlStatement);
        }
        if (sqlStatement instanceof DenyUserStatement) {
            return new DenyUserStatementContext(databaseType, (DenyUserStatement) sqlStatement);
        }
        return new UnknownSQLStatementContext(databaseType, sqlStatement);
    }
    
    private static SQLStatementContext getDALStatementContext(final ShardingSphereMetaData metaData, final DatabaseType databaseType,
                                                              final DALStatement sqlStatement, final List<Object> params, final String currentDatabaseName) {
        if (sqlStatement instanceof ExplainStatement) {
            return new ExplainStatementContext(metaData, databaseType, (ExplainStatement) sqlStatement, params, currentDatabaseName);
        }
        if (sqlStatement instanceof ShowCreateTableStatement) {
            return new ShowCreateTableStatementContext(databaseType, (ShowCreateTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof ShowColumnsStatement) {
            return new ShowColumnsStatementContext(databaseType, (ShowColumnsStatement) sqlStatement);
        }
        if (sqlStatement instanceof ShowTablesStatement) {
            return new ShowTablesStatementContext(databaseType, (ShowTablesStatement) sqlStatement);
        }
        if (sqlStatement instanceof ShowTableStatusStatement) {
            return new ShowTableStatusStatementContext(databaseType, (ShowTableStatusStatement) sqlStatement);
        }
        if (sqlStatement instanceof ShowIndexStatement) {
            return new ShowIndexStatementContext(databaseType, (ShowIndexStatement) sqlStatement);
        }
        if (sqlStatement instanceof AnalyzeTableStatement) {
            return new AnalyzeTableStatementContext(databaseType, (AnalyzeTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof FlushStatement) {
            return new FlushStatementContext(databaseType, (FlushStatement) sqlStatement);
        }
        if (sqlStatement instanceof OptimizeTableStatement) {
            return new OptimizeTableStatementContext(databaseType, (OptimizeTableStatement) sqlStatement);
        }
        return new UnknownSQLStatementContext(databaseType, sqlStatement);
    }
}
