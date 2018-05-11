/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.merger.dql.pagination;

import io.shardingsphere.core.merger.MergedResult;
import io.shardingsphere.core.merger.dql.common.DecoratorMergedResult;
import io.shardingsphere.core.parsing.parser.context.limit.Limit;

import java.sql.SQLException;

/**
 * Decorator merged result for limit pagination.
 *
 * @author zhangliang
 */
public final class LimitDecoratorMergedResult extends DecoratorMergedResult {
    
    private final Limit limit;
    
    private final boolean skipAll;
    
    private int rowNumber;
    
    public LimitDecoratorMergedResult(final MergedResult mergedResult, final Limit limit) throws SQLException {
        super(mergedResult);
        this.limit = limit;
        skipAll = skipOffset();
    }
    
    private boolean skipOffset() throws SQLException {
        for (int i = 0; i < limit.getOffsetValue(); i++) {
            if (!getMergedResult().next()) {
                return true;
            }
        }
        rowNumber = 0;
        return false;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (skipAll) {
            return false;
        }
        if (limit.getRowCountValue() < 0) {
            return getMergedResult().next();
        }
        return ++rowNumber <= limit.getRowCountValue() && getMergedResult().next();
    }
}
