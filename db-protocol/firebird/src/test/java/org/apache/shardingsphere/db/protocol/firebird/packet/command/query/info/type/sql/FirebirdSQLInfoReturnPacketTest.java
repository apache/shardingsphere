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

package org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdSQLInfoReturnPacketTest {
    
    @Test
    void assertWriteRecords() {
        FirebirdSQLInfoReturnPacket packet = new FirebirdSQLInfoReturnPacket(Arrays.asList(FirebirdSQLInfoPacketType.RECORDS, FirebirdCommonInfoPacketType.END));
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        packet.write(payload);
        payload.getByteBuf().readerIndex(0);
        FirebirdPacketPayload result = new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8);
        assertThat(result.readInt1(), is(FirebirdSQLInfoPacketType.RECORDS.getCode()));
        assertThat(result.readInt2LE(), is(0));
        assertThat(result.readInt1(), is(FirebirdSQLInfoReturnValue.SELECT.getCode()));
        assertThat(result.readInt2LE(), is(4));
        assertThat(result.readInt4(), is(0));
        assertThat(result.readInt1(), is(FirebirdSQLInfoReturnValue.INSERT.getCode()));
        assertThat(result.readInt2LE(), is(4));
        assertThat(result.readInt4(), is(0));
        assertThat(result.readInt1(), is(FirebirdSQLInfoReturnValue.UPDATE.getCode()));
        assertThat(result.readInt2LE(), is(4));
        assertThat(result.readInt4(), is(0));
        assertThat(result.readInt1(), is(FirebirdSQLInfoReturnValue.DELETE.getCode()));
        assertThat(result.readInt2LE(), is(4));
        assertThat(result.readInt4(), is(0));
        assertThat(result.readInt1(), is(FirebirdCommonInfoPacketType.END.getCode()));
    }
    
    @Test
    void assertUnknownTypeThrowsException() {
        FirebirdSQLInfoReturnPacket packet = new FirebirdSQLInfoReturnPacket(Collections.singletonList(FirebirdSQLInfoPacketType.STMT_TYPE));
        assertThrows(FirebirdProtocolException.class, () -> packet.write(new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8)));
    }
}
