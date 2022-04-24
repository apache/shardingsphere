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

package org.apache.shardingsphere.dbdiscovery.mysql.type.mgr;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbdiscovery.mysql.AbstractMySQLDatabaseDiscoveryType;
import org.apache.shardingsphere.infra.database.metadata.dialect.MySQLDataSourceMetaData;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.apache.shardingsphere.infra.storage.StorageNodeDataSource;
import org.apache.shardingsphere.infra.storage.StorageNodeRole;
import org.apache.shardingsphere.infra.storage.StorageNodeStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * MGR database discovery type for MySQL.
 */
@Slf4j
public final class MGRMySQLDatabaseDiscoveryType extends AbstractMySQLDatabaseDiscoveryType {
    
    private static final String QUERY_PLUGIN_STATUS = "SELECT PLUGIN_STATUS FROM information_schema.PLUGINS WHERE PLUGIN_NAME='group_replication'";
    
    private static final String QUERY_GROUP_NAME = "SELECT VARIABLE_VALUE FROM performance_schema.global_variables WHERE VARIABLE_NAME='group_replication_group_name'";
    
    private static final String QUERY_SINGLE_PRIMARY_MODE = "SELECT VARIABLE_VALUE FROM performance_schema.global_variables WHERE VARIABLE_NAME='group_replication_single_primary_mode'";
    
    private static final String QUERY_MEMBER_LIST = "SELECT MEMBER_HOST, MEMBER_PORT, MEMBER_STATE FROM performance_schema.replication_group_members";
    
    private static final String QUERY_PRIMARY_DATA_SOURCE = "SELECT MEMBER_HOST, MEMBER_PORT FROM performance_schema.replication_group_members WHERE MEMBER_ID = "
            + "(SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'group_replication_primary_member')";
    
    private static final String QUERY_CURRENT_MEMBER_STATE = "SELECT MEMBER_STATE FROM performance_schema.replication_group_members WHERE MEMBER_HOST=? AND MEMBER_PORT=?";
    
    @Override
    public MGRHighlyAvailableStatus loadHighlyAvailableStatus(final DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            return new MGRHighlyAvailableStatus(queryIsPluginActive(statement), queryIsSinglePrimaryMode(statement), queryGroupName(statement), queryMemberInstanceURLs(statement));
        }
    }
    
    private boolean queryIsPluginActive(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(QUERY_PLUGIN_STATUS)) {
            return resultSet.next() && "ACTIVE".equals(resultSet.getString("PLUGIN_STATUS"));
        }
    }
    
    private boolean queryIsSinglePrimaryMode(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(QUERY_SINGLE_PRIMARY_MODE)) {
            return resultSet.next() && "ON".equals(resultSet.getString("VARIABLE_VALUE"));
        }
    }
    
    private String queryGroupName(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(QUERY_GROUP_NAME)) {
            return resultSet.next() ? resultSet.getString("VARIABLE_VALUE") : "";
        }
    }
    
    private Collection<String> queryMemberInstanceURLs(final Statement statement) throws SQLException {
        Collection<String> result = new LinkedList<>();
        try (ResultSet resultSet = statement.executeQuery(QUERY_MEMBER_LIST)) {
            while (resultSet.next()) {
                result.add(String.format("%s:%s", resultSet.getString("MEMBER_HOST"), resultSet.getString("MEMBER_PORT")));
            }
        }
        return result;
    }
    
    @Override
    protected Optional<String> loadPrimaryDatabaseInstanceURL(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(QUERY_PRIMARY_DATA_SOURCE)) {
            return resultSet.next() ? Optional.of(String.format("%s:%s", resultSet.getString("MEMBER_HOST"), resultSet.getString("MEMBER_PORT"))) : Optional.empty();
        }
    }
    
    @Override
    public void updateMemberState(final String databaseName, final Map<String, DataSource> dataSourceMap, final String groupName) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            if (!entry.getKey().equals(getPrimaryDataSource())) {
                postDataSourceDisabledEvent(databaseName, groupName, entry.getKey(), entry.getValue());
            }
        }
    }
    
    private void postDataSourceDisabledEvent(final String databaseName, final String groupName, final String replicaDataSourceName, final DataSource replicaDataSource) {
        StorageNodeDataSource storageNodeDataSource = getStorageNodeDataSource(replicaDataSource);
        ShardingSphereEventBus.getInstance().post(new DataSourceDisabledEvent(databaseName, groupName, replicaDataSourceName, storageNodeDataSource));
    }
    
    private StorageNodeDataSource getStorageNodeDataSource(final DataSource replicaDataSource) {
        return new StorageNodeDataSource(StorageNodeRole.MEMBER, isDisabledDataSource(replicaDataSource) ? StorageNodeStatus.DISABLED : StorageNodeStatus.ENABLED);
    }
    
    private boolean isDisabledDataSource(final DataSource replicaDataSource) {
        try (Connection connection = replicaDataSource.getConnection()) {
            if (isOnlineDataSource(connection, new MySQLDataSourceMetaData(connection.getMetaData().getURL()))) {
                return false;
            }
        } catch (final SQLException ex) {
            log.error("An exception occurred while find data source urls", ex);
        }
        return true;
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
