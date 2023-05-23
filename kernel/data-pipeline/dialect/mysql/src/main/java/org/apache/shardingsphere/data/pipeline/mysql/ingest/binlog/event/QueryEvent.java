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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Query event.This event is written into the binary log file for:
 * 1. STATEMENT based replication (updating statements)
 * 2. DDLs
 * 3. COMMIT related to non transactional engines (MyISAM, BLACKHOLE etc).
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_replication_binlog_event.html#sect_protocol_replication_event_query">QUERY_EVENT</a>
 */
@Getter
@RequiredArgsConstructor
public final class QueryEvent extends AbstractBinlogEvent {
    
    private final long threadId;
    
    private final long executionTime;
    
    private final int errorCode;
    
    private final String databaseName;
    
    private final String sql;
}
