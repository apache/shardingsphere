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

package org.apache.shardingsphere.data.pipeline.core.datasource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.spi.PipelineDataSourceCreator;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Pipeline data source.
 */
@RequiredArgsConstructor
@Slf4j
public final class PipelineDataSource implements DataSource, AutoCloseable {
    
    private final DataSource dataSource;
    
    @Getter
    private final DatabaseType databaseType;
    
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    @SneakyThrows(SQLException.class)
    public PipelineDataSource(final PipelineDataSourceConfiguration pipelineDataSourceConfig) {
        dataSource = TypedSPILoader.getService(PipelineDataSourceCreator.class, pipelineDataSourceConfig.getType()).create(pipelineDataSourceConfig.getDataSourceConfiguration());
        databaseType = pipelineDataSourceConfig.getDatabaseType();
    }
    
    /**
     * Whether underlying data source is closed or not.
     *
     * @return true if closed
     */
    public boolean isClosed() {
        return closed.get();
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return dataSource.getConnection(username, password);
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return dataSource.unwrap(iface);
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return dataSource.isWrapperFor(iface);
    }
    
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }
    
    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }
    
    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }
    
    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }
    
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }
    
    @Override
    public void close() {
        if (closed.get()) {
            return;
        }
        if (!(dataSource instanceof AutoCloseable)) {
            log.warn("Data source is not closed, it might cause connection leak, data source: {}", dataSource);
            return;
        }
        new DataSourcePoolDestroyer(dataSource).asyncDestroy();
        closed.set(true);
    }
}
