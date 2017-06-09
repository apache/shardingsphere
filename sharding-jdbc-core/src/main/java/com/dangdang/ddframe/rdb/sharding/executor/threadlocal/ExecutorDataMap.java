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

package com.dangdang.ddframe.rdb.sharding.executor.threadlocal;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 执行器执行时数据处理类.
 *
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecutorDataMap {
    
    private static ThreadLocal<Map<String, Object>> dataMap = new ThreadLocal<Map<String, Object>>() {
        
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<>();
        }
    };
    
    /**
     * 设置数据Map.
     *
     * @param dataMap 数据Map
     */
    public static void setDataMap(final Map<String, Object> dataMap) {
        ExecutorDataMap.dataMap.set(dataMap);
    }
    
    /**
     * 获取数据Map.
     *
     * @return 数据Map
     */
    public static Map<String, Object> getDataMap() {
        return dataMap.get();
    }
}
