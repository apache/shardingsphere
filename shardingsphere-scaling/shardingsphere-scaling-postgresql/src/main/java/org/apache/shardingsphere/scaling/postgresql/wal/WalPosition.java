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

package org.apache.shardingsphere.scaling.postgresql.wal;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.scaling.core.job.position.Position;
import org.postgresql.replication.LogSequenceNumber;

/**
 * PostgreSQL wal position.
 */
@RequiredArgsConstructor
@Getter
public class WalPosition implements Position<WalPosition> {
    
    private static final long serialVersionUID = -3498484556749679001L;
    
    private static final Gson GSON = new Gson();
    
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
    
    @Override
    public JsonElement toJson() {
        return GSON.toJsonTree(logSequenceNumber.asLong());
    }
}
