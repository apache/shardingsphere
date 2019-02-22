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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response;

import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.unit.ExecuteResponseUnit;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.unit.ExecuteUpdateResponseUnit;
import org.apache.shardingsphere.shardingproxy.backend.result.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.common.SuccessResponse;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Execute update response.
 * 
 * @author zhangliang
 */
public final class ExecuteUpdateResponse implements ExecuteResponse {
    
    private final List<Integer> updateCounts = new LinkedList<>();
    
    private final List<Long> lastInsertIds = new LinkedList<>();
    
    public ExecuteUpdateResponse(final Collection<ExecuteResponseUnit> responseUnits) {
        for (ExecuteResponseUnit each : responseUnits) {
            updateCounts.add(((ExecuteUpdateResponseUnit) each).getUpdateCount());
            lastInsertIds.add(((ExecuteUpdateResponseUnit) each).getLastInsertId());
        }
    }
    
    /**
     * Get backend response.
     * 
     * @param isMerge is need merge
     * @return backend response
     */
    public BackendResponse getBackendResponse(final boolean isMerge) {
        return isMerge ? new SuccessResponse(1, mergeUpdateCount(), mergeLastInsertId()) : new SuccessResponse(1, updateCounts.get(0), lastInsertIds.get(0));
    }
    
    private int mergeUpdateCount() {
        int result = 0;
        for (int each : updateCounts) {
            result += each;
        }
        return result;
    }
    
    private long mergeLastInsertId() {
        long result = 0;
        for (long each : lastInsertIds) {
            result = Math.max(result, each);
        }
        return result;
    }
}
