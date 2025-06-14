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
import org.apache.shardingsphere.infra.binder.context.provider.DialectTableAvailableSQLStatementContextWarpProvider;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ShowColumnsStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ShowIndexStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ShowTableStatusStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ShowTablesStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.AlterIndexStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.AlterViewStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateProcedureStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateViewStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.DropIndexStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.FetchStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.MoveStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.TableAvailable;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowTablesStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.DCLStatement;
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
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.UpdateStatement;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        Optional<DialectTableAvailableSQLStatementContextWarpProvider> dialectTableAvailableSQLStatementContextWarpProvider = DatabaseTypedSPILoader.findService(
                DialectTableAvailableSQLStatementContextWarpProvider.class, databaseType);
        if (sqlStatement instanceof TableAvailable && dialectTableAvailableSQLStatementContextWarpProvider
                .map(optional -> dialectTableAvailableSQLStatementContextWarpProvider.get().getNeedToWarpTableAvailableSQLStatementContextTypes().contains(sqlStatement.getClass())).orElse(false)) {
            return new TableAvailableSQLStatementContext(databaseType, sqlStatement, ((TableAvailable) sqlStatement).getTables());
        }
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
        if (sqlStatement instanceof CallStatement || sqlStatement instanceof DoStatement || sqlStatement instanceof MergeStatement) {
            return new UnknownSQLStatementContext(databaseType, sqlStatement);
        }
        throw new UnsupportedSQLOperationException(String.format("Unsupported SQL statement `%s`", sqlStatement.getClass().getSimpleName()));
    }
    
    private static SQLStatementContext getDDLStatementContext(final ShardingSphereMetaData metaData, final DatabaseType databaseType,
                                                              final DDLStatement sqlStatement, final List<Object> params, final String currentDatabaseName) {
        if (sqlStatement instanceof CreateSchemaStatement) {
            return new UnknownSQLStatementContext(databaseType, sqlStatement);
        }
        if (sqlStatement instanceof CreateTableStatement) {
            return new CreateTableStatementContext(databaseType, (CreateTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof AlterTableStatement) {
            return new AlterTableStatementContext(databaseType, (AlterTableStatement) sqlStatement);
        }
        if (sqlStatement instanceof RenameTableStatement) {
            return new TableAvailableSQLStatementContext(databaseType, sqlStatement, ((RenameTableStatement) sqlStatement).getTables());
        }
        if (sqlStatement instanceof DropTableStatement) {
            return new TableAvailableSQLStatementContext(databaseType, sqlStatement, ((DropTableStatement) sqlStatement).getTables());
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
            return new TableAvailableSQLStatementContext(databaseType, sqlStatement, ((TruncateStatement) sqlStatement).getTables());
        }
        if (sqlStatement instanceof CreateFunctionStatement) {
            return new TableAvailableSQLStatementContext(databaseType, sqlStatement, ((CreateFunctionStatement) sqlStatement).getTables());
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
            return new TableAvailableSQLStatementContext(databaseType, sqlStatement, ((DropViewStatement) sqlStatement).getViews());
        }
        if (sqlStatement instanceof PrepareStatement) {
            return new TableAvailableSQLStatementContext(databaseType, sqlStatement, ((PrepareStatement) sqlStatement).getTables());
        }
        if (sqlStatement instanceof CommentStatement) {
            return new TableAvailableSQLStatementContext(databaseType, sqlStatement,
                    null == ((CommentStatement) sqlStatement).getTable() ? Collections.emptyList() : Collections.singletonList(((CommentStatement) sqlStatement).getTable()));
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
            return new TableAvailableSQLStatementContext(databaseType, sqlStatement, ((GrantStatement) sqlStatement).getTables());
        }
        if (sqlStatement instanceof RevokeStatement) {
            return new TableAvailableSQLStatementContext(databaseType, sqlStatement, ((RevokeStatement) sqlStatement).getTables());
        }
        return new UnknownSQLStatementContext(databaseType, sqlStatement);
    }
    
    private static SQLStatementContext getDALStatementContext(final ShardingSphereMetaData metaData, final DatabaseType databaseType,
                                                              final DALStatement sqlStatement, final List<Object> params, final String currentDatabaseName) {
        if (sqlStatement instanceof ExplainStatement) {
            return new ExplainStatementContext(metaData, databaseType, (ExplainStatement) sqlStatement, params, currentDatabaseName);
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
            return new TableAvailableSQLStatementContext(databaseType, sqlStatement, ((AnalyzeTableStatement) sqlStatement).getTables());
        }
        return new UnknownSQLStatementContext(databaseType, sqlStatement);
    }
}
