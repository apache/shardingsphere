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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;
import org.apache.shardingsphere.infra.database.metadata.dialect.MySQLDataSourceMetaData;
import org.apache.shardingsphere.infra.storage.StorageNodeDataSource;
import org.apache.shardingsphere.infra.storage.StorageNodeRole;
import org.apache.shardingsphere.infra.storage.StorageNodeStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
    public void checkEnvironment(final String databaseName, final DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            checkMasterInstance(databaseName, statement);
        }
    }
    
    private void checkMasterInstance(final String databaseName, final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(SHOW_SLAVE_STATUS)) {
            Preconditions.checkState(resultSet.next() && null != resultSet.getString("Master_Host") && null != resultSet.getString("Master_Port"),
                    "Can not load primary data source URL in database `%s`.", databaseName);
        }
    }
    
    @Override
    public boolean isPrimaryInstance(final DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(SHOW_SLAVE_STATUS)) {
            if (resultSet.next()) {
                MySQLDataSourceMetaData metaData = new MySQLDataSourceMetaData(connection.getMetaData().getURL());
                return metaData.getHostname().equals(resultSet.getString("Master_Host")) && Integer.toString(metaData.getPort()).equals(resultSet.getString("Master_Port"));
            }
            return false;
        }
    }
    
    @Override
    public StorageNodeDataSource getStorageNodeDataSource(final DataSource replicaDataSource) {
        try (
                Connection connection = replicaDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            long replicationDelayMilliseconds = queryReplicationDelayMilliseconds(statement);
            StorageNodeStatus storageNodeStatus = replicationDelayMilliseconds < Long.parseLong(getProps().getProperty("delay-milliseconds-threshold"))
                    ? StorageNodeStatus.ENABLED
                    : StorageNodeStatus.DISABLED;
            return new StorageNodeDataSource(StorageNodeRole.MEMBER, storageNodeStatus, replicationDelayMilliseconds);
        } catch (SQLException ex) {
            log.error("An exception occurred while find member data source `Seconds_Behind_Master`", ex);
        }
        return new StorageNodeDataSource(StorageNodeRole.MEMBER, StorageNodeStatus.DISABLED);
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
