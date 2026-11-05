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

package org.apache.shardingsphere.infra.exception.external.sql.type.kernel;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNull;

class KernelSQLExceptionTest {
    
    @Test
    void assertToSQLException() {
        SQLException actual = new KernelSQLException(XOpenSQLState.GENERAL_ERROR, 1, 1, "reason") {
        }.toSQLException();
        assertThat(actual.getSQLState(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actual.getErrorCode(), is(11001));
        assertThat(actual.getMessage(), is("reason"));
        assertNull(actual.getCause());
    }
    
    @Test
    void assertToSQLExceptionWithCause() {
        Exception cause = new RuntimeException("test");
        SQLException actual = new KernelSQLException(XOpenSQLState.GENERAL_ERROR, 1, 1, cause, "reason") {
        }.toSQLException();
        assertThat(actual.getSQLState(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actual.getErrorCode(), is(11001));
        assertThat(actual.getMessage(), is("reason" + System.lineSeparator() + "More details: java.lang.RuntimeException: test"));
        assertThat(actual.getCause(), is(cause));
    }
    
    @ParameterizedTest(name = "{index}: literal reason={0}")
    @ValueSource(strings = {"foo_reason", "foo_%s_%1$s_%n_%%_%", ""})
    void assertToSQLExceptionWithLiteralReason(final String reason) {
        SQLException actual = new KernelSQLException(reason, XOpenSQLState.GENERAL_ERROR, 1, 1, null) {
        }.toSQLException();
        assertThat(actual.getSQLState(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actual.getErrorCode(), is(11001));
        assertThat(actual.getMessage(), is(reason));
        assertNull(actual.getCause());
    }
    
    @Test
    void assertToSQLExceptionWithLiteralReasonAndCause() {
        Exception cause = new RuntimeException("foo_cause_%s");
        SQLException actual = new KernelSQLException("foo_%s_%n_%%_%", XOpenSQLState.GENERAL_ERROR, 1, 1, cause) {
        }.toSQLException();
        assertThat(actual.getSQLState(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actual.getErrorCode(), is(11001));
        assertThat(actual.getMessage(), is("foo_%s_%n_%%_%" + System.lineSeparator() + "More details: java.lang.RuntimeException: foo_cause_%s"));
        assertThat(actual.getCause(), sameInstance(cause));
    }
    
    @Test
    void assertToSQLExceptionWithExceptionMessageArgument() {
        Exception messageArg = new RuntimeException("foo_argument");
        SQLException actual = new KernelSQLException(XOpenSQLState.GENERAL_ERROR, 1, 1, "foo_%s", messageArg) {
        }.toSQLException();
        assertThat(actual.getMessage(), is("foo_java.lang.RuntimeException: foo_argument"));
        assertNull(actual.getCause());
    }
    
    @Test
    void assertToSQLExceptionWithCauseAndMessageArguments() {
        Exception cause = new RuntimeException("foo_cause");
        SQLException actual = new KernelSQLException(XOpenSQLState.GENERAL_ERROR, 1, 1, cause, "foo_%s_%d", "bar_value", 2) {
        }.toSQLException();
        assertThat(actual.getMessage(), is("foo_bar_value_2" + System.lineSeparator() + "More details: java.lang.RuntimeException: foo_cause"));
        assertThat(actual.getCause(), sameInstance(cause));
    }
}
