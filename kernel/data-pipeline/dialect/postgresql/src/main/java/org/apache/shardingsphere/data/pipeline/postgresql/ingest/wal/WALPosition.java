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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.BaseLogSequenceNumber;

/**
 * WAL position.
 */
@RequiredArgsConstructor
@Getter
public final class WALPosition implements IngestPosition<WALPosition> {
    
    private final BaseLogSequenceNumber logSequenceNumber;
    
    @Override
    public int compareTo(final WALPosition position) {
        return null == position ? 1 : Long.compare(logSequenceNumber.asLong(), position.logSequenceNumber.asLong());
    }
    
    @Override
    public String toString() {
        return String.valueOf(logSequenceNumber.asLong());
    }
}
