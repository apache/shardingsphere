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

package com.dangdang.ddframe.rdb.sharding.merger.pipeline.reducer;

import com.dangdang.ddframe.rdb.sharding.merger.ResultSetMergeContext;
import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.AbstractMemoryOrderByResultSet;

import java.sql.SQLException;

/**
 * 根据排序列内存排序的聚集结果集.
 *
 * @author gaohongtao
 * @author zhangliang
 */
public final class MemoryOrderByReducerResultSet extends AbstractMemoryOrderByResultSet {
    
    public MemoryOrderByReducerResultSet(final ResultSetMergeContext resultSetMergeContext) throws SQLException {
        super(resultSetMergeContext.getShardingResultSets().getResultSets(), resultSetMergeContext.getCurrentOrderByKeys());
    }
}
