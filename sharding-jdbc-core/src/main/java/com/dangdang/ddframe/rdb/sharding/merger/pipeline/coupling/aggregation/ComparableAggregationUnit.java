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

package com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling.aggregation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 比较聚合单元.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
@Slf4j
public final class ComparableAggregationUnit implements AggregationUnit {
    
    private final boolean asc;
    
    private Comparable<?> result;
    
    @SuppressWarnings("unchecked")
    @Override
    public void merge(final List<Comparable<?>> values) {
        if (null == values || null == values.get(0)) {
            return;
        }
        if (null == result) {
            result = values.get(0);
            log.trace("Comparable result: {}", result);
            return;
        }
        int comparedValue = ((Comparable) values.get(0)).compareTo(result);
        if (asc && comparedValue < 0 || !asc && comparedValue > 0) {
            result = values.get(0);
            log.trace("Comparable result: {}", result);
        }
    }
    
    @Override
    public Comparable<?> getResult() {
        return result;
    }
}
