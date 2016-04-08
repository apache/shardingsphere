/**
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

package com.dangdang.ddframe.rdb.sharding.merger.aggregation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.dangdang.ddframe.rdb.sharding.jdbc.AbstractShardingResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.common.ResultSetUtil;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext;

import lombok.Getter;

/**
 * 聚合结果集.
 * 
 * @author gaohongtao, zhangliang
 */
@Getter
public final class AggregationResultSet extends AbstractShardingResultSet {
    
    private final Collection<ResultSet> effectiveResultSets;
    
    private final List<AggregationColumn> aggregationColumns;
    
    private boolean hasIndexesForAggregationColumns;
    
    public AggregationResultSet(final List<ResultSet> resultSets, final MergeContext mergeContext) {
        super(resultSets, mergeContext.getLimit());
        aggregationColumns = mergeContext.getAggregationColumns();
        effectiveResultSets = new LinkedHashSet<>(resultSets.size());
    }
    
    @Override
    public boolean nextForSharding() throws SQLException {
        if (!hasIndexesForAggregationColumns) {
            ResultSetUtil.fillIndexesForDerivedAggregationColumns(getResultSets().iterator().next(), aggregationColumns);
            hasIndexesForAggregationColumns = true;
        }
        for (ResultSet each : getResultSets()) {
            if (!each.next()) {
                effectiveResultSets.remove(each);
                continue;
            }
            effectiveResultSets.add(each);
        }
        return !effectiveResultSets.isEmpty();
    }
}
