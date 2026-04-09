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

package org.apache.shardingsphere.database.connector.core.exception;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConnectionURLExceptionTest {
    
    @Test
    void assertToSQLException() {
        SQLException actual = new ConnectionURLException(XOpenSQLState.CONNECTION_EXCEPTION, 99, "reason %s", "value") {
            
            private static final long serialVersionUID = -4891045010469730614L;
        }.toSQLException();
        assertThat(actual.getSQLState(), is(XOpenSQLState.CONNECTION_EXCEPTION.getValue()));
        assertThat(actual.getErrorCode(), is(13199));
        assertThat(actual.getMessage(), is("reason value"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getInvalidErrorCodeArguments")
    void assertConstructWithInvalidErrorCode(final String name, final int errorCode) {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> new ConnectionURLException(XOpenSQLState.CONNECTION_EXCEPTION, errorCode, "reason") {
            
            private static final long serialVersionUID = 4755397848530493028L;
        });
        assertThat(actual.getMessage(), is("The value range of error code should be [0, 100)."));
    }
    
    private static Stream<Arguments> getInvalidErrorCodeArguments() {
        return Stream.of(
                Arguments.of("negative_error_code", -1),
                Arguments.of("upper_bound_error_code", 100),
                Arguments.of("above_upper_bound_error_code", 101));
    }
}
