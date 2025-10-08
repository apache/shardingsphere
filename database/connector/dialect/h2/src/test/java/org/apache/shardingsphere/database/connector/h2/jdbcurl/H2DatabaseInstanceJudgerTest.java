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
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class H2DatabaseInstanceJudgerTest {
    
    private final DialectDatabaseInstanceJudger judger = DatabaseTypedSPILoader.getService(DialectDatabaseInstanceJudger.class, TypedSPILoader.getService(DatabaseType.class, "H2"));
    
    private final ConnectionPropertiesParser parser = new H2ConnectionPropertiesParser();
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(H2DatabaseInstanceJudgerTest.IsInSameDatabaseInstanceTestCaseArgumentsProvider.class)
    void assertIsInSameDatabaseInstance(final String name, final String url1, final String url2, final boolean isSame) {
        ConnectionProperties actual1 = parser.parse(url1, null, null);
        ConnectionProperties actual2 = parser.parse(url2, null, null);
        assertThat(judger.isInSameDatabaseInstance(actual1, actual2), is(isSame));
    }
    
    private static final class IsInSameDatabaseInstanceTestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
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
