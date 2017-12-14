/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.merger.pagination;

import io.shardingjdbc.core.merger.ResultSetMerger;
import io.shardingjdbc.core.merger.common.AbstractDecoratorResultSetMerger;
import io.shardingjdbc.core.parsing.parser.context.limit.Limit;

import java.sql.SQLException;

/**
 * Decorator merger for limit pagination.
 *
 * @author zhangliang
 */
public final class LimitDecoratorResultSetMerger extends AbstractDecoratorResultSetMerger {
    
    private final Limit limit;
    
    private final boolean skipAll;
    
    private int rowNumber;
    
    public LimitDecoratorResultSetMerger(final ResultSetMerger resultSetMerger, final Limit limit) throws SQLException {
        super(resultSetMerger);
        this.limit = limit;
        skipAll = skipOffset();
    }
    
    private boolean skipOffset() throws SQLException {
        for (int i = 0; i < limit.getOffsetValue(); i++) {
            if (!getResultSetMerger().next()) {
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
            return getResultSetMerger().next();
        }
        return ++rowNumber <= limit.getRowCountValue() && getResultSetMerger().next();
    }
}
