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

package org.apache.shardingsphere.mcp.jdbc;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RuntimeDatabaseConfigurationTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDatabaseTypeArguments")
    void assertConstructWithInvalidDatabaseType(final String name, final String databaseType, final String expectedMessage) {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> new RuntimeDatabaseConfiguration(databaseType, "jdbc:test:logic", "", "", ""));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    @Test
    void assertOpenConnectionWithoutDriverClassName() throws SQLException {
        RecordingDriver.reset();
        try (Connection actual = new RuntimeDatabaseConfiguration("H2", RecordingDriver.JDBC_URL, "", "", "").openConnection("logic_db")) {
            assertThat(actual, is(RecordingDriver.CONNECTION));
            assertThat(RecordingDriver.lastUrl, is(RecordingDriver.JDBC_URL));
            assertTrue(RecordingDriver.lastProperties.isEmpty());
        }
    }
    
    @Test
    void assertOpenConnectionWithDriverClassNameAndCredentials() throws SQLException {
        RecordingDriver.reset();
        try (Connection actual = new RuntimeDatabaseConfiguration("H2", RecordingDriver.JDBC_URL, "sa", "pwd", RecordingDriver.class.getName()).openConnection("logic_db")) {
            assertThat(actual, is(RecordingDriver.CONNECTION));
            assertThat(RecordingDriver.lastProperties.getProperty("user"), is("sa"));
            assertThat(RecordingDriver.lastProperties.getProperty("password"), is("pwd"));
        }
    }
    
    @SuppressWarnings("resource")
    @Test
    void assertOpenConnectionWithUnavailableDriverClassName() {
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> new RuntimeDatabaseConfiguration("H2", "jdbc:test:missing-driver", "", "", "org.example.MissingDriver").openConnection("logic_db"));
        assertThat(actual.getMessage(), is("JDBC driver `org.example.MissingDriver` is not available for database `logic_db`."));
    }
    
    private static Stream<Arguments> invalidDatabaseTypeArguments() {
        return Stream.of(
                Arguments.of("null database type", null, "databaseType cannot be null."),
                Arguments.of("empty database type", "", "databaseType cannot be empty."),
                Arguments.of("blank database type", "   ", "databaseType cannot be empty."));
    }
    
    private static final class RecordingDriver implements Driver {
        
        private static final String JDBC_URL = "jdbc:recording:runtime-config";
        
        private static final Connection CONNECTION = mock(Connection.class);
        
        private static final RecordingDriver INSTANCE = new RecordingDriver();
        
        private static Properties lastProperties = new Properties();
        
        private static String lastUrl;
        
        static {
            try {
                DriverManager.registerDriver(INSTANCE);
            } catch (final SQLException ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }
        
        private static void reset() {
            lastProperties = new Properties();
            lastUrl = null;
        }
        
        @Override
        public Connection connect(final String url, final Properties info) {
            if (!acceptsURL(url)) {
                return null;
            }
            lastUrl = url;
            lastProperties = new Properties();
            if (null != info) {
                lastProperties.putAll(info);
            }
            return CONNECTION;
        }
        
        @Override
        public boolean acceptsURL(final String url) {
            return JDBC_URL.equals(url);
        }
        
        @Override
        public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) {
            return new DriverPropertyInfo[0];
        }
        
        @Override
        public int getMajorVersion() {
            return 1;
        }
        
        @Override
        public int getMinorVersion() {
            return 0;
        }
        
        @Override
        public boolean jdbcCompliant() {
            return false;
        }
        
        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("Recording driver does not expose a parent logger.");
        }
    }
}
