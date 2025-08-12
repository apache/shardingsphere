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

package org.apache.shardingsphere.timeservice.type.database;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.timeservice.spi.TimestampService;
import org.apache.shardingsphere.timeservice.type.database.exception.DatetimeLoadingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseTimestampServiceTest {
    
    private TimestampService timestampService;
    
    @BeforeEach
    void setUp() {
        Properties props = PropertiesBuilder.build(
                new Property("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource"),
                new Property("jdbcUrl", "jdbc:h2:mem:foo_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL"),
                new Property("username", "sa"),
                new Property("password", ""),
                new Property("maximumPoolSize", "1"));
        timestampService = TypedSPILoader.getService(TimestampService.class, "Database", props);
    }
    
    @Test
    void assertGetTimestamp() {
        long currentTime = System.currentTimeMillis();
        assertTrue(timestampService.getTimestamp().getTime() >= currentTime);
    }
    
    @Test
    void assertGetTimestampFailed() throws ReflectiveOperationException, SQLException {
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(any())).thenThrow(new SQLException(""));
        Plugins.getMemberAccessor().set(DatabaseTimestampService.class.getDeclaredField("dataSource"), timestampService, new MockedDataSource(connection));
        assertThrows(DatetimeLoadingException.class, () -> timestampService.getTimestamp().getTime());
    }
}
