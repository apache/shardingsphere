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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.PreparedStatementCacheKey;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;

import java.sql.SQLException;

/**
 * Firebird statement resource cleaner.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdStatementResourceCleaner {
    
    private static final String PREPARED_STATEMENT_CACHE_KEY_PREFIX = "firebird:";
    
    /**
     * Create prepared statement cache key.
     *
     * @param statementId statement ID
     * @return prepared statement cache key
     */
    public static PreparedStatementCacheKey createPreparedStatementCacheKey(final int statementId) {
        return new PreparedStatementCacheKey(PREPARED_STATEMENT_CACHE_KEY_PREFIX + statementId);
    }
    
    /**
     * Clean Firebird statement resources.
     *
     * @param connectionSession connection session
     * @param statementId statement ID
     * @param invalidatePreparedStatementCache whether invalidate prepared statement cache
     * @throws SQLException SQL exception
     */
    public static void clean(final ConnectionSession connectionSession, final int statementId, final boolean invalidatePreparedStatementCache) throws SQLException {
        ProxyBackendHandler proxyBackendHandler = FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(connectionSession.getConnectionId(), statementId);
        if (null != proxyBackendHandler) {
            connectionSession.getDatabaseConnectionManager().removeResource(proxyBackendHandler);
            FirebirdFetchStatementCache.getInstance().unregisterStatement(connectionSession.getConnectionId(), statementId);
            connectionSession.getConnectionContext().clearCursorContext();
            try {
                proxyBackendHandler.close();
            } finally {
                invalidatePreparedStatementCacheIfNecessary(connectionSession, statementId, invalidatePreparedStatementCache);
            }
            return;
        }
        FirebirdFetchStatementCache.getInstance().unregisterStatement(connectionSession.getConnectionId(), statementId);
        connectionSession.getConnectionContext().clearCursorContext();
        invalidatePreparedStatementCacheIfNecessary(connectionSession, statementId, invalidatePreparedStatementCache);
    }
    
    private static void invalidatePreparedStatementCacheIfNecessary(final ConnectionSession connectionSession, final int statementId,
                                                                    final boolean invalidatePreparedStatementCache) {
        if (invalidatePreparedStatementCache) {
            connectionSession.invalidatePreparedStatementCache(createPreparedStatementCacheKey(statementId));
        }
    }
}
