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

package org.apache.shardingsphere.scaling.postgresql;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.scaling.core.job.position.LogPosition;
import org.postgresql.replication.LogSequenceNumber;

/**
 * PostgreSQL wal position.
 */
@Getter
@RequiredArgsConstructor
public class WalPosition implements LogPosition<WalPosition> {
    
    private static final long serialVersionUID = -3498484556749679001L;
    
    private final LogSequenceNumber logSequenceNumber;
    
    @Override
    public final int compareTo(final WalPosition walPosition) {
        if (null == walPosition) {
            return 1;
        }
        long o1 = logSequenceNumber.asLong();
        long o2 = walPosition.getLogSequenceNumber().asLong();
        return Long.compare(o1, o2);
    }
}
