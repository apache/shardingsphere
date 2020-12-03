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

package org.apache.shardingsphere.proxy.backend.text.metadata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.data.DatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.metadata.environment.BroadcastBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;

/**
 * DAL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DALBackendHandlerFactory {
    
    /**
     * New instance of backend handler.
     * 
     * @param dalStatement DAL statement
     * @param sql SQL
     * @param backendConnection backend connection
     * @return backend handler
     */
    public static TextProtocolBackendHandler newInstance(final DALStatement dalStatement, final String sql, final BackendConnection backendConnection) {
        if (dalStatement instanceof SetStatement) {
            return new BroadcastBackendHandler(dalStatement, sql, backendConnection);
        }
        return new DatabaseBackendHandler(dalStatement, sql, backendConnection);
    }
}
