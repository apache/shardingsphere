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

package org.apache.shardingsphere.test.integration.discovery.build;

import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.test.integration.discovery.cases.base.BaseITCase;
import org.apache.shardingsphere.test.integration.discovery.command.MGRPrimaryReplicaCommand;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

/**
 * Build MySQL MGR.
 */
public final class MySQLMGRBuilder {
    
    private final MGRPrimaryReplicaCommand mgrPrimaryReplicaCommand;
    
    @Getter
    private final DataSource primaryDataSource;
    
    @Getter
    private final List<DataSource> replicaDataSources;
    
    public MySQLMGRBuilder(final List<DataSource> dataSources) throws SQLException {
        this.primaryDataSource = dataSources.get(0);
        this.replicaDataSources = dataSources.subList(1, dataSources.size());
        mgrPrimaryReplicaCommand = JAXB.unmarshal(Objects.requireNonNull(BaseITCase.class.getClassLoader().getResource("env/common/mgr-primary-replica-command.xml")),
                MGRPrimaryReplicaCommand.class);
        buildMGRPrimaryDataSource(primaryDataSource);
        buildMGRReplicaDataSources(dataSources.subList(1, dataSources.size()));
    }
    
    private void buildMGRPrimaryDataSource(final DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            buildMGRPrimaryDataSource(statement);
        }
    }
    
    private void buildMGRPrimaryDataSource(final Statement statement) throws SQLException {
        for (String each : Splitter.on(";").trimResults().omitEmptyStrings().split(mgrPrimaryReplicaCommand.getBuildPrimaryNodeSQL())) {
            statement.execute(each);
        }
    }
    
    private void buildMGRReplicaDataSources(final List<DataSource> dataSources) throws SQLException {
        for (DataSource each : dataSources) {
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
    
    /**
     * Create database.
     * @throws SQLException SQL exception
     */
    public void createDatabase() throws SQLException {
        try (
                Connection connection = primaryDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE it_discovery_test");
        }
    }
}
