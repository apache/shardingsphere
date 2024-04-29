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

package org.apache.shardingsphere.proxy.backend.connector;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

/**
 * Database connector factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseConnectorFactory {
    
    private static final DatabaseConnectorFactory INSTANCE = new DatabaseConnectorFactory();
    
    /**
     * Get backend handler factory instance.
     *
     * @return backend handler factory
     */
    public static DatabaseConnectorFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * Create new instance of {@link DatabaseConnector}.
     *
     * @param queryContext query context
     * @param databaseConnectionManager database connection manager
     * @param preferPreparedStatement use prepared statement as possible
     * @return created instance
     */
    public DatabaseConnector newInstance(final QueryContext queryContext, final ProxyDatabaseConnectionManager databaseConnectionManager, final boolean preferPreparedStatement) {
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getDatabase(databaseConnectionManager.getConnectionSession().getDatabaseName());
        String driverType = preferPreparedStatement || !queryContext.getParameters().isEmpty() ? JDBCDriverType.PREPARED_STATEMENT : JDBCDriverType.STATEMENT;
        DatabaseConnector result = new DatabaseConnector(driverType, database, queryContext, databaseConnectionManager);
        databaseConnectionManager.add(result);
        return result;
    }
}
