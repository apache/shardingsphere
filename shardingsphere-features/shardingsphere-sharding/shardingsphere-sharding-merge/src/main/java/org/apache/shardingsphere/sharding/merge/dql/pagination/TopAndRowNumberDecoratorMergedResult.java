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

package org.apache.shardingsphere.sharding.merge.dql.pagination;

import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.decorator.DecoratorMergedResult;
import org.apache.shardingsphere.infra.binder.segment.select.pagination.PaginationContext;

import java.sql.SQLException;

/**
 * Decorator merged result for top and row number pagination.
 */
public final class TopAndRowNumberDecoratorMergedResult extends DecoratorMergedResult {
    
    private final PaginationContext pagination;
    
    private final boolean skipAll;
    
    private long rowNumber;
    
    public TopAndRowNumberDecoratorMergedResult(final MergedResult mergedResult, final PaginationContext pagination) throws SQLException {
        super(mergedResult);
        this.pagination = pagination;
        skipAll = skipOffset();
    }
    
    private boolean skipOffset() throws SQLException {
        long end = pagination.getActualOffset();
        for (int i = 0; i < end; i++) {
            if (!getMergedResult().next()) {
                return true;
            }
        }
        rowNumber = end + 1;
        return false;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (skipAll) {
            return false;
        }
        if (!pagination.getActualRowCount().isPresent()) {
            return getMergedResult().next();
        }
        return rowNumber++ <= pagination.getActualRowCount().get() && getMergedResult().next();
    }
}
