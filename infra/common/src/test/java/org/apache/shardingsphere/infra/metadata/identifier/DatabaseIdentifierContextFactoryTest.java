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

package org.apache.shardingsphere.infra.metadata.identifier;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRule;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.MetadataIdentifierCaseSensitivity;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseIdentifierContextFactoryTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private static final DatabaseType MYSQL_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private static final DatabaseType ORACLE_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "Oracle");
    
    @Test
    void assertCreateDefault() {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        IdentifierCaseRule actualRule = actual.getRule(IdentifierScope.TABLE);
        assertThat(actualRule.getLookupMode(QuoteCharacter.NONE), is(LookupMode.NORMALIZED));
        assertTrue(actualRule.matches("Foo", "foo", QuoteCharacter.NONE));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createWithProtocolTypeAndPropsArguments")
    void assertCreateWithProtocolTypeAndProps(final String name, final DatabaseType protocolType, final ConfigurationProperties props, final LookupMode expectedLookupMode,
                                              final String actualIdentifier, final String logicIdentifier, final boolean expectedMatched) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(protocolType, props);
        IdentifierCaseRule actualRule = actual.getRule(IdentifierScope.TABLE);
        assertThat(actualRule.getLookupMode(QuoteCharacter.NONE), is(expectedLookupMode));
        assertThat(actualRule.matches(actualIdentifier, logicIdentifier, QuoteCharacter.NONE), is(expectedMatched));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createWithResourceMetaDataAndPropsArguments")
    void assertCreateWithResourceMetaDataAndProps(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                  final ConfigurationProperties props, final LookupMode expectedLookupMode,
                                                  final String actualIdentifier, final String logicIdentifier, final boolean expectedMatched) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(protocolType, resourceMetaData, props);
        IdentifierCaseRule actualRule = actual.getRule(IdentifierScope.TABLE);
        assertThat(actualRule.getLookupMode(QuoteCharacter.NONE), is(expectedLookupMode));
        assertThat(actualRule.matches(actualIdentifier, logicIdentifier, QuoteCharacter.NONE), is(expectedMatched));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("refreshWithProtocolTypeAndPropsArguments")
    void assertRefreshWithProtocolTypeAndProps(final String name, final DatabaseType protocolType, final ConfigurationProperties props, final LookupMode expectedLookupMode,
                                               final String actualIdentifier, final String logicIdentifier, final boolean expectedMatched) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        DatabaseIdentifierContextFactory.refresh(actual, protocolType, props);
        IdentifierCaseRule actualRule = actual.getRule(IdentifierScope.TABLE);
        assertThat(actualRule.getLookupMode(QuoteCharacter.NONE), is(expectedLookupMode));
        assertThat(actualRule.matches(actualIdentifier, logicIdentifier, QuoteCharacter.NONE), is(expectedMatched));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("refreshWithResourceMetaDataAndPropsArguments")
    void assertRefreshWithResourceMetaDataAndProps(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                   final ConfigurationProperties props, final LookupMode expectedLookupMode,
                                                   final String actualIdentifier, final String logicIdentifier, final boolean expectedMatched) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        DatabaseIdentifierContextFactory.refresh(actual, protocolType, resourceMetaData, props);
        IdentifierCaseRule actualRule = actual.getRule(IdentifierScope.TABLE);
        assertThat(actualRule.getLookupMode(QuoteCharacter.NONE), is(expectedLookupMode));
        assertThat(actualRule.matches(actualIdentifier, logicIdentifier, QuoteCharacter.NONE), is(expectedMatched));
    }
    
    @Test
    void assertCreateUsesProtocolRuleForSchemaAndStorageRuleForTable() {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(MYSQL_DATABASE_TYPE,
                createResourceMetaDataWithStorageUrls("jdbc:oracle:thin:@localhost:1521:xe"), new ConfigurationProperties(new Properties()));
        IdentifierCaseRule actualSchemaRule = actual.getRule(IdentifierScope.SCHEMA);
        IdentifierCaseRule actualTableRule = actual.getRule(IdentifierScope.TABLE);
        assertTrue(actualSchemaRule.matches("test_db", "TEST_DB", QuoteCharacter.NONE));
        assertTrue(actualTableRule.matches("T_ORDER", "t_order", QuoteCharacter.NONE));
    }
    
    @Test
    void assertRefreshUsesProtocolRuleForSchemaAndStorageRuleForTable() {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        DatabaseIdentifierContextFactory.refresh(actual, MYSQL_DATABASE_TYPE, createResourceMetaDataWithStorageUrls("jdbc:oracle:thin:@localhost:1521:xe"),
                new ConfigurationProperties(new Properties()));
        IdentifierCaseRule actualSchemaRule = actual.getRule(IdentifierScope.SCHEMA);
        IdentifierCaseRule actualTableRule = actual.getRule(IdentifierScope.TABLE);
        assertTrue(actualSchemaRule.matches("test_db", "TEST_DB", QuoteCharacter.NONE));
        assertTrue(actualTableRule.matches("T_ORDER", "t_order", QuoteCharacter.NONE));
    }
    
    @Test
    void assertCreateUsesInsensitiveRuleForDatabaseScope() {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(ORACLE_DATABASE_TYPE,
                createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE));
        IdentifierCaseRule actualRule = actual.getRule(IdentifierScope.DATABASE);
        assertThat(actualRule.getLookupMode(QuoteCharacter.NONE), is(LookupMode.NORMALIZED));
        assertTrue(actualRule.matches("foo_db", "FOO_DB", QuoteCharacter.NONE));
    }
    
    @Test
    void assertRefreshUsesInsensitiveRuleForDatabaseScope() {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        DatabaseIdentifierContextFactory.refresh(actual, ORACLE_DATABASE_TYPE, createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE));
        IdentifierCaseRule actualRule = actual.getRule(IdentifierScope.DATABASE);
        assertThat(actualRule.getLookupMode(QuoteCharacter.NONE), is(LookupMode.NORMALIZED));
        assertTrue(actualRule.matches("foo_db", "FOO_DB", QuoteCharacter.NONE));
    }
    
    @Test
    void assertCreateContainsQuotedTableWithMySQLInsensitiveStorageRule() {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(MYSQL_DATABASE_TYPE,
                createResourceMetaDataWithMySQLLowerCaseTableNames(1), new ConfigurationProperties(new Properties()));
        ShardingSphereTable table = new ShardingSphereTable("t_mask", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", MYSQL_DATABASE_TYPE, Collections.singleton(table), Collections.emptyList());
        schema.refreshIdentifierContext(actual);
        assertTrue(schema.containsTable(new IdentifierValue("`T_MASK`")));
    }
    
    private static Stream<Arguments> createWithProtocolTypeAndPropsArguments() {
        return Stream.of(
                Arguments.of("null protocol type and null props use insensitive rules", null, null, LookupMode.NORMALIZED, "Foo", "foo", true),
                Arguments.of("sensitive props override protocol rules", DATABASE_TYPE,
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false),
                Arguments.of("insensitive props normalize identifiers", DATABASE_TYPE,
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.INSENSITIVE), LookupMode.NORMALIZED, "Foo", "foo", true));
    }
    
    private static Stream<Arguments> createWithResourceMetaDataAndPropsArguments() {
        return Stream.of(
                Arguments.of("null resource metadata and null props use insensitive rules", null, null, null, LookupMode.NORMALIZED, "Foo", "foo", true),
                Arguments.of("null storage units keep explicit sensitive rules", DATABASE_TYPE, new ResourceMetaData(Collections.emptyMap(), null),
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false),
                Arguments.of("empty storage units keep explicit sensitive rules", DATABASE_TYPE, new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()),
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false),
                Arguments.of("first data source path keeps explicit sensitive rules", DATABASE_TYPE, createResourceMetaDataWithFirstDataSource(),
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false),
                Arguments.of("storage type overrides protocol type for oracle backend", MYSQL_DATABASE_TYPE, createResourceMetaDataWithStorageUrls("jdbc:oracle:thin:@localhost:1521:xe"),
                        new ConfigurationProperties(new Properties()), LookupMode.NORMALIZED, "T_ORDER", "t_order", true),
                Arguments.of("mixed storage trunk types fallback to insensitive rules", MYSQL_DATABASE_TYPE,
                        createResourceMetaDataWithStorageUrls("jdbc:mysql://localhost:3306/foo_db", "jdbc:oracle:thin:@localhost:1521:xe"),
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.NORMALIZED, "Foo", "foo", true));
    }
    
    private static Stream<Arguments> refreshWithProtocolTypeAndPropsArguments() {
        return Stream.of(
                Arguments.of("null protocol type and null props refresh to insensitive rules", null, null, LookupMode.NORMALIZED, "Foo", "foo", true),
                Arguments.of("sensitive props refresh to exact lookup", DATABASE_TYPE,
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false),
                Arguments.of("insensitive props refresh to normalized lookup", DATABASE_TYPE,
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.INSENSITIVE), LookupMode.NORMALIZED, "Foo", "foo", true));
    }
    
    private static Stream<Arguments> refreshWithResourceMetaDataAndPropsArguments() {
        return Stream.of(
                Arguments.of("null resource metadata and null props refresh to insensitive rules", null, null, null, LookupMode.NORMALIZED, "Foo", "foo", true),
                Arguments.of("null storage units refresh to exact lookup", DATABASE_TYPE, new ResourceMetaData(Collections.emptyMap(), null),
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false),
                Arguments.of("empty storage units refresh to exact lookup", DATABASE_TYPE, new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()),
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false),
                Arguments.of("first data source path refreshes to exact lookup", DATABASE_TYPE, createResourceMetaDataWithFirstDataSource(),
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false),
                Arguments.of("refresh uses oracle storage type when protocol type is mysql", MYSQL_DATABASE_TYPE, createResourceMetaDataWithStorageUrls("jdbc:oracle:thin:@localhost:1521:xe"),
                        new ConfigurationProperties(new Properties()), LookupMode.NORMALIZED, "T_ORDER", "t_order", true),
                Arguments.of("refresh falls back to insensitive rules for mixed storage types", MYSQL_DATABASE_TYPE,
                        createResourceMetaDataWithStorageUrls("jdbc:mysql://localhost:3306/foo_db", "jdbc:oracle:thin:@localhost:1521:xe"),
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.NORMALIZED, "Foo", "foo", true));
    }
    
    private static ConfigurationProperties createConfigurationProperties(final MetadataIdentifierCaseSensitivity caseSensitivity) {
        return new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.METADATA_IDENTIFIER_CASE_SENSITIVITY.getKey(), caseSensitivity.name())));
    }
    
    private static ResourceMetaData createResourceMetaDataWithFirstDataSource() {
        return createResourceMetaDataWithStorageUrls("jdbc:mysql://localhost:3306/foo_db");
    }
    
    private static ResourceMetaData createResourceMetaDataWithMySQLLowerCaseTableNames(final int lowerCaseTableNames) {
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(1, 1F);
        storageUnits.put("ds_0", createStorageUnit("ds_0", "jdbc:mysql://localhost:3306/foo_db", new LowerCaseTableNamesDataSource(lowerCaseTableNames)));
        return new ResourceMetaData(Collections.emptyMap(), storageUnits);
    }
    
    private static ResourceMetaData createResourceMetaDataWithStorageUrls(final String... urls) {
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(urls.length, 1F);
        for (int i = 0; i < urls.length; i++) {
            storageUnits.put("ds_" + i, createStorageUnit("ds_" + i, urls[i]));
        }
        return new ResourceMetaData(Collections.emptyMap(), storageUnits);
    }
    
    private static StorageUnit createStorageUnit(final String name, final String url) {
        return createStorageUnit(name, url, new FixtureDataSource());
    }
    
    private static StorageUnit createStorageUnit(final String name, final String url, final DataSource dataSource) {
        Map<String, Object> props = new LinkedHashMap<>(2, 1F);
        props.put("url", url);
        props.put("username", "root");
        return new StorageUnit(new StorageNode(name), new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", props), dataSource);
    }
    
    private static final class FixtureDataSource implements DataSource {
        
        @Override
        public Connection getConnection() throws SQLException {
            throw new SQLFeatureNotSupportedException("Not supported in fixture.");
        }
        
        @Override
        public Connection getConnection(final String username, final String password) throws SQLException {
            throw new SQLFeatureNotSupportedException("Not supported in fixture.");
        }
        
        @Override
        public PrintWriter getLogWriter() {
            return null;
        }
        
        @Override
        public void setLogWriter(final PrintWriter out) {
        }
        
        @Override
        public void setLoginTimeout(final int seconds) {
        }
        
        @Override
        public int getLoginTimeout() {
            return 0;
        }
        
        @Override
        public Logger getParentLogger() {
            return Logger.getGlobal();
        }
        
        @Override
        public <T> T unwrap(final Class<T> iface) throws SQLException {
            throw new SQLFeatureNotSupportedException("Not supported in fixture.");
        }
        
        @Override
        public boolean isWrapperFor(final Class<?> iface) {
            return false;
        }
    }
    
    private static final class LowerCaseTableNamesDataSource implements DataSource {
        
        private final int lowerCaseTableNames;
        
        private LowerCaseTableNamesDataSource(final int lowerCaseTableNames) {
            this.lowerCaseTableNames = lowerCaseTableNames;
        }
        
        @Override
        public Connection getConnection() {
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{Connection.class},
                    (proxy, method, args) -> "prepareStatement".equals(method.getName()) ? createPreparedStatement(lowerCaseTableNames) : getDefaultValue(method.getReturnType()));
        }
        
        @Override
        public Connection getConnection(final String username, final String password) {
            return getConnection();
        }
        
        private PreparedStatement createPreparedStatement(final int lowerCaseTableNames) {
            return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(), new Class[]{PreparedStatement.class},
                    (proxy, method, args) -> "executeQuery".equals(method.getName()) ? createResultSet(lowerCaseTableNames) : getDefaultValue(method.getReturnType()));
        }
        
        private ResultSet createResultSet(final int lowerCaseTableNames) {
            boolean[] nextInvoked = new boolean[1];
            return (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(), new Class[]{ResultSet.class}, (proxy, method, args) -> {
                if ("next".equals(method.getName())) {
                    boolean result = !nextInvoked[0];
                    nextInvoked[0] = true;
                    return result;
                }
                return "getInt".equals(method.getName()) ? lowerCaseTableNames : getDefaultValue(method.getReturnType());
            });
        }
        
        private Object getDefaultValue(final Class<?> returnType) {
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (boolean.class == returnType) {
                return false;
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
            if (byte.class == returnType) {
                return (byte) 0;
            }
            if (short.class == returnType) {
                return (short) 0;
            }
            if (char.class == returnType) {
                return '\0';
            }
            return null;
        }
        
        @Override
        public PrintWriter getLogWriter() {
            return null;
        }
        
        @Override
        public void setLogWriter(final PrintWriter out) {
        }
        
        @Override
        public void setLoginTimeout(final int seconds) {
        }
        
        @Override
        public int getLoginTimeout() {
            return 0;
        }
        
        @Override
        public Logger getParentLogger() {
            return Logger.getGlobal();
        }
        
        @Override
        public <T> T unwrap(final Class<T> iface) {
            return null;
        }
        
        @Override
        public boolean isWrapperFor(final Class<?> iface) {
            return false;
        }
    }
}
