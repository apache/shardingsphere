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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.context;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 分组上下文.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class GroupByContext implements IndexColumn {
    
    private final Optional<String> owner;
    
    private final String name;
    
    private final OrderType orderByType;
    
    @Setter
    private Optional<String> alias;
    
    @Setter
    private int columnIndex;
    
    public GroupByContext(final Optional<String> owner, final String name, final OrderType orderByType, final Optional<String> alias) {
        this.owner = owner;
        this.name = name;
        this.orderByType = orderByType;
        this.alias = alias;
    }
    
    @Override
    public Optional<String> getColumnLabel() {
        return alias;
    }
    
    @Override
    public Optional<String> getColumnName() {
        return Optional.of(name);
    }
}
