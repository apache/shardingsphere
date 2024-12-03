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

package org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.impl;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.ComboITContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.ITContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.AdapterContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.testcontainers.lifecycle.Startable;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ShardingSphere proxy container for cluster mode.
 */
public final class ShardingSphereMultiProxyClusterContainer implements AdapterContainer, ComboITContainer {
    
    private final AtomicReference<DataSource> targetDataSourceProvider = new AtomicReference<>();
    
    private final Collection<ShardingSphereProxyClusterContainer> proxyClusterContainers = new LinkedList<>();
    
    public ShardingSphereMultiProxyClusterContainer(final DatabaseType databaseType, final AdaptorContainerConfiguration config) {
        ShardingSphereProxyClusterContainer proxy1 = new ShardingSphereProxyClusterContainer(databaseType, config);
        proxy1.setAbbreviation("proxy1");
        proxy1.setName("proxy1");
        proxyClusterContainers.add(proxy1);
        ShardingSphereProxyClusterContainer proxy2 = new ShardingSphereProxyClusterContainer(databaseType, config);
        proxy1.setAbbreviation("proxy2");
        proxy1.setName("proxy2");
        proxyClusterContainers.add(proxy2);
    }
    
    @Override
    public DataSource getTargetDataSource(final String serverLists) {
        DataSource dataSource = targetDataSourceProvider.get();
        if (null == dataSource) {
            targetDataSourceProvider.set(new RandomDataSourceAdapter(proxyClusterContainers.stream().map(each -> each.getTargetDataSource(serverLists)).collect(Collectors.toSet())));
        }
        return targetDataSourceProvider.get();
    }
    
    @Override
    public String getAbbreviation() {
        return ProxyContainerConstants.PROXY_CONTAINER_ABBREVIATION;
    }
    
    @Override
    public void start() {
        proxyClusterContainers.forEach(Startable::start);
    }
    
    @Override
    public Collection<ITContainer> getContainers() {
        return proxyClusterContainers.stream().map(ITContainer.class::cast).collect(Collectors.toList());
    }
    
    private static class RandomDataSourceAdapter implements DataSource {
        
        private final DataSource[] dataSources;
        
        RandomDataSourceAdapter(final Set<DataSource> dataSources) {
            this.dataSources = dataSources.toArray(new DataSource[0]);
        }
        
        private DataSource getDataSource() {
            return dataSources[ThreadLocalRandom.current().nextInt(dataSources.length)];
        }
        
        @Override
        public Connection getConnection() throws SQLException {
            return getDataSource().getConnection();
        }
        
        @Override
        public Connection getConnection(final String username, final String password) throws SQLException {
            return getDataSource().getConnection(username, password);
        }
        
        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return getDataSource().getLogWriter();
        }
        
        @Override
        public void setLogWriter(final PrintWriter out) throws SQLException {
            getDataSource().setLogWriter(out);
        }
        
        @Override
        public void setLoginTimeout(final int seconds) throws SQLException {
            for (DataSource each : dataSources) {
                each.setLoginTimeout(seconds);
            }
        }
        
        @Override
        public int getLoginTimeout() throws SQLException {
            return getDataSource().getLoginTimeout();
        }
        
        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return getDataSource().getParentLogger();
        }
        
        @Override
        public <T> T unwrap(final Class<T> iface) throws SQLException {
            return getDataSource().unwrap(iface);
        }
        
        @Override
        public boolean isWrapperFor(final Class<?> iface) throws SQLException {
            return getDataSource().isWrapperFor(iface);
        }
    }
}
