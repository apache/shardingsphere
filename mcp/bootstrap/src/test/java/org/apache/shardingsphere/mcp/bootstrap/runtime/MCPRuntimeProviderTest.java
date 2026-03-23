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

package org.apache.shardingsphere.mcp.bootstrap.runtime;

import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeTopologyConfiguration;
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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPRuntimeProviderTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertLoadWithH2Runtime() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "runtime-provider");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        MCPRuntimeProvider runtimeProvider = new MCPRuntimeProvider();
        LoadedRuntime actual = runtimeProvider.load(H2RuntimeTestSupport.createRuntimeProps("logic_db", jdbcUrl));
        assertThat(actual.getMetadataCatalog().getDatabaseTypes().size(), is(1));
        assertTrue(actual.getMetadataCatalog().getDatabaseTypes().containsKey("logic_db"));
        assertThat(actual.getMetadataCatalog().getMetadataObjects().size(), greaterThan(0));
    }
    
    @Test
    void assertLoadWithRuntimeTopology() throws SQLException {
        String firstJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "runtime-provider-first");
        String secondJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "runtime-provider-second");
        H2RuntimeTestSupport.initializeDatabase(firstJdbcUrl);
        H2RuntimeTestSupport.initializeDatabase(secondJdbcUrl);
        MCPRuntimeProvider runtimeProvider = new MCPRuntimeProvider();
        LoadedRuntime actual = runtimeProvider.load(new RuntimeTopologyConfiguration(Map.of(
                "logic_db", createRuntimeDatabaseConfiguration(firstJdbcUrl),
                "analytics_db", createRuntimeDatabaseConfiguration(secondJdbcUrl))));
        assertThat(actual.getMetadataCatalog().getDatabaseTypes().size(), is(2));
        assertTrue(actual.getMetadataCatalog().getDatabaseTypes().containsKey("logic_db"));
        assertTrue(actual.getMetadataCatalog().getDatabaseTypes().containsKey("analytics_db"));
        assertThat(actual.getMetadataCatalog().getMetadataObjects().size(), greaterThan(0));
    }
    
    @Test
    void assertLoadWithMissingDatabaseName() {
        MCPRuntimeProvider runtimeProvider = new MCPRuntimeProvider();
        Properties props = new Properties();
        props.setProperty("databaseType", "H2");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> runtimeProvider.load(props));
        assertThat(actual.getMessage(), is("Runtime property `databaseName` is required."));
    }
    
    @Test
    void assertLoadWithSingleStartupConnection() throws SQLException {
        CountingDriver driver = new CountingDriver();
        DriverManager.registerDriver(driver);
        try {
            MCPRuntimeProvider runtimeProvider = new MCPRuntimeProvider();
            LoadedRuntime actual = runtimeProvider.load(PropertiesBuilder.build(
                    new Property("databaseName", "logic_db"),
                    new Property("databaseType", "H2"),
                    new Property("jdbcUrl", CountingDriver.JDBC_URL),
                    new Property("schemaPattern", "public"),
                    new Property("defaultSchema", "public")));
            assertThat(driver.getConnectionCount(), is(1));
            assertThat(actual.getMetadataCatalog().getMetadataObjects().size(), is(1));
        } finally {
            DriverManager.deregisterDriver(driver);
        }
    }
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String jdbcUrl) {
        return new RuntimeDatabaseConfiguration("H2", jdbcUrl, "", "", "org.h2.Driver", "public", "public", true, false);
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
                            ? emptyResultSet
                            : getDefaultValue(method.getReturnType()));
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
