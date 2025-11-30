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
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HiveConnectionPropertiesParserTest {
    
    private final ConnectionPropertiesParser parser = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, TypedSPILoader.getService(DatabaseType.class, "Hive"));
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(NewConstructorTestCaseArgumentsProvider.class)
    void assertNewConstructor(final String name, final String url, final String hostname, final int port, final String catalog, final String schema, final Properties queryProps) {
        ConnectionProperties actual = parser.parse(url, null, null);
        assertThat(actual.getHostname(), is(hostname));
        assertThat(actual.getPort(), is(port));
        assertThat(actual.getCatalog(), is(catalog));
        assertThat(actual.getSchema(), is(schema));
        assertThat(actual.getQueryProperties(), is(queryProps));
    }
    
    @Test
    void assertNewConstructorFailure() {
        assertThrows(JdbcUriParseException.class, () -> parser.parse("jdbc:hive2://localhost:10000;principal=test", null, null));
        assertThrows(JdbcUriParseException.class, () -> parser.parse("jdbc:hive2://localhost:10000;principal=hive/HiveServer2Host@YOUR-REALM.COM", null, null));
        assertThrows(JdbcUriParseException.class, () -> parser.parse("jdbc:hive2://localhost:10000test", null, null));
        assertThrows(RuntimeException.class, () -> parser.parse("jdbc:hive2://", null, null));
    }
    
    private static final class NewConstructorTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(
                    Arguments.of("simple_first", "jdbc:hive2://localhost:10001/default", "localhost", 10001, "default", null, new Properties()),
                    Arguments.of("simple_second", "jdbc:hive2://localhost/notdefault", "localhost", 10000, "notdefault", null, new Properties()),
                    Arguments.of("simple_third", "jdbc:hive2://foo:1243", "foo", 1243, "default", null, new Properties()),
                    Arguments.of("complex", "jdbc:hive2://server:10002/db;user=foo;password=bar?transportMode=http;httpPath=hs2",
                            "server", 10002, "db", null, PropertiesBuilder.build(
                                    new Property("user", "foo"),
                                    new Property("password", "bar"),
                                    new Property("transportMode", "http"),
                                    new Property("httpPath", "hs2"))));
        }
    }
}
