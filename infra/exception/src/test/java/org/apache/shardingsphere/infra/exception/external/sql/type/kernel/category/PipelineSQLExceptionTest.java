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

package org.apache.shardingsphere.infra.exception.external.sql.type.kernel.category;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNull;

class PipelineSQLExceptionTest {
    
    @ParameterizedTest(name = "{index}: literal reason={0}")
    @ValueSource(strings = {"foo_reason", "foo_%s_%1$s_%n_%%_%", ""})
    void assertToSQLException(final String reason) {
        SQLException actual = new PipelineSQLException(XOpenSQLState.INVALID_PARAMETER_VALUE, 0, reason) {
        }.toSQLException();
        assertThat(actual.getSQLState(), is(XOpenSQLState.INVALID_PARAMETER_VALUE.getValue()));
        assertThat(actual.getErrorCode(), is(18000));
        assertThat(actual.getMessage(), is(reason));
        assertNull(actual.getCause());
    }
    
    @Test
    void assertToSQLExceptionWithCause() {
        Exception cause = new RuntimeException("foo_cause_%s");
        SQLException actual = new PipelineSQLException(XOpenSQLState.INVALID_PARAMETER_VALUE, 0, "foo_%s_%n_%%_%", cause) {
        }.toSQLException();
        assertThat(actual.getSQLState(), is(XOpenSQLState.INVALID_PARAMETER_VALUE.getValue()));
        assertThat(actual.getErrorCode(), is(18000));
        assertThat(actual.getMessage(), is("foo_%s_%n_%%_%" + System.lineSeparator() + "More details: java.lang.RuntimeException: foo_cause_%s"));
        assertThat(actual.getCause(), sameInstance(cause));
    }
}
