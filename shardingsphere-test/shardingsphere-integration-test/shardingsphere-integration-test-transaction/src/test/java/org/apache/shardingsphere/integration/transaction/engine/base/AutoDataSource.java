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

import lombok.Getter;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Datasource for caching connections.
 */
public abstract class AutoDataSource implements DataSource {
    
    @Getter
    private final Set<Connection> connectionCache = new HashSet<>();
    
    /**
     * Close all connections.
     * 
     * @throws SQLException SQL exception
     */
    public synchronized void close() throws SQLException {
        for (Connection each : connectionCache) {
            if (!each.isClosed()) {
                each.close();
            }
        }
        connectionCache.clear();
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) {
        return null;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return false;
    }
    
    @Override
    public PrintWriter getLogWriter() {
        return null;
    }
    
    @Override
    public void setLogWriter(final PrintWriter out) {
        
    }
    
    @Override
    public void setLoginTimeout(final int seconds) {
        
    }
    
    @Override
    public int getLoginTimeout() {
        return 0;
    }
    
    @Override
    public Logger getParentLogger() {
        return null;
    }
}
