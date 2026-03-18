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

import org.apache.shardingsphere.database.connector.core.jdbcurl.judger.DialectDatabaseInstanceJudger;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class H2DatabaseInstanceJudgerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    private final DialectDatabaseInstanceJudger judger = DatabaseTypedSPILoader.getService(DialectDatabaseInstanceJudger.class, databaseType);
    
    private final ConnectionPropertiesParser parser = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType);
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isInSameDatabaseInstanceArguments")
    void assertIsInSameDatabaseInstance(final String name, final String url1, final String url2, final boolean isSame) {
        assertThat(judger.isInSameDatabaseInstance(parser.parse(url1, null, null), parser.parse(url2, null, null)), is(isSame));
    }
    
    private static Stream<Arguments> isInSameDatabaseInstanceArguments() {
        return Stream.of(
                Arguments.of("memWithMem", "jdbc:h2:mem:ds_0", "jdbc:h2:mem:ds_1", true),
                Arguments.of("memWithPwd", "jdbc:h2:mem:ds_0", "jdbc:h2:~:ds-1", true),
                Arguments.of("memWithFile", "jdbc:h2:mem:ds_0", "jdbc:h2:file:/data/ds-1", true),
                Arguments.of("memWithTcp", "jdbc:h2:mem:ds_0", "jdbc:h2:tcp://localhost:8082/~/test2/test3", false),
                Arguments.of("pwdWithPwd", "jdbc:h2:~:ds-0", "jdbc:h2:~:ds-1", true),
                Arguments.of("pwdWithMem", "jdbc:h2:~:ds-0", "jdbc:h2:mem:ds_1", true),
                Arguments.of("pwdWithFile", "jdbc:h2:~:ds-0", "jdbc:h2:file:/data/ds-1", true),
                Arguments.of("pwdWithTcp", "jdbc:h2:~:ds-0", "jdbc:h2:tcp://localhost:8082/~/test2/test3", false),
                Arguments.of("fileWithFile", "jdbc:h2:file:/data/ds-0", "jdbc:h2:file:/data/ds-1", true),
                Arguments.of("fileWithMem", "jdbc:h2:file:/data/ds-0", "jdbc:h2:mem:ds_1", true),
                Arguments.of("fileWithPwd", "jdbc:h2:file:/data/ds-0", "jdbc:h2:~:ds-1", true),
                Arguments.of("fileWithTcp", "jdbc:h2:file:/data/ds-0", "jdbc:h2:tcp://localhost:8082/~/test2/test3", false),
                Arguments.of("tcpWithTcp", "jdbc:h2:tcp://localhost:8082/~/test1/test2", "jdbc:h2:tcp://localhost:8082/~/test3/test4", true),
                Arguments.of("tcpWithTcpDifferentHost", "jdbc:h2:tcp://localhost:8082/~/test1/test2", "jdbc:h2:tcp://192.168.64.76:8082/~/test3/test4", false),
                Arguments.of("tcpWithSsl", "jdbc:h2:tcp://localhost:8082/~/test1/test2", "jdbc:h2:ssl:127.0.0.1/home/test-two", false));
    }
}
