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

package org.apache.shardingsphere.proxy.backend.response.update;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Update response.
 */
public final class UpdateResponse implements BackendResponse {
    
    private final List<Integer> updateCounts = new LinkedList<>();
    
    @Getter
    private final long lastInsertId;
    
    @Getter
    private long updateCount;
    
    @Getter
    @Setter
    private String type;
    
    public UpdateResponse() {
        this(Collections.emptyList());
    }
    
    public UpdateResponse(final Collection<ExecuteResult> executeResults) {
        for (ExecuteResult each : executeResults) {
            updateCount = ((UpdateResult) each).getUpdateCount();
            updateCounts.add(((UpdateResult) each).getUpdateCount());
        }
        lastInsertId = getLastInsertId(executeResults);
    }
    
    private long getLastInsertId(final Collection<ExecuteResult> executeResults) {
        long result = 0;
        for (ExecuteResult each : executeResults) {
            result = Math.max(result, ((UpdateResult) each).getLastInsertId());
        }
        return result;
    }
    
    /**
     * Merge updated counts.
     */
    public void mergeUpdateCount() {
        updateCount = 0;
        for (int each : updateCounts) {
            updateCount += each;
        }
    }
}
