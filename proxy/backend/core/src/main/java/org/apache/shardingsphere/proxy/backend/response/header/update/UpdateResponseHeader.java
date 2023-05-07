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
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
        this(sqlStatement, Collections.emptyList(), Collections.emptyList());
    }
    
    public UpdateResponseHeader(final SQLStatement sqlStatement, final Collection<UpdateResult> updateResults) {
        this(sqlStatement, updateResults, Collections.emptyList());
    }
    
    public UpdateResponseHeader(final SQLStatement sqlStatement, final Collection<UpdateResult> updateResults, final Collection<Comparable<?>> autoIncrementGeneratedValues) {
        this.sqlStatement = sqlStatement;
        lastInsertId = getLastInsertId(updateResults, autoIncrementGeneratedValues);
        updateCount = updateResults.iterator().hasNext() ? updateResults.iterator().next().getUpdateCount() : 0;
        for (UpdateResult each : updateResults) {
            updateCounts.add(each.getUpdateCount());
        }
    }
    
    private long getLastInsertId(final Collection<UpdateResult> updateResults, final Collection<Comparable<?>> autoIncrementGeneratedValues) {
        List<Long> lastInsertIds = new ArrayList<>(updateResults.size() + autoIncrementGeneratedValues.size());
        for (UpdateResult each : updateResults) {
            if (each.getLastInsertId() > 0) {
                lastInsertIds.add(each.getLastInsertId());
            }
        }
        for (Comparable<?> each : autoIncrementGeneratedValues) {
            if (each instanceof Number) {
                lastInsertIds.add(((Number) each).longValue());
            }
        }
        return lastInsertIds.isEmpty() ? 0 : getMinLastInsertId(lastInsertIds);
    }
    
    private long getMinLastInsertId(final List<Long> lastInsertIds) {
        Collections.sort(lastInsertIds);
        return lastInsertIds.iterator().next();
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
