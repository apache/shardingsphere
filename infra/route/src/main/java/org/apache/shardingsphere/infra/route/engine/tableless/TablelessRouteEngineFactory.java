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
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.CursorAvailable;
import org.apache.shardingsphere.infra.route.engine.tableless.type.broadcast.DataSourceBroadcastRouteEngine;
import org.apache.shardingsphere.infra.route.engine.tableless.type.broadcast.InstanceBroadcastRouteEngine;
import org.apache.shardingsphere.infra.route.engine.tableless.type.ignore.IgnoreRouteEngine;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.AlterResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.CreateResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.DropResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.LoadStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ResetParameterStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.SetResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowTablesStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.TCLStatement;

/**
 * Tableless route engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TablelessRouteEngineFactory {
    
    /**
     * Create new instance of route engine.
     *
     * @param queryContext query context
     * @return created instance
     */
    public static TablelessRouteEngine newInstance(final QueryContext queryContext) {
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        if (sqlStatement instanceof DALStatement) {
            return getDALRouteEngine(sqlStatement);
        }
        // TODO remove this logic when savepoint handle in proxy and jdbc adapter
        if (sqlStatement instanceof TCLStatement) {
            return new DataSourceBroadcastRouteEngine();
        }
        if (sqlStatement instanceof DDLStatement) {
            return getDDLRouteEngine(queryContext.getSqlStatementContext());
        }
        return new IgnoreRouteEngine();
    }
    
    private static TablelessRouteEngine getDALRouteEngine(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof ShowTablesStatement || sqlStatement instanceof ShowTableStatusStatement || sqlStatement instanceof SetStatement) {
            return new DataSourceBroadcastRouteEngine();
        }
        if (sqlStatement instanceof ResetParameterStatement || sqlStatement instanceof ShowDatabasesStatement || sqlStatement instanceof LoadStatement) {
            return new DataSourceBroadcastRouteEngine();
        }
        if (isResourceGroupStatement(sqlStatement)) {
            return new InstanceBroadcastRouteEngine();
        }
        return new IgnoreRouteEngine();
    }
    
    private static boolean isResourceGroupStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof CreateResourceGroupStatement || sqlStatement instanceof AlterResourceGroupStatement || sqlStatement instanceof DropResourceGroupStatement
                || sqlStatement instanceof SetResourceGroupStatement;
    }
    
    private static TablelessRouteEngine getDDLRouteEngine(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof CursorAvailable) {
            return getCursorRouteEngine(sqlStatementContext);
        }
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (isFunctionDDLStatement(sqlStatement) || isSchemaDDLStatement(sqlStatement)) {
            return new DataSourceBroadcastRouteEngine();
        }
        return new IgnoreRouteEngine();
    }
    
    private static boolean isFunctionDDLStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof CreateFunctionStatement || sqlStatement instanceof AlterFunctionStatement || sqlStatement instanceof DropFunctionStatement;
    }
    
    private static boolean isSchemaDDLStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof CreateSchemaStatement || sqlStatement instanceof AlterSchemaStatement || sqlStatement instanceof DropSchemaStatement;
    }
    
    private static TablelessRouteEngine getCursorRouteEngine(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof CloseStatementContext && ((CloseStatementContext) sqlStatementContext).getSqlStatement().isCloseAll()) {
            return new DataSourceBroadcastRouteEngine();
        }
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof CreateTablespaceStatement || sqlStatement instanceof AlterTablespaceStatement || sqlStatement instanceof DropTablespaceStatement) {
            return new InstanceBroadcastRouteEngine();
        }
        return new IgnoreRouteEngine();
    }
}
