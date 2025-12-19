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

package org.apache.shardingsphere.database.connector.oracle.jdbcurl;

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

class OracleConnectionPropertiesParserTest {
    
    private final ConnectionPropertiesParser parser = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, TypedSPILoader.getService(DatabaseType.class, "Oracle"));
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(NewConstructorTestCaseArgumentsProvider.class)
    void assertNewConstructor(final String name, final String url, final String hostname, final int port, final String catalog, final String schema) {
        ConnectionProperties actual = parser.parse(url, "test", null);
        assertThat(actual.getHostname(), is(hostname));
        assertThat(actual.getPort(), is(port));
        assertThat(actual.getCatalog(), is(catalog));
        assertThat(actual.getSchema(), is(schema));
        assertTrue(actual.getQueryProperties().isEmpty());
    }
    
    @Test
    void assertNewConstructorFailure() {
        assertThrows(UnrecognizedDatabaseURLException.class, () -> parser.parse("jdbc:oracle:xxxxxxxx", "test", null));
    }
    
    private static final class NewConstructorTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(
                    Arguments.of("port", "jdbc:oracle:thin:@//127.0.0.1:9999/foo_ds", "127.0.0.1", 9999, "foo_ds", "test"),
                    Arguments.of("domainPort", "jdbc:oracle:oci:@ax-xx.frex.cc:9999/foo_ds", "ax-xx.frex.cc", 9999, "foo_ds", "test"),
                    Arguments.of("ipDefaultPort", "jdbc:oracle:oci:@127.0.0.1/foo_ds", "127.0.0.1", 1521, "foo_ds", "test"),
                    Arguments.of("domainDefaultPort", "jdbc:oracle:oci:@axxx.frex.cc/foo_ds", "axxx.frex.cc", 1521, "foo_ds", "test"),
                    Arguments.of("connectDescriptorIpUrl", "jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS = (PROTOCOL = TCP)(HOST = 127.0.0.1)(PORT = 1521))(ADDRESS = (PROTOCOL = TCP)"
                            + "(HOST = 127.0.0.1)(PORT = 1521))(LOAD_BALANCE = yes)(FAILOVER = ON)(CONNECT_DATA =(SERVER = DEDICATED)"
                            + "(SERVICE_NAME = rac)(FAILOVER_MODE=(TYPE = SELECT)(METHOD = BASIC)(RETIRES = 20)(DELAY = 15))))", "127.0.0.1", 1521, "rac", "test"),
                    Arguments.of("connectDescriptorDomainUrl", "jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS = (PROTOCOL = TCP)(HOST = axxx.frex.cc)(PORT = 1521))(ADDRESS = (PROTOCOL = TCP)"
                            + "(HOST = axxx.frex.cc)(PORT = 1521))(LOAD_BALANCE = yes)(FAILOVER = ON)(CONNECT_DATA =(SERVER = DEDICATED)"
                            + "(SERVICE_NAME = rac)(FAILOVER_MODE=(TYPE = SELECT)(METHOD = BASIC)(RETIRES = 20)(DELAY = 15))))", "axxx.frex.cc", 1521, "rac", "test"),
                    Arguments.of("connectDescriptorHalfenDomainUrl", "jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS = (PROTOCOL = TCP)(HOST = ax-xx.frex.cc)(PORT = 1521))(ADDRESS = (PROTOCOL = TCP)"
                            + "(HOST = ax-xx.frex.cc)(PORT = 1521))(LOAD_BALANCE = yes)(FAILOVER = ON)(CONNECT_DATA =(SERVER = DEDICATED)"
                            + "(SERVICE_NAME = rac)(FAILOVER_MODE=(TYPE = SELECT)(METHOD = BASIC)(RETIRES = 20)(DELAY = 15))))", "ax-xx.frex.cc", 1521, "rac", "test"),
                    Arguments.of("connectDescriptorUrlWithExtraSpaces", "jdbc:oracle:thin:@(DESCRIPTION = description"
                            + "(HOST   =   127.0.0.1)(PORT   =  1521))(LOAD_BALANCE = yes)(FAILOVER = ON)(CONNECT_DATA =(SERVER = DEDICATED)"
                            + "(SERVICE_NAME   =   rac)(FAILOVER_MODE=(TYPE = SELECT)(METHOD = BASIC)(RETIRES = 20)(DELAY = 15))))", "127.0.0.1", 1521, "rac", "test"));
        }
    }
}
