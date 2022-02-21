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

package org.apache.shardingsphere.db.protocol.mysql.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * MySQL binlog event flag.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/binlog-event-flag.html">Binlog Event Flag</a>
 */
@RequiredArgsConstructor
@Getter
public enum MySQLBinlogEventFlag {
    
    LOG_EVENT_BINLOG_IN_USE_F(0x0001),
    
    LOG_EVENT_FORCED_ROTATE_F(0x0002),
    
    LOG_EVENT_THREAD_SPECIFIC_F(0x0004),
    
    LOG_EVENT_SUPPRESS_USE_F(0x0008),
    
    LOG_EVENT_UPDATE_TABLE_MAP_VERSION_F(0x0010),
    
    LOG_EVENT_ARTIFICIAL_F(0x0020),
    
    LOG_EVENT_RELAY_LOG_F(0x0040),
    
    LOG_EVENT_IGNORABLE_F(0x0080),
    
    LOG_EVENT_NO_FILTER_F(0x0100),
    
    LOG_EVENT_MTS_ISOLATE_F(0x0200);
    
    private final int value;
}
