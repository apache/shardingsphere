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

package org.apache.shardingsphere.database.connector.core.jdbcurl.appender;

import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

class JdbcUrlAppenderTest {
    
    @ParameterizedTest(name = "{index}: url={0}")
    @MethodSource("appendQueryPropertiesProvider")
    void assertAppendQueryProperties(final String url, final Properties properties, final String expectedPrefix, final String[] expectedFragments) {
        String actual = new JdbcUrlAppender().appendQueryProperties(url, properties);
        assertThat(actual, startsWith(expectedPrefix));
        for (String each : expectedFragments) {
            assertThat(actual, containsString(each));
        }
    }
    
    private static Stream<Arguments> appendQueryPropertiesProvider() {
        return Stream.of(
                Arguments.arguments(
                        "jdbc:trunk://192.168.0.1:3306/foo_ds?useSSL=false&rewriteBatchedStatements=true",
                        new Properties(),
                        "jdbc:trunk://192.168.0.1:3306/foo_ds",
                        new String[]{"rewriteBatchedStatements=true", "useSSL=false"}),
                Arguments.arguments(
                        "jdbc:trunk://192.168.0.1:3306/foo_ds",
                        PropertiesBuilder.build(new Property("useSSL", Boolean.FALSE.toString()), new Property("rewriteBatchedStatements", Boolean.TRUE.toString())),
                        "jdbc:trunk://192.168.0.1:3306/foo_ds?",
                        new String[]{"rewriteBatchedStatements=true", "useSSL=false"}),
                Arguments.arguments(
                        "jdbc:trunk://192.168.0.1:3306/foo_ds?useSSL=false&rewriteBatchedStatements=true",
                        PropertiesBuilder.build(new Property("useSSL", Boolean.FALSE.toString()), new Property("rewriteBatchedStatements", Boolean.TRUE.toString())),
                        "jdbc:trunk://192.168.0.1:3306/foo_ds?",
                        new String[]{"rewriteBatchedStatements=true", "useSSL=false"}),
                Arguments.arguments(
                        "jdbc:trunk://192.168.0.1:3306/foo_ds?useSSL=false",
                        PropertiesBuilder.build(new Property("rewriteBatchedStatements", Boolean.TRUE.toString())),
                        "jdbc:trunk://192.168.0.1:3306/foo_ds?",
                        new String[]{"rewriteBatchedStatements=true", "useSSL=false"}));
    }
}
