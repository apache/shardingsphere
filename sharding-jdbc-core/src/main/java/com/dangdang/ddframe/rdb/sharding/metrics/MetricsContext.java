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

package com.dangdang.ddframe.rdb.sharding.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import com.dangdang.ddframe.rdb.sharding.config.ShardingProperties;
import com.dangdang.ddframe.rdb.sharding.config.ShardingPropertiesConstant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 度量上下文持有者.
 * 
 * <p>
 * 多个ShardingDataSource使用静态度量上下文会造成数据污染, 所以将度量上下文对象绑定到ThreadLocal中.
 * </p>
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetricsContext {
    
    private static final ThreadLocal<MetricRegistry> HOLDER = new ThreadLocal<>();
    
    private static final String LOGGER_NAME = "Sharding-JDBC-Metrics";
    
    /**
     * 初始化度量上下文持有者.
     * 
     * @param shardingProperties Sharding-JDBC的配置属性
     */
    public static void init(final ShardingProperties shardingProperties) {
        HOLDER.remove();
        boolean metricsEnabled = shardingProperties.getValue(ShardingPropertiesConstant.METRICS_ENABLE);
        if (!metricsEnabled) {
            return;
        }
        long period = shardingProperties.getValue(ShardingPropertiesConstant.METRICS_MILLISECONDS_PERIOD);
        MetricRegistry metricRegistry = new MetricRegistry();
        Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger(LOGGER_NAME))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .withLoggingLevel(Slf4jReporter.LoggingLevel.DEBUG)
                .build().start(period, TimeUnit.MILLISECONDS);
        HOLDER.set(metricRegistry);
    }
    
    /**
     * 开始计时.
     *
     * @param name 度量目标名称
     *
     * @return 计时上下文
     */
    public static Timer.Context start(final String name) {
        return null == HOLDER.get() ? null : HOLDER.get().timer(MetricRegistry.name(name)).time();
    }
    
    /**
     * 停止计时.
     *
     * @param context 计时上下文
     */
    public static void stop(final Timer.Context context) {
        if (null != context) {
            context.stop();
        }
    }
    
    /**
     * 清理数据.
     */
    public static void clear() {
        HOLDER.remove();
    }
}
