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

import com.alibaba.druid.sql.ast.SQLOrderingSpecification;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 排序列对象.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public final class OrderByColumn implements IndexColumn {
    
    private final Optional<String> name;
    
    private final Optional<Integer> index;
    
    private final Optional<String> alias;
    
    private final OrderByType orderByType;
    
    private int columnIndex;
    
    public OrderByColumn(final String name, final String alias, final OrderByType orderByType) {
        this(Optional.of(name), Optional.<Integer>absent(), Optional.fromNullable(alias), orderByType);
    }
    
    public OrderByColumn(final String name, final OrderByType orderByType) {
        this(Optional.of(name), Optional.<Integer>absent(), Optional.<String>absent(), orderByType);
    }
    
    public OrderByColumn(final int index, final OrderByType orderByType) {
        this(Optional.<String>absent(), Optional.of(index), Optional.<String>absent(), orderByType);
        columnIndex = index;
    }
    
    @Override
    public void setColumnIndex(final int index) {
        if (this.index.isPresent()) {
            return;
        }
        columnIndex = index;
    }
        
    @Override
    public Optional<String> getColumnLabel() {
        return alias;
    }
    
    @Override
    public Optional<String> getColumnName() {
        return name;
    }
    
    /**
     * 排序类型.
     * 
     * @author gaohongtao, zhangliang
     */
    public enum OrderByType {
        ASC, 
        DESC;
        
        /**
         * 适配Druid的枚举类型.
         * 
         * @param sqlOrderingSpecification Druid的枚举类型
         * @return 排序类型
         */
        public static OrderByType valueOf(final SQLOrderingSpecification sqlOrderingSpecification) {
            return OrderByType.valueOf(sqlOrderingSpecification.name());
        }
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderByColumn)) {
            return false;
        }
        OrderByColumn that = (OrderByColumn) o;
        return orderByType == that.orderByType && (columnIndex == that.columnIndex 
                || index.isPresent() && that.index.isPresent() && index.get().equals(that.index.get())
                || name.isPresent() && that.name.isPresent() && name.get().equals(that.name.get())
                || alias.isPresent() && that.alias.isPresent() && alias.get().equals(that.alias.get()));
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(orderByType, columnIndex);
    }
}
