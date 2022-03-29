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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.scaling.query;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.scaling.QueryableScalingRALStatement;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import java.util.Properties;

/**
 * Queryable scaling RAL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryableScalingRALBackendHandlerFactory {
    
    static {
        ShardingSphereServiceLoader.register(DistSQLResultSet.class);
    }
    
    /**
     * Create new instance of queryable RAL backend handler.
     * 
     * @param sqlStatement queryable RAL statement
     * @param connectionSession connection session
     * @return queryable RAL backend handler
     */
    public static TextProtocolBackendHandler newInstance(final QueryableScalingRALStatement sqlStatement, final ConnectionSession connectionSession) {
        DistSQLResultSet resultSet = TypedSPIRegistry.getRegisteredService(DistSQLResultSet.class, sqlStatement.getClass().getCanonicalName(), new Properties());
        return new QueryableScalingRALBackendHandler(sqlStatement, connectionSession, resultSet);
    }
}
