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

package com.dangdang.ddframe.rdb.sharding.parser.result.merger;

import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚合列对象.
 * 
 * @author zhangliang
 */
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public final class AggregationColumn implements IndexColumn {
    
    private final String expression;
    
    private final AggregationType aggregationType;
    
    private final Optional<String> alias;
    
    private final Optional<String> option;
    
    private final List<AggregationColumn> derivedColumns = new ArrayList<>(2);
    
    @Setter
    private int columnIndex = -1;
    
    @Override
    public Optional<String> getColumnLabel() {
        return alias;
    }
    
    @Override
    public Optional<String> getColumnName() {
        return Optional.of(expression);
    }
    
    /**
     * 聚合函数类型.
     * 
     * @author gaohongtao
     */
    public enum AggregationType {
        MAX, MIN, SUM, COUNT, AVG
    }
}
