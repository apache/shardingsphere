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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol;

import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdInt4BinaryProtocolValueTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertRead() {
        when(payload.readInt4()).thenReturn(1);
        new FirebirdInt4BinaryProtocolValue().read(payload);
        verify(payload).readInt4();
    }
    
    @Test
    void assertWriteWithBigDecimal() {
        new FirebirdInt4BinaryProtocolValue().write(payload, BigDecimal.ONE);
        verify(payload).writeInt4(1);
    }
    
    @Test
    void assertWriteWithInteger() {
        new FirebirdInt4BinaryProtocolValue().write(payload, 1);
        verify(payload).writeInt4(1);
    }
    
    @Test
    void assertWriteWithLong() {
        new FirebirdInt4BinaryProtocolValue().write(payload, 1L);
        verify(payload).writeInt4(1);
    }
}
