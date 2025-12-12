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

package org.apache.shardingsphere.infra.route.engine.tableless;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.generic.NoTablelessRouteInfoException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.engine.tableless.type.broadcast.TablelessDataSourceBroadcastRouteEngine;
import org.apache.shardingsphere.infra.route.engine.tableless.type.broadcast.TablelessInstanceBroadcastRouteEngine;
import org.apache.shardingsphere.infra.route.engine.tableless.type.unicast.TablelessDataSourceUnicastRouteEngine;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.CursorSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TablelessDataSourceBroadcastRouteSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.AlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.CreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.tablespace.DropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;

import java.util.Optional;

/**
 * Tableless route engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TablelessRouteEngineFactory {
    
    /**
     * Create new instance of route engine.
     *
     * @param queryContext query context
     * @param database database
     * @return created instance
     * @throws NoTablelessRouteInfoException if the SQL statement is not supported by tableless route engine
     */
    public static TablelessRouteEngine newInstance(final QueryContext queryContext, final ShardingSphereDatabase database) {
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        if (sqlStatement instanceof DMLStatement) {
            return getDMLRouteEngine(queryContext.getSqlStatementContext());
        }
        if (sqlStatement instanceof DDLStatement) {
            return getDDLRouteEngine(queryContext.getSqlStatementContext(), database);
        }
        // TODO remove this logic when proxy and jdbc support all dal statement @duanzhengqiang
        if (sqlStatement instanceof DALStatement) {
            return getDALRouteEngine((DALStatement) sqlStatement, database);
        }
        // TODO Support more TCL statements by transaction module, then remove this.
        if (sqlStatement instanceof TCLStatement) {
            return new TablelessDataSourceBroadcastRouteEngine();
        }
        throw new NoTablelessRouteInfoException();
    }
    
    private static TablelessRouteEngine getDMLRouteEngine(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof SelectStatementContext) {
            return new TablelessDataSourceUnicastRouteEngine();
        }
        throw new NoTablelessRouteInfoException();
    }
    
    private static TablelessRouteEngine getDDLRouteEngine(final SQLStatementContext sqlStatementContext, final ShardingSphereDatabase database) {
        if (sqlStatementContext.getSqlStatement().getAttributes().findAttribute(CursorSQLStatementAttribute.class).isPresent()) {
            return getCursorRouteEngine(sqlStatementContext.getSqlStatement(), database);
        }
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (isFunctionDDLStatement(sqlStatement) || isSchemaDDLStatement(sqlStatement)) {
            return new TablelessDataSourceBroadcastRouteEngine();
        }
        throw new NoTablelessRouteInfoException();
    }
    
    private static TablelessRouteEngine getCursorRouteEngine(final SQLStatement sqlStatement, final ShardingSphereDatabase database) {
        if (sqlStatement instanceof CloseStatement && ((CloseStatement) sqlStatement).isCloseAll()) {
            return new TablelessDataSourceBroadcastRouteEngine();
        }
        if (sqlStatement instanceof CreateTablespaceStatement || sqlStatement instanceof AlterTablespaceStatement || sqlStatement instanceof DropTablespaceStatement) {
            return new TablelessInstanceBroadcastRouteEngine(database);
        }
        throw new NoTablelessRouteInfoException();
    }
    
    private static boolean isFunctionDDLStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof CreateFunctionStatement || sqlStatement instanceof AlterFunctionStatement || sqlStatement instanceof DropFunctionStatement;
    }
    
    private static boolean isSchemaDDLStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof CreateSchemaStatement || sqlStatement instanceof AlterSchemaStatement || sqlStatement instanceof DropSchemaStatement;
    }
    
    private static TablelessRouteEngine getDALRouteEngine(final DALStatement sqlStatement, final ShardingSphereDatabase database) {
        if (sqlStatement instanceof SetStatement || sqlStatement.getAttributes().findAttribute(TablelessDataSourceBroadcastRouteSQLStatementAttribute.class).isPresent()) {
            return new TablelessDataSourceBroadcastRouteEngine();
        }
        Optional<DialectDALStatementBroadcastRouteDecider> dialectDALStatementBroadcastRouteDecider = DatabaseTypedSPILoader.findService(
                DialectDALStatementBroadcastRouteDecider.class, sqlStatement.getDatabaseType());
        if (dialectDALStatementBroadcastRouteDecider.map(optional -> optional.isDataSourceBroadcastRoute(sqlStatement)).orElse(false)) {
            return new TablelessDataSourceBroadcastRouteEngine();
        }
        if (dialectDALStatementBroadcastRouteDecider.map(optional -> optional.isInstanceBroadcastRoute(sqlStatement)).orElse(false)) {
            return new TablelessInstanceBroadcastRouteEngine(database);
        }
        throw new NoTablelessRouteInfoException();
    }
}
