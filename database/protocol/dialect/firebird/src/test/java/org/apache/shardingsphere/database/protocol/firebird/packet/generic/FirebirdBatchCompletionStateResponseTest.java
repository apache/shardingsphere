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

package org.apache.shardingsphere.database.protocol.firebird.packet.generic;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FirebirdBatchCompletionStateResponseTest {
    
    @Test
    void assertWrite() {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        new FirebirdBatchCompletionStateResponse().setHandle(11).setRecordsCount(5L).setUpdateCounts(new int[]{2, -3}).write(payload);
        ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(payload, atLeastOnce()).writeInt4(argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues(),
                is(Arrays.asList(FirebirdCommandPacketType.BATCH_CS.getValue(), 11, 5, 2, 0, 0, 2, -3)));
    }
    
    @Test
    void assertSetUpdateCountsWithNull() {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        new FirebirdBatchCompletionStateResponse().setUpdateCounts(null).write(payload);
        ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(payload, atLeastOnce()).writeInt4(argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues(),
                is(Arrays.asList(FirebirdCommandPacketType.BATCH_CS.getValue(), 0, 0, 0, 0, 0)));
    }
}
