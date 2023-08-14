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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.result;

import lombok.Getter;
import lombok.ToString;

/**
 * Table data consistency check result.
 */
@Getter
@ToString
public final class TableDataConsistencyCheckResult {
    
    private final TableDataConsistencyCountCheckResult countCheckResult;
    
    private final TableDataConsistencyContentCheckResult contentCheckResult;
    
    private final TableDataConsistencyCheckIgnoredType ignoredType;
    
    public TableDataConsistencyCheckResult(final TableDataConsistencyCountCheckResult countCheckResult, final TableDataConsistencyContentCheckResult contentCheckResult) {
        this.countCheckResult = countCheckResult;
        this.contentCheckResult = contentCheckResult;
        ignoredType = null;
    }
    
    public TableDataConsistencyCheckResult(final TableDataConsistencyCheckIgnoredType ignoredType) {
        countCheckResult = new TableDataConsistencyCountCheckResult(-1, -1);
        contentCheckResult = new TableDataConsistencyContentCheckResult(false);
        this.ignoredType = ignoredType;
    }
    
    /**
     * Is ignored.
     *
     * @return ignored or not
     */
    public boolean isIgnored() {
        return null != ignoredType;
    }
    
    /**
     * Is count and content matched.
     *
     * @return matched or not
     */
    public boolean isMatched() {
        if (null != ignoredType) {
            return false;
        }
        return countCheckResult.isMatched() && contentCheckResult.isMatched();
    }
}
