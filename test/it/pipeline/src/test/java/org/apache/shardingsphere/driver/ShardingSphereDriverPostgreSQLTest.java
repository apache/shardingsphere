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

package org.apache.shardingsphere.driver;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO Enable it after H2DatabaseType supports different MODEs (MySQL and PostgreSQL)
@Disabled
class ShardingSphereDriverPostgreSQLTest {
    
    @Test
    void assertDriverWorks() throws SQLException {
        try (
                Connection connection = DriverManager.getConnection("jdbc:shardingsphere:classpath:config/driver/driver-fixture-h2-postgresql.yaml");
                Statement statement = connection.createStatement()) {
            assertThat(connection, isA(ShardingSphereConnection.class));
            statement.execute("DROP SCHEMA IF EXISTS test");
            statement.execute("CREATE SCHEMA test");
            statement.execute("DROP TABLE IF EXISTS test.t_order");
            statement.execute("CREATE TABLE test.t_order (order_id INT PRIMARY KEY, user_id INT)");
            statement.execute("CREATE INDEX idx_uid ON test.t_order (user_id)");
            statement.execute("INSERT INTO test.t_order (order_id, user_id) VALUES (1, 101), (2, 102)");
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(1) AS cnt FROM test.t_order")) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(2));
            }
        }
    }
}
