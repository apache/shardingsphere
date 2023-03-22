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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.e2e.discovery.command.DiscoveryDistSQLCommand;
import org.awaitility.Awaitility;
import org.awaitility.Durations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Build discovery rule.
 */
@RequiredArgsConstructor
public final class DiscoveryRuleBuilder {
    
    private final DataSource proxyDataSource;
    
    private final DiscoveryDistSQLCommand discoveryDistSQL;
    
    /**
     * Build Discovery Environment.
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
        statement.execute(discoveryDistSQL.getCreateDatabase().getExecuteSQL());
        Awaitility.await().atMost(Durations.ONE_SECOND).until(() -> getCreateDatabaseResult(statement, discoveryDistSQL.getCreateDatabase().getAssertionSQL()));
    }
    
    private boolean getCreateDatabaseResult(final Statement statement, final String assertionSQL) {
        try (ResultSet ignored = statement.executeQuery(assertionSQL)) {
            return true;
        } catch (final SQLException ignored) {
            return false;
        }
    }
    
    private void registerStorageUnits(final Statement statement) throws SQLException {
        statement.execute(discoveryDistSQL.getRegisterStorageUnits().getExecuteSQL());
        Awaitility.await().atMost(Durations.TWO_SECONDS).until(() -> getExecuteResult(statement, discoveryDistSQL.getRegisterStorageUnits().getAssertionSQL()));
    }
    
    private void createDiscoveryRule(final Statement statement) throws SQLException {
        statement.execute(discoveryDistSQL.getCreateDiscoveryRule().getExecuteSQL());
        Awaitility.await().atMost(Durations.TWO_SECONDS).until(() -> getExecuteResult(statement, discoveryDistSQL.getCreateDiscoveryRule().getAssertionSQL()));
    }
    
    private void createReadwriteSplittingRule(final Statement statement) throws SQLException {
        statement.execute(discoveryDistSQL.getCreateReadwriteSplittingRule().getExecuteSQL());
        Awaitility.await().atMost(Durations.FIVE_SECONDS).until(() -> getExecuteResult(statement, discoveryDistSQL.getCreateReadwriteSplittingRule().getAssertionSQL()));
    }
    
    private boolean getExecuteResult(final Statement statement, final String assertionSQL) {
        try (ResultSet resultSet = statement.executeQuery(assertionSQL)) {
            return resultSet.next();
        } catch (final SQLException ignored) {
            return false;
        }
    }
}
