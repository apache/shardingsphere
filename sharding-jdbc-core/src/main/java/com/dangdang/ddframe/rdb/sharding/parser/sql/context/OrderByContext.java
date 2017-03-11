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

package com.dangdang.ddframe.rdb.sharding.parser.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.google.common.base.Optional;
import lombok.Getter;

/**
 * 排序上下文.
 *
 * @author zhangliang
 */
@Getter
public final class OrderByContext {
    
    private final Optional<String> owner;
    
    private final Optional<String> name;
    
    private final Optional<Integer> index;
    
    private final OrderByColumn.OrderByType orderByType;
    
    public OrderByContext(final String name, final OrderByColumn.OrderByType orderByType) {
        this.owner = Optional.absent();
        this.name = Optional.of(name);
        index = Optional.absent();
        this.orderByType = orderByType;
    }
    
    public OrderByContext(final String owner, final String name, final OrderByColumn.OrderByType orderByType) {
        this.owner = Optional.of(owner);
        this.name = Optional.of(name);
        index = Optional.absent();
        this.orderByType = orderByType;
    }
    
    public OrderByContext(final int index, final OrderByColumn.OrderByType orderByType) {
        owner = Optional.absent();
        name = Optional.absent();
        this.index = Optional.of(index);
        this.orderByType = orderByType;
    }
}
