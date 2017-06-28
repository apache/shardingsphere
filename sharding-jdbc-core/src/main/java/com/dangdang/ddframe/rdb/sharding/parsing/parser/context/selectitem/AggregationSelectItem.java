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
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚合选择项.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public final class AggregationSelectItem implements SelectItem {
    
    private final AggregationType type;
    
    private final String innerExpression;
    
    private final Optional<String> alias;
    
    private final List<AggregationSelectItem> derivedAggregationSelectItems = new ArrayList<>(2);
    
    @Setter
    private int index = -1;
    
    @Override
    public String getExpression() {
        return SQLUtil.getExactlyValue(type.name() + innerExpression);
    }
    
    /**
     * 获取列标签.
     * 
     * @return 列标签
     */
    public String getColumnLabel() {
        return alias.isPresent() ? alias.get() : getExpression();
    }
}
