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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.variable.session;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.session.ReplayedSessionVariableProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MySQLReplayedSessionVariableProviderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final ReplayedSessionVariableProvider provider = TypedSPILoader.getService(ReplayedSessionVariableProvider.class, databaseType);
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("replayArguments")
    void assertIsNeedToReplay(final String name, final String variableName, final boolean expected) {
        assertThat(provider.isNeedToReplay(variableName), is(expected));
    }
    
    private static Stream<Arguments> replayArguments() {
        return Stream.of(
                Arguments.of("user variable", "@@tx_isolation", true),
                Arguments.of("connection collation", "COLLATION_CONNECTION", true),
                Arguments.of("discarded system variable", "tx_isolation", false));
    }
}
