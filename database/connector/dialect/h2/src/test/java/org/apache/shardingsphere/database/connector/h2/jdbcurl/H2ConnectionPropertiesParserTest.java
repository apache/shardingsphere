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

package org.apache.shardingsphere.database.connector.h2.jdbcurl;

import org.apache.shardingsphere.database.connector.core.exception.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class H2ConnectionPropertiesParserTest {
    
    private final ConnectionPropertiesParser parser = new H2ConnectionPropertiesParser();
    
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideArguments")
    void assertParse(final String name, final String url, final String expectedHost, final int expectedPort, final String expectedCatalog, final String expectedModel, final boolean expectException) {
        if (expectException) {
            assertThrows(UnrecognizedDatabaseURLException.class, () -> parser.parse(url, null, null));
        } else {
            assertParseSuccess(url, expectedHost, expectedPort, expectedCatalog, expectedModel);
        }
    }
    
    private void assertParseSuccess(final String url, final String expectedHost, final int expectedPort, final String expectedCatalog, final String expectedModel) {
        ConnectionProperties actual = parser.parse(url, null, null);
        assertThat(actual.getHostname(), is(expectedHost));
        assertThat(actual.getPort(), is(expectedPort));
        assertThat(actual.getCatalog(), is(expectedCatalog));
        assertThat(actual.getQueryProperties().getProperty("model"), is(expectedModel));
    }
    
    private static Stream<Arguments> provideArguments() {
        return Stream.of(
                Arguments.of("mem", "jdbc:h2:mem:ds_mem;MODE=MySQL", "", -1, "ds_mem", "mem", false),
                Arguments.of("tcp", "jdbc:h2:tcp://127.0.0.1:9092/~/demo/name;DB_CLOSE_DELAY=-1", "127.0.0.1", 9092, "name", "tcp:", false),
                Arguments.of("file", "jdbc:h2:file:/data/demo/db_file;MODE=MySQL", "", -1, "db_file", "file:", false),
                Arguments.of("invalid", "jdbc:invalid:h2", "", -1, "", "", true));
    }
}
