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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.MySQLBinlogTableMapEventPacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global table map event mapping.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalTableMapEventMapping {
    
    private static final Map<String, Map<Long, MySQLBinlogTableMapEventPacket>> TABLE_MAP_EVENT_MAPPING = new ConcurrentHashMap<>();
    
    /**
     * Get table map event map by database URL.
     *
     * @param databaseURL database URL
     * @return table map event map
     */
    public static Map<Long, MySQLBinlogTableMapEventPacket> getTableMapEventMap(final String databaseURL) {
        return TABLE_MAP_EVENT_MAPPING.computeIfAbsent(databaseURL, key -> new ConcurrentHashMap<>());
    }
}
