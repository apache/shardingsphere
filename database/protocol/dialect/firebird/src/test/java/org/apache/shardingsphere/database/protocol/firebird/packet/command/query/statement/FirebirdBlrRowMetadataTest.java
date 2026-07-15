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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdBlrRowMetadataTest {
    
    @Test
    void assertParseBLRWhenEmpty() {
        FirebirdBlrRowMetadata actual = FirebirdBlrRowMetadata.parseBLR(Unpooled.EMPTY_BUFFER);
        assertThat(actual.getLength(), is(0));
        assertTrue(actual.getColumnTypes().isEmpty());
    }
    
    @Test
    void assertParseBLRWithMultipleTypes() {
        FirebirdBlrRowMetadata actual = FirebirdBlrRowMetadata.parseBLR(createBlr());
        assertThat(actual.getLength(), is(21));
        assertThat(actual.getColumnTypes(), is(Arrays.asList(FirebirdBinaryColumnType.LONG, FirebirdBinaryColumnType.VARYING, FirebirdBinaryColumnType.DOUBLE)));
    }
    
    @Test
    void assertParseBLRKeepsReaderIndex() {
        ByteBuf blr = Unpooled.buffer().writeByte(99);
        blr.writeBytes(createBlr());
        blr.readerIndex(1);
        FirebirdBlrRowMetadata.parseBLR(blr);
        assertThat(blr.readerIndex(), is(1));
    }
    
    @Test
    void assertParseBLRWhenTruncated() {
        ByteBuf blr = Unpooled.buffer().writeZero(4);
        assertThrows(IndexOutOfBoundsException.class, () -> FirebirdBlrRowMetadata.parseBLR(blr));
    }
    
    private ByteBuf createBlr() {
        return Unpooled.buffer()
                .writeZero(4)
                .writeByte(6)
                .writeByte(0)
                .writeByte(BlrConstants.blr_long)
                .writeZero(3)
                .writeByte(BlrConstants.blr_varying2)
                .writeZero(6)
                .writeByte(BlrConstants.blr_double)
                .writeZero(2)
                .writeByte(BlrConstants.blr_end);
    }
}
