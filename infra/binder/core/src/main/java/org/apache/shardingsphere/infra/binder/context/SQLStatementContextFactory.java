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

package org.apache.shardingsphere.infra.binder.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.AlterViewStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateProcedureStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateViewStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorHeldSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.CursorSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

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
        if (sqlStatement.getAttributes().findAttribute(TableSQLStatementAttribute.class).isPresent()
                && DatabaseTypedSPILoader.findService(DialectCommonSQLStatementContextWarpProvider.class, databaseType)
                        .map(optional -> optional.getNeedToWarpSQLStatementTypes().contains(sqlStatement.getClass())).orElse(false)) {
            return new CommonSQLStatementContext(databaseType, sqlStatement);
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
        return new CommonSQLStatementContext(databaseType, sqlStatement);
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
        return new CommonSQLStatementContext(databaseType, sqlStatement);
    }
    
    private static SQLStatementContext getDDLStatementContext(final ShardingSphereMetaData metaData, final DatabaseType databaseType,
                                                              final DDLStatement sqlStatement, final List<Object> params, final String currentDatabaseName) {
        if (sqlStatement instanceof CreateProcedureStatement) {
            return new CreateProcedureStatementContext(databaseType, (CreateProcedureStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateViewStatement) {
            return new CreateViewStatementContext(metaData, databaseType, params, (CreateViewStatement) sqlStatement, currentDatabaseName);
        }
        if (sqlStatement instanceof AlterViewStatement) {
            return new AlterViewStatementContext(metaData, databaseType, params, (AlterViewStatement) sqlStatement, currentDatabaseName);
        }
        if (sqlStatement instanceof CursorStatement) {
            return new CursorStatementContext(metaData, databaseType, params, (CursorStatement) sqlStatement, currentDatabaseName);
        }
        if (sqlStatement.getAttributes().findAttribute(CursorSQLStatementAttribute.class).isPresent()) {
            return new CursorHeldSQLStatementContext(databaseType, sqlStatement);
        }
        return new CommonSQLStatementContext(databaseType, sqlStatement);
    }
    
    private static SQLStatementContext getDCLStatementContext(final DatabaseType databaseType, final DCLStatement sqlStatement) {
        return new CommonSQLStatementContext(databaseType, sqlStatement);
    }
    
    private static SQLStatementContext getDALStatementContext(final ShardingSphereMetaData metaData, final DatabaseType databaseType,
                                                              final DALStatement sqlStatement, final List<Object> params, final String currentDatabaseName) {
        if (sqlStatement instanceof ExplainStatement) {
            return new ExplainStatementContext(metaData, databaseType, (ExplainStatement) sqlStatement, params, currentDatabaseName);
        }
        return new CommonSQLStatementContext(databaseType, sqlStatement);
    }
}
