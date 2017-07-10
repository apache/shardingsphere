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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

/**
 * 平均值聚合单元.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
@Slf4j
public final class AverageAggregationUnit implements AggregationUnit {
    
    private BigDecimal count;
    
    private BigDecimal sum;
    
    @Override
    public void merge(final List<Comparable<?>> values) {
        if (null == values || null == values.get(0) || null == values.get(1)) {
            return;
        }
        if (null == count) {
            count = new BigDecimal("0");
        }
        if (null == sum) {
            sum = new BigDecimal("0");
        }
        count = count.add(new BigDecimal(values.get(0).toString()));
        sum = sum.add(new BigDecimal(values.get(1).toString()));
        log.trace("AVG result COUNT: {} SUM: {}", count, sum);
    }
    
    @Override
    public Comparable<?> getResult() {
        if (null == count || BigDecimal.ZERO.equals(count)) {
            return count;
        }
        // TODO 通过metadata获取数据库的浮点数精度值
        return sum.divide(count, 4, BigDecimal.ROUND_HALF_UP);
    }
}
