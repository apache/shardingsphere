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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.transaction.framework.container.compose.DockerContainerComposer;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * JDBC data source.
 */
@Slf4j
public final class JdbcDataSource extends AutoDataSource {
    
    private final DockerContainerComposer containerComposer;
    
    public JdbcDataSource(final DockerContainerComposer containerComposer) {
        this.containerComposer = containerComposer;
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
        Connection result = containerComposer.getJdbcContainer().getTargetDataSource().getConnection();
        synchronized (this) {
            getConnectionCache().add(result);
        }
        return result;
    }
    
    private Connection createConnection() throws SQLException {
        return containerComposer.getJdbcContainer().getTargetDataSource().getConnection();
    }
}
