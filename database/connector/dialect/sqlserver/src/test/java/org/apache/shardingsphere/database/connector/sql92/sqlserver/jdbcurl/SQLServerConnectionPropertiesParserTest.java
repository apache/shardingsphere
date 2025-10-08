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

package org.apache.shardingsphere.database.connector.sql92.sqlserver.jdbcurl;

import org.apache.shardingsphere.database.connector.core.exception.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLServerConnectionPropertiesParserTest {
    
    private final ConnectionPropertiesParser parser = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, TypedSPILoader.getService(DatabaseType.class, "SQLServer"));
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(NewConstructorTestCaseArgumentsProvider.class)
    void assertNewConstructor(final String name, final String url, final String hostname, final int port, final String catalog, final String schema) {
        ConnectionProperties actual = parser.parse(url, null, null);
        assertThat(actual.getHostname(), is(hostname));
        assertThat(actual.getPort(), is(port));
        assertThat(actual.getCatalog(), is(catalog));
        assertThat(actual.getSchema(), is(schema));
        assertTrue(actual.getQueryProperties().isEmpty());
    }
    
    @Test
    void assertNewConstructorFailure() {
        assertThrows(UnrecognizedDatabaseURLException.class, () -> parser.parse("jdbc:sqlserver:xxxxxxxx", null, null));
    }
    
    private static final class NewConstructorTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(
                    Arguments.of("portAndMicrosoft", "jdbc:microsoft:sqlserver://127.0.0.1:9999;DatabaseName=foo_ds", "127.0.0.1", 9999, "foo_ds", null),
                    Arguments.of("portAndWithoutMicrosoft", "jdbc:sqlserver://127.0.0.1:9999;DatabaseName=foo_ds", "127.0.0.1", 9999, "foo_ds", null),
                    Arguments.of("defaultPortAndMicrosoft", "jdbc:microsoft:sqlserver://127.0.0.1;DatabaseName=foo_ds", "127.0.0.1", 1433, "foo_ds", null),
                    Arguments.of("defaultPortWithoutMicrosoft", "jdbc:sqlserver://127.0.0.1;database=foo_ds", "127.0.0.1", 1433, "foo_ds", null),
                    Arguments.of("databaseNameContainDotAndMicrosoft", "jdbc:microsoft:sqlserver://127.0.0.1:9999;DatabaseName=foo_0.0.0", "127.0.0.1", 9999, "foo_0.0.0", null),
                    Arguments.of("databaseNameContainDotAndWithoutMicrosoft", "jdbc:sqlserver://127.0.0.1:9999;DatabaseName=foo_0.0.0", "127.0.0.1", 9999, "foo_0.0.0", null));
        }
    }
}
