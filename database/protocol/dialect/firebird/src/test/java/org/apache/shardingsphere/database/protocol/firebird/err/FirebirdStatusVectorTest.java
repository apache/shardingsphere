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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class FirebirdStatusVectorTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertWriteWithArithmeticCodeAndTrimmedMessage() {
        SQLException richMessage = new SQLException("prefix; detail [SQLState:42000]", "42000", ISCConstants.isc_arith_except);
        FirebirdStatusVector vector = new FirebirdStatusVector(richMessage);
        assertThat(vector.getGdsCode(), is(richMessage.getErrorCode()));
        assertThat(vector.getErrorMessage(), is("detail"));
        vector.write(payload);
        InOrder inOrder = inOrder(payload);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_gds);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arith_except);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_string);
        inOrder.verify(payload).writeString("detail");
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_end);
        verifyNoMoreInteractions(payload);
    }
    
    @Test
    void assertWriteUsesRandomCodeWhenErrorCodeIsLowerThanArithExcept() {
        SQLException plainMessage = new SQLException("plain", "00000", ISCConstants.isc_arith_except - 1);
        FirebirdStatusVector vector = new FirebirdStatusVector(plainMessage);
        vector.write(payload);
        InOrder inOrder = inOrder(payload);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_gds);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_random);
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_string);
        inOrder.verify(payload).writeString("plain");
        inOrder.verify(payload).writeInt4(ISCConstants.isc_arg_end);
        verifyNoMoreInteractions(payload);
    }
}
