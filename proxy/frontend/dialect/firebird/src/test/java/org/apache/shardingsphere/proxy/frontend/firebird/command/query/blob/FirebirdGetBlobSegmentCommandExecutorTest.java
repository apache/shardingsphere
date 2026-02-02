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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdBlobRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdGetBlobSegmentCommandExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdGetBlobSegmentCommandExecutorTest {
    
    @Mock
    private FirebirdGetBlobSegmentCommandPacket packet;
    
    private FirebirdGetBlobSegmentCommandExecutor executor;
    
    @AfterEach
    void tearDown() {
        FirebirdBlobRegistry.clearSegment();
    }
    
    @Test
    void assertExecuteWithNullSegment() {
        FirebirdBlobRegistry.clearSegment();
        when(packet.getBlobHandle()).thenReturn(7);
        executor = new FirebirdGetBlobSegmentCommandExecutor(packet);
        Collection<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(1));
        DatabasePacket actualResponsePacket = actualPackets.iterator().next();
        assertThat(actualResponsePacket, isA(FirebirdGenericResponsePacket.class));
        FirebirdGenericResponsePacket actualGenericPacket = (FirebirdGenericResponsePacket) actualResponsePacket;
        assertThat(actualGenericPacket.getHandle(), is(7));
        assertThat(actualGenericPacket.getData(), isA(FirebirdGetBlobSegmentResponsePacket.class));
        byte[] actualSegment = getResponseSegment(actualGenericPacket);
        assertThat(actualSegment.length, is(0));
        assertNull(FirebirdBlobRegistry.getSegment());
    }
    
    @Test
    void assertExecuteWithZeroRequestedLength() {
        FirebirdBlobRegistry.setSegment(new byte[]{9, 8});
        when(packet.getSegmentLength()).thenReturn(0);
        when(packet.getBlobHandle()).thenReturn(11);
        executor = new FirebirdGetBlobSegmentCommandExecutor(packet);
        Collection<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(1));
        DatabasePacket actualResponsePacket = actualPackets.iterator().next();
        assertThat(actualResponsePacket, isA(FirebirdGenericResponsePacket.class));
        FirebirdGenericResponsePacket actualGenericPacket = (FirebirdGenericResponsePacket) actualResponsePacket;
        assertThat(actualGenericPacket.getHandle(), is(11));
        byte[] actualSegment = getResponseSegment(actualGenericPacket);
        assertThat(actualSegment.length, is(0));
        byte[] actualRemainingSegment = FirebirdBlobRegistry.getSegment();
        assertThat(actualRemainingSegment.length, is(2));
        assertThat(actualRemainingSegment[0], is((byte) 9));
        assertThat(actualRemainingSegment[1], is((byte) 8));
    }
    
    @Test
    void assertExecuteWithPartialSegmentLeft() {
        FirebirdBlobRegistry.setSegment(new byte[]{1, 2, 3});
        when(packet.getSegmentLength()).thenReturn(2);
        when(packet.getBlobHandle()).thenReturn(99);
        executor = new FirebirdGetBlobSegmentCommandExecutor(packet);
        Collection<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(1));
        DatabasePacket actualResponsePacket = actualPackets.iterator().next();
        assertThat(actualResponsePacket, isA(FirebirdGenericResponsePacket.class));
        FirebirdGenericResponsePacket actualGenericPacket = (FirebirdGenericResponsePacket) actualResponsePacket;
        assertThat(actualGenericPacket.getHandle(), is(99));
        byte[] actualSegment = getResponseSegment(actualGenericPacket);
        assertThat(actualSegment.length, is(2));
        assertThat(actualSegment[0], is((byte) 1));
        assertThat(actualSegment[1], is((byte) 2));
        byte[] actualRemainingSegment = FirebirdBlobRegistry.getSegment();
        assertThat(actualRemainingSegment.length, is(1));
        assertThat(actualRemainingSegment[0], is((byte) 3));
    }
    
    @Test
    void assertExecuteWithSegmentCleared() {
        FirebirdBlobRegistry.setSegment(new byte[]{4, 5});
        when(packet.getSegmentLength()).thenReturn(4);
        when(packet.getBlobHandle()).thenReturn(15);
        executor = new FirebirdGetBlobSegmentCommandExecutor(packet);
        Collection<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(1));
        DatabasePacket actualResponsePacket = actualPackets.iterator().next();
        assertThat(actualResponsePacket, isA(FirebirdGenericResponsePacket.class));
        FirebirdGenericResponsePacket actualGenericPacket = (FirebirdGenericResponsePacket) actualResponsePacket;
        assertThat(actualGenericPacket.getHandle(), is(15));
        byte[] actualSegment = getResponseSegment(actualGenericPacket);
        assertThat(actualSegment.length, is(2));
        assertThat(actualSegment[0], is((byte) 4));
        assertThat(actualSegment[1], is((byte) 5));
        assertNull(FirebirdBlobRegistry.getSegment());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private byte[] getResponseSegment(final FirebirdGenericResponsePacket responsePacket) {
        return (byte[]) Plugins.getMemberAccessor().get(FirebirdGetBlobSegmentResponsePacket.class.getDeclaredField("segment"), responsePacket.getData());
    }
}
