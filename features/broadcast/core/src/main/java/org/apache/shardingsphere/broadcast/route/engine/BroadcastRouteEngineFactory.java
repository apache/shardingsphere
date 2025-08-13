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

package org.apache.shardingsphere.broadcast.route.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.broadcast.route.engine.type.BroadcastRouteEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.broadcast.BroadcastDatabaseBroadcastRouteEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.broadcast.BroadcastTableBroadcastRouteEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.unicast.BroadcastUnicastRouteEngine;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.CursorSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Collection;

/**
 * Broadcast routing engine factory.
 */
@HighFrequencyInvocation
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BroadcastRouteEngineFactory {
    
    /**
     * Create new instance of broadcast routing engine.
     *
     * @param queryContext query context
     * @param broadcastTableNames broadcast table names
     * @return broadcast route engine
     */
    public static BroadcastRouteEngine newInstance(final QueryContext queryContext, final Collection<String> broadcastTableNames) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof DDLStatement) {
            return getDDLRouteEngine(queryContext, broadcastTableNames, sqlStatementContext);
        }
        if (sqlStatement instanceof DALStatement) {
            return getDALRouteEngine(broadcastTableNames);
        }
        if (sqlStatement instanceof DCLStatement) {
            return getDCLRouteEngine(broadcastTableNames);
        }
        return getDMLRouteEngine(sqlStatementContext, queryContext.getConnectionContext(), broadcastTableNames);
    }
    
    private static BroadcastRouteEngine getDDLRouteEngine(final QueryContext queryContext, final Collection<String> broadcastTableNames, final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement().getAttributes().findAttribute(CursorSQLStatementAttribute.class).isPresent()
                ? new BroadcastUnicastRouteEngine(sqlStatementContext, broadcastTableNames, queryContext.getConnectionContext())
                : new BroadcastTableBroadcastRouteEngine(broadcastTableNames);
    }
    
    private static BroadcastRouteEngine getDALRouteEngine(final Collection<String> broadcastTableNames) {
        return new BroadcastTableBroadcastRouteEngine(broadcastTableNames);
    }
    
    private static BroadcastRouteEngine getDCLRouteEngine(final Collection<String> broadcastTableNames) {
        return new BroadcastTableBroadcastRouteEngine(broadcastTableNames);
    }
    
    private static BroadcastRouteEngine getDMLRouteEngine(final SQLStatementContext sqlStatementContext, final ConnectionContext connectionContext, final Collection<String> broadcastTableNames) {
        return sqlStatementContext.getSqlStatement() instanceof SelectStatement
                ? new BroadcastUnicastRouteEngine(sqlStatementContext, broadcastTableNames, connectionContext)
                : new BroadcastDatabaseBroadcastRouteEngine();
    }
}
