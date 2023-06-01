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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.SocketSinkImporter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CDC ack holder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CDCAckHolder {
    
    private static final CDCAckHolder INSTANCE = new CDCAckHolder();
    
    private final Map<String, Map<SocketSinkImporter, CDCAckPosition>> ackIdPositionMap = new ConcurrentHashMap<>();
    
    /**
     * the ack of CDC.
     *
     * @param ackId ack id
     */
    public void ack(final String ackId) {
        Map<SocketSinkImporter, CDCAckPosition> importerDataRecordMap = ackIdPositionMap.remove(ackId);
        if (null != importerDataRecordMap) {
            importerDataRecordMap.forEach(SocketSinkImporter::ackWithLastDataRecord);
        }
    }
    
    /**
     * Bind ack id.
     *
     * @param importerDataRecordMap import data record map
     * @return ack id
     */
    public String bindAckIdWithPosition(final Map<SocketSinkImporter, CDCAckPosition> importerDataRecordMap) {
        String result = generateAckId();
        // TODO it's might need to persist to registry center in cluster mode.
        ackIdPositionMap.put(result, importerDataRecordMap);
        return result;
    }
    
    private String generateAckId() {
        return "ACK-" + UUID.randomUUID();
    }
    
    /**
     * Clean up.
     *
     * @param socketSinkImporter CDC importer
     */
    public void cleanUp(final SocketSinkImporter socketSinkImporter) {
        if (ackIdPositionMap.isEmpty()) {
            return;
        }
        ackIdPositionMap.entrySet().removeIf(entry -> entry.getValue().containsKey(socketSinkImporter));
    }
    
    /**
     * Get instance.
     *
     * @return CDC ack holder
     */
    public static CDCAckHolder getInstance() {
        return INSTANCE;
    }
}
