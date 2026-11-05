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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction;

import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdStatusVector.Segment;
import org.firebirdsql.gds.GDSExceptionHelper;
import org.firebirdsql.gds.GDSExceptionHelper.GDSMessage;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jdbc.FBSQLExceptionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdTransactionErrorResolverTest {
    
    @ParameterizedTest
    @ValueSource(ints = {ISCConstants.isc_tpb_reserv_long_tlen, ISCConstants.isc_tpb_reserv_missing_tname, ISCConstants.isc_tpb_reserv_corrup_tlen,
            ISCConstants.isc_tpb_missing_value, ISCConstants.isc_tpb_corrupt_len, ISCConstants.isc_tpb_overflow_len, ISCConstants.isc_tpb_invalid_value})
    void assertNumericArgumentsRemainNumeric(final int code) {
        FBSQLExceptionInfo first = info(ISCConstants.isc_bad_tpb_content);
        first.setNextException(info(code, "2", "isc_tpb_lock_timeout"));
        List<Segment> actual = FirebirdTransactionErrorResolver.resolve(error(first)).get().getSegments();
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getGdsCode(), is(ISCConstants.isc_bad_tpb_content));
        assertThat(actual.get(1).getGdsCode(), is(code));
        assertThat(actual.get(1).getTypedArguments().get(0).getType(), is(ISCConstants.isc_arg_number));
        assertThat(actual.get(1).getTypedArguments().get(0).getNumber(), is(2));
        assertThat(actual.get(1).getTypedArguments().get(1).getText(), is("isc_tpb_lock_timeout"));
    }
    
    @Test
    void assertVersionErrorSegments() {
        FBSQLExceptionInfo first = info(ISCConstants.isc_bad_tpb_form);
        first.setNextException(info(ISCConstants.isc_wrotpbver));
        List<Segment> actual = FirebirdTransactionErrorResolver.resolve(error(first)).get().getSegments();
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getGdsCode(), is(ISCConstants.isc_bad_tpb_form));
        assertThat(actual.get(1).getGdsCode(), is(ISCConstants.isc_wrotpbver));
        assertTrue(actual.get(1).getTypedArguments().isEmpty());
    }
    
    @Test
    void assertZeroSnapshotArgument() {
        FBSQLExceptionInfo first = new FBSQLExceptionInfo("invalid parameter in transaction parameter block; isc_tpb_at_snapshot_number", "HY000", ISCConstants.isc_bad_tpb_content);
        Segment actual = FirebirdTransactionErrorResolver.resolve(error(first)).get().getSegments().get(0);
        assertThat(actual.getGdsCode(), is(ISCConstants.isc_bad_tpb_content));
        assertThat(actual.getTypedArguments().size(), is(1));
        assertThat(actual.getTypedArguments().get(0).getText(), is("isc_tpb_at_snapshot_number"));
    }
    
    @Test
    void assertReservationTableNameIsPreserved() {
        String table = "quoted; table [SQLState: {0}]";
        FBSQLExceptionInfo first = info(ISCConstants.isc_bad_tpb_content);
        first.setNextException(info(ISCConstants.isc_tpb_reserv_relnotfound, table, "isc_tpb_lock_read"));
        Segment actual = FirebirdTransactionErrorResolver.resolve(error(first)).get().getSegments().get(1);
        assertThat(actual.getGdsCode(), is(ISCConstants.isc_tpb_reserv_relnotfound));
        assertThat(actual.getArguments(), is(Arrays.asList(table, "isc_tpb_lock_read")));
    }
    
    @Test
    void assertUnrecognizedCauseIsNotReconstructed() {
        assertFalse(FirebirdTransactionErrorResolver.resolve(new SQLException("failure")).isPresent());
        FBSQLExceptionInfo first = info(ISCConstants.isc_bad_tpb_content);
        first.setNextException(new SQLException("failure"));
        assertFalse(FirebirdTransactionErrorResolver.resolve(error(first)).isPresent());
    }
    
    private static FBSQLExceptionInfo info(final int code, final String... arguments) {
        GDSMessage message = GDSExceptionHelper.getMessage(code);
        message.setParameters(Arrays.asList(arguments));
        return new FBSQLExceptionInfo(message.toString(), "HY000", code);
    }
    
    private static SQLException error(final FBSQLExceptionInfo cause) {
        SQLException result = new SQLException("backend", "HY000", cause.getErrorCode());
        result.initCause(cause);
        return result;
    }
}
