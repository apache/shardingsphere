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

package org.apache.shardingsphere.database.connector.hive.jdbcurl;

import org.apache.hive.jdbc.JdbcUriParseException;
import org.apache.hive.jdbc.Utils;
import org.apache.hive.jdbc.Utils.JdbcConnectionParams;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class HiveConnectionPropertiesParserTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Hive");
    
    private final ConnectionPropertiesParser parser = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType);
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("parseSuccessArguments")
    void assertParseWithValidUrl(final String name, final String url, final String expectedHostname, final int expectedPort, final String expectedCatalog, final Properties expectedQueryProps) {
        ConnectionProperties actual = parser.parse(url, null, null);
        assertThat(actual.getHostname(), is(expectedHostname));
        assertThat(actual.getPort(), is(expectedPort));
        assertThat(actual.getCatalog(), is(expectedCatalog));
        assertNull(actual.getSchema());
        assertThat(actual.getQueryProperties(), is(expectedQueryProps));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("parseJdbcUriParseExceptionArguments")
    void assertParseWithInvalidUrl(final String name, final String url) {
        assertThrows(JdbcUriParseException.class, () -> parser.parse(url, null, null));
    }
    
    @Test
    void assertParseWithEmbeddedMode() {
        assertThrows(RuntimeException.class, () -> parser.parse("jdbc:hive2://", null, null));
    }
    
    @Test
    void assertParseWithNullHostAndNonZeroPort() {
        JdbcConnectionParams jdbcConnectionParams = mock(JdbcConnectionParams.class);
        when(jdbcConnectionParams.getHost()).thenReturn(null);
        when(jdbcConnectionParams.getPort()).thenReturn(10000);
        when(jdbcConnectionParams.getDbName()).thenReturn("default");
        when(jdbcConnectionParams.getSessionVars()).thenReturn(Collections.singletonMap("sessionKey", "sessionValue"));
        when(jdbcConnectionParams.getHiveConfs()).thenReturn(Collections.singletonMap("hiveConfKey", "hiveConfValue"));
        when(jdbcConnectionParams.getHiveVars()).thenReturn(Collections.singletonMap("hiveVarKey", "hiveVarValue"));
        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class, CALLS_REAL_METHODS)) {
            mockedUtils.when(() -> Utils.parseURL(anyString(), any(Properties.class))).thenReturn(jdbcConnectionParams);
            ConnectionProperties actualConnectionProperties = parser.parse("jdbc:hive2://branch/null-host-port/default", null, null);
            assertNull(actualConnectionProperties.getHostname());
            assertThat(actualConnectionProperties.getPort(), is(10000));
            assertThat(actualConnectionProperties.getCatalog(), is("default"));
            assertNull(actualConnectionProperties.getSchema());
            assertThat(actualConnectionProperties.getQueryProperties(),
                    is(PropertiesBuilder.build(new Property("sessionKey", "sessionValue"), new Property("hiveConfKey", "hiveConfValue"), new Property("hiveVarKey", "hiveVarValue"))));
        }
    }
    
    private static Stream<Arguments> parseSuccessArguments() {
        return Stream.of(
                Arguments.of("simple_first", "jdbc:hive2://localhost:10001/default", "localhost", 10001, "default", new Properties()),
                Arguments.of("simple_second", "jdbc:hive2://localhost/notdefault", "localhost", 10000, "notdefault", new Properties()),
                Arguments.of("simple_third", "jdbc:hive2://foo:1243", "foo", 1243, "default", new Properties()),
                Arguments.of("complex", "jdbc:hive2://server:10002/db;user=foo;password=bar?transportMode=http;httpPath=hs2", "server", 10002, "db",
                        PropertiesBuilder.build(new Property("user", "foo"), new Property("password", "bar"), new Property("transportMode", "http"), new Property("httpPath", "hs2"))));
    }
    
    private static Stream<Arguments> parseJdbcUriParseExceptionArguments() {
        return Stream.of(
                Arguments.of("principal_without_realm", "jdbc:hive2://localhost:10000;principal=test"),
                Arguments.of("principal_with_realm", "jdbc:hive2://localhost:10000;principal=hive/HiveServer2Host@YOUR-REALM.COM"),
                Arguments.of("missing_separator", "jdbc:hive2://localhost:10000test"));
    }
}
