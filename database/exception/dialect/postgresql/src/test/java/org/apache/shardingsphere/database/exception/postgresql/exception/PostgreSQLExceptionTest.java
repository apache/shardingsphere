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

package org.apache.shardingsphere.database.exception.postgresql.exception;

import org.apache.shardingsphere.database.exception.postgresql.exception.PostgreSQLException.ServerErrorMessage;
import org.apache.shardingsphere.database.exception.postgresql.vendor.PostgreSQLVendorError;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.SQLState;
import org.apache.shardingsphere.infra.exception.external.sql.vendor.VendorError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLExceptionTest {
    
    @Test
    void assertConstructWithMessageAndState() {
        PostgreSQLException actual = new PostgreSQLException("unexpected", "XX000");
        assertThat(actual.getMessage(), is("unexpected"));
        assertThat(actual.getSQLState(), is("XX000"));
        assertNull(actual.getServerErrorMessage());
    }
    
    @Test
    void assertConstructWithServerErrorMessage() {
        ServerErrorMessage serverErrorMessage = new ServerErrorMessage("FATAL", PostgreSQLVendorError.INVALID_CATALOG_NAME, "logic_db");
        PostgreSQLException actual = new PostgreSQLException(serverErrorMessage);
        assertThat(actual.getMessage(), is(serverErrorMessage.toString()));
        assertThat(actual.getSQLState(), is(serverErrorMessage.getSqlState()));
        assertThat(actual.getServerErrorMessage(), is(serverErrorMessage));
    }
    
    @Test
    void assertConstructServerErrorMessage() {
        ServerErrorMessage actual = new ServerErrorMessage("FATAL", PostgreSQLVendorError.INVALID_CATALOG_NAME, "logic_db");
        assertThat(actual.getSeverity(), is("FATAL"));
        assertThat(actual.getSqlState(), is(PostgreSQLVendorError.INVALID_CATALOG_NAME.getSqlState().getValue()));
        assertThat(actual.getMessage(), is(String.format(PostgreSQLVendorError.INVALID_CATALOG_NAME.getReason(), "logic_db")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("toStringArguments")
    void assertToString(final String name, final ServerErrorMessage serverErrorMessage, final String expected) {
        assertThat(serverErrorMessage.toString(), is(expected));
    }
    
    private static Stream<Arguments> toStringArguments() throws ReflectiveOperationException {
        return Stream.of(
                Arguments.of("all_fields_present", createServerErrorMessage("FATAL", "3D000", "logic_db"),
                        "FATAL: database \"logic_db\" does not exist\n  Server SQLState: 3D000"),
                Arguments.of("null_severity", createServerErrorMessage(null, "3D000", "logic_db"),
                        "database \"logic_db\" does not exist\n  Server SQLState: 3D000"),
                Arguments.of("null_message", createServerErrorMessageWithNullMessage(), "FATAL: \n  Server SQLState: 3D000"),
                Arguments.of("null_sql_state", createServerErrorMessage("FATAL", null, "logic_db"),
                        "FATAL: database \"logic_db\" does not exist"));
    }
    
    private static ServerErrorMessage createServerErrorMessage(final String severity, final String sqlStateValue, final Object... reasonArgs) {
        SQLState sqlState = mock(SQLState.class);
        when(sqlState.getValue()).thenReturn(sqlStateValue);
        VendorError vendorError = mock(VendorError.class);
        when(vendorError.getSqlState()).thenReturn(sqlState);
        when(vendorError.getReason()).thenReturn("database \"%s\" does not exist");
        return new ServerErrorMessage(severity, vendorError, reasonArgs);
    }
    
    private static ServerErrorMessage createServerErrorMessageWithNullMessage() throws ReflectiveOperationException {
        ServerErrorMessage result = createServerErrorMessage("FATAL", "3D000", "logic_db");
        Plugins.getMemberAccessor().set(ServerErrorMessage.class.getDeclaredField("message"), result, null);
        return result;
    }
}
