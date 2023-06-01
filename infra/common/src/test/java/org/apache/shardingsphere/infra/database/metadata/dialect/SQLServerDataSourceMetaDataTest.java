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

package org.apache.shardingsphere.infra.database.metadata.dialect;

import org.apache.shardingsphere.infra.database.metadata.UnrecognizedDatabaseURLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SQLServerDataSourceMetaDataTest {
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(NewConstructorTestCaseArgumentsProvider.class)
    void assertNewConstructor(final String name, final String url, final String hostname, final int port, final String catalog, final String schema, final Properties queryProps) {
        SQLServerDataSourceMetaData actual = new SQLServerDataSourceMetaData(url);
        assertThat(actual.getHostname(), is(hostname));
        assertThat(actual.getPort(), is(port));
        assertThat(actual.getCatalog(), is(catalog));
        assertThat(actual.getSchema(), is(schema));
        assertThat(actual.getQueryProperties(), is(queryProps));
    }
    
    @Test
    void assertNewConstructorFailure() {
        assertThrows(UnrecognizedDatabaseURLException.class, () -> new SQLServerDataSourceMetaData("jdbc:sqlserver:xxxxxxxx"));
    }
    
    private static class NewConstructorTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("portAndMicrosoft", "jdbc:microsoft:sqlserver://127.0.0.1:9999;DatabaseName=ds_0", "127.0.0.1", 9999, "ds_0", null, new Properties()),
                    Arguments.of("portAndWithoutMicrosoft", "jdbc:sqlserver://127.0.0.1:9999;DatabaseName=ds_0", "127.0.0.1", 9999, "ds_0", null, new Properties()),
                    Arguments.of("defaultPortAndMicrosoft", "jdbc:microsoft:sqlserver://127.0.0.1;DatabaseName=ds_0", "127.0.0.1", 1433, "ds_0", null, new Properties()),
                    Arguments.of("defaultPortWithoutMicrosoft", "jdbc:sqlserver://127.0.0.1;DatabaseName=ds_0", "127.0.0.1", 1433, "ds_0", null, new Properties()),
                    Arguments.of("databaseNameContainDotAndMicrosoft", "jdbc:microsoft:sqlserver://127.0.0.1:9999;DatabaseName=ds_0.0.0", "127.0.0.1", 9999, "ds_0.0.0", null, new Properties()),
                    Arguments.of("databaseNameContainDotAndWithoutMicrosoft", "jdbc:sqlserver://127.0.0.1:9999;DatabaseName=ds_0.0.0", "127.0.0.1", 9999, "ds_0.0.0", null, new Properties()));
        }
    }
}
