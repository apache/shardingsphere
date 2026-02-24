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

package org.apache.shardingsphere.database.connector.core.spi;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseTypedSPITest {
    
    private final DatabaseTypedSPI databaseTypedSPI = mock(DatabaseTypedSPI.class, CALLS_REAL_METHODS);
    
    @Test
    void assertGetDatabaseType() {
        assertNull(databaseTypedSPI.getDatabaseType());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTypeArguments")
    void assertGetType(final String name, final String databaseType, final String expectedType) {
        when(databaseTypedSPI.getDatabaseType()).thenReturn(databaseType);
        DatabaseType actualDatabaseType = databaseTypedSPI.getType();
        if (null != actualDatabaseType) {
            assertThat(actualDatabaseType.getType(), is(expectedType));
        }
    }
    
    private static Stream<Arguments> getTypeArguments() {
        return Stream.of(
                Arguments.of("database type is null", null, null),
                Arguments.of("database type is trunk", "TRUNK", "TRUNK"),
                Arguments.of("database type is branch", "BRANCH", "BRANCH"),
                Arguments.of("database type is unknown then fallback to default", "UNKNOWN", "TRUNK"));
    }
}
