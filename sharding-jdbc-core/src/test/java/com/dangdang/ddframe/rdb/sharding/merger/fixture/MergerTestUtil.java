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

package com.dangdang.ddframe.rdb.sharding.merger.fixture;

import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext;
import com.google.common.base.Optional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergerTestUtil {
    
    public static MockResultSet<Integer> createMock(final List<String> columns, final List<Integer> values) {
        Map<String, Integer> result = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            result.put(columns.get(i), values.get(i));
        }
        return new MockResultSet<>(Collections.singletonList(result));
    }
    
    public static MergeContext createMergeContext(final int index, final String name, final String alias, final AggregationColumn.AggregationType aggregationType) {
        AggregationColumn column = new AggregationColumn(name, aggregationType, Optional.fromNullable(alias), Optional.<String>absent(), index);
        if (AggregationColumn.AggregationType.AVG.equals(aggregationType)) {
            column.getDerivedColumns().add(new AggregationColumn(AggregationColumn.AggregationType.COUNT.name(), 
                    AggregationColumn.AggregationType.COUNT, Optional.of("sharding_gen_1"), Optional.<String>absent()));
            column.getDerivedColumns().add(new AggregationColumn(AggregationColumn.AggregationType.SUM.name(), 
                    AggregationColumn.AggregationType.SUM, Optional.of("sharding_gen_2"), Optional.<String>absent()));
        }
        MergeContext result = new MergeContext();
        result.getAggregationColumns().add(column);
        return result;
    }
}
