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

import com.google.common.base.Joiner;
import lombok.Getter;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;

import java.util.Collection;
import java.util.Collections;
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
    
    private final Collection<String> usedDatabaseNames;
    
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
        usedDatabaseNames = getUsedDatabaseNames(sqlStatementContext, connectionContext);
        this.useCache = useCache;
    }
    
    private Collection<String> getUsedDatabaseNames(final SQLStatementContext sqlStatementContext, final ConnectionContext connectionContext) {
        Collection<String> databaseNamesFromSQL = sqlStatementContext.getTablesContext().getDatabaseNames();
        return databaseNamesFromSQL.isEmpty() ? connectionContext.getCurrentDatabaseName().map(Collections::singletonList).orElse(Collections.emptyList()) : databaseNamesFromSQL;
    }
    
    /**
     * Get used database.
     *
     * @return used database
     */
    public ShardingSphereDatabase getUsedDatabase() {
        ShardingSpherePreconditions.checkState(usedDatabaseNames.size() <= 1,
                () -> new UnsupportedSQLOperationException(String.format("Can not support multiple logic databases [%s]", Joiner.on(", ").join(usedDatabaseNames))));
        ShardingSpherePreconditions.checkState(usedDatabaseNames.size() == 1, NoDatabaseSelectedException::new);
        String databaseName = usedDatabaseNames.iterator().next();
        ShardingSpherePreconditions.checkState(metaData.containsDatabase(databaseName), () -> new UnknownDatabaseException(databaseName));
        return metaData.getDatabase(databaseName);
    }
}
