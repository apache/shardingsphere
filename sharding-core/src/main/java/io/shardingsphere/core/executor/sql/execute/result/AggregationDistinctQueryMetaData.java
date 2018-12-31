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

package io.shardingsphere.core.executor.sql.execute.result;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.AggregationType;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationDistinctSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Aggregation distinct query metadata.
 *
 * @author panjuan
 */
public final class AggregationDistinctQueryMetaData {
    
    private final Collection<AggregationDistinctColumnMetaData> columnMetaDatas;
    
    public AggregationDistinctQueryMetaData(final Collection<AggregationDistinctSelectItem> aggregationDistinctSelectItems, final int aggregationDistinctColumnIndex, final int derivedBeginIndex) {
        columnMetaDatas = new LinkedList<>();
        List<Integer> derivedBeginPosition = new ArrayList<>(1);
        derivedBeginPosition.add(derivedBeginIndex);
        for (AggregationDistinctSelectItem each : aggregationDistinctSelectItems) {
            columnMetaDatas.add(getAggregationDistinctColumnMetaData(each, aggregationDistinctColumnIndex, derivedBeginPosition));
        }
    }
    
    private AggregationDistinctColumnMetaData getAggregationDistinctColumnMetaData(final AggregationDistinctSelectItem selectItem, final int aggregationDistinctColumnIndex, final List<Integer> derivedBeginPosition) {
        List<AggregationSelectItem> derivedSelectItems = selectItem.getDerivedAggregationSelectItems();
        if (derivedSelectItems.isEmpty()) {
            return new AggregationDistinctColumnMetaData(aggregationDistinctColumnIndex, selectItem.getColumnLabel(), selectItem.getType(), Optional.<Integer>absent(), Optional.<Integer>absent());
        }
        derivedBeginPosition.set(0, derivedBeginPosition.get(0) + 2);
        return new AggregationDistinctColumnMetaData(aggregationDistinctColumnIndex, selectItem.getColumnLabel(), selectItem.getType(), Optional.of(derivedBeginPosition.get(0)), Optional.of(derivedBeginPosition.get(0) + 1));
    }
    
    @RequiredArgsConstructor 
    final class AggregationDistinctColumnMetaData {
        
        private final int columnIndex;
        
        private final String columnLabel;
        
        private final AggregationType aggregationType;
        
        private final Optional<Integer> derivedCountIndex;
        
        private final Optional<Integer> derivedSumIndex;
    }
}
