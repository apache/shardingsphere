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

package org.apache.shardingsphere.infra.session.query;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;

import java.util.List;

/**
 * Query context.
 */
@Getter
public final class QueryContext {
    
    private final SQLStatementContext sqlStatementContext;
    
    private final String sql;
    
    private final List<Object> parameters;
    
    private final HintValueContext hintValueContext;
    
    private final ConnectionContext connectionContext;
    
    private final ShardingSphereMetaData metaData;
    
    private final ShardingSphereDatabase usedDatabase;
    
    private final boolean useCache;
    
    public QueryContext(final SQLStatementContext sqlStatementContext, final String sql, final List<Object> params, final HintValueContext hintValueContext, final ConnectionContext connectionContext,
                        final ShardingSphereMetaData metaData) {
        this(sqlStatementContext, sql, params, hintValueContext, connectionContext, metaData, false);
    }
    
    public QueryContext(final SQLStatementContext sqlStatementContext, final String sql, final List<Object> params, final HintValueContext hintValueContext, final ConnectionContext connectionContext,
                        final ShardingSphereMetaData metaData, final boolean useCache) {
        this.sqlStatementContext = sqlStatementContext;
        this.sql = sql;
        parameters = params;
        this.hintValueContext = hintValueContext;
        this.connectionContext = connectionContext;
        this.metaData = metaData;
        usedDatabase = getUsedDatabaseFromSQLStatement(sqlStatementContext, metaData, connectionContext);
        this.useCache = useCache;
    }
    
    private ShardingSphereDatabase getUsedDatabaseFromSQLStatement(final SQLStatementContext sqlStatementContext, final ShardingSphereMetaData metaData, final ConnectionContext connectionContext) {
        String usedDatabaseName =
                sqlStatementContext instanceof TableAvailable ? ((TableAvailable) sqlStatementContext).getTablesContext().getDatabaseName().orElse(getCurrentDatabaseName(connectionContext))
                        : getCurrentDatabaseName(connectionContext);
        if (metaData.containsDatabase(usedDatabaseName)) {
            return metaData.getDatabase(usedDatabaseName);
        }
        throw new UnknownDatabaseException(usedDatabaseName);
    }
    
    private String getCurrentDatabaseName(final ConnectionContext connectionContext) {
        return connectionContext.getCurrentDatabaseName().orElseThrow(NoDatabaseSelectedException::new);
    }
}
