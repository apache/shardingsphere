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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare;

import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MySQLComStmtPrepareOKPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    void assertWrite() {
        MySQLComStmtPrepareOKPacket actual = new MySQLComStmtPrepareOKPacket(1, 0, 1, 0);
        actual.write(payload);
        verify(payload).writeInt1(0x00);
        verify(payload, times(2)).writeInt2(0);
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(1);
        verify(payload).writeReserved(1);
    }
}
