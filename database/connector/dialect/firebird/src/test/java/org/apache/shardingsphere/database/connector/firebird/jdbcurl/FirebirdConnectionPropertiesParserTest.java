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

package org.apache.shardingsphere.database.connector.firebird.jdbcurl;

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

import java.sql.SQLNonTransientConnectionException;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdConnectionPropertiesParserTest {
    
    private final ConnectionPropertiesParser parser = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, TypedSPILoader.getService(DatabaseType.class, "Firebird"));
    
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
        assertDoesNotThrow(() -> parser.parse("jdbc:firebirdsql:xxxxxxxx", null, null));
        assertThrows(SQLNonTransientConnectionException.class, () -> parser.parse("jdbc:firebirdsql://localhost:c:/data/db/test.fdb", null, null));
    }
    
    private static final class NewConstructorTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(
                    Arguments.of("simple_first", "jdbc:firebirdsql://127.0.0.1/foo_ds", "127.0.0.1", 3050, "foo_ds", null, new Properties()),
                    Arguments.of("simple_second", "jdbc:firebird://localhost:32783//var/lib/firebird/data/demo_ds_2.fdb",
                            "localhost", 32783, "/var/lib/firebird/data/demo_ds_2.fdb", null, new Properties()),
                    Arguments.of("simple_third", "jdbc:firebirdsql://localhost/database?socket_buffer_size=32767", "localhost", 3050, "database", null, PropertiesBuilder.build(
                            new Property("socketBufferSize", "32767"))),
                    Arguments.of("complex",
                            "jdbc:firebirdsql://localhost/database?socket_buffer_size=32767"
                                    + "&TRANSACTION_REPEATABLE_READ=concurrency,write,no_wait&columnLabelForName&soTimeout=1000&nonStandard2=value2",
                            "localhost", 3050, "database", null, PropertiesBuilder.build(
                                    new Property("socketBufferSize", "32767"),
                                    new Property("TRANSACTION_REPEATABLE_READ", "concurrency,write,no_wait"),
                                    new Property("columnLabelForName", ""),
                                    new Property("soTimeout", "1000"),
                                    new Property("nonStandard2", "value2"))));
        }
    }
}
