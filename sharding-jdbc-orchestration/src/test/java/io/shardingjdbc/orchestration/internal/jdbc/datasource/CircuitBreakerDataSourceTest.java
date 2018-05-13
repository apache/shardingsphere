/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.orchestration.internal.jdbc.datasource;

import io.shardingjdbc.orchestration.internal.jdbc.connection.CircuitBreakerConnection;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class CircuitBreakerDataSourceTest {
    
    private CircuitBreakerDataSource dataSource = new CircuitBreakerDataSource();
    
    @Test
    public void assertClose() throws Exception {
        dataSource.close();
    }
    
    @Test
    public void assertGetConnection() throws SQLException {
        assertTrue(dataSource.getConnection() instanceof CircuitBreakerConnection);
        assertTrue(dataSource.getConnection("", "") instanceof CircuitBreakerConnection);
    }
    
    @Test
    public void assertGetLogWriter() throws SQLException {
        assertNull(dataSource.getLogWriter());
    }
    
    @Test
    public void assertSetLogWriter() throws Exception {
        dataSource.setLogWriter(null);
        assertNull(dataSource.getLogWriter());
    }
    
    @Test
    public void assertGetParentLogger() throws SQLFeatureNotSupportedException {
        assertNull(dataSource.getParentLogger());
    }
}
