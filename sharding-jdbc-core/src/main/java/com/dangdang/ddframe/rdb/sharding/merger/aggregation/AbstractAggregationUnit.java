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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.dangdang.ddframe.rdb.sharding.merger.common.ResultSetQueryIndex;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;

/**
 * 归并计算单元的抽象类.
 * 
 * @author zhangliang
 */
public abstract class AbstractAggregationUnit implements AggregationUnit {
    
    @Override
    public final void merge(final AggregationColumn aggregationColumn, final AggregationValue aggregationValue, final ResultSetQueryIndex resultSetQueryIndex) throws SQLException {
        if (!aggregationColumn.getDerivedColumns().isEmpty()) {
            Collection<Comparable<?>> paramList = new ArrayList<>(aggregationColumn.getDerivedColumns().size());
            for (AggregationColumn each : aggregationColumn.getDerivedColumns()) {
                paramList.add(aggregationValue.getValue(new ResultSetQueryIndex(each.getAlias().get())));
            }
            doMerge(paramList.toArray(new Comparable<?>[aggregationColumn.getDerivedColumns().size()]));
        } else {
            doMerge(aggregationValue.getValue(resultSetQueryIndex));
        }
    }
    
    protected abstract void doMerge(Comparable<?>... values);
}
