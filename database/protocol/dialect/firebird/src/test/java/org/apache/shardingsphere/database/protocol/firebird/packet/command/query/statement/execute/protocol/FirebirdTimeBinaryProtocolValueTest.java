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

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.util.FirebirdDateTimeUtils;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Time;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdTimeBinaryProtocolValueTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertRead() {
        LocalDateTime dateTime = LocalDateTime.of(1970, 1, 1, 12, 0, 0);
        int encoded = new FirebirdDateTimeUtils(dateTime).getEncodedTime();
        when(payload.readInt4()).thenReturn(encoded);
        assertThat(new FirebirdTimeBinaryProtocolValue().read(payload), is(FirebirdDateTimeUtils.getTime(encoded)));
    }
    
    @Test
    void assertWrite() {
        Time time = Time.valueOf(LocalTime.of(12, 0, 0));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time.getTime()), ZoneId.systemDefault());
        int encoded = new FirebirdDateTimeUtils(localDateTime).getEncodedTime();
        new FirebirdTimeBinaryProtocolValue().write(payload, time);
        verify(payload).writeInt4(encoded);
    }
}
