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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql;

import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class FirebirdSQLInfoReturnPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertWriteRecords() {
        FirebirdSQLInfoReturnPacket packet = new FirebirdSQLInfoReturnPacket(Arrays.asList(FirebirdSQLInfoPacketType.RECORDS, FirebirdCommonInfoPacketType.END));
        packet.write(payload);
        org.mockito.InOrder io = org.mockito.Mockito.inOrder(payload);
        io.verify(payload).writeInt1(FirebirdSQLInfoPacketType.RECORDS.getCode());
        io.verify(payload).writeInt2LE(0);
        io.verify(payload).writeInt1(FirebirdSQLInfoReturnValue.SELECT.getCode());
        io.verify(payload).writeInt2LE(4);
        io.verify(payload).writeInt4LE(0);
        io.verify(payload).writeInt1(FirebirdSQLInfoReturnValue.INSERT.getCode());
        io.verify(payload).writeInt2LE(4);
        io.verify(payload).writeInt4LE(0);
        io.verify(payload).writeInt1(FirebirdSQLInfoReturnValue.UPDATE.getCode());
        io.verify(payload).writeInt2LE(4);
        io.verify(payload).writeInt4LE(0);
        io.verify(payload).writeInt1(FirebirdSQLInfoReturnValue.DELETE.getCode());
        io.verify(payload).writeInt2LE(4);
        io.verify(payload).writeInt4LE(0);
        io.verify(payload).writeInt1(FirebirdCommonInfoPacketType.END.getCode());
    }
    
    @Test
    void assertParseSQLInfoWithUnknownType() {
        FirebirdSQLInfoReturnPacket packet = new FirebirdSQLInfoReturnPacket(Collections.singletonList(FirebirdSQLInfoPacketType.STMT_TYPE));
        assertThrows(FirebirdProtocolException.class, () -> packet.write(payload));
    }
}
