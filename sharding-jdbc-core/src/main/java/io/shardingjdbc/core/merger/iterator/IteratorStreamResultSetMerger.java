/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.merger.iterator;

import io.shardingjdbc.core.merger.ResultSetMergerInput;
import io.shardingjdbc.core.merger.common.AbstractStreamResultSetMerger;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * Stream merger for iterator.
 *
 * @author zhangliang
 */
public final class IteratorStreamResultSetMerger extends AbstractStreamResultSetMerger {
    
    private final Iterator<ResultSetMergerInput> resultSetMergerInputs;
    
    public IteratorStreamResultSetMerger(final List<ResultSetMergerInput> resultSetMergerInputs) {
        this.resultSetMergerInputs = resultSetMergerInputs.iterator();
        setCurrentResultSetMergerInput(this.resultSetMergerInputs.next());
    }
    
    @Override
    public boolean next() throws SQLException {
        if (getCurrentResultSetMergerInput().next()) {
            return true;
        }
        if (!resultSetMergerInputs.hasNext()) {
            return false;
        }
        setCurrentResultSetMergerInput(resultSetMergerInputs.next());
        boolean hasNext = getCurrentResultSetMergerInput().next();
        if (hasNext) {
            return true;
        }
        while (!hasNext && resultSetMergerInputs.hasNext()) {
            setCurrentResultSetMergerInput(resultSetMergerInputs.next());
            hasNext = getCurrentResultSetMergerInput().next();
        }
        return hasNext;
    }
}
