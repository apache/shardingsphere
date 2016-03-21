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

package com.dangdang.ddframe.rdb.sharding.api.config;

import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 配置项常量.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
@Getter
public enum ShardingConfigurationConstant {
    
    /**
     * 度量输出周期.
     * 单位为秒
     * 默认值:30秒
     */
    METRICS_SECOND_PERIOD("metrics.second.period", "30"), 
    
    /**
     * 是否开启度量采集.
     * 默认值: 不开启
     */
    METRICS_ENABLE("metrics.enable", Boolean.FALSE.toString()), 
    
    /**
     * 度量输出在日志中的标识名称.
     */
    METRICS_PACKAGE_NAME("metrics.package.name", "com.dangdang.ddframe.rdb.sharding.metrics"),
    
    /**
     * 最小空闲工作线程数量.
     */
    PARALLEL_EXECUTOR_WORKER_MIN_IDLE_SIZE("parallelExecutor.worker.minIdleSize", "0"),
    
    /**
     * 最大工作线程数量.
     */
    PARALLEL_EXECUTOR_WORKER_MAX_SIZE("parallelExecutor.worker.maxSize", defaultMaxThreads()),
    
    /**
     * 工作线程空闲时超时时间.
     */
    PARALLEL_EXECUTOR_WORKER_MAX_IDLE_TIMEOUT("parallelExecutor.worker.maxIdleTimeout", "60"),
    
    /**
     * 工作线程空闲时超时时间单位.
     */
    PARALLEL_EXECUTOR_WORKER_MAX_IDLE_TIMEOUT_TIME_UNIT("parallelExecutor.worker.maxIdleTimeout.timeUnit", TimeUnit.SECONDS.toString());
    
    private final String key;
    
    private final String defaultValue;
    
    private static String defaultMaxThreads() {
        return String.valueOf(Runtime.getRuntime().availableProcessors() * 2);
    }
}
