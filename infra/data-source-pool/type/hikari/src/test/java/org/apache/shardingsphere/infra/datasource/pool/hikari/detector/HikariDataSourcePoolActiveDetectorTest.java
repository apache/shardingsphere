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

package org.apache.shardingsphere.infra.datasource.pool.hikari.detector;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDriver;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HikariDataSourcePoolActiveDetectorTest {
    
    @Test
    void assertNotContainsActiveConnectionWhenEmptyPool() {
        assertFalse(new HikariDataSourcePoolActiveDetector().containsActiveConnection(new HikariDataSource()));
    }
    
    @Test
    void assertNotContainsActiveConnection() {
        assertFalse(new HikariDataSourcePoolActiveDetector().containsActiveConnection(createHikariDataSource()));
    }
    
    @Test
    void assertContainsActiveConnection() throws SQLException {
        DataSource dataSource = createHikariDataSource();
        try (Connection ignored = dataSource.getConnection()) {
            assertTrue(new HikariDataSourcePoolActiveDetector().containsActiveConnection(dataSource));
        }
    }
    
    private HikariDataSource createHikariDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(MockedDriver.class.getName());
        config.setJdbcUrl("jdbc:mock");
        return new HikariDataSource(config);
    }
}
