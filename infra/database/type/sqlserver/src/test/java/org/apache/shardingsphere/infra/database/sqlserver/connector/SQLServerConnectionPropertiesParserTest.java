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

package org.apache.shardingsphere.infra.database.sqlserver.connector;

import org.apache.shardingsphere.infra.database.core.connector.ConnectionPropertiesParser;
import org.apache.shardingsphere.infra.database.core.exception.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionProperties;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

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
    
    private static class NewConstructorTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("portAndMicrosoft", "jdbc:microsoft:sqlserver://127.0.0.1:9999;DatabaseName=foo_ds", "127.0.0.1", 9999, "foo_ds", null),
                    Arguments.of("portAndWithoutMicrosoft", "jdbc:sqlserver://127.0.0.1:9999;DatabaseName=foo_ds", "127.0.0.1", 9999, "foo_ds", null),
                    Arguments.of("defaultPortAndMicrosoft", "jdbc:microsoft:sqlserver://127.0.0.1;DatabaseName=foo_ds", "127.0.0.1", 1433, "foo_ds", null),
                    Arguments.of("defaultPortWithoutMicrosoft", "jdbc:sqlserver://127.0.0.1;DatabaseName=foo_ds", "127.0.0.1", 1433, "foo_ds", null),
                    Arguments.of("databaseNameContainDotAndMicrosoft", "jdbc:microsoft:sqlserver://127.0.0.1:9999;DatabaseName=foo_0.0.0", "127.0.0.1", 9999, "foo_0.0.0", null),
                    Arguments.of("databaseNameContainDotAndWithoutMicrosoft", "jdbc:sqlserver://127.0.0.1:9999;DatabaseName=foo_0.0.0", "127.0.0.1", 9999, "foo_0.0.0", null),
                    Arguments.of("testcontainers", "jdbc:tc:sqlserver:2022-CU10-ubuntu-22.04://test-hostname;databaseName=test_database", "test-hostname", 1433, "test_database", null));
        }
    }
}
