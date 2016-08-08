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

import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn.OrderByType;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 分组列对象.
 * 
 * @author zhangliang
 */
@Getter
@ToString(callSuper = true)
public final class GroupByColumn extends AbstractSortableColumn implements IndexColumn {
    
    @Setter
    private int columnIndex;
    
    public GroupByColumn(final Optional<String> owner, final String name, final Optional<String> alias, final OrderByType orderByType) {
        super(owner, Optional.of(name), alias, orderByType);
    }
    
    @Override
    public Optional<String> getColumnLabel() {
        return getAlias();
    }
    
    @Override
    public Optional<String> getColumnName() {
        return getName();
    }
}
