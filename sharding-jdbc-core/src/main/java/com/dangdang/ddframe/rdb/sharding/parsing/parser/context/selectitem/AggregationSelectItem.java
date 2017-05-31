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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem;

import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.IndexColumn;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚合选择项.
 *
 * @author zhangliang
 */
@Getter
@ToString
public final class AggregationSelectItem implements SelectItem, IndexColumn {
    
    private final String innerExpression;
    
    private final Optional<String> alias;
    
    @Setter
    private int columnIndex = -1;
    
    private final AggregationType aggregationType;
    
    private final List<AggregationSelectItem> derivedAggregationSelectItems = new ArrayList<>(2);
    
    public AggregationSelectItem(final String innerExpression, final Optional<String> alias, final int columnIndex, final AggregationType aggregationType) {
        this.innerExpression = innerExpression;
        this.alias = alias;
        this.columnIndex = columnIndex;
        this.aggregationType = aggregationType;
    }
    
    @Override
    public String getExpression() {
        return aggregationType.name() + innerExpression;
    }
    
    @Override
    public Optional<String> getColumnLabel() {
        return alias;
    }
    
    @Override
    public Optional<String> getColumnName() {
        return Optional.of(getExpression());
    }
}
