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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.provider;

import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCharacterSets;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariable;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariableScope;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.variable.charset.MySQLSessionCharsetContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CharsetValueProviderTest {
    
    private final CharsetValueProvider provider = new CharsetValueProvider();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("sessionValueArguments")
    void assertGetSessionValue(final String name, final MySQLSystemVariable variable, final String expected) {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        DefaultAttributeMap attributeMap = new DefaultAttributeMap();
        MySQLSessionCharsetContext.create(MySQLCharacterSets.LATIN1_SWEDISH_CI).withConnectionCollation(MySQLCharacterSets.UTF8MB4_BIN).apply(attributeMap);
        when(connectionSession.getAttributeMap()).thenReturn(attributeMap);
        assertThat(provider.getOptional(MySQLSystemVariableScope.SESSION, connectionSession, variable), is(Optional.of(expected)));
    }
    
    @Test
    void assertGetNullResultValue() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        DefaultAttributeMap attributeMap = new DefaultAttributeMap();
        MySQLSessionCharsetContext.create(MySQLCharacterSets.LATIN1_SWEDISH_CI).withoutResultConversion().apply(attributeMap);
        when(connectionSession.getAttributeMap()).thenReturn(attributeMap);
        assertThat(provider.get(MySQLSystemVariableScope.SESSION, connectionSession, MySQLSystemVariable.CHARACTER_SET_RESULTS), is("NULL"));
    }
    
    @Test
    void assertGetDefaultValue() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getAttributeMap()).thenReturn(new DefaultAttributeMap());
        assertThat(provider.getOptional(MySQLSystemVariableScope.SESSION, connectionSession, MySQLSystemVariable.AUTOCOMMIT), is(Optional.of("1")));
    }
    
    private static Stream<Arguments> sessionValueArguments() {
        return Stream.of(
                Arguments.of("client character set", MySQLSystemVariable.CHARACTER_SET_CLIENT, "latin1"),
                Arguments.of("connection character set", MySQLSystemVariable.CHARACTER_SET_CONNECTION, "utf8mb4"),
                Arguments.of("result character set", MySQLSystemVariable.CHARACTER_SET_RESULTS, "latin1"),
                Arguments.of("connection collation", MySQLSystemVariable.COLLATION_CONNECTION, "utf8mb4_bin"));
    }
}
