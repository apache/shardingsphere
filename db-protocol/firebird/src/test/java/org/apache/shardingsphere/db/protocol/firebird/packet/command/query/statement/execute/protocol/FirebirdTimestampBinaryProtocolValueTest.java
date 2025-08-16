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

package org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.execute.protocol;

import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.execute.protocol.util.FirebirdDateTimeUtils;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdTimestampBinaryProtocolValueTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertRead() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 1, 12, 0);
        int encodedDate = FirebirdDateTimeUtils.getEncodedDate(dateTime);
        int encodedTime = new FirebirdDateTimeUtils(dateTime).getEncodedTime();
        when(payload.readInt4()).thenReturn(encodedDate, encodedTime);
        assertThat(new FirebirdTimestampBinaryProtocolValue().read(payload), is(FirebirdDateTimeUtils.getDateTime(encodedDate, encodedTime)));
    }
    
    @Test
    void assertWrite() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 1, 12, 0);
        new FirebirdTimestampBinaryProtocolValue().write(payload, Timestamp.valueOf(dateTime));
        verify(payload).writeInt4(FirebirdDateTimeUtils.getEncodedDate(dateTime));
        verify(payload).writeInt4(new FirebirdDateTimeUtils(dateTime).getEncodedTime());
    }
}
