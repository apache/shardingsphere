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

package org.apache.shardingsphere.test.e2e.discovery.cases.mysql.env;

import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.test.e2e.discovery.cases.base.BaseDiscoveryE2EIT;
import org.apache.shardingsphere.test.e2e.discovery.command.MGRPrimaryReplicaCommand;
import org.apache.shardingsphere.test.e2e.discovery.cases.DatabaseClusterEnvironment;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Build MySQL MGR Environment.
 */
public final class MySQLMGREnvironment implements DatabaseClusterEnvironment {
    
    private final MGRPrimaryReplicaCommand mgrPrimaryReplicaCommand;
    
    @Getter
    private final Map<String, DataSource> dataSources;
    
    private final DataSource primaryDataSource;
    
    private final List<DataSource> replicationDataSources;
    
    public MySQLMGREnvironment(final List<DataSource> dataSources) throws SQLException {
        primaryDataSource = dataSources.get(0);
        replicationDataSources = dataSources.subList(1, dataSources.size());
        this.dataSources = getAllDataSources();
        mgrPrimaryReplicaCommand = JAXB.unmarshal(Objects.requireNonNull(BaseDiscoveryE2EIT.class.getClassLoader().getResource("env/common/mgr-primary-replica-command.xml")),
                MGRPrimaryReplicaCommand.class);
        buildMGRPrimaryDataSource();
        buildMGRReplicaDataSources();
        createDatabase();
    }
    
    private Map<String, DataSource> getAllDataSources() {
        Map<String, DataSource> result = new LinkedHashMap<>(4, 1);
        result.put("ds_0", primaryDataSource);
        result.putAll(getReplicationDataSources());
        return result;
    }
    
    private Map<String, DataSource> getReplicationDataSources() {
        Map<String, DataSource> result = new LinkedHashMap<>(3, 1);
        for (int i = 0; i < replicationDataSources.size(); i++) {
            result.put("ds_" + (i + 1), replicationDataSources.get(i));
        }
        return result;
    }
    
    private void buildMGRPrimaryDataSource() throws SQLException {
        try (
                Connection connection = primaryDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            buildMGRPrimaryDataSource(statement);
        }
    }
    
    private void buildMGRPrimaryDataSource(final Statement statement) throws SQLException {
        for (String each : Splitter.on(";").trimResults().omitEmptyStrings().split(mgrPrimaryReplicaCommand.getBuildPrimaryNodeSQL())) {
            statement.execute(each);
        }
    }
    
    private void buildMGRReplicaDataSources() throws SQLException {
        for (DataSource each : replicationDataSources) {
            buildMGRReplicaDataSource(each);
        }
    }
    
    private void buildMGRReplicaDataSource(final DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            buildMGRReplicaDataSource(statement);
        }
    }
    
    private void buildMGRReplicaDataSource(final Statement statement) throws SQLException {
        for (String each : Splitter.on(";").trimResults().omitEmptyStrings().split(mgrPrimaryReplicaCommand.getBuildReplicaNodeSQL())) {
            statement.execute(each);
        }
    }
    
    private void createDatabase() throws SQLException {
        try (
                Connection connection = primaryDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE it_discovery_test");
        }
    }
}
