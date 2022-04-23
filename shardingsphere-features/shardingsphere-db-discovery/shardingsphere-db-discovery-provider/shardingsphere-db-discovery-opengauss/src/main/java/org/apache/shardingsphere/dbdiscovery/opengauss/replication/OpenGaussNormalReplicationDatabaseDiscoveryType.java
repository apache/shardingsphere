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

package org.apache.shardingsphere.dbdiscovery.opengauss.replication;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
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
 * Normal replication database discovery type for openGauss.
 */
@Getter
@Setter
@Slf4j
public final class OpenGaussNormalReplicationDatabaseDiscoveryType implements DatabaseDiscoveryType {
    
    private static final String DB_ROLE = "SELECT local_role,db_state FROM pg_stat_get_stream_replications()";
    
    private String oldPrimaryDataSource;
    
    private Properties props = new Properties();
    
    @Override
    public OpenGaussNormalReplicationHighlyAvailableStatus loadHighlyAvailableStatus(final DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(DB_ROLE)) {
            return new OpenGaussNormalReplicationHighlyAvailableStatus(resultSet.next() && resultSet.getString("local_role").equals("Primary"));
        }
    }
    
    @Override
    public Optional<String> determinePrimaryDataSource(final Map<String, DataSource> dataSourceMap) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            try (
                    Connection connection = entry.getValue().getConnection();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(DB_ROLE)) {
                if (resultSet.next()) {
                    if (resultSet.getString("local_role").equals("Primary") && resultSet.getString("db_state").equals("Normal")) {
                        return Optional.of(entry.getKey());
                    }
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while find primary data source url", ex);
            }
        }
        return Optional.empty();
    }
    
    @Override
    public void updateMemberState(final String databaseName, final Map<String, DataSource> dataSourceMap, final String groupName) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            boolean disable = true;
            try (
                    Connection connection = entry.getValue().getConnection();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(DB_ROLE)) {
                if (resultSet.next()) {
                    if ((resultSet.getString("local_role").equals("Standby") && resultSet.getString("db_state").equals("Normal"))
                            || entry.getKey().equals(oldPrimaryDataSource)) {
                        disable = false;
                    }
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while find data source urls", ex);
            }
            ShardingSphereEventBus.getInstance().post(new DataSourceDisabledEvent(databaseName, groupName, entry.getKey(),
                    new StorageNodeDataSource(StorageNodeRole.MEMBER, disable ? StorageNodeStatus.DISABLED : StorageNodeStatus.ENABLED)));
        }
    }
    
    @Override
    public String getPrimaryDataSource() {
        return oldPrimaryDataSource;
    }
    
    @Override
    public String getType() {
        return "openGauss.NORMAL_REPLICATION";
    }
}
