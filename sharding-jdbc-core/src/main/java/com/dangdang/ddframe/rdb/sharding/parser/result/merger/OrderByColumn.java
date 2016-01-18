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

package com.dangdang.ddframe.rdb.sharding.parser.result.merger;

import com.alibaba.druid.sql.ast.SQLOrderingSpecification;
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
public final class OrderByColumn {
    
    private final Optional<String> name;
    
    private final Optional<Integer> index;
    
    private final OrderByType orderByType;
    
    public OrderByColumn(final String name, final OrderByType orderByType) {
        this(Optional.of(name), Optional.<Integer>absent(), orderByType);
    }
    
    public OrderByColumn(final int index, final OrderByType orderByType) {
        this(Optional.<String>absent(), Optional.of(index), orderByType);
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
}
