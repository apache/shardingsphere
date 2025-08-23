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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol;

import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLInt4BinaryProtocolValueTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Test
    void assertGetColumnLength() {
        PostgreSQLInt4BinaryProtocolValue actual = new PostgreSQLInt4BinaryProtocolValue();
        assertThat(actual.getColumnLength(payload, null), is(4));
    }
    
    @Test
    void assertRead() {
        PostgreSQLInt4BinaryProtocolValue actual = new PostgreSQLInt4BinaryProtocolValue();
        when(payload.readInt4()).thenReturn(1);
        assertThat(actual.read(payload, 4), is(1));
    }
    
    @Test
    void assertWrite() {
        PostgreSQLInt4BinaryProtocolValue actual = new PostgreSQLInt4BinaryProtocolValue();
        actual.write(payload, 1);
        verify(payload).writeInt4(1);
    }
}
