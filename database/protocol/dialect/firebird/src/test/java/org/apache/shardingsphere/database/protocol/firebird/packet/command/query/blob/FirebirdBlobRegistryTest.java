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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class FirebirdBlobRegistryTest {
    
    @Test
    void assertBuildSegmentPayloadWhenSegmentMissing() {
        FirebirdBlobRegistry.clearSegment();
        assertNull(FirebirdBlobRegistry.buildSegmentPayload(UnpooledByteBufAllocator.DEFAULT, StandardCharsets.UTF_8));
    }
    
    @Test
    void assertBuildSegmentPayloadWithoutPadding() {
        byte[] segment = {1, 2, 3, 4};
        FirebirdBlobRegistry.setSegment(segment);
        ByteBuf buffer = FirebirdBlobRegistry.buildSegmentPayload(UnpooledByteBufAllocator.DEFAULT, StandardCharsets.UTF_8).getByteBuf();
        assertThat(buffer.readInt(), is(segment.length));
        byte[] actual = new byte[segment.length];
        buffer.readBytes(actual);
        assertThat(actual, is(segment));
        assertThat(buffer.readableBytes(), is(0));
        buffer.release();
    }
    
    @Test
    void assertBuildSegmentPayloadWithPadding() {
        byte[] segment = {5, 6, 7};
        FirebirdBlobRegistry.setSegment(segment);
        ByteBuf buffer = FirebirdBlobRegistry.buildSegmentPayload(UnpooledByteBufAllocator.DEFAULT, StandardCharsets.UTF_8).getByteBuf();
        assertThat(buffer.readInt(), is(segment.length));
        byte[] actual = new byte[segment.length];
        buffer.readBytes(actual);
        assertThat(actual, is(segment));
        assertThat(buffer.readableBytes(), is(1));
        assertThat(buffer.readByte(), is((byte) 0));
        buffer.release();
    }
}
