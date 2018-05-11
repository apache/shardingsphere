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

package io.shardingsphere.core.merger.dql.groupby.aggregation;

import io.shardingsphere.core.constant.AggregationType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Aggregation unit factory.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AggregationUnitFactory {
    
    /**
     * Create aggregation unit instance.
     * 
     * @param type aggregation function type
     * @return aggregation unit instance
     */
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
