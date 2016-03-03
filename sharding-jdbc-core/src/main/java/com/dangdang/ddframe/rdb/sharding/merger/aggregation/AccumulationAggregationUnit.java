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

import java.math.BigDecimal;

import com.dangdang.ddframe.rdb.sharding.merger.common.ResultSetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 累加聚合单元.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
public final class AccumulationAggregationUnit extends AbstractAggregationUnit {
    
    private final Class<?> returnType;
    
    private BigDecimal result;
    
    @Override
    public void doMerge(final Comparable<?>... values) {
        if (null == values || null == values[0]) {
            return;
        }
        if (null == result) {
            result = new BigDecimal("0");
        }
        result = result.add(new BigDecimal(values[0].toString()));
        log.trace("Accumulation result: {}", result.toString());
    }
    
    @Override
    public Comparable<?>  getResult() {
        return (Comparable<?>) ResultSetUtil.convertValue(result, returnType);
    }
}
