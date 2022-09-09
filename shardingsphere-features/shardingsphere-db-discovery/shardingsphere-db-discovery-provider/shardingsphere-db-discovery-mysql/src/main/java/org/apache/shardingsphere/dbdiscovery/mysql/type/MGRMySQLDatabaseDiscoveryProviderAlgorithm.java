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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbdiscovery.mysql.exception.InvalidMGRGroupNameConfigurationException;
import org.apache.shardingsphere.dbdiscovery.mysql.exception.InvalidMGRModeException;
import org.apache.shardingsphere.dbdiscovery.mysql.exception.InvalidMGRPluginException;
import org.apache.shardingsphere.dbdiscovery.mysql.exception.InvalidMGRReplicationGroupMemberException;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;
import org.apache.shardingsphere.dbdiscovery.spi.ReplicaDataSourceStatus;
import org.apache.shardingsphere.infra.database.metadata.dialect.MySQLDataSourceMetaData;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.wrapper.SQLWrapperException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * MGR database discovery provider algorithm for MySQL.
 */
@Getter
@Slf4j
public final class MGRMySQLDatabaseDiscoveryProviderAlgorithm implements DatabaseDiscoveryProviderAlgorithm {
    
    private static final String QUERY_PLUGIN_STATUS = "SELECT PLUGIN_STATUS FROM information_schema.PLUGINS WHERE PLUGIN_NAME='group_replication'";
    
    private static final String QUERY_SINGLE_PRIMARY_MODE = "SELECT VARIABLE_VALUE FROM performance_schema.global_variables WHERE VARIABLE_NAME='group_replication_single_primary_mode'";
    
    private static final String QUERY_GROUP_NAME = "SELECT VARIABLE_VALUE FROM performance_schema.global_variables WHERE VARIABLE_NAME='group_replication_group_name'";
    
    private static final String QUERY_MEMBER_LIST = "SELECT MEMBER_HOST, MEMBER_PORT, MEMBER_STATE FROM performance_schema.replication_group_members";
    
    private static final String QUERY_PRIMARY_DATA_SOURCE = "SELECT MEMBER_HOST, MEMBER_PORT FROM performance_schema.replication_group_members WHERE MEMBER_ID = "
            + "(SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'group_replication_primary_member')";
    
    private static final String QUERY_CURRENT_MEMBER_STATE = "SELECT MEMBER_STATE FROM performance_schema.replication_group_members WHERE MEMBER_HOST=? AND MEMBER_PORT=?";
    
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @Override
    public void checkEnvironment(final String databaseName, final Collection<DataSource> dataSources) {
        ExecutorService executorService = ExecutorEngine.createExecutorEngineWithCPUAndResources(dataSources.size()).getExecutorServiceManager().getExecutorService();
        Collection<CompletableFuture<Void>> completableFutures = new LinkedList<>();
        for (DataSource dataSource : dataSources) {
            completableFutures.add(runAsyncCheckEnvironment(databaseName, dataSource, executorService));
        }
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
        Iterator<CompletableFuture<Void>> mgrInstancesFuture = completableFutures.stream().iterator();
        while (mgrInstancesFuture.hasNext()) {
            mgrInstancesFuture.next().join();
        }
    }
    
    private CompletableFuture<Void> runAsyncCheckEnvironment(final String databaseName, final DataSource dataSource, final ExecutorService executorService) {
        return CompletableFuture.runAsync(() -> {
            try {
                checkSingleDataSourceEnvironment(databaseName, dataSource);
            } catch (SQLException ex) {
                throw new SQLWrapperException(ex);
            }
        }, executorService);
    }
    
    private void checkSingleDataSourceEnvironment(final String databaseName, final DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            checkPluginActive(databaseName, statement);
            checkSinglePrimaryMode(databaseName, statement);
            checkGroupName(databaseName, statement);
            checkMemberInstanceURL(databaseName, connection.getMetaData().getURL(), statement);
        }
    }
    
    private void checkPluginActive(final String databaseName, final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(QUERY_PLUGIN_STATUS)) {
            ShardingSpherePreconditions.checkState(resultSet.next() && "ACTIVE".equals(resultSet.getString("PLUGIN_STATUS")), new InvalidMGRPluginException(databaseName));
        }
    }
    
    private void checkSinglePrimaryMode(final String databaseName, final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(QUERY_SINGLE_PRIMARY_MODE)) {
            ShardingSpherePreconditions.checkState(resultSet.next() && "ON".equals(resultSet.getString("VARIABLE_VALUE")), new InvalidMGRModeException(databaseName));
        }
    }
    
    private void checkGroupName(final String databaseName, final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(QUERY_GROUP_NAME)) {
            ShardingSpherePreconditions.checkState(resultSet.next() && props.getProperty("group-name", "").equals(resultSet.getString("VARIABLE_VALUE")),
                    new InvalidMGRGroupNameConfigurationException(props.getProperty("group-name"), databaseName));
        }
    }
    
    private void checkMemberInstanceURL(final String databaseName, final String url, final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(QUERY_MEMBER_LIST)) {
            while (resultSet.next()) {
                if (url.contains(String.join(":", resultSet.getString("MEMBER_HOST"), resultSet.getString("MEMBER_PORT")))) {
                    return;
                }
            }
        }
        throw new InvalidMGRReplicationGroupMemberException(url, databaseName);
    }
    
    @Override
    public boolean isPrimaryInstance(final DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(QUERY_PRIMARY_DATA_SOURCE)) {
            if (resultSet.next()) {
                MySQLDataSourceMetaData metaData = new MySQLDataSourceMetaData(connection.getMetaData().getURL());
                return metaData.getHostname().equals(resultSet.getString("MEMBER_HOST")) && Integer.toString(metaData.getPort()).equals(resultSet.getString("MEMBER_PORT"));
            }
        }
        return false;
    }
    
    @Override
    public ReplicaDataSourceStatus loadReplicaStatus(final DataSource replicaDataSource) throws SQLException {
        try (Connection connection = replicaDataSource.getConnection()) {
            return new ReplicaDataSourceStatus(isOnlineDataSource(connection, new MySQLDataSourceMetaData(connection.getMetaData().getURL())), 0L);
        }
    }
    
    private boolean isOnlineDataSource(final Connection connection, final MySQLDataSourceMetaData metaData) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(QUERY_CURRENT_MEMBER_STATE)) {
            preparedStatement.setString(1, metaData.getHostname());
            preparedStatement.setString(2, Integer.toString(metaData.getPort()));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && "ONLINE".equals(resultSet.getString("MEMBER_STATE"));
            }
        }
    }
    
    @Override
    public String getType() {
        return "MySQL.MGR";
    }
}
