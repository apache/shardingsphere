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

package org.apache.shardingsphere.infra.datasource.pool.destroyer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DataSourcePoolDestroyerTest {
    
    @Test
    public void assertAsyncDestroyWithoutAutoCloseable() {
        new DataSourcePoolDestroyer(new MockedDataSource()).asyncDestroy();
    }
    
    @Test(timeout = 2000L)
    public void assertAsyncDestroyHikariDataSource() throws SQLException, InterruptedException {
        HikariDataSource dataSource = createHikariDataSource();
        try (Connection ignored = dataSource.getConnection()) {
            new DataSourcePoolDestroyer(dataSource).asyncDestroy();
            assertFalse(dataSource.isClosed());
        }
        while (!dataSource.isClosed()) {
            Thread.sleep(10L);
        }
        assertTrue(dataSource.isClosed());
    }
    
    private HikariDataSource createHikariDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:foo_ds;DB_CLOSE_DELAY=-1");
        config.setUsername("root");
        config.setPassword("root");
        return new HikariDataSource(config);
    }
}
