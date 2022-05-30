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

package org.apache.shardingsphere.dbdiscovery.opengauss;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;
import org.apache.shardingsphere.dbdiscovery.spi.ReplicaDataSourceStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Properties;

/**
 * Normal replication database discovery provider algorithm for openGauss.
 */
@Getter
@Slf4j
public final class OpenGaussNormalReplicationDatabaseDiscoveryProviderAlgorithm implements DatabaseDiscoveryProviderAlgorithm {
    
    private static final String QUERY_DB_ROLE = "SELECT local_role,db_state FROM pg_stat_get_stream_replications()";
    
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @Override
    public void checkEnvironment(final String databaseName, final Collection<DataSource> dataSources) {
    }
    
    @Override
    public boolean isPrimaryInstance(final DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(QUERY_DB_ROLE)) {
            return resultSet.next() && "Primary".equals(resultSet.getString("local_role")) && "Normal".equals(resultSet.getString("db_state"));
        }
    }
    
    @Override
    public ReplicaDataSourceStatus loadReplicaStatus(final DataSource replicaDataSource) throws SQLException {
        try (
                Connection connection = replicaDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            return new ReplicaDataSourceStatus(isOnlineDataSource(statement), 0L);
        }
    }
    
    private boolean isOnlineDataSource(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(QUERY_DB_ROLE)) {
            return resultSet.next() && resultSet.getString("local_role").equals("Standby") && resultSet.getString("db_state").equals("Normal");
        }
    }
    
    @Override
    public String getType() {
        return "openGauss.NORMAL_REPLICATION";
    }
}
