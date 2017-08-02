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

package com.dangdang.ddframe.rdb.sharding.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 配置项常量.
 * 
 * @author gaohongtao
 * @author caohao
 */
@RequiredArgsConstructor
@Getter
public enum ShardingPropertiesConstant {
    
    /**
     * 是否开启显示SQL.
     * 
     * <p>
     * 默认值: 关闭
     * </p>
     */
    SQL_SHOW("sql.show", Boolean.TRUE.toString(), boolean.class),
    
    /**
     * 是否开启SQL.
     *
     * <p>
     * 默认值: 关闭
     * </p>
     */
    METRICS_ENABLE("metrics.enable", Boolean.FALSE.toString(), boolean.class),
    
    /**
     * 度量输出周期.
     * 
     * <p>
     * 单位: 毫秒.
     * 默认值: 30000毫秒.
     * </p>
     */
    METRICS_MILLISECONDS_PERIOD("metrics.millisecond.period", "30000", long.class),
    
    /**
     * 工作线程数量.
     * 
     * <p>
     * 默认值: CPU核数
     * </p>
     */
    EXECUTOR_SIZE("executor.size", String.valueOf(Runtime.getRuntime().availableProcessors()), int.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
    
    /**
     * 根据属性键查找枚举.
     * 
     * @param key 属性键
     * @return 枚举值
     */
    public static ShardingPropertiesConstant findByKey(final String key) {
        for (ShardingPropertiesConstant each : ShardingPropertiesConstant.values()) {
            if (each.getKey().equals(key)) {
                return each;
            }
        }
        return null;
    }
}
