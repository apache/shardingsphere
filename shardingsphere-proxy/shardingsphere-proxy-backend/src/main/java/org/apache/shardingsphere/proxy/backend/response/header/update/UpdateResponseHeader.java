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

package org.apache.shardingsphere.proxy.backend.response.header.update;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Update response header.
 */
@Getter
public final class UpdateResponseHeader implements ResponseHeader {
    
    private final SQLStatement sqlStatement;
    
    private final long lastInsertId;
    
    @Getter(AccessLevel.NONE)
    private final Collection<Integer> updateCounts = new LinkedList<>();
    
    private long updateCount;
    
    public UpdateResponseHeader(final SQLStatement sqlStatement) {
        this(sqlStatement, Collections.emptyList());
    }
    
    public UpdateResponseHeader(final SQLStatement sqlStatement, final Collection<ExecuteResult> executeResults) {
        this.sqlStatement = sqlStatement;
        lastInsertId = getLastInsertId(executeResults);
        updateCount = executeResults.iterator().hasNext() ? ((UpdateResult) executeResults.iterator().next()).getUpdateCount() : 0;
        for (ExecuteResult each : executeResults) {
            updateCounts.add(((UpdateResult) each).getUpdateCount());
        }
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
