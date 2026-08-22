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

package org.apache.shardingsphere.test.e2e.driver.federation;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.test.e2e.driver.AbstractDriverTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for <a href="https://github.com/apache/shardingsphere/issues/39121">#39121</a>.
 *
 * <p>Runs a real query through ShardingSphere-JDBC with SQL federation forced on for every {@code SELECT}
 * ({@code allQueryUseSQLFederation: true}), so the {@code ResultSet} returned to the caller is the actual
 * production {@code SQLFederationResultSet} produced by the compiler and dialect SPI, rather than a manually
 * constructed one. Asserts that {@code getMetaData().getColumnClassName(column)} always agrees with the
 * runtime class of {@code getObject(column)}, both for a plain numeric column and for a column whose value
 * class is rewritten by {@code DialectSQLFederationColumnTypeConverter} (MySQL {@code BOOLEAN -> Integer}).</p>
 */
class SQLFederationResultSetColumnClassNameTest extends AbstractDriverTest {
    
    private static final String YAML_CONFIG = String.join("\n",
            "databaseName: federation_db",
            "",
            "rules:",
            "- !SINGLE",
            "  tables:",
            "    - \"*.*\"",
            "",
            "sqlFederation:",
            "  sqlFederationEnabled: true",
            "  allQueryUseSQLFederation: true",
            "  executionPlanCache:",
            "    initialCapacity: 2000",
            "    maximumSize: 65535",
            "",
            "props:",
            "  proxy-frontend-database-protocol-type: MySQL");
    
    private static ShardingSphereDataSource dataSource;
    
    @BeforeAll
    static void initShardingSphereDataSource() throws SQLException, IOException {
        if (null == dataSource) {
            DataSource actualDataSource = getActualDataSources().get("single_jdbc");
            dataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(actualDataSource, YAML_CONFIG.getBytes(StandardCharsets.UTF_8));
        }
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS t_federation_column_type (id BIGINT NOT NULL, enabled BOOLEAN NOT NULL, PRIMARY KEY (id))");
        }
    }
    
    @AfterAll
    static void closeShardingSphereDataSource() throws Exception {
        if (null == dataSource) {
            return;
        }
        dataSource.close();
        dataSource = null;
    }
    
    @BeforeEach
    void initData() throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM t_federation_column_type");
                PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO t_federation_column_type (id, enabled) VALUES (?, ?)")) {
            deleteStatement.executeUpdate();
            insertStatement.setLong(1, 1L);
            insertStatement.setBoolean(2, true);
            insertStatement.executeUpdate();
        }
    }
    
    @Test
    void assertColumnClassNameMatchesGetObjectValueClassForBigint() throws SQLException {
        assertColumnClassNameMatchesGetObjectValueClass("id");
    }
    
    @Test
    void assertColumnClassNameMatchesGetObjectValueClassForDialectConvertedBoolean() throws SQLException {
        assertColumnClassNameMatchesGetObjectValueClass("enabled");
    }
    
    private void assertColumnClassNameMatchesGetObjectValueClass(final String columnLabel) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT id, enabled FROM t_federation_column_type WHERE id = 1")) {
            assertTrue(resultSet.next());
            int columnIndex = resultSet.findColumn(columnLabel);
            Object actualValue = resultSet.getObject(columnIndex);
            assertThat(resultSet.getMetaData().getColumnClassName(columnIndex), is(actualValue.getClass().getName()));
        }
    }
}
