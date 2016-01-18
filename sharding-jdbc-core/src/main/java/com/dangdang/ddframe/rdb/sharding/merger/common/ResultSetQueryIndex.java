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

package com.dangdang.ddframe.rdb.sharding.merger.common;

import lombok.Getter;

/**
 * 结果集查询索引.
 * 
 * <p>
 * 用于查询结果集.
 * 如果索引类型是int, 表示根据序号查询, 从1开始.
 * 如果索引类型是String, 表示根据字段返回值名称查询.
 * </p>
 * 
 * @author zhangliang
 */
@Getter
public final class ResultSetQueryIndex {
    
    private static final int NO_INDEX = -1;
    
    private final int queryIndex;
    
    private final String queryName;
    
    public ResultSetQueryIndex(final Object queryParam) {
        if (queryParam instanceof Integer) {
            queryIndex = (int) queryParam;
            queryName = null;
        } else if (queryParam instanceof String) {
            queryIndex = NO_INDEX;
            queryName = queryParam.toString();
        } else {
            throw new IllegalArgumentException(queryParam.getClass().getName());
        }
    }
    
    /**
     * 获取是否通过序号查询.
     * 
     * @return 通过序号查询返回true, 通过名称查询返回false
     */
    public boolean isQueryBySequence() {
        return NO_INDEX != queryIndex;
    }
    
    /**
     * 忽略类型获取查询索引.
     * 
     * @return 查询索引
     */
    public Object getRawQueryIndex() {
        return isQueryBySequence() ? queryIndex : queryName;
    }
}
