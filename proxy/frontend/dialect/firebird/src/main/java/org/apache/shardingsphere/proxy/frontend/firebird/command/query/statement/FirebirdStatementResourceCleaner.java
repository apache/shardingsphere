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
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;

/**
 * Firebird statement resource cleaner.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdStatementResourceCleaner {
    
    /**
     * Clean Firebird statement resources.
     *
     * @param connectionSession connection session
     * @param statementId statement ID
     * @param invalidatePreparedStatementCache whether invalidate prepared statement cache
     */
    public static void clean(final ConnectionSession connectionSession, final int statementId, final boolean invalidatePreparedStatementCache) {
        if (invalidatePreparedStatementCache) {
            connectionSession.invalidateFirebirdPreparedStatementCache(statementId);
        }
        connectionSession.getConnectionContext().clearCursorContext();
        ProxyBackendHandler proxyBackendHandler = FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(connectionSession.getConnectionId(), statementId);
        if (null != proxyBackendHandler) {
            connectionSession.getDatabaseConnectionManager().unmarkResourceInUse(proxyBackendHandler);
        }
        FirebirdFetchStatementCache.getInstance().unregisterStatement(connectionSession.getConnectionId(), statementId);
    }
}
