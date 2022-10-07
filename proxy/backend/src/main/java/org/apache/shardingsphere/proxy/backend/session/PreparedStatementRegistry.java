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
 * {@link PreparedStatement} registry for {@link ConnectionSession}.
 */
public final class PreparedStatementRegistry {
    
    private final Map<Object, PreparedStatement> preparedStatements = new ConcurrentHashMap<>();
    
    /**
     * Add {@link PreparedStatement} into registry.
     *
     * @param statementId statement ID
     * @param preparedStatement prepared statement
     */
    public void addPreparedStatement(final Object statementId, final PreparedStatement preparedStatement) {
        preparedStatements.put(statementId, preparedStatement);
    }
    
    /**
     * Get prepared statement by statement ID.
     *
     * @param <T> implementation of {@link PreparedStatement}
     * @param statementId statement ID
     * @return {@link PreparedStatement}
     */
    @SuppressWarnings("unchecked")
    public <T extends PreparedStatement> T getPreparedStatement(final Object statementId) {
        return (T) preparedStatements.get(statementId);
    }
    
    /**
     * Remove {@link PreparedStatement} from registry.
     *
     * @param statementId statement ID
     */
    public void removePreparedStatement(final Object statementId) {
        preparedStatements.remove(statementId);
    }
}
