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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableRALStatement;
import org.apache.shardingsphere.infra.distsql.query.RQLResultSet;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;

import java.util.Properties;

/**
 * Queryable RAL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryableRALBackendHandlerFactory {
    
    static {
        ShardingSphereServiceLoader.register(RQLResultSet.class);
    }
    
    /**
     * Create new instance of queryable RAL backend handler.
     * 
     * @param sqlStatement queryable RAL statement
     * @param backendConnection backend connection
     * @return queryable RAL backend handler
     */
    public static TextProtocolBackendHandler newInstance(final QueryableRALStatement sqlStatement, final BackendConnection backendConnection) {
        RQLResultSet rqlResultSet = TypedSPIRegistry.getRegisteredService(RQLResultSet.class, sqlStatement.getClass().getCanonicalName(), new Properties());
        return new QueryableRALBackendHandler(sqlStatement, backendConnection, rqlResultSet);
    }
}
