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

package org.apache.shardingsphere.infra.database.h2;

import org.apache.shardingsphere.infra.database.spi.DataSourceMetaData;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class H2DataSourceMetaDataTest {
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(NewConstructorTestCaseArgumentsProvider.class)
    void assertNewConstructor(final String name, final String url, final String hostname, final int port, final String catalog, final String schema) {
        DataSourceMetaData actual = new H2DataSourceMetaData(url);
        assertThat(actual.getHostname(), is(hostname));
        assertThat(actual.getPort(), is(port));
        assertThat(actual.getCatalog(), is(catalog));
        assertThat(actual.getSchema(), is(schema));
        assertTrue(actual.getQueryProperties().isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(IsInSameDatabaseInstanceTestCaseArgumentsProvider.class)
    void assertIsInSameDatabaseInstance(final String name, final String url1, final String url2, final boolean isSame) {
        H2DataSourceMetaData actual1 = new H2DataSourceMetaData(url1);
        H2DataSourceMetaData actual2 = new H2DataSourceMetaData(url2);
        assertThat(actual1.isInSameDatabaseInstance(actual2), is(isSame));
    }
    
    private static class NewConstructorTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("mem", "jdbc:h2:mem:foo_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", "", -1, "foo_ds", null),
                    Arguments.of("symbol", "jdbc:h2:~:foo-ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", "", -1, "foo-ds", null),
                    Arguments.of("tcp", "jdbc:h2:tcp://localhost:8082/~/home/foo_ds;DB_CLOSE_DELAY=-1", "localhost", 8082, "foo_ds", null),
                    Arguments.of("ssl", "jdbc:h2:ssl:127.0.0.1/home/foo_ds", "127.0.0.1", -1, "foo_ds", null),
                    Arguments.of("file", "jdbc:h2:file:/data/foo_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", "", -1, "foo_ds", null));
        }
    }
    
    private static class IsInSameDatabaseInstanceTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("mem", "jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", "jdbc:h2:mem:ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", true),
                    Arguments.of("symbol", "jdbc:h2:~:ds-0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", "jdbc:h2:~:ds-1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", true),
                    Arguments.of("memAndSymbol", "jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", "jdbc:h2:~:ds-1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", true),
                    Arguments.of("tcp", "jdbc:h2:tcp://localhost:8082/~/test1/test2;DB_CLOSE_DELAY=-1", "jdbc:h2:tcp://localhost:8082/~/test3/test4;DB_CLOSE_DELAY=-1", true),
                    Arguments.of("tcpNotSame", "jdbc:h2:tcp://localhost:8082/~/test1/test2;DB_CLOSE_DELAY=-1", "jdbc:h2:tcp://192.168.64.76:8082/~/test3/test4;DB_CLOSE_DELAY=-1", false),
                    Arguments.of("ssl", "jdbc:h2:ssl:127.0.0.1/home/test-one", "jdbc:h2:ssl:127.0.0.1/home/test-two", true),
                    Arguments.of("sslNotSame", "jdbc:h2:ssl:127.0.0.1/home/test-one", "jdbc:h2:ssl:127.0.0.2/home/test-two", false),
                    Arguments.of("file", "jdbc:h2:file:/data/ds-0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", "jdbc:h2:file:/data/ds-1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", true));
        }
    }
}
