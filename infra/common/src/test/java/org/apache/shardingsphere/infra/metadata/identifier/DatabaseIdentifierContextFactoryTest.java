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
import org.apache.shardingsphere.infra.exception.kernel.metadata.AmbiguousIdentifierException;
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
import java.util.Locale;
import java.util.Optional;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseIdentifierContextFactoryTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private static final DatabaseType MYSQL_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private static final DatabaseType POSTGRESQL_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private static final DatabaseType OPEN_GAUSS_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private static final DatabaseType ORACLE_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "Oracle");
    
    private static final ResourceMetaData MYSQL_INSENSITIVE_RESOURCE_META_DATA = createResourceMetaDataWithMySQLLowerCaseTableNames(1);
    
    private static final ResourceMetaData MYSQL_QUOTED_INSENSITIVE_RESOURCE_META_DATA = createResourceMetaDataWithMySQLLowerCaseTableNames(2);
    
    private static final ResourceMetaData POSTGRESQL_RESOURCE_META_DATA = createResourceMetaDataWithStorageUrls("jdbc:postgresql://localhost:5432/foo_db");
    
    private static final ResourceMetaData OPEN_GAUSS_RESOURCE_META_DATA = createResourceMetaDataWithStorageUrls("jdbc:opengauss://localhost:5432/foo_db");
    
    private static final ResourceMetaData ORACLE_RESOURCE_META_DATA = createResourceMetaDataWithStorageUrls("jdbc:oracle:thin:@localhost:1521:xe");
    
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
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(MYSQL_DATABASE_TYPE, ORACLE_RESOURCE_META_DATA, new ConfigurationProperties(new Properties()));
        IdentifierCaseRule actualSchemaRule = actual.getRule(IdentifierScope.SCHEMA);
        IdentifierCaseRule actualTableRule = actual.getRule(IdentifierScope.TABLE);
        assertTrue(actualSchemaRule.matches("test_db", "TEST_DB", QuoteCharacter.NONE));
        assertTrue(actualTableRule.matches("T_ORDER", "t_order", QuoteCharacter.NONE));
    }
    
    @Test
    void assertRefreshUsesProtocolRuleForSchemaAndStorageRuleForTable() {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        DatabaseIdentifierContextFactory.refresh(actual, MYSQL_DATABASE_TYPE, ORACLE_RESOURCE_META_DATA, new ConfigurationProperties(new Properties()));
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
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createWithSupportedDatabaseSchemaLookupArguments")
    void assertCreateFindSupportedDatabaseSchemaIdentifiers(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                            final String actualSchemaName, final IdentifierValue lookupIdentifier, final String expectedSchemaName) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(protocolType, resourceMetaData, new ConfigurationProperties(new Properties()));
        IdentifierIndex<String> schemaIndex = createIdentifierIndex(actual, IdentifierScope.SCHEMA, actualSchemaName);
        assertThat(schemaIndex.find(lookupIdentifier), is(getExpectedResult(expectedSchemaName)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createWithSupportedDatabaseTableLookupArguments")
    void assertCreateFindSupportedDatabaseTableIdentifiers(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                           final String actualTableName, final IdentifierValue lookupIdentifier, final String expectedTableName) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(protocolType, resourceMetaData, new ConfigurationProperties(new Properties()));
        IdentifierIndex<String> tableIndex = createIdentifierIndex(actual, IdentifierScope.TABLE, actualTableName);
        assertThat(tableIndex.find(lookupIdentifier), is(getExpectedResult(expectedTableName)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createWithSupportedDatabaseColumnLookupArguments")
    void assertCreateFindSupportedDatabaseColumnIdentifiers(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                            final String actualColumnName, final IdentifierValue lookupIdentifier, final String expectedColumnName) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(protocolType, resourceMetaData, new ConfigurationProperties(new Properties()));
        IdentifierIndex<String> columnIndex = createIdentifierIndex(actual, IdentifierScope.COLUMN, actualColumnName);
        assertThat(columnIndex.find(lookupIdentifier), is(getExpectedResult(expectedColumnName)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("refreshWithSupportedDatabaseSchemaLookupArguments")
    void assertRefreshFindSupportedDatabaseSchemaIdentifiers(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                             final String actualSchemaName, final IdentifierValue lookupIdentifier, final String expectedSchemaName) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        DatabaseIdentifierContextFactory.refresh(actual, protocolType, resourceMetaData, new ConfigurationProperties(new Properties()));
        IdentifierIndex<String> schemaIndex = createIdentifierIndex(actual, IdentifierScope.SCHEMA, actualSchemaName);
        assertThat(schemaIndex.find(lookupIdentifier), is(getExpectedResult(expectedSchemaName)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("refreshWithSupportedDatabaseTableLookupArguments")
    void assertRefreshFindSupportedDatabaseTableIdentifiers(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                            final String actualTableName, final IdentifierValue lookupIdentifier, final String expectedTableName) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        DatabaseIdentifierContextFactory.refresh(actual, protocolType, resourceMetaData, new ConfigurationProperties(new Properties()));
        IdentifierIndex<String> tableIndex = createIdentifierIndex(actual, IdentifierScope.TABLE, actualTableName);
        assertThat(tableIndex.find(lookupIdentifier), is(getExpectedResult(expectedTableName)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("refreshWithSupportedDatabaseColumnLookupArguments")
    void assertRefreshFindSupportedDatabaseColumnIdentifiers(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                             final String actualColumnName, final IdentifierValue lookupIdentifier, final String expectedColumnName) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        DatabaseIdentifierContextFactory.refresh(actual, protocolType, resourceMetaData, new ConfigurationProperties(new Properties()));
        IdentifierIndex<String> columnIndex = createIdentifierIndex(actual, IdentifierScope.COLUMN, actualColumnName);
        assertThat(columnIndex.find(lookupIdentifier), is(getExpectedResult(expectedColumnName)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createWithMixedStoredCaseSchemaLookupArguments")
    void assertCreateFindMixedStoredCaseSchemaIdentifiers(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                          final IdentifierValue lookupIdentifier, final String expectedSchemaName, final String expectedExceptionMessage) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(protocolType, resourceMetaData, new ConfigurationProperties(new Properties()));
        IdentifierIndex<String> schemaIndex = createIdentifierIndex(actual, IdentifierScope.SCHEMA, "foo_schema", "FOO_SCHEMA");
        assertFindResult(schemaIndex, lookupIdentifier, expectedSchemaName, expectedExceptionMessage);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createWithMixedStoredCaseTableLookupArguments")
    void assertCreateFindMixedStoredCaseTableIdentifiers(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                         final IdentifierValue lookupIdentifier, final String expectedTableName, final String expectedExceptionMessage) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(protocolType, resourceMetaData, new ConfigurationProperties(new Properties()));
        IdentifierIndex<String> tableIndex = createIdentifierIndex(actual, IdentifierScope.TABLE, "foo_tbl", "FOO_TBL");
        assertFindResult(tableIndex, lookupIdentifier, expectedTableName, expectedExceptionMessage);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createWithMixedStoredCaseColumnLookupArguments")
    void assertCreateFindMixedStoredCaseColumnIdentifiers(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                          final IdentifierValue lookupIdentifier, final String expectedColumnName, final String expectedExceptionMessage) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(protocolType, resourceMetaData, new ConfigurationProperties(new Properties()));
        IdentifierIndex<String> columnIndex = createIdentifierIndex(actual, IdentifierScope.COLUMN, "foo_col", "FOO_COL");
        assertFindResult(columnIndex, lookupIdentifier, expectedColumnName, expectedExceptionMessage);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("refreshWithMixedStoredCaseSchemaLookupArguments")
    void assertRefreshFindMixedStoredCaseSchemaIdentifiers(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                           final IdentifierValue lookupIdentifier, final String expectedSchemaName, final String expectedExceptionMessage) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        DatabaseIdentifierContextFactory.refresh(actual, protocolType, resourceMetaData, new ConfigurationProperties(new Properties()));
        IdentifierIndex<String> schemaIndex = createIdentifierIndex(actual, IdentifierScope.SCHEMA, "foo_schema", "FOO_SCHEMA");
        assertFindResult(schemaIndex, lookupIdentifier, expectedSchemaName, expectedExceptionMessage);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("refreshWithMixedStoredCaseTableLookupArguments")
    void assertRefreshFindMixedStoredCaseTableIdentifiers(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                          final IdentifierValue lookupIdentifier, final String expectedTableName, final String expectedExceptionMessage) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        DatabaseIdentifierContextFactory.refresh(actual, protocolType, resourceMetaData, new ConfigurationProperties(new Properties()));
        IdentifierIndex<String> tableIndex = createIdentifierIndex(actual, IdentifierScope.TABLE, "foo_tbl", "FOO_TBL");
        assertFindResult(tableIndex, lookupIdentifier, expectedTableName, expectedExceptionMessage);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("refreshWithMixedStoredCaseColumnLookupArguments")
    void assertRefreshFindMixedStoredCaseColumnIdentifiers(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                           final IdentifierValue lookupIdentifier, final String expectedColumnName, final String expectedExceptionMessage) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        DatabaseIdentifierContextFactory.refresh(actual, protocolType, resourceMetaData, new ConfigurationProperties(new Properties()));
        IdentifierIndex<String> columnIndex = createIdentifierIndex(actual, IdentifierScope.COLUMN, "foo_col", "FOO_COL");
        assertFindResult(columnIndex, lookupIdentifier, expectedColumnName, expectedExceptionMessage);
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
    
    private static Stream<Arguments> createWithSupportedDatabaseSchemaLookupArguments() {
        return Stream.of(
                createInsensitiveQuotedExactLookupArguments("mysql schema", MYSQL_DATABASE_TYPE, MYSQL_INSENSITIVE_RESOURCE_META_DATA, "foo_schema", "`"),
                createLowerCaseLookupArguments("postgresql schema", POSTGRESQL_DATABASE_TYPE, POSTGRESQL_RESOURCE_META_DATA, "foo_schema", "\""),
                createLowerCaseLookupArguments("openGauss schema", OPEN_GAUSS_DATABASE_TYPE, OPEN_GAUSS_RESOURCE_META_DATA, "foo_schema", "\""),
                createUpperCaseLookupArguments("oracle schema", ORACLE_DATABASE_TYPE, ORACLE_RESOURCE_META_DATA, "foo_schema", "\""))
                .flatMap(each -> each);
    }
    
    private static Stream<Arguments> createWithSupportedDatabaseTableLookupArguments() {
        return Stream.of(
                createNormalizedLookupArguments("mysql table lower_case_table_names=1", MYSQL_DATABASE_TYPE, MYSQL_INSENSITIVE_RESOURCE_META_DATA, "foo_tbl", "`"),
                createNormalizedLookupArguments("mysql table lower_case_table_names=2", MYSQL_DATABASE_TYPE, MYSQL_QUOTED_INSENSITIVE_RESOURCE_META_DATA, "foo_tbl", "`"),
                createLowerCaseLookupArguments("postgresql table", POSTGRESQL_DATABASE_TYPE, POSTGRESQL_RESOURCE_META_DATA, "foo_tbl", "\""),
                createLowerCaseLookupArguments("openGauss table", OPEN_GAUSS_DATABASE_TYPE, OPEN_GAUSS_RESOURCE_META_DATA, "foo_tbl", "\""),
                createUpperCaseLookupArguments("oracle table", ORACLE_DATABASE_TYPE, ORACLE_RESOURCE_META_DATA, "foo_tbl", "\""))
                .flatMap(each -> each);
    }
    
    private static Stream<Arguments> createWithSupportedDatabaseColumnLookupArguments() {
        return Stream.of(
                createNormalizedLookupArguments("mysql column lower_case_table_names=1", MYSQL_DATABASE_TYPE, MYSQL_INSENSITIVE_RESOURCE_META_DATA, "foo_col", "`"),
                createNormalizedLookupArguments("mysql column lower_case_table_names=2", MYSQL_DATABASE_TYPE, MYSQL_QUOTED_INSENSITIVE_RESOURCE_META_DATA, "foo_col", "`"),
                createLowerCaseLookupArguments("postgresql column", POSTGRESQL_DATABASE_TYPE, POSTGRESQL_RESOURCE_META_DATA, "foo_col", "\""),
                createLowerCaseLookupArguments("openGauss column", OPEN_GAUSS_DATABASE_TYPE, OPEN_GAUSS_RESOURCE_META_DATA, "foo_col", "\""),
                createUpperCaseLookupArguments("oracle column", ORACLE_DATABASE_TYPE, ORACLE_RESOURCE_META_DATA, "foo_col", "\""))
                .flatMap(each -> each);
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
    
    private static Stream<Arguments> refreshWithSupportedDatabaseSchemaLookupArguments() {
        return createWithSupportedDatabaseSchemaLookupArguments();
    }
    
    private static Stream<Arguments> refreshWithSupportedDatabaseTableLookupArguments() {
        return createWithSupportedDatabaseTableLookupArguments();
    }
    
    private static Stream<Arguments> refreshWithSupportedDatabaseColumnLookupArguments() {
        return createWithSupportedDatabaseColumnLookupArguments();
    }
    
    private static Stream<Arguments> createWithMixedStoredCaseSchemaLookupArguments() {
        return Stream.of(
                createInsensitiveQuotedExactMixedLookupArguments("mysql schema", MYSQL_DATABASE_TYPE, MYSQL_INSENSITIVE_RESOURCE_META_DATA, "foo_schema", "`"),
                createLowerCaseMixedLookupArguments("postgresql schema", POSTGRESQL_DATABASE_TYPE, POSTGRESQL_RESOURCE_META_DATA, "foo_schema", "\""),
                createLowerCaseMixedLookupArguments("openGauss schema", OPEN_GAUSS_DATABASE_TYPE, OPEN_GAUSS_RESOURCE_META_DATA, "foo_schema", "\""),
                createUpperCaseMixedLookupArguments("oracle schema", ORACLE_DATABASE_TYPE, ORACLE_RESOURCE_META_DATA, "foo_schema", "\""))
                .flatMap(each -> each);
    }
    
    private static Stream<Arguments> createWithMixedStoredCaseTableLookupArguments() {
        return Stream.of(
                createNormalizedMixedLookupArguments("mysql table lower_case_table_names=1", MYSQL_DATABASE_TYPE, MYSQL_INSENSITIVE_RESOURCE_META_DATA, "foo_tbl", "`"),
                createNormalizedMixedLookupArguments("mysql table lower_case_table_names=2", MYSQL_DATABASE_TYPE, MYSQL_QUOTED_INSENSITIVE_RESOURCE_META_DATA, "foo_tbl", "`"),
                createLowerCaseMixedLookupArguments("postgresql table", POSTGRESQL_DATABASE_TYPE, POSTGRESQL_RESOURCE_META_DATA, "foo_tbl", "\""),
                createLowerCaseMixedLookupArguments("openGauss table", OPEN_GAUSS_DATABASE_TYPE, OPEN_GAUSS_RESOURCE_META_DATA, "foo_tbl", "\""),
                createUpperCaseMixedLookupArguments("oracle table", ORACLE_DATABASE_TYPE, ORACLE_RESOURCE_META_DATA, "foo_tbl", "\""))
                .flatMap(each -> each);
    }
    
    private static Stream<Arguments> createWithMixedStoredCaseColumnLookupArguments() {
        return Stream.of(
                createNormalizedMixedLookupArguments("mysql column lower_case_table_names=1", MYSQL_DATABASE_TYPE, MYSQL_INSENSITIVE_RESOURCE_META_DATA, "foo_col", "`"),
                createNormalizedMixedLookupArguments("mysql column lower_case_table_names=2", MYSQL_DATABASE_TYPE, MYSQL_QUOTED_INSENSITIVE_RESOURCE_META_DATA, "foo_col", "`"),
                createLowerCaseMixedLookupArguments("postgresql column", POSTGRESQL_DATABASE_TYPE, POSTGRESQL_RESOURCE_META_DATA, "foo_col", "\""),
                createLowerCaseMixedLookupArguments("openGauss column", OPEN_GAUSS_DATABASE_TYPE, OPEN_GAUSS_RESOURCE_META_DATA, "foo_col", "\""),
                createUpperCaseMixedLookupArguments("oracle column", ORACLE_DATABASE_TYPE, ORACLE_RESOURCE_META_DATA, "foo_col", "\""))
                .flatMap(each -> each);
    }
    
    private static Stream<Arguments> refreshWithMixedStoredCaseSchemaLookupArguments() {
        return createWithMixedStoredCaseSchemaLookupArguments();
    }
    
    private static Stream<Arguments> refreshWithMixedStoredCaseTableLookupArguments() {
        return createWithMixedStoredCaseTableLookupArguments();
    }
    
    private static Stream<Arguments> refreshWithMixedStoredCaseColumnLookupArguments() {
        return createWithMixedStoredCaseColumnLookupArguments();
    }
    
    private static ConfigurationProperties createConfigurationProperties(final MetadataIdentifierCaseSensitivity caseSensitivity) {
        return new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.METADATA_IDENTIFIER_CASE_SENSITIVITY.getKey(), caseSensitivity.name())));
    }
    
    private static IdentifierIndex<String> createIdentifierIndex(final DatabaseIdentifierContext identifierContext, final IdentifierScope identifierScope, final String... actualNames) {
        IdentifierIndex<String> result = new IdentifierIndex<>(identifierContext, identifierScope);
        Map<String, String> values = new LinkedHashMap<>(actualNames.length, 1F);
        for (String each : actualNames) {
            values.put(each, each);
        }
        result.rebuild(values);
        return result;
    }
    
    private static Optional<String> getExpectedResult(final String expectedName) {
        return null == expectedName ? Optional.empty() : Optional.of(expectedName);
    }
    
    private static void assertFindResult(final IdentifierIndex<String> identifierIndex, final IdentifierValue lookupIdentifier,
                                         final String expectedName, final String expectedExceptionMessage) {
        if (null == expectedExceptionMessage) {
            assertThat(identifierIndex.find(lookupIdentifier), is(getExpectedResult(expectedName)));
            return;
        }
        AmbiguousIdentifierException actual = assertThrows(AmbiguousIdentifierException.class, () -> identifierIndex.find(lookupIdentifier));
        assertThat(actual.getMessage(), is(expectedExceptionMessage));
    }
    
    private static Stream<Arguments> createInsensitiveQuotedExactLookupArguments(final String databaseName, final DatabaseType protocolType,
                                                                                 final ResourceMetaData resourceMetaData, final String lowerActualName,
                                                                                 final String quoteCharacter) {
        String upperActualName = lowerActualName.toUpperCase(Locale.ENGLISH);
        return Stream.of(
                createLookupArgument(databaseName + " finds lower actual by unquoted lower lookup",
                        protocolType, resourceMetaData, lowerActualName, lowerActualName, false, quoteCharacter, lowerActualName),
                createLookupArgument(databaseName + " finds lower actual by unquoted upper lookup",
                        protocolType, resourceMetaData, lowerActualName, upperActualName, false, quoteCharacter, lowerActualName),
                createLookupArgument(databaseName + " finds lower actual by quoted lower lookup",
                        protocolType, resourceMetaData, lowerActualName, lowerActualName, true, quoteCharacter, lowerActualName),
                createLookupArgument(databaseName + " does not find lower actual by quoted upper lookup",
                        protocolType, resourceMetaData, lowerActualName, upperActualName, true, quoteCharacter, null),
                createLookupArgument(databaseName + " finds upper actual by unquoted lower lookup",
                        protocolType, resourceMetaData, upperActualName, lowerActualName, false, quoteCharacter, upperActualName),
                createLookupArgument(databaseName + " finds upper actual by unquoted upper lookup",
                        protocolType, resourceMetaData, upperActualName, upperActualName, false, quoteCharacter, upperActualName),
                createLookupArgument(databaseName + " does not find upper actual by quoted lower lookup",
                        protocolType, resourceMetaData, upperActualName, lowerActualName, true, quoteCharacter, null),
                createLookupArgument(databaseName + " finds upper actual by quoted upper lookup",
                        protocolType, resourceMetaData, upperActualName, upperActualName, true, quoteCharacter, upperActualName));
    }
    
    private static Stream<Arguments> createNormalizedLookupArguments(final String databaseName, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                                     final String lowerActualName, final String quoteCharacter) {
        String upperActualName = lowerActualName.toUpperCase(Locale.ENGLISH);
        return Stream.of(
                createLookupArgument(databaseName + " finds lower actual by unquoted lower lookup",
                        protocolType, resourceMetaData, lowerActualName, lowerActualName, false, quoteCharacter, lowerActualName),
                createLookupArgument(databaseName + " finds lower actual by unquoted upper lookup",
                        protocolType, resourceMetaData, lowerActualName, upperActualName, false, quoteCharacter, lowerActualName),
                createLookupArgument(databaseName + " finds lower actual by quoted lower lookup",
                        protocolType, resourceMetaData, lowerActualName, lowerActualName, true, quoteCharacter, lowerActualName),
                createLookupArgument(databaseName + " finds lower actual by quoted upper lookup",
                        protocolType, resourceMetaData, lowerActualName, upperActualName, true, quoteCharacter, lowerActualName),
                createLookupArgument(databaseName + " finds upper actual by unquoted lower lookup",
                        protocolType, resourceMetaData, upperActualName, lowerActualName, false, quoteCharacter, upperActualName),
                createLookupArgument(databaseName + " finds upper actual by unquoted upper lookup",
                        protocolType, resourceMetaData, upperActualName, upperActualName, false, quoteCharacter, upperActualName),
                createLookupArgument(databaseName + " finds upper actual by quoted lower lookup",
                        protocolType, resourceMetaData, upperActualName, lowerActualName, true, quoteCharacter, upperActualName),
                createLookupArgument(databaseName + " finds upper actual by quoted upper lookup",
                        protocolType, resourceMetaData, upperActualName, upperActualName, true, quoteCharacter, upperActualName));
    }
    
    private static Stream<Arguments> createLowerCaseLookupArguments(final String databaseName, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                                    final String lowerActualName, final String quoteCharacter) {
        String upperActualName = lowerActualName.toUpperCase(Locale.ENGLISH);
        return Stream.of(
                createLookupArgument(databaseName + " finds lower actual by unquoted lower lookup",
                        protocolType, resourceMetaData, lowerActualName, lowerActualName, false, quoteCharacter, lowerActualName),
                createLookupArgument(databaseName + " finds lower actual by unquoted upper lookup",
                        protocolType, resourceMetaData, lowerActualName, upperActualName, false, quoteCharacter, lowerActualName),
                createLookupArgument(databaseName + " finds lower actual by quoted lower lookup",
                        protocolType, resourceMetaData, lowerActualName, lowerActualName, true, quoteCharacter, lowerActualName),
                createLookupArgument(databaseName + " does not find lower actual by quoted upper lookup",
                        protocolType, resourceMetaData, lowerActualName, upperActualName, true, quoteCharacter, null),
                createLookupArgument(databaseName + " does not find upper actual by unquoted lower lookup",
                        protocolType, resourceMetaData, upperActualName, lowerActualName, false, quoteCharacter, null),
                createLookupArgument(databaseName + " does not find upper actual by unquoted upper lookup",
                        protocolType, resourceMetaData, upperActualName, upperActualName, false, quoteCharacter, null),
                createLookupArgument(databaseName + " does not find upper actual by quoted lower lookup",
                        protocolType, resourceMetaData, upperActualName, lowerActualName, true, quoteCharacter, null),
                createLookupArgument(databaseName + " finds upper actual by quoted upper lookup",
                        protocolType, resourceMetaData, upperActualName, upperActualName, true, quoteCharacter, upperActualName));
    }
    
    private static Stream<Arguments> createUpperCaseLookupArguments(final String databaseName, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                                    final String lowerActualName, final String quoteCharacter) {
        String upperActualName = lowerActualName.toUpperCase(Locale.ENGLISH);
        return Stream.of(
                createLookupArgument(databaseName + " does not find lower actual by unquoted lower lookup",
                        protocolType, resourceMetaData, lowerActualName, lowerActualName, false, quoteCharacter, null),
                createLookupArgument(databaseName + " does not find lower actual by unquoted upper lookup",
                        protocolType, resourceMetaData, lowerActualName, upperActualName, false, quoteCharacter, null),
                createLookupArgument(databaseName + " finds lower actual by quoted lower lookup",
                        protocolType, resourceMetaData, lowerActualName, lowerActualName, true, quoteCharacter, lowerActualName),
                createLookupArgument(databaseName + " does not find lower actual by quoted upper lookup",
                        protocolType, resourceMetaData, lowerActualName, upperActualName, true, quoteCharacter, null),
                createLookupArgument(databaseName + " finds upper actual by unquoted lower lookup",
                        protocolType, resourceMetaData, upperActualName, lowerActualName, false, quoteCharacter, upperActualName),
                createLookupArgument(databaseName + " finds upper actual by unquoted upper lookup",
                        protocolType, resourceMetaData, upperActualName, upperActualName, false, quoteCharacter, upperActualName),
                createLookupArgument(databaseName + " does not find upper actual by quoted lower lookup",
                        protocolType, resourceMetaData, upperActualName, lowerActualName, true, quoteCharacter, null),
                createLookupArgument(databaseName + " finds upper actual by quoted upper lookup",
                        protocolType, resourceMetaData, upperActualName, upperActualName, true, quoteCharacter, upperActualName));
    }
    
    private static Stream<Arguments> createInsensitiveQuotedExactMixedLookupArguments(final String databaseName, final DatabaseType protocolType,
                                                                                      final ResourceMetaData resourceMetaData, final String lowerActualName,
                                                                                      final String quoteCharacter) {
        String upperActualName = lowerActualName.toUpperCase(Locale.ENGLISH);
        return Stream.of(
                createMixedLookupArgument(databaseName + " finds lower actual by quoted lower lookup", protocolType, resourceMetaData, lowerActualName, true, quoteCharacter, lowerActualName, null),
                createMixedLookupArgument(databaseName + " finds upper actual by quoted upper lookup", protocolType, resourceMetaData, upperActualName, true, quoteCharacter, upperActualName, null),
                createMixedLookupArgument(databaseName + " treats unquoted lower lookup as ambiguous", protocolType, resourceMetaData, lowerActualName, false, quoteCharacter, null,
                        createAmbiguousIdentifierMessage(lowerActualName, lowerActualName, upperActualName)),
                createMixedLookupArgument(databaseName + " treats unquoted upper lookup as ambiguous", protocolType, resourceMetaData, upperActualName, false, quoteCharacter, null,
                        createAmbiguousIdentifierMessage(upperActualName, lowerActualName, upperActualName)));
    }
    
    private static Stream<Arguments> createNormalizedMixedLookupArguments(final String databaseName, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                                          final String lowerActualName, final String quoteCharacter) {
        String upperActualName = lowerActualName.toUpperCase(Locale.ENGLISH);
        return Stream.of(
                createMixedLookupArgument(databaseName + " treats quoted lower lookup as ambiguous", protocolType, resourceMetaData, lowerActualName, true, quoteCharacter, null,
                        createAmbiguousIdentifierMessage(lowerActualName, lowerActualName, upperActualName)),
                createMixedLookupArgument(databaseName + " treats quoted upper lookup as ambiguous", protocolType, resourceMetaData, upperActualName, true, quoteCharacter, null,
                        createAmbiguousIdentifierMessage(upperActualName, lowerActualName, upperActualName)),
                createMixedLookupArgument(databaseName + " treats unquoted lower lookup as ambiguous", protocolType, resourceMetaData, lowerActualName, false, quoteCharacter, null,
                        createAmbiguousIdentifierMessage(lowerActualName, lowerActualName, upperActualName)),
                createMixedLookupArgument(databaseName + " treats unquoted upper lookup as ambiguous", protocolType, resourceMetaData, upperActualName, false, quoteCharacter, null,
                        createAmbiguousIdentifierMessage(upperActualName, lowerActualName, upperActualName)));
    }
    
    private static Stream<Arguments> createLowerCaseMixedLookupArguments(final String databaseName, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                                         final String lowerActualName, final String quoteCharacter) {
        String upperActualName = lowerActualName.toUpperCase(Locale.ENGLISH);
        return Stream.of(
                createMixedLookupArgument(databaseName + " finds lower actual by quoted lower lookup", protocolType, resourceMetaData, lowerActualName, true, quoteCharacter, lowerActualName, null),
                createMixedLookupArgument(databaseName + " finds upper actual by quoted upper lookup", protocolType, resourceMetaData, upperActualName, true, quoteCharacter, upperActualName, null),
                createMixedLookupArgument(databaseName + " finds lower actual by unquoted lower lookup", protocolType, resourceMetaData, lowerActualName, false, quoteCharacter, lowerActualName, null),
                createMixedLookupArgument(databaseName + " finds lower actual by unquoted upper lookup", protocolType, resourceMetaData, upperActualName, false, quoteCharacter, lowerActualName,
                        null));
    }
    
    private static Stream<Arguments> createUpperCaseMixedLookupArguments(final String databaseName, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                                         final String lowerActualName, final String quoteCharacter) {
        String upperActualName = lowerActualName.toUpperCase(Locale.ENGLISH);
        return Stream.of(
                createMixedLookupArgument(databaseName + " finds lower actual by quoted lower lookup", protocolType, resourceMetaData, lowerActualName, true, quoteCharacter, lowerActualName, null),
                createMixedLookupArgument(databaseName + " finds upper actual by quoted upper lookup", protocolType, resourceMetaData, upperActualName, true, quoteCharacter, upperActualName, null),
                createMixedLookupArgument(databaseName + " finds upper actual by unquoted lower lookup", protocolType, resourceMetaData, lowerActualName, false, quoteCharacter, upperActualName, null),
                createMixedLookupArgument(databaseName + " finds upper actual by unquoted upper lookup", protocolType, resourceMetaData, upperActualName, false, quoteCharacter, upperActualName,
                        null));
    }
    
    private static Arguments createLookupArgument(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData, final String actualName,
                                                  final String lookupName, final boolean quoted, final String quoteCharacter, final String expectedName) {
        return Arguments.of(name, protocolType, resourceMetaData, actualName, createIdentifierValue(lookupName, quoted, quoteCharacter), expectedName);
    }
    
    private static Arguments createMixedLookupArgument(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                       final String lookupName, final boolean quoted, final String quoteCharacter,
                                                       final String expectedName, final String expectedExceptionMessage) {
        return Arguments.of(name, protocolType, resourceMetaData, createIdentifierValue(lookupName, quoted, quoteCharacter), expectedName, expectedExceptionMessage);
    }
    
    private static IdentifierValue createIdentifierValue(final String lookupName, final boolean quoted, final String quoteCharacter) {
        return new IdentifierValue(quoted ? quoteCharacter + lookupName + quoteCharacter : lookupName);
    }
    
    private static String createAmbiguousIdentifierMessage(final String identifierName, final String lowerActualName, final String upperActualName) {
        return String.format("Identifier '%s' is ambiguous, matched actual identifiers: %s, %s.", identifierName, lowerActualName, upperActualName);
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
