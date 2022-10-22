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

package org.apache.shardingsphere.proxy.backend.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ServerPreparedStatement} registry for {@link ConnectionSession}.
 */
public final class ServerPreparedStatementRegistry {
    
    private final Map<Object, ServerPreparedStatement> preparedStatements = new ConcurrentHashMap<>();
    
    /**
     * Add {@link ServerPreparedStatement} into registry.
     *
     * @param statementId statement ID
     * @param serverPreparedStatement server prepared statement
     */
    public void addPreparedStatement(final Object statementId, final ServerPreparedStatement serverPreparedStatement) {
        preparedStatements.put(statementId, serverPreparedStatement);
    }
    
    /**
     * Get {@link ServerPreparedStatement} by statement ID.
     *
     * @param <T> implementation of {@link ServerPreparedStatement}
     * @param statementId statement ID
     * @return {@link ServerPreparedStatement}
     */
    @SuppressWarnings("unchecked")
    public <T extends ServerPreparedStatement> T getPreparedStatement(final Object statementId) {
        return (T) preparedStatements.get(statementId);
    }
    
    /**
     * Remove {@link ServerPreparedStatement} from registry.
     *
     * @param statementId statement ID
     */
    public void removePreparedStatement(final Object statementId) {
        preparedStatements.remove(statementId);
    }
}
