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

package org.apache.shardingsphere.integration.transaction.engine.base;

import org.apache.shardingsphere.integration.transaction.framework.container.compose.BaseContainerComposer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Proxy datasource.
 */
public final class ProxyDataSource extends AutoDataSource {
    
    private final BaseContainerComposer containerComposer;
    
    private final String databaseName;
    
    private final String userName;
    
    private final String password;
    
    public ProxyDataSource(final BaseContainerComposer containerComposer, final String databaseName, final String userName, final String password) {
        this.containerComposer = containerComposer;
        this.databaseName = databaseName;
        this.userName = userName;
        this.password = password;
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        Connection result = createConnection();
        synchronized (this) {
            getConnectionCache().add(result);
        }
        return result;
    }
    
    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        String jdbcUrl = containerComposer.getProxyJdbcUrl(databaseName);
        Connection result = DriverManager.getConnection(jdbcUrl, username, password);
        synchronized (this) {
            getConnectionCache().add(result);
        }
        return result;
    }
    
    private Connection createConnection() throws SQLException {
        String jdbcUrl = containerComposer.getProxyJdbcUrl(databaseName);
        return DriverManager.getConnection(jdbcUrl, userName, password);
    }
}
