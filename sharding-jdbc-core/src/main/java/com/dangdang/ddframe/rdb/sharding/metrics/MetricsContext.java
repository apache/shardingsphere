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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Slf4jReporter.LoggingLevel;
import com.codahale.metrics.Timer.Context;
import com.google.common.base.Joiner;
import org.slf4j.LoggerFactory;

/**
 * 度量工具上下文.
 * 
 * @author gaohongtao
 */
public final class MetricsContext {
    
    private final MetricRegistry metricRegistry;
    
    public MetricsContext(final long period, final String packageName) {
        metricRegistry = new MetricRegistry();
        Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger(packageName))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .withLoggingLevel(LoggingLevel.DEBUG)
                .build();
        reporter.start(period, TimeUnit.SECONDS);
        
    }
    
    /**
     * 开始计时.
     *
     * @param name 度量目标名称
     * 
     * @return 计时上下文
     */
    public static Context start(final String... name) {
        MetricsContext context = ThreadLocalObjectContainer.getItem(MetricsContext.class);
        return null == context ? null : context.metricRegistry.timer(MetricRegistry.name(Joiner.on("-").join(name))).time();
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
