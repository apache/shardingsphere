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

package org.apache.shardingsphere.dbdiscovery.mysql.type;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;
import org.apache.shardingsphere.dbdiscovery.spi.ReplicaDataSourceStatus;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Normal replication database discovery provider algorithm for MySQL.
 */
@Getter
@Slf4j
public final class MySQLNormalReplicationDatabaseDiscoveryProviderAlgorithm implements DatabaseDiscoveryProviderAlgorithm {
    
    private static final String SHOW_SLAVE_STATUS = "SHOW SLAVE STATUS";
    
    private static final String SHOW_SLAVE_HOSTS = "SHOW SLAVE HOSTS";
    
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @Override
    public void checkEnvironment(final String databaseName, final Collection<DataSource> dataSources) {
        ExecutorService executorService = ExecutorEngine.createExecutorEngineWithCPUAndResources(dataSources.size()).getExecutorServiceManager().getExecutorService();
        Collection<CompletableFuture<Collection<String>>> completableFutures = new LinkedList<>();
        for (DataSource dataSource : dataSources) {
            completableFutures.add(supplyAsyncCheckEnvironment(dataSource, executorService));
        }
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
        Iterator<CompletableFuture<Collection<String>>> replicationInstancesFuture = completableFutures.stream().iterator();
        int replicationGroupCount = 0;
        while (replicationInstancesFuture.hasNext()) {
            if (!replicationInstancesFuture.next().join().isEmpty()) {
                replicationGroupCount++;
            }
        }
        Preconditions.checkState(1 == replicationGroupCount, "Check Environment are failed in database `%s`.", databaseName);
    }
    
    private CompletableFuture<Collection<String>> supplyAsyncCheckEnvironment(final DataSource dataSource, final ExecutorService executorService) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getReplicationInstances(dataSource);
            } catch (SQLException ex) {
                throw new ShardingSphereException(ex);
            }
        }, executorService);
    }
    
    private Collection<String> getReplicationInstances(final DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            return getReplicationInstances(statement);
        }
    }
    
    private Collection<String> getReplicationInstances(final Statement statement) throws SQLException {
        Collection<String> result = new LinkedList<>();
        try (ResultSet resultSet = statement.executeQuery(SHOW_SLAVE_HOSTS)) {
            while (resultSet.next()) {
                result.add(String.join(":", resultSet.getString("HOST"), resultSet.getString("PORT")));
            }
        }
        return result;
    }
    
    @Override
    public boolean isPrimaryInstance(final DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(SHOW_SLAVE_STATUS)) {
            return !resultSet.next();
        }
    }
    
    @Override
    public ReplicaDataSourceStatus loadReplicaStatus(final DataSource replicaDataSource) throws SQLException {
        try (
                Connection connection = replicaDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            long replicationDelayMilliseconds = queryReplicationDelayMilliseconds(statement);
            boolean isDelay = replicationDelayMilliseconds >= Long.parseLong(getProps().getProperty("delay-milliseconds-threshold"));
            return new ReplicaDataSourceStatus(!isDelay, replicationDelayMilliseconds);
        }
    }
    
    private long queryReplicationDelayMilliseconds(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(SHOW_SLAVE_STATUS)) {
            return resultSet.next() ? resultSet.getLong("Seconds_Behind_Master") * 1000L : 0L;
        }
    }
    
    @Override
    public String getType() {
        return "MySQL.NORMAL_REPLICATION";
    }
}
