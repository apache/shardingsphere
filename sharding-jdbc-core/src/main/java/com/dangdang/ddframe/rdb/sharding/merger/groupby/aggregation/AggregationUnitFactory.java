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

package com.dangdang.ddframe.rdb.sharding.merger.groupby.aggregation;

import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 聚合函数结果集归并单元工厂.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AggregationUnitFactory {
    
    public static AggregationUnit create(final AggregationType type) {
        switch (type) {
            case MAX:
                return new ComparableAggregationUnit(false);
            case MIN:
                return new ComparableAggregationUnit(true);
            case SUM:
            case COUNT:
                return new AccumulationAggregationUnit();
            case AVG:
                return new AverageAggregationUnit();
            default:
                throw new UnsupportedOperationException(type.name());
        }
    }
}
