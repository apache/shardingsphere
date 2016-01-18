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

package com.dangdang.ddframe.rdb.sharding.metrics;

import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Slf4jReporter.LoggingLevel;
import com.codahale.metrics.Timer.Context;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;

/**
 * 度量工具上下文.
 * 
 * @author gaohongtao
 */
public final class MetricsContext {
    
    private static final ThreadLocal<MetricsContext> CONTEXT = new ThreadLocal<>();
    
    private final Optional<MetricRegistry> metricRegistry;
    
    public MetricsContext(final boolean enable, final long period, final String packageName) {
        if (enable) {
            metricRegistry = Optional.of(new MetricRegistry());
            Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry.get())
                    .outputTo(LoggerFactory.getLogger(packageName))
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .withLoggingLevel(LoggingLevel.DEBUG)
                    .build();
            reporter.start(period, TimeUnit.SECONDS);
        } else {
            metricRegistry = Optional.absent();
        }
    }
    
    /**
     * 注册度量上下文.
     */
    public void register() {
        if (metricRegistry.isPresent() && !this.equals(CONTEXT.get())) {
            CONTEXT.set(this);
        }
    }
    
    /**
     * 开始计时.
     *
     * @param name 度量目标名称
     * 
     * @return 计时上下文
     */
    public static Context start(final String... name) {
        return null == CONTEXT.get() ? null : CONTEXT.get().metricRegistry.get().timer(MetricRegistry.name(Joiner.on("-").join(name))).time();
    }
    
    /**
     * 停止计时.
     *
     * @param context 计时上下文
     */
    public static void stop(final Context context) {
        if (null != context) {
            context.stop();
        }
    }
}
