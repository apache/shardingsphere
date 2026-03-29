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

package org.apache.shardingsphere.mcp.bootstrap;

import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPRuntimeServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.stdio.StdioMCPServer;
import org.apache.shardingsphere.mcp.jdbc.runtime.H2RuntimeTestSupport;
import org.apache.shardingsphere.mcp.runtime.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPRuntimeLauncherTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertLaunchWithH2Runtime() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "launcher");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
        MCPRuntimeServer actual = assertDoesNotThrow(() -> runtimeLauncher.launch(
                new MCPLaunchConfiguration(createHttpTransportConfiguration(true, "/mcp"), new StdioTransportConfiguration(false),
                        H2RuntimeTestSupport.createRuntimeDatabases("logic_db", jdbcUrl))));
        actual.stop();
    }
    
    @Test
    void assertLaunchWithRuntimeDatabases() throws SQLException {
        String firstJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "launcher-first");
        String secondJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "launcher-second");
        H2RuntimeTestSupport.initializeDatabase(firstJdbcUrl);
        H2RuntimeTestSupport.initializeDatabase(secondJdbcUrl);
        MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
        MCPRuntimeServer actual = assertDoesNotThrow(() -> runtimeLauncher.launch(
                new MCPLaunchConfiguration(createHttpTransportConfiguration(true, "/mcp"), new StdioTransportConfiguration(false),
                        Map.of("logic_db", createRuntimeDatabaseConfiguration(firstJdbcUrl), "analytics_db", createRuntimeDatabaseConfiguration(secondJdbcUrl)))));
        actual.stop();
    }
    
    @Test
    void assertLaunchWithoutRuntimeDatabases() {
        MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> runtimeLauncher.launch(new MCPLaunchConfiguration(createHttpTransportConfiguration(true, "/mcp"), new StdioTransportConfiguration(false), Map.of())));
        assertThat(actual.getMessage(), is("At least one runtime database must be configured."));
    }
    
    @Test
    void assertLaunchWithStdioTransport() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "launcher-stdio");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
        MCPRuntimeServer actual = assertDoesNotThrow(
                () -> runtimeLauncher.launch(new MCPLaunchConfiguration(createHttpTransportConfiguration(false, "/mcp"), new StdioTransportConfiguration(true),
                        H2RuntimeTestSupport.createRuntimeDatabases("logic_db", jdbcUrl))));
        assertThat(actual, isA(StdioMCPServer.class));
        actual.stop();
    }
    
    @Test
    void assertLaunchWithSingleStartupConnection() throws SQLException {
        CountingDriver driver = new CountingDriver();
        DriverManager.registerDriver(driver);
        try {
            MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
            MCPRuntimeServer actual = assertDoesNotThrow(() -> runtimeLauncher.launch(
                    new MCPLaunchConfiguration(createHttpTransportConfiguration(true, "/mcp"), new StdioTransportConfiguration(false),
                            Map.of("logic_db", new RuntimeDatabaseConfiguration("H2", CountingDriver.JDBC_URL, "", "", "")))));
            actual.stop();
            assertThat(driver.getConnectionCount(), is(1));
        } finally {
            DriverManager.deregisterDriver(driver);
        }
    }
    
    private HttpTransportConfiguration createHttpTransportConfiguration(final boolean enabled, final String endpointPath) {
        return new HttpTransportConfiguration(enabled, "127.0.0.1", 0, endpointPath);
    }
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String jdbcUrl) {
        return new RuntimeDatabaseConfiguration("H2", jdbcUrl, "", "", "org.h2.Driver");
    }
    
    private static final class CountingDriver implements Driver {
        
        private static final String JDBC_URL = "jdbc:counting:h2";
        
        private int connectionCount;
        
        @Override
        public Connection connect(final String url, final Properties info) {
            if (!acceptsURL(url)) {
                return null;
            }
            connectionCount++;
            DatabaseMetaData databaseMetaData = createDatabaseMetaData();
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{Connection.class},
                    (proxy, method, args) -> "getMetaData".equals(method.getName()) ? databaseMetaData : getDefaultValue(method.getReturnType()));
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
        public Logger getParentLogger() {
            return Logger.getGlobal();
        }
        
        private int getConnectionCount() {
            return connectionCount;
        }
        
        private DatabaseMetaData createDatabaseMetaData() {
            ResultSet emptyResultSet = createEmptyResultSet();
            return (DatabaseMetaData) Proxy.newProxyInstance(DatabaseMetaData.class.getClassLoader(), new Class[]{DatabaseMetaData.class},
                    (proxy, method, args) -> "getTables".equals(method.getName()) || "getColumns".equals(method.getName()) || "getIndexInfo".equals(method.getName())
                            || "getSchemas".equals(method.getName()) ? emptyResultSet : getDefaultValue(method.getReturnType()));
        }
        
        private ResultSet createEmptyResultSet() {
            return (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(), new Class[]{ResultSet.class},
                    (proxy, method, args) -> "next".equals(method.getName()) ? false : getDefaultValue(method.getReturnType()));
        }
        
        private Object getDefaultValue(final Class<?> returnType) {
            if (Void.TYPE == returnType) {
                return null;
            }
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (boolean.class == returnType) {
                return false;
            }
            if (byte.class == returnType) {
                return (byte) 0;
            }
            if (short.class == returnType) {
                return (short) 0;
            }
            if (int.class == returnType) {
                return 0;
            }
            if (long.class == returnType) {
                return 0L;
            }
            if (float.class == returnType) {
                return 0F;
            }
            if (double.class == returnType) {
                return 0D;
            }
            return '\0';
        }
    }
}
