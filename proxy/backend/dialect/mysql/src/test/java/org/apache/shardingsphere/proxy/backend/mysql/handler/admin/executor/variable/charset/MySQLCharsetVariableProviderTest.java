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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.variable.charset;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.mysql.exception.UnknownCharsetException;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.charset.CharsetVariableProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MySQLCharsetVariableProviderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final CharsetVariableProvider provider = DatabaseTypedSPILoader.getService(CharsetVariableProvider.class, databaseType);
    
    @Test
    void assertCharsetVariablesAndDatabaseType() {
        assertThat(provider.getCharsetVariables(), contains("charset", "character_set_client"));
    }
    
    @Test
    void assertParseCharsetWithUnknown() {
        assertThrows(UnknownCharsetException.class, () -> provider.parseCharset("unknown_charset"));
    }
    
    @ParameterizedTest
    @MethodSource("successArguments")
    void assertParseCharset(final String input, final Charset expected) {
        assertThat(provider.parseCharset(input), is(expected));
    }
    
    private static Stream<Arguments> successArguments() {
        return Stream.of(
                arguments(" default ", MySQLConstants.DEFAULT_CHARSET.getCharset()),
                arguments("latin1 ", Charset.forName("latin1")),
                arguments("'utf8mb4'", StandardCharsets.UTF_8),
                arguments("\"utf8mb4\"", StandardCharsets.UTF_8));
    }
}
