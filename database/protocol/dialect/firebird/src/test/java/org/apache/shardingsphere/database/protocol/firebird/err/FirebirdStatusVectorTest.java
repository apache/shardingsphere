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

package org.apache.shardingsphere.database.protocol.firebird.err;

import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class FirebirdStatusVectorTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertWriteReflectsChangesToLegacyStringArguments() {
        List<String> arguments = new ArrayList<>();
        FirebirdStatusVector.Segment segment = new FirebirdStatusVector.Segment(ISCConstants.isc_sqlerr, arguments, null);
        arguments.add("later");
        FirebirdStatusVector actual = FirebirdStatusVector.fromSegments(Collections.singletonList(segment));
        actual.write(payload);
        InOrder inOrder = inOrder(payload);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_gds);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_sqlerr);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_string);
        inOrder.verify(payload).writeString("later");
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_end);
        verifyNoMoreInteractions(payload);
    }
    
    @Test
    void assertWriteOrderedStringAndNumberArguments() {
        FirebirdStatusVector.Segment segment = FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_sqlerr, Arrays.asList(
                FirebirdStatusVector.Argument.ofString("before"), FirebirdStatusVector.Argument.ofNumber(-901), FirebirdStatusVector.Argument.ofString("after")), null);
        FirebirdStatusVector actual = FirebirdStatusVector.fromSegments(Collections.singletonList(segment));
        assertThat(actual.getSegments().get(0).getArguments(), is(Arrays.asList("before", "after")));
        actual.write(payload);
        InOrder inOrder = inOrder(payload);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_gds);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_sqlerr);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_string);
        inOrder.verify(payload).writeString("before");
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_number);
        inOrder.verify(payload).writeInt4(-901);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_string);
        inOrder.verify(payload).writeString("after");
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_end);
        verifyNoMoreInteractions(payload);
    }
    
    @Test
    void assertWriteKeepsNativeCodeAndStripsStateSuffix() {
        FirebirdStatusVector vector = new FirebirdStatusVector(new SQLException("prefix; detail [SQLState:42000]", "42000", ISCConstants.isc_arith_except));
        assertThat(vector.getGdsCode(), is(ISCConstants.isc_arith_except));
        assertThat(vector.getErrorMessage(), is("prefix; detail"));
        vector.write(payload);
        InOrder inOrder = inOrder(payload);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_gds);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arith_except);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_string);
        inOrder.verify(payload).writeString("prefix; detail");
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_end);
        verifyNoMoreInteractions(payload);
    }
    
    @Test
    void assertWriteRebuildsDuplicateKeyVectorAsSegments() {
        String message = "violation of PRIMARY or UNIQUE KEY constraint \"INTEG_2\" on table \"MY_TABLE\"; Problematic key value is (\"COL1\" = 1) [SQLState:23000, ISC error code:335544665]";
        FirebirdStatusVector vector = new FirebirdStatusVector(new SQLException(message, "23000", ISCConstants.isc_unique_key_violation));
        assertThat(vector.getGdsCode(), is(ISCConstants.isc_unique_key_violation));
        vector.write(payload);
        InOrder inOrder = inOrder(payload);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_gds);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_unique_key_violation);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_string);
        inOrder.verify(payload).writeString("INTEG_2");
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_string);
        inOrder.verify(payload).writeString("MY_TABLE");
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_gds);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_random);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_string);
        inOrder.verify(payload).writeString("Problematic key value is (\"COL1\" = 1)");
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_end);
        verifyNoMoreInteractions(payload);
    }
    
    @Test
    void assertWriteOmitsArgumentWhenMessageIsEmpty() {
        FirebirdStatusVector vector = new FirebirdStatusVector(new SQLException("", "28000", ISCConstants.isc_login));
        vector.write(payload);
        InOrder inOrder = inOrder(payload);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_gds);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_login);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_end);
        verifyNoMoreInteractions(payload);
    }
    
    @Test
    void assertWriteWithNullMessage() {
        FirebirdStatusVector vector = new FirebirdStatusVector(new SQLException(null, "28000", ISCConstants.isc_login));
        assertThat(vector.getErrorMessage(), is(""));
        vector.write(payload);
        InOrder inOrder = inOrder(payload);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_gds);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_login);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_end);
        verifyNoMoreInteractions(payload);
    }
    
    @Test
    void assertWriteUsesRandomCodeWhenErrorCodeIsLowerThanArithExcept() {
        FirebirdStatusVector vector = new FirebirdStatusVector(new SQLException("plain", "00000", ISCConstants.isc_arith_except - 1));
        assertThat(vector.getGdsCode(), is(ISCConstants.isc_random));
        vector.write(payload);
        InOrder inOrder = inOrder(payload);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_gds);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_random);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_string);
        inOrder.verify(payload).writeString("plain");
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_end);
        verifyNoMoreInteractions(payload);
    }
    
    @Test
    void assertWriteFailSafeUsesRandomCodeAndKeepsSqlStateForUnmatchedParameterizedTemplate() {
        FirebirdStatusVector vector = new FirebirdStatusVector(new SQLException("totally unrelated text", "23000", ISCConstants.isc_unique_key_violation));
        assertThat(vector.getGdsCode(), is(ISCConstants.isc_random));
        vector.write(payload);
        InOrder inOrder = inOrder(payload);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_gds);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_random);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_string);
        inOrder.verify(payload).writeString("totally unrelated text");
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_sql_state);
        inOrder.verify(payload).writeString("23000");
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_end);
        verifyNoMoreInteractions(payload);
    }
}
