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

package org.apache.shardingsphere.test.infra.fixture.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mocked driver.
 */
public final class MockedDriver implements Driver {
    
    static {
        try {
            DriverManager.registerDriver(new MockedDriver());
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
        if (url.contains("invalid")) {
            throw new SQLException("Invalid URL.");
        }
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getURL()).thenReturn(url);
        return result;
    }
    
    @Override
    public boolean acceptsURL(final String url) {
        return url.startsWith("jdbc:mock");
    }
    
    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) {
        return new DriverPropertyInfo[0];
    }
    
    @Override
    public int getMajorVersion() {
        return 0;
    }
    
    @Override
    public int getMinorVersion() {
        return 0;
    }
    
    @Override
    public boolean jdbcCompliant() {
        return true;
    }
    
    @Override
    public Logger getParentLogger() {
        return mock(Logger.class);
    }
}
