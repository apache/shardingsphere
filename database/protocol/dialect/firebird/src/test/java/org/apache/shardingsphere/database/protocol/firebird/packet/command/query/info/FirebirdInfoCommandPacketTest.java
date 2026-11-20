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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob.FirebirdBlobInfoItems;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoItems;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.database.FirebirdDatabaseInfoItems;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoItems;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdInfoCommandPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    void assertGetLength() {
        when(payload.getBufferLength(12)).thenReturn(8);
        assertThat(FirebirdInfoCommandPacket.getLength(payload), is(24));
    }
    
    @Test
    void assertNewInstanceWithDatabaseItems() {
        assertNewInstance(FirebirdCommandPacketType.INFO_DATABASE, FirebirdDatabaseInfoItems.PAGE_SIZE);
    }
    
    @Test
    void assertNewInstanceWithBlobItems() {
        assertNewInstance(FirebirdCommandPacketType.INFO_BLOB, FirebirdBlobInfoItems.TOTAL_LENGTH);
    }
    
    @Test
    void assertNewInstanceWithSQLItems() {
        assertNewInstance(FirebirdCommandPacketType.INFO_SQL, FirebirdSQLInfoItems.RECORDS);
    }
    
    @Test
    void assertNewInstanceWithCommonItemFallback() {
        stubPayload(FirebirdSQLInfoItems.RECORDS.getCode(), FirebirdCommonInfoItems.TRUNCATED.getCode());
        FirebirdInfoCommandPacket actual = new FirebirdInfoCommandPacket(FirebirdCommandPacketType.INFO_SQL, payload);
        assertThat(actual.getInfoObject(), is(1));
        assertThat(actual.getIncarnation(), is(2));
        assertThat(actual.getBufferLength(), is(100));
        List<FirebirdInfoItem> actualItems = actual.getItems();
        assertThat(actualItems.size(), is(2));
        assertThat(actualItems.get(0), is(FirebirdSQLInfoItems.RECORDS));
        assertThat(actualItems.get(1), is(FirebirdCommonInfoItems.TRUNCATED));
    }
    
    @Test
    void assertNewInstanceWithUnsupportedCommandType() {
        when(payload.readInt4()).thenReturn(1, 2);
        when(payload.readBuffer()).thenReturn(byteBuf);
        assertThrows(IllegalArgumentException.class, () -> new FirebirdInfoCommandPacket(FirebirdCommandPacketType.VOID, payload));
    }
    
    private void assertNewInstance(final FirebirdCommandPacketType commandPacketType, final FirebirdInfoItem expectedItem) {
        stubPayload(expectedItem.getCode(), FirebirdCommonInfoItems.END.getCode());
        FirebirdInfoCommandPacket actual = new FirebirdInfoCommandPacket(commandPacketType, payload);
        assertThat(actual.getInfoObject(), is(1));
        assertThat(actual.getIncarnation(), is(2));
        assertThat(actual.getBufferLength(), is(100));
        List<FirebirdInfoItem> actualItems = actual.getItems();
        assertThat(actualItems.size(), is(2));
        assertThat(actualItems.get(0), is(expectedItem));
        assertThat(actualItems.get(1), is(FirebirdCommonInfoItems.END));
    }
    
    private void stubPayload(final int... itemCodes) {
        when(payload.readInt4()).thenReturn(1, 2, 100);
        when(payload.readBuffer()).thenReturn(byteBuf);
        when(byteBuf.isReadable()).thenReturn(true, true, false);
        AtomicInteger itemIndex = new AtomicInteger();
        when(byteBuf.readByte()).thenAnswer(invocation -> (byte) itemCodes[itemIndex.getAndIncrement()]);
    }
}
