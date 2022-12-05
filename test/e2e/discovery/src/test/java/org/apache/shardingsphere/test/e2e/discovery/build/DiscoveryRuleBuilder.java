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

package org.apache.shardingsphere.test.e2e.discovery.build;

import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.test.e2e.discovery.cases.base.BaseDiscoveryE2EIT;
import org.apache.shardingsphere.test.e2e.discovery.command.DiscoveryDistSQLCommand;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Build discovery rule.
 */
public final class DiscoveryRuleBuilder {
    
    private final DiscoveryDistSQLCommand discoveryDistSQLCommand;
    
    private final DataSource proxyDataSource;
    
    public DiscoveryRuleBuilder(final DataSource proxyDataSource) {
        this.proxyDataSource = proxyDataSource;
        discoveryDistSQLCommand = JAXB.unmarshal(Objects.requireNonNull(BaseDiscoveryE2EIT.class.getClassLoader().getResource("env/common/discovery-command.xml")), DiscoveryDistSQLCommand.class);
    }
    
    /**
     *  build Discovery Environment.
     *
     * @throws SQLException SQL exception
     */
    public void buildDiscoveryEnvironment() throws SQLException {
        try (
                Connection connection = proxyDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            createDatabase(statement);
            registerStorageUnits(statement);
            createDiscoveryRule(statement);
            createReadwriteSplittingRule(statement);
        }
    }
    
    private void createDatabase(final Statement statement) throws SQLException {
        statement.execute("CREATE DATABASE db_discovery");
        ThreadUtil.sleep(1, TimeUnit.SECONDS);
        statement.execute("USE db_discovery");
    }
    
    private void registerStorageUnits(final Statement statement) throws SQLException {
        statement.execute(discoveryDistSQLCommand.getRegisterStorageUnit());
        ThreadUtil.sleep(2, TimeUnit.SECONDS);
    }
    
    private void createDiscoveryRule(final Statement statement) throws SQLException {
        statement.execute(discoveryDistSQLCommand.getCreateDiscoveryRule());
        ThreadUtil.sleep(2, TimeUnit.SECONDS);
    }
    
    private void createReadwriteSplittingRule(final Statement statement) throws SQLException {
        statement.execute(discoveryDistSQLCommand.getCreateReadwriteSplittingRule());
        ThreadUtil.sleep(2, TimeUnit.SECONDS);
    }
}
