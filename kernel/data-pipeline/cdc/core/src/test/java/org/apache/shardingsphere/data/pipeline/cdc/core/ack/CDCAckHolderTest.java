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

package org.apache.shardingsphere.data.pipeline.cdc.core.ack;

import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.SocketSinkImporter;
import org.apache.shardingsphere.infra.util.reflection.ReflectionUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public final class CDCAckHolderTest {
    
    @Test
    public void assertBindAckIdWithPositionAndAck() {
        CDCAckHolder cdcAckHolder = CDCAckHolder.getInstance();
        final Map<SocketSinkImporter, CDCAckPosition> importerDataRecordMap = new HashMap<>();
        SocketSinkImporter socketSinkImporter = mock(SocketSinkImporter.class);
        importerDataRecordMap.put(socketSinkImporter, new CDCAckPosition(new FinishedRecord(new FinishedPosition()), 0));
        Optional<Map<String, Map<SocketSinkImporter, CDCAckPosition>>> ackIdPositionMap = ReflectionUtil.getFieldValue(cdcAckHolder, "ackIdPositionMap");
        assertTrue(ackIdPositionMap.isPresent());
        assertTrue(ackIdPositionMap.get().isEmpty());
        String ackId = cdcAckHolder.bindAckIdWithPosition(importerDataRecordMap);
        assertThat(ackIdPositionMap.get().size(), is(1));
        cdcAckHolder.ack(ackId);
        assertTrue(ackIdPositionMap.get().isEmpty());
    }
    
    @Test
    public void assertCleanUpTimeoutAckId() {
        CDCAckHolder cdcAckHolder = CDCAckHolder.getInstance();
        final Map<SocketSinkImporter, CDCAckPosition> importerDataRecordMap = new HashMap<>();
        SocketSinkImporter socketSinkImporter = mock(SocketSinkImporter.class);
        importerDataRecordMap.put(socketSinkImporter, new CDCAckPosition(new FinishedRecord(new FinishedPosition()), 0, System.currentTimeMillis() - 60 * 1000 * 10));
        cdcAckHolder.bindAckIdWithPosition(importerDataRecordMap);
        cdcAckHolder.cleanUp(socketSinkImporter);
        Optional<Map<String, Map<SocketSinkImporter, CDCAckPosition>>> ackIdPositionMap = ReflectionUtil.getFieldValue(cdcAckHolder, "ackIdPositionMap");
        assertTrue(ackIdPositionMap.isPresent());
        assertTrue(ackIdPositionMap.get().isEmpty());
    }
}
