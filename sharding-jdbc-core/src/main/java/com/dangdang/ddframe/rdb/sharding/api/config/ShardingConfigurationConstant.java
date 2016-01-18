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
    METRICS_SECOND_PERIOD("metrics.second.period", "1"), 
    
    /**
     * 是否开启度量采集.
     * 默认值: 不开启
     */
    METRICS_ENABLE("metrics.enable", Boolean.FALSE.toString()), 
    
    /**
     * 度量输出在日志中的标识名称.
     */
    METRICS_PACKAGE_NAME("metrics.package.name", "com.dangdang.ddframe.rdb.sharding.metrics");
    
    private final String key;
    
    private final String defaultValue;
}
