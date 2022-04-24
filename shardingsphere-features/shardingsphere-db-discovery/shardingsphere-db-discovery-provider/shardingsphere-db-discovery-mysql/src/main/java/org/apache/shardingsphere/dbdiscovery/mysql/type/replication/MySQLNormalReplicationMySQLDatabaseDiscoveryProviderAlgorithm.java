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

package org.apache.shardingsphere.dbdiscovery.mysql.type.replication;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;
import org.apache.shardingsphere.dbdiscovery.spi.instance.type.IPPortPrimaryDatabaseInstance;
import org.apache.shardingsphere.infra.storage.StorageNodeDataSource;
import org.apache.shardingsphere.infra.storage.StorageNodeRole;
import org.apache.shardingsphere.infra.storage.StorageNodeStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * Normal replication database discovery provider algorithm for MySQL.
 */
@Getter
@Setter
@Slf4j
public final class MySQLNormalReplicationMySQLDatabaseDiscoveryProviderAlgorithm implements DatabaseDiscoveryProviderAlgorithm {
    
    private static final String SHOW_SLAVE_STATUS = "SHOW SLAVE STATUS";
    
    private Properties props = new Properties();
    
    @Override
    public MySQLNormalReplicationHighlyAvailableStatus loadHighlyAvailableStatus(final DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            return new MySQLNormalReplicationHighlyAvailableStatus(loadPrimaryDatabaseInstance(statement).orElse(null));
        }
    }
    
    @Override
    public Optional<IPPortPrimaryDatabaseInstance> findPrimaryInstance(final Map<String, DataSource> dataSourceMap) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            try (
                    Connection connection = entry.getValue().getConnection();
                    Statement statement = connection.createStatement()) {
                return loadPrimaryDatabaseInstance(statement);
            } catch (final SQLException ex) {
                log.error("An exception occurred while find primary data source name", ex);
            }
        }
        return Optional.empty();
    }
    
    private Optional<IPPortPrimaryDatabaseInstance> loadPrimaryDatabaseInstance(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(SHOW_SLAVE_STATUS)) {
            if (resultSet.next()) {
                String masterHost = resultSet.getString("Master_Host");
                String masterPort = resultSet.getString("Master_Port");
                if (null != masterHost && null != masterPort) {
                    return Optional.of(new IPPortPrimaryDatabaseInstance(masterHost, masterPort));
                }
            }
            return Optional.empty();
        }
    }
    
    @Override
    public StorageNodeDataSource getStorageNodeDataSource(final DataSource replicaDataSource) {
        try (
                Connection connection = replicaDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            long replicationDelayMilliseconds = loadReplicationDelayMilliseconds(statement);
            StorageNodeStatus storageNodeStatus = replicationDelayMilliseconds < Long.parseLong(getProps().getProperty("delay-milliseconds-threshold"))
                    ? StorageNodeStatus.ENABLED
                    : StorageNodeStatus.DISABLED;
            return new StorageNodeDataSource(StorageNodeRole.MEMBER, storageNodeStatus, replicationDelayMilliseconds);
        } catch (SQLException ex) {
            log.error("An exception occurred while find member data source `Seconds_Behind_Master`", ex);
        }
        return new StorageNodeDataSource(StorageNodeRole.MEMBER, StorageNodeStatus.DISABLED);
    }
    
    private long loadReplicationDelayMilliseconds(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(SHOW_SLAVE_STATUS)) {
            return resultSet.next() ? resultSet.getLong("Seconds_Behind_Master") * 1000L : 0L;
        }
    }
    
    @Override
    public String getType() {
        return "MySQL.NORMAL_REPLICATION";
    }
}
