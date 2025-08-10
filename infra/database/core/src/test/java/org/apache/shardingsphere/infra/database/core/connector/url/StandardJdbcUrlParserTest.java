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

package org.apache.shardingsphere.infra.database.core.connector.url;

import org.apache.shardingsphere.infra.database.core.exception.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.test.props.PropertiesBuilder;
import org.apache.shardingsphere.test.props.PropertiesBuilder.Property;
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

class StandardJdbcUrlParserTest {
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertParse(final String name, final String actualURL, final String expectedHostname, final int expectedPort, final String expectedDatabaseName, final Properties expectedQueryProps) {
        JdbcUrl actual = new StandardJdbcUrlParser().parse(actualURL);
        assertThat(actual.getHostname(), is(expectedHostname));
        assertThat(actual.getPort(), is(expectedPort));
        assertThat(actual.getDatabase(), is(expectedDatabaseName));
        assertThat(actual.getQueryProperties(), is(expectedQueryProps));
    }
    
    @Test
    void assertParseIncorrectURL() {
        assertThrows(UnrecognizedDatabaseURLException.class, () -> new StandardJdbcUrlParser().parse("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(
                    Arguments.arguments("simple", "jdbc:mock://127.0.0.1/", "127.0.0.1", -1, "", new Properties()),
                    Arguments.arguments("MySQL_v4", "jdbc:mysql://127.0.0.1:3306/foo_ds?useSSL=false&sessionVariables=group_concat_max_len=204800,SQL_SAFE_UPDATES=0", "127.0.0.1", 3306, "foo_ds",
                            PropertiesBuilder.build(new Property("useSSL", Boolean.FALSE.toString()), new Property("sessionVariables", "group_concat_max_len=204800,SQL_SAFE_UPDATES=0"))),
                    Arguments.arguments("MySQL_v6", "jdbc:mysql://[fe80::d114:22b3:a0d9:2b3]:3306/foo_ds", "fe80::d114:22b3:a0d9:2b3", 3306, "foo_ds", new Properties()),
                    Arguments.arguments("MySQL_with_replication", "jdbc:mysql:replication://master-ip:3306,slave-1-ip:3306,slave-2-ip:3306/foo_ds?useUnicode=true", "master-ip", 3306, "foo_ds",
                            PropertiesBuilder.build(new Property("useUnicode", Boolean.TRUE.toString()))),
                    Arguments.arguments("PostgreSQL_v4", "jdbc:postgresql://127.0.0.1:5432/foo_ds?prepareThreshold=1&preferQueryMode=extendedForPrepared", "127.0.0.1", 5432, "foo_ds",
                            PropertiesBuilder.build(new Property("prepareThreshold", "1"), new Property("preferQueryMode", "extendedForPrepared"))));
            
        }
    }
}
