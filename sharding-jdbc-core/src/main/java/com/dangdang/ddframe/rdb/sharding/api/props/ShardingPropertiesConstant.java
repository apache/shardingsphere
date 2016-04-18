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

package com.dangdang.ddframe.rdb.sharding.api.props;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 配置项常量.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
@Getter
public enum ShardingPropertiesConstant {
    
    /**
     * 是否开启度量采集.
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
     * 度量输出在日志中的标识名称.
     * 
     * <p>
     * 默认值: com.dangdang.ddframe.rdb.sharding.metrics
     * </p>
     */
    METRICS_LOGGER_NAME("metrics.logger.name", "com.dangdang.ddframe.rdb.sharding.metrics", String.class),
    
    /**
     * 最小空闲工作线程数量.
     * 
     * <p>
     * 默认值: 0
     * </p>
     */
    EXECUTOR_MIN_IDLE_SIZE("executor.min.idle.size", "0", int.class),
    
    /**
     * 最大工作线程数量.
     * 
     * <p>
     * 默认值: CPU的核数 * 2
     * </p>
     */
    EXECUTOR_MAX_SIZE("executor.max.size", String.valueOf(Runtime.getRuntime().availableProcessors() * 2), int.class),
    
    /**
     * 工作线程空闲时超时时间.
     * 
     * <p>
     * 单位: 毫秒.
     * 默认值: 60000毫秒.
     * </p>
     */
    EXECUTOR_MAX_IDLE_TIMEOUT_MILLISECONDS("executor.max.idle.timeout.millisecond", "60000", long.class);
    
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
